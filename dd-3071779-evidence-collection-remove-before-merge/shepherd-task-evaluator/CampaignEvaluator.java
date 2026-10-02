///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.2
//DEPS com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CampaignEvaluator {
    private static final ObjectMapper JSON = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final String EVALUATOR_NAME = "shepherd-task-evaluator";
    private static final String REFERENCE_CAMPAIGN_ID =
            "cb68d348-f71e-4ce0-a529-ca5a5d5cbc20";
    private static final int CPD_MINIMUM_TOKENS = 100;

    private static final Pattern SESSION_FILE = Pattern.compile(
            "phase([12])-task-(\\d{8}-\\d{6})-(\\d+)\\.md");
    private static final Pattern STARTED = Pattern.compile(
            "\\*\\*Started:\\*\\*\\s*(.+?)\\s{0,2}$");
    private static final Pattern SESSION_ID = Pattern.compile(
            "\\*\\*Session ID:\\*\\*\\s*`([^`]+)`");
    private static final Pattern DURATION = Pattern.compile(
            "\\*\\*Duration:\\*\\*\\s*(?:(\\d+)h\\s*)?(?:(\\d+)m\\s*)?(?:(\\d+)s)?");
    private static final Pattern RELATIVE_TIME = Pattern.compile(
            "<sub>(?:(\\d+)h\\s*)?(?:(\\d+)m\\s*)?(?:(\\d+)s)</sub>");
    private static final Pattern TOOL_HEADING = Pattern.compile("^### `([^`]+)`\\s*$");
    private static final Pattern TOOL_TERMINATOR = Pattern.compile(
            "<shellId:\\s*[^>]*?completed with exit code\\s+(-?\\d+)>");
    private static final Pattern PR_INPUT = Pattern.compile("(?m)^- PR_NUMBER:\\s*(\\d+)\\s*$");
    private static final Pattern REVIEW_ID_OUTPUT = Pattern.compile(
            "(?m)^(?:COPILOT_REVIEW_ID=|.*\\breview_id=)(\\d+)\\b");
    private static final Pattern JAVA_HOME = Pattern.compile(
            "JAVA_HOME\\s*=\\s*[\"']?([^\"'\\s;]+)");
    private static final Pattern JAVAC_LEVEL = Pattern.compile(
            "Compiling\\s+\\d+\\s+source files with javac \\[[^\\]]*?\\b(?:target|release)\\s+([^\\]\\s]+)");
    private static final Pattern COMMIT_OUTPUT = Pattern.compile(
            "(?m)^\\[(?:detached HEAD|[^\\]]+)\\s+([0-9a-f]{7,40})\\]\\s+(.+)$");
    private static final Pattern SHA = Pattern.compile("\\b[0-9a-f]{40}\\b");
    private static final Pattern HEAD_REF_OID = Pattern.compile(
            "\"headRefOid\"\\s*:\\s*\"([0-9a-f]{40})\"");
    private static final Pattern HEAD_ASSIGNMENT = Pattern.compile(
            "\\b(?:CURRENT_SHA|EXPECTED_HEAD|VALIDATED_HEAD|REVIEW_TARGET_HEAD|HEAD)=['\"]([0-9a-f]{40})");
    private static final Pattern TEST_SUMMARY = Pattern.compile(
            "Tests run:\\s*(\\d+),\\s*Failures:\\s*(\\d+),\\s*Errors:\\s*(\\d+),\\s*Skipped:\\s*(\\d+)");
    private static final Pattern ASSERTION = Pattern.compile(
            "\\b(?:assertEquals|assertSame|assertTrue|assertFalse|assertNull|assertNotNull|assertThat|assertThrows|fail)\\s*\\(");
    private static final Pattern READ_BASH_DELAY = Pattern.compile(
            "\"delay\"\\s*:\\s*(\\d+)");
    private static final Pattern READ_BASH_SHELL = Pattern.compile(
            "\"shellId\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern BACKGROUND_SHELL = Pattern.compile(
            "<(?:command with )?shellId:\\s*([^\\s>]+)");
    private static final Pattern COMPLETED_SHELL = Pattern.compile(
            "<shellId:\\s*([^\\s>]+)\\s+completed\\s+with\\s+exit\\s+code");
    private static final Pattern JAVA_INVOCATION = Pattern.compile(
            "(?m)(?:^|[;&|]\\s*|\\s)(\\./mvnw|mvn|java|javac)\\b");

    private final Config config;
    private final Path evaluatorDir;
    private final String evaluatorVersion;
    private final String evaluatorCommit;
    private final boolean evaluatorDirty;
    private final List<ObjectNode> events = new ArrayList<>();
    private final List<ObjectNode> unclassified = new ArrayList<>();
    private int eventSequence;

    private CampaignEvaluator(Config config) throws Exception {
        this.config = config;
        this.evaluatorDir = config.evaluatorDir;
        this.evaluatorVersion = readVersion(evaluatorDir);
        this.evaluatorCommit = gitCommit(evaluatorDir);
        this.evaluatorDirty = gitDirty(evaluatorDir);
        if (!config.allowUnversioned
                && (!evaluatorVersion.matches("\\d+\\.\\d+\\.\\d+")
                || !evaluatorCommit.matches("[0-9a-f]{40}"))) {
            throw new UsageException("Cannot resolve evaluator version and 40-character Git commit from "
                    + evaluatorDir + "; use --allow-unversioned only for an explicitly unversioned run.");
        }
    }

    public static void main(String[] args) {
        try {
            Config config = Config.parse(args);
            CampaignEvaluator evaluator = new CampaignEvaluator(config);
            if (config.mode == Mode.COMPARE_EVALS) {
                evaluator.compareEvaluations();
            } else {
                evaluator.run();
            }
        } catch (UsageException error) {
            System.err.println(error.getMessage());
            System.err.println();
            System.err.println(usage());
            System.exit(2);
        } catch (Exception error) {
            System.err.println("Evaluation failed: " + error.getMessage());
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void run() throws Exception {
        requireReadableCampaign();
        Files.createDirectories(config.outputDir);

        JsonNode manifest = combinedManifest();
        Map<Integer, TaskResult> tasks = initializeTasks(manifest);
        List<SessionResult> sessions = analyzeSessions(tasks);
        sessions.sort(Comparator.comparingInt((SessionResult value) -> value.attempt)
                .thenComparing(value -> value.startedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(value -> value.issue)
                .thenComparingInt(value -> value.shepherdStage));

        applyFlakyRerunRules(sessions);
        events.sort(Comparator
                .comparing((ObjectNode event) -> event.path("timestamp").asText("~"))
                .thenComparing(event -> event.path("id").asText()));
        ObjectNode traceCost = aggregateTraceCost(sessions);
        ObjectNode postMortem = analyzePostMortem();
        ObjectNode repository = config.repo == null
                ? unavailableSection("Repository analysis requires --repo.")
                : analyzeRepository(tasks);
        resolveEvents(tasks, sessions);
        refreshUnclassified();
        List<ObjectNode> defects = deduplicateProductDefects();

        ObjectNode root = JSON.createObjectNode();
        root.put("schemaVersion", "1.2");
        root.set("evaluator", evaluatorMetadata());
        root.set("campaign", campaignMetadata(manifest, sessions));
        root.set("evidenceQuality", evidenceQuality(sessions));
        root.set("sessions", sessionsJson(sessions));
        root.set("tasks", tasksJson(tasks, sessions));
        root.set("failureEvents", array(events));
        root.set("underlyingProblems", aggregateUnderlyingProblems());
        root.set("recurrenceSignatures", aggregateRecurrences());
        root.set("productDefects", array(defects));
        root.set("transitions", aggregateTransitions(sessions));
        root.set("hallucinationSignals", aggregateHallucinations());
        root.set("convergence", aggregateConvergence(sessions));
        root.set("cost", traceCost);
        root.set("environment", aggregateEnvironment(sessions));
        root.set("runInvariants", aggregateRunInvariants(manifest, sessions));
        root.set("ciAnalysis", aggregateCiAnalysis(repository, sessions));
        root.set("interpretationNotes", interpretationNotes());
        root.set("humanInterventions", aggregateHumanInterventions(sessions));
        root.set("repositoryAnalysis", repository);
        root.set("postMortemAgent", postMortem);
        root.set("unclassified", array(unclassified));
        root.set("reconciliation", reconciliation(manifest, root, tasks, sessions));
        root.set("acceptanceChecks", acceptanceChecks(root));

        writeJson(config.outputDir.resolve("findings.json"), root);
        writeSummaryCsv(manifest, root, tasks, sessions);
        writeDefectsCsv(manifest, defects);
        writeReport(manifest, root, tasks, sessions);
        writeUnclassified();

        System.out.println("Wrote evaluation to " + config.outputDir);
    }

    private void compareEvaluations() throws IOException {
        Files.createDirectories(config.outputDir);
        ArrayNode campaigns = JSON.createArrayNode();
        Map<String, Map<String, String>> values = new LinkedHashMap<>();
        for (Path directory : config.evaluationDirs) {
            Path findings = directory.resolve("findings.json");
            if (!Files.isRegularFile(findings)) {
                throw new UsageException("Missing findings.json in evaluation directory: "
                        + directory);
            }
            JsonNode root = JSON.readTree(findings.toFile());
            String id = root.path("campaign").path("campaignId").asText(
                    directory.getFileName().toString());
            ObjectNode campaign = campaigns.addObject();
            campaign.put("campaignId", id);
            campaign.put("directory", directory.toString());
            campaign.put("arm", root.path("campaign").path("arm").asText(""));
            Map<String, String> observed = new LinkedHashMap<>();
            observed.put("copilotCliVersions",
                    root.path("runInvariants").path("copilotCliVersions").toString());
            observed.put("stage30SkillContentLength",
                    root.path("runInvariants").path("skillContentLengths")
                            .path("shepherd-task-30-from-assignment-to-ready").toString());
            observed.put("skillContentVerification",
                    root.path("runInvariants").path("skillContentVerification")
                            .path("status").asText("unverified"));
            observed.put("models", root.path("runInvariants").path("models").toString());
            observed.put("reasoningLevels",
                    root.path("runInvariants").path("reasoningLevels").toString());
            values.put(id, observed);
            campaign.set("invariants", JSON.valueToTree(observed));
        }
        ObjectNode output = JSON.createObjectNode();
        output.put("schemaVersion", "1.0");
        output.set("campaigns", campaigns);
        ArrayNode drift = output.putArray("invariantDrift");
        Set<String> keys = values.values().stream().flatMap(map -> map.keySet().stream())
                .collect(Collectors.toCollection(TreeSet::new));
        for (String key : keys) {
            Set<String> distinct = values.values().stream()
                    .map(map -> map.getOrDefault(key, "unavailable"))
                    .collect(Collectors.toCollection(TreeSet::new));
            if (distinct.size() <= 1) continue;
            ObjectNode row = drift.addObject();
            row.put("invariant", key);
            row.put("warning", true);
            ObjectNode observed = row.putObject("observed");
            values.forEach((campaign, map) ->
                    observed.put(campaign, map.getOrDefault(key, "unavailable")));
        }
        writeJson(config.outputDir.resolve("invariant-drift.json"), output);
        StringBuilder report = new StringBuilder("# Cross-campaign invariant comparison\n\n");
        report.append("> [!WARNING]\n> Invariant differences can confound arm comparisons.\n\n")
                .append("| Invariant | Campaign values |\n|---|---|\n");
        for (JsonNode row : drift) {
            report.append("| ").append(row.path("invariant").asText()).append(" | ")
                    .append(row.path("observed").toString().replace("|", "\\|"))
                    .append(" |\n");
        }
        if (drift.isEmpty()) report.append("| none | no differences observed |\n");
        report.append("\nReference observation: control CLI `1.0.89` versus treatment `1.0.91`; ")
                .append("stage-30 skill content length `25804` versus `27427`. ")
                .append("Skill identity is unverified unless a skill-content hash artifact is present.\n");
        Files.writeString(config.outputDir.resolve("report.md"), report,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("Wrote comparison to " + config.outputDir);
    }

    private static String usage() {
        return """
                Usage:
                  ./evaluate-campaign <campaign-dir> --arm <control|treatment> [options]
                  ./evaluate-campaign --combine <dir1> <dir2> ... --arm <control|treatment> [options]
                  ./evaluate-campaign --compare-evals <eval-dir1> <eval-dir2> ... --out <dir> [options]
                Options:
                  --repo <path> --control-start <repo-path>@<sha> --out <dir>
                  --with-build --allow-unversioned --evaluator-dir <dir>
                """.strip();
    }

    private JsonNode combinedManifest() throws IOException {
        ObjectNode combined = null;
        String campaignId = null;
        Instant previousStart = null;
        Set<Integer> issues = new TreeSet<>();
        for (Path directory : config.campaignDirs) {
            ObjectNode current = (ObjectNode) JSON.readTree(directory
                    .resolve("shepherd-task-25-given-list-run.json").toFile());
            String currentId = current.path("campaignId").asText();
            if (campaignId == null) campaignId = currentId;
            if (!Objects.equals(campaignId, currentId)) {
                throw new UsageException("--combine directories must have the same campaignId.");
            }
            Instant start = parseInstant(current.path("startedAt").asText());
            if (previousStart != null && start != null && start.isBefore(previousStart)) {
                throw new UsageException("--combine directories must be ordered by startedAt.");
            }
            if (start != null) previousStart = start;
            current.path("taskIssues").forEach(value -> issues.add(value.asInt()));
            if (combined == null) {
                combined = current.deepCopy();
            } else {
                copy(current, combined, "completedAt");
                copy(current, combined, "status");
                copy(current, combined, "exitCode");
            }
        }
        if (combined == null) throw new UsageException("No campaign directories supplied.");
        ArrayNode taskIssues = combined.putArray("taskIssues");
        issues.forEach(taskIssues::add);
        combined.put("combinedDirectoryCount", config.campaignDirs.size());
        return combined;
    }

    private static Instant parseInstant(String value) {
        try {
            return value == null || value.isBlank() ? null : Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String readVersion(Path directory) {
        try {
            return Files.readString(directory.resolve("VERSION"), StandardCharsets.UTF_8).trim();
        } catch (IOException ignored) {
            return config.allowUnversioned ? "unversioned" : "";
        }
    }

    private String gitCommit(Path directory) {
        CommandResult result = command(List.of(
                "git", "-C", directory.toString(), "rev-parse", "HEAD"), null, Map.of());
        return result.exitCode == 0 ? result.output.trim()
                : config.allowUnversioned ? "unversioned" : "";
    }

    private static boolean gitDirty(Path directory) {
        CommandResult result = command(List.of(
                "git", "-C", directory.toString(), "status", "--porcelain",
                "--untracked-files=all", "--", "."), null, Map.of());
        return result.exitCode == 0 && !result.output.isBlank();
    }

    private void requireReadableCampaign() throws IOException {
        for (Path campaignDir : config.campaignDirs) {
            if (!Files.isDirectory(campaignDir)) {
                throw new UsageException("Campaign directory does not exist: " + campaignDir);
            }
            Path manifest = campaignDir.resolve("shepherd-task-25-given-list-run.json");
            if (!Files.isRegularFile(manifest)) {
                throw new UsageException("Missing campaign manifest: " + manifest);
            }
        }
        if (config.repo != null && !Files.isDirectory(config.repo.resolve(".git"))) {
            CommandResult result = command(
                    List.of("git", "-C", config.repo.toString(), "rev-parse", "--git-dir"),
                    null, Map.of());
            if (result.exitCode != 0) {
                throw new UsageException("--repo is not a Git checkout: " + config.repo);
            }
        }
    }

    private Map<Integer, TaskResult> initializeTasks(JsonNode manifest) {
        Map<Integer, TaskResult> tasks = new TreeMap<>();
        for (JsonNode issue : manifest.path("taskIssues")) {
            tasks.put(issue.asInt(), new TaskResult(issue.asInt()));
        }
        return tasks;
    }

    private List<SessionResult> analyzeSessions(Map<Integer, TaskResult> tasks) throws Exception {
        List<SessionResult> sessions = new ArrayList<>();
        for (int directoryIndex = 0; directoryIndex < config.campaignDirs.size();
                directoryIndex++) {
            Path campaignDir = config.campaignDirs.get(directoryIndex);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                    campaignDir, "phase*-task-*.md")) {
                for (Path markdown : stream) {
                    Matcher matcher = SESSION_FILE.matcher(markdown.getFileName().toString());
                    if (!matcher.matches()) {
                        continue;
                    }
                    int phase = Integer.parseInt(matcher.group(1));
                    int issue = Integer.parseInt(matcher.group(3));
                    int stage = phase == 1 ? 30 : 40;
                    Path jsonl = replaceSuffix(markdown, ".md", ".jsonl");
                    Path otel = campaignDir.resolve(
                            markdown.getFileName().toString()
                                    .replace("-task-", "-otel-")
                                    .replace(".md", ".jsonl"));
                    SessionResult session = analyzeSession(
                            issue, stage, markdown, jsonl, otel,
                            campaignDir, directoryIndex + 1);
                    sessions.add(session);
                    tasks.computeIfAbsent(issue, TaskResult::new).sessions.add(session);
                    if (session.prNumber != null) {
                        tasks.get(issue).prNumber = session.prNumber;
                    }
                }
            }
        }
        if (sessions.isEmpty()) {
            throw new IllegalStateException("No phase task transcripts found.");
        }
        return sessions;
    }

    private SessionResult analyzeSession(
            int issue, int stage, Path markdown, Path jsonl, Path otel,
            Path campaignDirectory, int attempt) throws Exception {
        SessionResult result = new SessionResult(
                issue, stage, markdown, jsonl, otel, campaignDirectory, attempt);
        String transcript = Files.readString(markdown, StandardCharsets.UTF_8);
        List<String> lines = Files.readAllLines(markdown, StandardCharsets.UTF_8);
        parseTranscriptHeader(result, lines);
        parseSessionJsonl(result);
        calibrateTranscriptStart(result);
        parseOtel(result);
        result.prNumber = parsePrNumber(transcript);
        parseTranscript(result, lines);
        finalizeSessionEvidence(result, transcript);
        return result;
    }

    private static Path replaceSuffix(Path path, String oldSuffix, String newSuffix) {
        String name = path.getFileName().toString();
        return path.resolveSibling(name.substring(0, name.length() - oldSuffix.length()) + newSuffix);
    }

    private void parseTranscriptHeader(SessionResult result, List<String> lines) {
        for (int index = 0; index < Math.min(lines.size(), 20); index++) {
            String line = lines.get(index);
            Matcher started = STARTED.matcher(line);
            if (started.find()) {
                result.headerLocalStart = parseTranscriptLocalDate(started.group(1).trim());
            }
            Matcher sessionId = SESSION_ID.matcher(line);
            if (sessionId.find()) {
                result.sessionId = sessionId.group(1);
            }
            Matcher duration = DURATION.matcher(line);
            if (duration.find()) {
                result.durationSeconds = durationSeconds(duration);
            }
        }
    }

    private static LocalDateTime parseTranscriptLocalDate(String value) {
        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("M/d/yyyy, h:mm:ss a", Locale.US),
                DateTimeFormatter.ofPattern("M/d/yyyy, h:mm a", Locale.US));
        for (DateTimeFormatter format : formats) {
            try {
                LocalDateTime local = LocalDateTime.parse(value, format);
                return local;
            } catch (DateTimeParseException ignored) {
                // Try the next known transcript format.
            }
        }
        return null;
    }

    private static void calibrateTranscriptStart(SessionResult result) {
        if (result.headerLocalStart == null) return;
        if (result.firstJsonlTimestamp == null) {
            result.startedAt = result.headerLocalStart
                    .atZone(ZoneId.systemDefault()).toInstant();
            result.timestampConversionMethod = "host_zone_fallback";
            return;
        }
        Instant best = null;
        long bestDifference = Long.MAX_VALUE;
        for (int offsetHours = -14; offsetHours <= 14; offsetHours++) {
            Instant candidate = result.headerLocalStart
                    .toInstant(java.time.ZoneOffset.ofHours(offsetHours));
            long difference = Math.abs(Duration.between(
                    candidate, result.firstJsonlTimestamp).getSeconds());
            if (difference < bestDifference) {
                best = candidate;
                bestDifference = difference;
            }
        }
        result.startedAt = best;
        result.timestampConversionMethod =
                "local_header_zone_offset_calibrated_to_first_jsonl_timestamp";
    }

    private static long durationSeconds(Matcher matcher) {
        long hours = parseLong(matcher.group(1));
        long minutes = parseLong(matcher.group(2));
        long seconds = parseLong(matcher.group(3));
        return hours * 3600 + minutes * 60 + seconds;
    }

    private static long parseLong(String value) {
        return value == null || value.isBlank() ? 0 : Long.parseLong(value);
    }

    private void parseSessionJsonl(SessionResult result) throws IOException {
        if (!Files.isRegularFile(result.jsonl)) {
            result.missingArtifacts.add(result.jsonl.getFileName().toString());
            return;
        }
        JsonNode lastUsage = null;
        Map<String, String> toolNames = new HashMap<>();
        int userMessageIndex = 0;
        try (BufferedReader reader = Files.newBufferedReader(result.jsonl, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                JsonNode event;
                try {
                    event = JSON.readTree(line);
                } catch (JsonProcessingException error) {
                    result.jsonlParseErrors++;
                    continue;
                }
                if (result.firstJsonlTimestamp == null && event.has("timestamp")) {
                    try {
                        result.firstJsonlTimestamp =
                                Instant.parse(event.path("timestamp").asText());
                    } catch (DateTimeParseException ignored) {
                        // Later events may carry a parseable timestamp.
                    }
                }
                String type = event.path("type").asText();
                result.eventTypeCounts.merge(type, 1, Integer::sum);
                JsonNode data = event.path("data");
                if ("session.usage_checkpoint".equals(type)) {
                    lastUsage = data;
                } else if ("result".equals(type)) {
                    result.jsonlDurationMs = event.path("usage")
                            .path("sessionDurationMs").asLong();
                    if (result.sessionId == null) {
                        result.sessionId = event.path("sessionId").asText(null);
                    }
                } else if ("model.call_start".equals(type)) {
                    result.inferenceCalls++;
                    text(data, "model").ifPresent(result.models::add);
                } else if ("tool.execution_start".equals(type)) {
                    toolNames.put(data.path("toolCallId").asText(),
                            data.path("toolName").asText(""));
                } else if ("tool.execution_partial_result".equals(type)) {
                    result.partialOutputEvents++;
                    String toolCallId = data.path("toolCallId").asText();
                    String partial = data.path("partialOutput").asText("");
                    result.partialOutputUtf16CodeUnits += partial.length();
                    result.partialOutputUnicodeCodePoints +=
                            partial.codePointCount(0, partial.length());
                    result.partialOutputs.put(toolCallId, partial);
                } else if ("assistant.reasoning_delta".equals(type)) {
                    result.reasoningDeltaEvents++;
                    result.reasoningDeltaCharacters +=
                            data.path("deltaContent").asText("").length();
                } else if ("session.skills_loaded".equals(type)) {
                    for (JsonNode skill : data.path("skills")) {
                        result.loadedSkills.add(skill.path("name").asText());
                    }
                } else if ("tool.execution_complete".equals(type)) {
                    result.toolCalls++;
                    String completedAt = event.path("timestamp").asText("");
                    result.toolCompletionTimestamps.add(completedAt);
                    if (data.path("success").asBoolean()) {
                        result.toolSuccessTrue++;
                    } else {
                        result.toolSuccessFalse++;
                    }
                    String toolCallId = data.path("toolCallId").asText();
                    String partial = result.partialOutputs.getOrDefault(toolCallId, "");
                    Matcher reviewId = Pattern.compile(
                            "(?m)^COPILOT_REVIEW_ID=(\\d+)").matcher(partial);
                    if (reviewId.find() && !completedAt.isBlank()) {
                        result.reviewCompletionTimestamps.put(
                                reviewId.group(1), completedAt);
                    }
                    JsonNode telemetry = data.path("toolTelemetry");
                    String skillName = telemetry.path("restrictedProperties")
                            .path("skillName").asText("");
                    String skillHash = telemetry.path("properties")
                            .path("skillNameHash").asText("");
                    if (!skillName.isBlank() && !skillHash.isBlank()) {
                        result.skillNameHashes.put(skillName, skillHash);
                    }
                    JsonNode skillLength = telemetry.path("metrics")
                            .path("skillContentLength");
                    if (!skillName.isBlank() && skillLength.isNumber()) {
                        result.skillContentLengths.put(
                                skillName, skillLength.asLong());
                    }
                    String external = data.path("toolTelemetry").path("properties")
                            .path("largeSessionLogWrittenToFile").asText();
                    if ("true".equals(external)) {
                        result.externalOutputReferences++;
                        result.externalizedToolCalls.put(toolCallId,
                                toolNames.getOrDefault(toolCallId, ""));
                    }
                } else if ("user.message".equals(type)) {
                    String content = data.path("transformedContent").asText("");
                    if (userMessageIndex++ > 0) {
                        ObjectNode intervention = JSON.createObjectNode();
                        intervention.put("timestamp", event.path("timestamp").asText(""));
                        intervention.put("classification", classifyHumanIntervention(content));
                        intervention.put("excerpt", excerpt(content, 500));
                        result.humanInterventions.add(intervention);
                    }
                }
            }
        }
        if (lastUsage != null) {
            result.nanoAiu = lastUsage.path("totalNanoAiu").decimalValue();
            if (lastUsage.has("totalPremiumRequests")) {
                result.premiumRequests = lastUsage.path("totalPremiumRequests").asInt();
            }
        }
    }

    private static String classifyHumanIntervention(String content) {
        String lower = content.toLowerCase(Locale.ROOT);
        if (lower.matches("(?s).*(continue|resume|approved?|retry|proceed|status).*")) {
            return "administration";
        }
        return "substantive";
    }

    private void parseOtel(SessionResult result) throws IOException {
        if (!Files.isRegularFile(result.otel)) {
            result.missingArtifacts.add(result.otel.getFileName().toString());
            return;
        }
        Map<String, MetricPoint> last = new HashMap<>();
        Map<String, MetricPoint> previous = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(result.otel, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                JsonNode record;
                try {
                    record = JSON.readTree(line);
                } catch (JsonProcessingException error) {
                    result.otelParseErrors++;
                    continue;
                }
                if ("span".equals(record.path("type").asText())) {
                    JsonNode attributes = record.path("attributes");
                    text(attributes, "gen_ai.request.model").ifPresent(result.models::add);
                    text(attributes, "gen_ai.response.model").ifPresent(result.models::add);
                    text(attributes, "gen_ai.request.reasoning.level")
                            .ifPresent(result.reasoningLevels::add);
                    text(record.path("resource").path("attributes"), "service.version")
                            .ifPresent(result.copilotCliVersions::add);
                    continue;
                }
                if (!"metric".equals(record.path("type").asText())) {
                    continue;
                }
                String metricName = record.path("name").asText();
                if (!TokenTotals.METRICS.containsKey(metricName)) {
                    continue;
                }
                for (JsonNode point : record.path("dataPoints")) {
                    String attributes = canonical(point.path("attributes"));
                    String key = metricName + "\u0000" + attributes;
                    JsonNode metricValue = point.path("value");
                    BigDecimal value = metricValue.isNumber()
                            ? metricValue.decimalValue()
                            : metricValue.path("sum").decimalValue();
                    long endNanos = timeTuple(point.path("endTime"));
                    MetricPoint candidate = new MetricPoint(endNanos, value);
                    MetricPoint prior = previous.get(key);
                    if (prior != null && candidate.endNanos >= prior.endNanos
                            && candidate.value.compareTo(prior.value) < 0) {
                        result.otelMonotonicityViolations++;
                    }
                    previous.put(key, candidate);
                    MetricPoint current = last.get(key);
                    if (current == null || candidate.endNanos >= current.endNanos) {
                        last.put(key, candidate);
                    }
                }
            }
        }
        for (Map.Entry<String, MetricPoint> entry : last.entrySet()) {
            String metric = entry.getKey().substring(0, entry.getKey().indexOf('\u0000'));
            result.tokens.add(TokenTotals.METRICS.get(metric), entry.getValue().value.longValue());
        }
    }

    private static String canonical(JsonNode node) {
        try {
            return JSON.writeValueAsString(node);
        } catch (JsonProcessingException error) {
            return node.toString();
        }
    }

    private static long timeTuple(JsonNode node) {
        if (!node.isArray() || node.size() < 2) {
            return 0;
        }
        return node.get(0).asLong() * 1_000_000_000L + node.get(1).asLong();
    }

    private static Optional<String> text(JsonNode node, String field) {
        if (node.has(field) && !node.path(field).asText().isBlank()) {
            return Optional.of(node.path(field).asText());
        }
        return Optional.empty();
    }

    private static Integer parsePrNumber(String transcript) {
        Matcher matcher = PR_INPUT.matcher(transcript);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private void parseTranscript(SessionResult session, List<String> lines) {
        long relativeSeconds = 0;
        String currentHead = null;
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            Matcher relative = RELATIVE_TIME.matcher(line);
            if (relative.find()) {
                relativeSeconds = durationSeconds(relative);
            }
            Matcher heading = TOOL_HEADING.matcher(line);
            if (!heading.matches()) {
                Matcher javaHome = JAVA_HOME.matcher(line);
                while (javaHome.find()) {
                    session.javaHomes.add(javaHome.group(1));
                }
                Matcher level = JAVAC_LEVEL.matcher(line);
                while (level.find()) {
                    session.javacLevels.add(level.group(1));
                }
                continue;
            }
            ToolBlock block = parseToolBlock(lines, index, heading.group(1), relativeSeconds);
            index = block.endLine - 1;
            if (block.command != null) {
                Matcher javaHome = JAVA_HOME.matcher(block.command);
                while (javaHome.find()) {
                    session.javaHomes.add(javaHome.group(1));
                }
            }
            Matcher level = JAVAC_LEVEL.matcher(block.output);
            while (level.find()) {
                session.javacLevels.add(level.group(1));
            }
            Matcher assigned = HEAD_ASSIGNMENT.matcher(
                    Objects.toString(block.command, ""));
            while (assigned.find()) {
                currentHead = assigned.group(1);
            }
            Matcher headRef = HEAD_REF_OID.matcher(block.output);
            while (headRef.find()) {
                currentHead = headRef.group(1);
            }
            block.headSha = currentHead;
            if (session.toolBlocks.size() < session.toolCompletionTimestamps.size()) {
                block.jsonlCompletedAt = session.toolCompletionTimestamps.get(
                        session.toolBlocks.size());
            }
            session.toolBlocks.add(block);
            if (block.output.contains("Tests are skipped.")) {
                session.testsSkippedMessages++;
                if (block.command != null && block.command.contains("gh run view")
                        && block.command.contains("--log")) {
                    session.ciTestsSkippedMessages++;
                }
            }
            recordJavaInvocation(session, block);
            parseCiTestSummaries(session, block);
            classifyToolBlock(session, block);
            parseTransitions(session, block);
            parseReviewEvidence(session, block);
            parseStage30ChangeRequests(session, block);
            parseCommitEvidence(session, block);
        }
        if (lines.stream().anyMatch(line ->
                line.contains("SHEPHERD FAILED: no remediation push"))) {
            session.convergenceFailure = true;
            session.convergenceFailureReason =
                    "SHEPHERD FAILED: no remediation push";
        }
        calculateCcaWait(session);
    }

    private static void parseCiTestSummaries(SessionResult session, ToolBlock block) {
        if (block.command == null || !block.command.contains("gh run view")
                || !block.command.contains("--log")) {
            return;
        }
        session.ciLogCaptured = true;
        if (block.command.contains("--log-failed")) {
            session.ciFailedLogOnlyCaptured = true;
        } else {
            session.ciFullLogCaptured = true;
        }
        String run = "unknown";
        Matcher runId = Pattern.compile("gh\\s+run\\s+view\\s+(\\d+)").matcher(block.command);
        if (runId.find()) run = runId.group(1);
        String[] lines = block.output.split("\\R");
        for (int index = 0; index < lines.length; index++) {
            Matcher summary = TEST_SUMMARY.matcher(lines[index]);
            while (summary.find()) {
                String context = String.join("\n", Arrays.copyOfRange(lines,
                        Math.max(0, index - 8), Math.min(lines.length, index + 2)))
                        .toLowerCase(Locale.ROOT);
                String provider = context.contains("failsafe")
                        || context.contains("integration-test")
                        ? "failsafe" : "surefire";
                session.ciTestSummaries.add(new CiTestSummary(
                        run, provider, Integer.parseInt(summary.group(1)),
                        Integer.parseInt(summary.group(2)),
                        Integer.parseInt(summary.group(3)),
                        Integer.parseInt(summary.group(4))));
            }
        }
    }

    private static ToolBlock parseToolBlock(
            List<String> lines, int headingLine, String tool, long relativeSeconds) {
        ToolBlock block = new ToolBlock(tool, headingLine + 1, relativeSeconds);
        StringBuilder command = new StringBuilder();
        StringBuilder output = new StringBuilder();
        StringBuilder raw = new StringBuilder();
        boolean commandStarted = false;
        boolean outputStarted = false;
        for (int index = headingLine + 1; index < lines.size(); index++) {
            String line = lines.get(index);
            raw.append(line).append('\n');
            if (index > headingLine + 1 && (TOOL_HEADING.matcher(line).matches()
                    || line.equals("---"))) {
                block.endLine = index + 1;
                break;
            }
            if (!commandStarted && line.startsWith("$ ")) {
                command.append(line.substring(2));
                commandStarted = true;
                continue;
            }
            if (commandStarted && !outputStarted
                    && (line.startsWith("<details>") || line.startsWith("```"))) {
                outputStarted = true;
            }
            Matcher terminator = TOOL_TERMINATOR.matcher(line);
            if (terminator.find()) {
                block.exitCode = Integer.parseInt(terminator.group(1));
            }
            if (commandStarted && !outputStarted) {
                command.append('\n').append(line);
            } else if (outputStarted) {
                output.append(line).append('\n');
            }
            block.endLine = index + 1;
        }
        block.command = command.isEmpty() ? null : command.toString().stripTrailing();
        block.output = output.toString();
        block.raw = raw.toString();
        return block;
    }

    private static void recordJavaInvocation(SessionResult session, ToolBlock block) {
        if (!"bash".equals(block.tool) || block.command == null) {
            return;
        }
        Matcher invocation = JAVA_INVOCATION.matcher(block.command);
        if (!invocation.find()) {
            return;
        }
        String activeHome = null;
        Matcher homes = JAVA_HOME.matcher(block.command);
        while (homes.find() && homes.start() < invocation.start()) {
            activeHome = homes.group(1);
        }
        ObjectNode value = JSON.createObjectNode();
        value.put("line", block.startLine);
        value.put("executable", invocation.group(1));
        if (activeHome == null) {
            value.putNull("javaHome");
            value.put("availability", "unavailable");
        } else {
            value.put("javaHome", activeHome);
            value.put("availability", "measured");
        }
        session.javaInvocations.add(value);
    }

    private static void calculateCcaWait(SessionResult session) {
        Map<String, Long> remoteShellStarts = new HashMap<>();
        for (ToolBlock block : session.toolBlocks) {
            if (!"bash".equals(block.tool) || block.command == null
                    || !block.command.contains("copilot_work_finished")) {
                continue;
            }
            Matcher shell = BACKGROUND_SHELL.matcher(block.output);
            while (shell.find()) {
                remoteShellStarts.put(shell.group(1), block.relativeSeconds);
            }
        }
        Set<String> completedShells = new HashSet<>();
        for (ToolBlock block : session.toolBlocks) {
            if (!"read_bash".equals(block.tool)) {
                continue;
            }
            Matcher shell = READ_BASH_SHELL.matcher(block.raw);
            Matcher delay = READ_BASH_DELAY.matcher(block.raw);
            if (shell.find() && delay.find()
                    && remoteShellStarts.containsKey(shell.group(1))) {
                session.ccaWaitPollCount++;
                session.ccaWaitConfiguredCeilingSeconds +=
                        Integer.parseInt(delay.group(1));
                Matcher completed = COMPLETED_SHELL.matcher(block.raw);
                if (completed.find() && completed.group(1).equals(shell.group(1))
                        && completedShells.add(shell.group(1))) {
                    session.ccaWaitElapsedSeconds += Math.max(
                            0, block.relativeSeconds
                                    - remoteShellStarts.get(shell.group(1)));
                }
            }
        }
    }

    private static void finalizeSessionEvidence(SessionResult session, String transcript) {
        for (Map.Entry<String, String> marker : session.externalizedToolCalls.entrySet()) {
            String partial = session.partialOutputs.getOrDefault(marker.getKey(), "");
            boolean preserved = !partial.isBlank()
                    || ("skill".equals(marker.getValue())
                    && transcript.contains("loaded successfully"));
            if (!preserved) {
                session.confirmedEvidenceGaps++;
            }
        }
    }

    private void classifyToolBlock(SessionResult session, ToolBlock block) {
        if (!"bash".equals(block.tool)) {
            return;
        }
        boolean failedLog = block.command != null
                && block.command.contains("gh run view")
                && block.command.contains("--log-failed");
        List<CiFailure> ciFailures = failedCiFailures(block.output, failedLog).stream()
                .filter(failure -> !isCopilotOrchestrationCheck(failure.job()))
                .filter(failure -> !isAggregateWorkflowCheck(failure.job()))
                .toList();
        boolean preciseCiFailure = ciFailures.stream()
                .anyMatch(failure -> failure.step() != null);
        if (block.exitCode != null && block.exitCode != 0) {
            if (ciFailures.isEmpty()) {
                addEvent(session, block, "nonzero_tool_exit",
                        detector(block), block.exitCode, classifyFailure(block));
            }
            session.nonzeroToolExits++;
        }
        if (block.output.contains("BUILD FAILURE") && ciFailures.isEmpty()) {
            addEvent(session, block, "build_failure",
                    detector(block), block.exitCode, classifyFailure(block));
        }
        Matcher tests = TEST_SUMMARY.matcher(block.output);
        Set<String> seen = new HashSet<>();
        while (!preciseCiFailure && tests.find()) {
            int failures = Integer.parseInt(tests.group(2));
            int errors = Integer.parseInt(tests.group(3));
            String summary = tests.group();
            if ((failures > 0 || errors > 0) && seen.add(summary)) {
                String gate = isContainerTest(block) ? "arquillian_container_tests" : "unit_tests";
                ObjectNode classification = classifyFailure(block);
                ObjectNode event = addEvent(session, block, "test_failure", gate,
                        block.exitCode, classification);
                event.put("testsRun", Integer.parseInt(tests.group(1)));
                event.put("failures", failures);
                event.put("errors", errors);
                event.put("skipped", Integer.parseInt(tests.group(4)));
                block.testFailureEvents.add(event);
            }
        }
        if (block.output.contains("Test timeout of") && isTestCommand(block.command)) {
            ObjectNode classification = classifyFailure(block);
            if ("unclassified".equals(classification.path("status").asText())) {
                classification = unclassifiedClassification(
                        "A timeout alone does not distinguish product behavior, test harness error, or infrastructure.");
            }
            ObjectNode event = addEvent(session, block, "test_failure",
                    detector(block), block.exitCode, classification);
            block.testFailureEvents.add(event);
        }
        for (CiFailure failure : ciFailures) {
            ObjectNode classification;
            String gate;
            if (failure.step() == null) {
                gate = ciGate(failure.job());
                classification = unclassifiedClassification(
                        "The failing CI job was identified, but no failing step was captured.");
            } else {
                gate = ciGate(failure.step());
                classification = ciStepClassification(failure.step());
            }
            ObjectNode event = addEvent(session, block, "failed_ci_check",
                    gate, null, classification);
            event.put("commandOrCheck", excerpt(
                    failure.step() == null ? failure.job()
                            : failure.job() + " / " + failure.step(), 300));
            event.put("where", "ci");
            event.put("gateSource", failure.source());
            event.put("ciJob", failure.job());
            if (failure.step() == null) {
                event.putNull("ciFailingStep");
                event.put("fallbackGate", gate);
            } else {
                event.put("ciFailingStep", failure.step());
                event.put("ciFailingStepGate", gate);
            }
        }
        for (String line : block.output.split("\\R")) {
            if (line.trim().startsWith("SHEPHERD FAILED:")) {
                if (line.contains("no remediation push")) {
                    session.convergenceFailure = true;
                    session.convergenceFailureReason =
                            "SHEPHERD FAILED: no remediation push";
                }
                addEvent(session, block, "acceptance_failure", "functional_acceptance",
                        block.exitCode, classifyFailure(block));
            }
        }
        countHallucinations(session, block);
        countConvergenceSignals(session, block);
    }

    private static String detector(ToolBlock block) {
        String text = ((block.command == null ? "" : block.command) + "\n" + block.output)
                .toLowerCase(Locale.ROOT);
        if (text.contains("playwright") || text.contains("chromium")
                || text.contains("http://localhost")) {
            return "functional_acceptance";
        }
        if (text.contains("spotless") || text.contains("formatting")
                || text.contains("format check")) {
            return "formatting";
        }
        if (text.contains("source-gates") || text.contains("source gates")) {
            return "static_analysis";
        }
        if (isContainerTest(block)) {
            return "arquillian_container_tests";
        }
        if (isTestCommand(block.command) || text.contains("tests run:")) {
            return "unit_tests";
        }
        if (text.contains("compilation error") || text.contains("javac")
                || text.contains("cannot find symbol")) {
            return "compiler";
        }
        if (text.contains("pmd") || text.contains("checkstyle")
                || text.contains("spotbugs") || text.contains("enforcer")) {
            return "static_analysis";
        }
        if (text.contains("gh pr checks") || text.contains("statuscheckrollup")
                || text.contains("check-runs")) {
            return "ci";
        }
        return "functional_acceptance";
    }

    private static String failedCiCheckName(String line) {
        try {
            JsonNode node = JSON.readTree(line.trim());
            return firstText(node, "name", "context", "workflowName");
        } catch (JsonProcessingException ignored) {
            Matcher matcher = Pattern.compile(
                    "\"(?:name|context|workflowName)\"\\s*:\\s*\"([^\"]+)\"")
                    .matcher(line);
            return matcher.find() ? matcher.group(1) : null;
        }
    }

    private static boolean isContainerTest(ToolBlock block) {
        String text = ((block.command == null ? "" : block.command) + "\n" + block.output)
                .toLowerCase(Locale.ROOT);
        return text.contains("arquillian") || text.contains("liberty:")
                || text.contains("payara") || text.contains("failsafe")
                || text.contains("container test");
    }

    private static boolean isTestCommand(String command) {
        if (command == null) {
            return false;
        }
        String lower = command.toLowerCase(Locale.ROOT);
        return lower.contains(" test") || lower.contains("test ")
                || lower.contains("verify") || lower.contains("playwright")
                || lower.contains("surefire") || lower.contains("failsafe");
    }

    static List<CiFailure> failedCiFailures(String output, boolean failedLog) {
        Map<String, CiFailure> failures = new LinkedHashMap<>();
        if (failedLog) {
            Map<String, StringBuilder> stepLogs = new LinkedHashMap<>();
            Map<String, CiFailure> stepIdentities = new LinkedHashMap<>();
            Matcher logLine = Pattern.compile(
                    "(?m)^([^\\t\\r\\n]+)\\t([^\\t\\r\\n]+)\\t(.*)$")
                    .matcher(output);
            while (logLine.find()) {
                String job = logLine.group(1).strip();
                String step = logLine.group(2).strip();
                if (!job.isBlank() && !step.isBlank()) {
                    CiFailure failure = new CiFailure(job, step, "step_log");
                    String key = job + "\u0000" + step;
                    stepIdentities.put(key, failure);
                    stepLogs.computeIfAbsent(key, ignored -> new StringBuilder())
                            .append(logLine.group(3)).append('\n');
                }
            }
            Set<String> failedJobs = new HashSet<>();
            stepLogs.forEach((key, log) -> {
                if (failedStepLog(log.toString())) {
                    CiFailure failure = stepIdentities.get(key);
                    if (failedJobs.add(failure.job())) {
                        failures.put(key, failure);
                    }
                }
            });
            if (failures.isEmpty()) {
                String step = ciFailingStep(output);
                if (step != null) {
                    failures.put("\u0000" + step,
                            new CiFailure("unknown", step, "step_log"));
                }
            }
        }
        for (JsonNode root : parseJsonValues(output)) {
            Deque<JsonNode> pending = new ArrayDeque<>();
            pending.add(root);
            while (!pending.isEmpty()) {
                JsonNode node = pending.removeFirst();
                if (node.isContainerNode()) {
                    node.elements().forEachRemaining(pending::addLast);
                }
                if (!node.isObject()) {
                    continue;
                }
                String conclusion = node.path("conclusion").asText().toLowerCase(Locale.ROOT);
                if (Set.of("failure", "timed_out", "cancelled").contains(conclusion)) {
                    String job = firstText(node, "name", "context", "workflowName");
                    String step = firstText(node, "stepName", "step");
                    if (job != null && !job.isBlank()) {
                        CiFailure failure = new CiFailure(job, step,
                                step == null || step.isBlank()
                                        ? "job_fallback" : "status_rollup_step");
                        failures.put(job + "\u0000" + Objects.toString(step, ""), failure);
                    }
                }
            }
        }
        Matcher tabular = Pattern.compile(
                "(?m)^([^\\t\\r\\n]+)\\t(?:fail|failure|timed_out|cancelled)\\t")
                .matcher(output);
        while (tabular.find()) {
            String job = tabular.group(1).trim();
            failures.put(job + "\u0000",
                    new CiFailure(job, null, "job_fallback"));
        }
        return new ArrayList<>(failures.values());
    }

    private static boolean failedStepLog(String log) {
        String lower = log.toLowerCase(Locale.ROOT);
        Matcher summary = TEST_SUMMARY.matcher(log);
        while (summary.find()) {
            if (Integer.parseInt(summary.group(2)) > 0
                    || Integer.parseInt(summary.group(3)) > 0) {
                return true;
            }
        }
        return lower.contains("##[error]")
                || lower.contains("build failure")
                || lower.contains("[error]")
                || lower.matches("(?s).*process completed with exit code\\s+[1-9]\\d*.*")
                || lower.contains("test inventory does not match");
    }

    private static List<String> failedCiChecks(String output) {
        return failedCiFailures(output, false).stream()
                .map(CiFailure::job)
                .distinct()
                .toList();
    }

    private static boolean isCopilotOrchestrationCheck(String check) {
        String lower = check.toLowerCase(Locale.ROOT).trim();
        return lower.equals("copilot")
                || lower.contains("running copilot cloud agent")
                || lower.matches("addressing comment on pr #?\\d+");
    }

    private static boolean isAggregateWorkflowCheck(String check) {
        return "main build".equals(check.toLowerCase(Locale.ROOT).trim());
    }

    private ObjectNode classifyFailure(ToolBlock block) {
        String text = ((block.command == null ? "" : block.command) + "\n" + block.output);
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("shepherd failed: no remediation push")) {
            return classification("agent_operational_error", "convergence_failure",
                    "NO_REMEDIATION_PUSH",
                    "The task attempt ended without a remediation commit.");
        }
        if (lower.contains("source option 7 is no longer supported")
                || lower.contains("target option 7 is no longer supported")) {
            return classification("agent_operational_error", "incompatible_default_jdk",
                    "UNSUPPORTED_SOURCE_LEVEL_FOR_SELECTED_JDK",
                    "The selected JDK rejects the project's configured language level.");
        }
        if (lower.contains("no plugin found for prefix")) {
            return classification("agent_operational_error", "nonexistent_plugin",
                    "MAVEN_PLUGIN_PREFIX_NOT_AVAILABLE",
                    "The command invoked a Maven plugin prefix unavailable in the active profile.");
        }
        if (lower.contains("gh: not found (http 404)")
                || lower.contains("error decoding base64 input stream")) {
            return classification("agent_operational_error", "missing_campaign_metadata",
                    "INVALID_REMOTE_RESOURCE_LOOKUP",
                    "The command requested a repository resource or encoded payload that did not exist.");
        }
        if (lower.contains("bot comments require inspection")) {
            return classification("agent_operational_error", "review_gate_pending",
                    "BOT_COMMENT_REVIEW_REQUIRED",
                    "The stage gate stopped for manual inspection of bot comments.");
        }
        if (lower.contains("expected \"actual\" to be strictly unequal")
                && lower.contains("12/15/2026")) {
            return classification("agent_operational_error", "leftover_state",
                    "ACCEPTANCE_LEFTOVER_STATE",
                    "The acceptance script encountered a value persisted by its previous attempt.");
        }
        if (lower.contains("intercepts pointer events")
                || lower.contains("input did not match the regular expression")
                || lower.contains("expected \"actual\" to be strictly unequal")
                || lower.contains("acceptance.mjs") && lower.contains("err_assertion")) {
            return classification("agent_operational_error", "test_harness_error",
                    "FUNCTIONAL_TEST_HARNESS_ERROR",
                    "The functional harness used an invalid interaction or expectation.");
        }
        if (lower.contains("spotless:apply")
                && lower.contains("cannot find git repository in any parent directory")) {
            return classification("agent_operational_error", "worktree_context",
                    "SPOTLESS_GIT_WORKTREE_CONTEXT",
                    "Spotless could not resolve a Git worktree from the agent's execution context.");
        }
        if (lower.contains("command not found") || lower.contains("unknown option")
                || lower.contains("unknown flag") || lower.contains("no such file or directory")
                || lower.contains("specify only one of")
                || lower.contains("--slurp` option is not supported")
                || lower.matches("(?s).*accepts\\s+\\d+\\s+arg\\(s\\),\\s+received\\s+\\d+.*")) {
            return classification("agent_operational_error", "malformed_invocation",
                    "INVALID_TOOL_INVOCATION",
                    "The failure is caused by the agent's command or execution context.");
        }
        if (lower.contains("executable doesn't exist")
                && lower.contains("playwright install")) {
            return classification("infrastructure", "missing_tool_dependency",
                    "PLAYWRIGHT_BROWSER_NOT_INSTALLED",
                    "The functional test was invoked before its browser dependency was installed.");
        }
        if (lower.contains("could not resolve host") || lower.contains("connection reset")
                || lower.contains("connection timed out") || lower.contains("http 502")
                || lower.contains("http 503") || lower.contains("service unavailable")
                || lower.contains("runner") && lower.contains("offline")) {
            return classification("infrastructure", "external_service",
                    "INFRASTRUCTURE_CONNECTIVITY",
                    "The evidence indicates a network, service, or runner failure.");
        }
        if (lower.contains("cannot find symbol") || lower.contains("compilation error")
                || lower.matches("(?s).*tests run:\\s*\\d+,\\s*failures:\\s*[1-9].*")
                || lower.matches("(?s).*tests run:\\s*\\d+.*errors:\\s*[1-9].*")) {
            return classification("product_defect", null,
                    "CODE_OR_TEST_FAILURE",
                    "The code under change failed compilation or a test assertion.");
        }
        boolean failedFormatting = lower.contains("spotless")
                && (lower.contains("build failure") || lower.contains("[error]")
                || lower.matches("(?s).*formatting\\s+(?:fail|failure)\\b.*")
                || lower.matches("(?s).*\"name\"\\s*:\\s*\"formatting\".*"
                + "\"conclusion\"\\s*:\\s*\"failure\".*"));
        if (failedFormatting) {
            return classification("product_defect", "formatting",
                    "FORMATTING_GATE_FAILURE",
                    "The formatting or Spotless gate reported a violation.");
        }
        boolean failedSourceGate = (lower.contains("source-gates")
                || lower.contains("source gates") || lower.contains("verify-source-gates"))
                && (lower.contains("build failure") || lower.contains("[error]")
                || lower.matches("(?s).*source-gates\\s+(?:fail|failure)\\b.*")
                || lower.matches("(?s).*\"name\"\\s*:\\s*\"source-gates\".*"
                + "\"conclusion\"\\s*:\\s*\"failure\".*"));
        if (failedSourceGate) {
            return classification("product_defect", "static_analysis_finding",
                    "SOURCE_GATE_FAILURE",
                    "The repository source-gates check reported a violation.");
        }
        List<String> failedChecks = failedCiChecks(text);
        if (!failedChecks.isEmpty()) {
            String check = failedChecks.get(0);
            String gate = ciGate(check);
            return classification("product_defect",
                    "formatting".equals(gate) ? "style"
                            : "static_analysis".equals(gate)
                            ? "static_analysis_finding" : "behavioral_defect",
                    "FAILED_CI_CHECK",
                    "The substantive CI check '" + check + "' reported failure.");
        }
        return unclassifiedClassification(
                "No deterministic product, operational, or infrastructure rule matched.");
    }

    private static ObjectNode classification(
            String category, String subtype, String rule, String explanation) {
        ObjectNode node = JSON.createObjectNode();
        node.put("status", "classified");
        node.put("category", category);
        if (subtype == null) {
            node.putNull("subtype");
        } else {
            node.put("subtype", subtype);
        }
        node.put("origin", classificationOrigin(category, subtype));
        node.put("ruleId", rule);
        node.put("explanation", explanation);
        return node;
    }

    private static ObjectNode unclassifiedClassification(String explanation) {
        ObjectNode node = JSON.createObjectNode();
        node.put("status", "unclassified");
        node.putNull("category");
        node.putNull("subtype");
        node.putNull("origin");
        node.putNull("ruleId");
        node.put("explanation", explanation);
        return node;
    }

    private ObjectNode addEvent(
            SessionResult session, ToolBlock block, String kind, String gate,
            Integer exitCode, ObjectNode classification) {
        ObjectNode event = JSON.createObjectNode();
        event.put("id", String.format(Locale.ROOT, "event-%04d", ++eventSequence));
        event.put("taskIssue", session.issue);
        if (session.prNumber == null) {
            event.putNull("prNumber");
        } else {
            event.put("prNumber", session.prNumber);
        }
        event.put("sessionId", session.sessionId == null ? "" : session.sessionId);
        event.put("attempt", session.attempt);
        event.put("campaignDirectory", session.campaignDirectory.toString());
        event.put("shepherdStage", session.shepherdStage);
        event.put("eventKind", kind);
        event.put("relativeOffsetSeconds", block.relativeSeconds);
        if (block.jsonlCompletedAt != null && !block.jsonlCompletedAt.isBlank()) {
            event.put("timestamp", block.jsonlCompletedAt);
            event.put("timestampSource", "jsonl_tool_execution_complete");
        } else if (session.startedAt == null) {
            event.putNull("timestamp");
            event.put("timestampSource", "unavailable");
        } else {
            event.put("timestamp", session.startedAt.plusSeconds(block.relativeSeconds).toString());
            event.put("timestampSource", "transcript_sub_offset");
        }
        if (block.headSha == null) {
            event.putNull("headSha");
        } else {
            event.put("headSha", block.headSha);
        }
        boolean productDefect = "product_defect".equals(
                classification.path("category").asText());
        if (productDefect) {
            String normalizedGate = gate == null || gate.isBlank() || "ci".equals(gate)
                    ? "ci_other" : gate;
            event.put("detectionGate", normalizedGate);
            canonicalizeProductClassification(
                    classification, normalizedGate, kind, block.output);
            event.put("defectClass",
                    defectClass(classification.path("subtype").asText()));
        } else {
            event.putNull("detectionGate");
            event.putNull("defectClass");
        }
        event.put("where", isCiBlock(block) ? "ci" : "local");
        event.put("activity", activity(block));
        event.put("commandOrCheck", excerpt(block.command == null ? block.tool : block.command, 600));
        if (exitCode == null) {
            event.putNull("exitCode");
        } else {
            event.put("exitCode", exitCode);
        }
        ObjectNode evidence = event.putObject("evidence");
        evidence.put("artifact", session.markdown.getFileName().toString());
        evidence.put("lineStart", block.startLine);
        evidence.put("lineEnd", block.endLine);
        evidence.put("excerpt", excerpt(block.output, 1200));
        evidence.put("partialOutputCorroborated",
                partialOutputCorroborates(session, block.output));
        event.set("classification", classification);
        event.put("underlyingProblemId", "problem-" + session.issue + "-"
                + session.shepherdStage + "-" + block.startLine);
        event.put("recurrenceSignature", recurrenceSignature(block, classification));
        ObjectNode resolution = event.putObject("resolution");
        resolution.put("status", "unknown");
        resolution.putNull("timestamp");
        resolution.putNull("headSha");
        resolution.putNull("summary");
        events.add(event);
        session.eventIds.add(event.path("id").asText());
        return event;
    }

    private static boolean isCiBlock(ToolBlock block) {
        String command = Objects.toString(block.command, "").toLowerCase(Locale.ROOT);
        return command.contains("gh run view") && command.contains("--log")
                || command.contains("gh pr checks");
    }

    private static String ciFailingStep(String output) {
        String candidate = null;
        for (String line : output.split("\\R")) {
            String cleaned = line.replaceFirst(
                    "^\\S+\\s+\\S+\\s+\\d{4}-\\d{2}-\\d{2}T\\S+\\s+", "").trim();
            Matcher group = Pattern.compile("##\\[group](?:Run\\s+)?(.+)").matcher(cleaned);
            if (group.find()) candidate = group.group(1).trim();
            Matcher error = Pattern.compile("##\\[error](.+)").matcher(cleaned);
            if (error.find() && candidate != null) return candidate;
        }
        return candidate;
    }

    static void canonicalizeProductClassification(
            ObjectNode classification, String gate, String kind, String evidence) {
        String subtype = classification.path("subtype").asText("");
        if ("formatting".equals(gate) || kind.contains("format")) {
            subtype = "formatting";
        } else if ("ccra_review".equals(gate)) {
            subtype = "behavioral_defect";
        } else if ("static_analysis".equals(gate) || "security".equals(gate)) {
            subtype = "static_analysis_finding";
        } else if (subtype.isBlank()) {
            subtype = "behavioral_defect";
        }
        classification.put("subtype", subtype);
        classification.put("origin", classificationOrigin("product_defect", subtype));
    }

    static ObjectNode ciStepClassification(String step) {
        String lower = step.toLowerCase(Locale.ROOT);
        ObjectNode result;
        if (lower.contains("write test inventory")) {
            result = classification("product_defect", "test_inventory",
                    "CI_TEST_INVENTORY_STEP",
                    "The failing CI step enforces the repository test inventory.");
        } else if (lower.contains("verify-build-contract.sh")) {
            result = contractViolation("build");
        } else if (lower.contains("verify-compatibility-contract.sh")) {
            result = contractViolation("compatibility");
        } else if (lower.contains("verify-source-gates.sh")) {
            result = contractViolation("source_gates");
        } else {
            result = classification("product_defect", null,
                    "FAILED_CI_STEP",
                    "A captured CI step reported failure.");
        }
        return result;
    }

    private static ObjectNode contractViolation(String contract) {
        ObjectNode result = classification(
                "product_defect", "contract_violation",
                "CI_CONTRACT_STEP",
                "The failing CI step enforces the " + contract + " contract.");
        result.put("contract", contract);
        return result;
    }

    static String defectClass(String subtype) {
        return switch (subtype) {
            case "formatting" -> "style";
            case "static_analysis_finding" -> "static_analysis_finding";
            case "scope_violation", "test_inventory", "contract_violation",
                    "completeness_gap" -> "completeness";
            case "behavioral_defect" -> "behavioral";
            default -> "behavioral";
        };
    }

    private static String activity(ToolBlock block) {
        String command = Objects.toString(block.command, "").toLowerCase(Locale.ROOT);
        if (command.contains("gh api")) return "github_api_query";
        if (command.contains("gh pr view")) return "github_pr_query";
        if (command.contains("mvn") && command.contains("liberty:")) return "maven_liberty_goal";
        if (command.contains("mvn") && command.contains("package")) return "maven_package";
        if (command.contains("playwright") || command.contains("acceptance.mjs")) {
            return "browser_acceptance_script";
        }
        if (command.contains("atomic") || command.contains("merge")) return "merge_gate_script";
        return block.tool.replace('-', '_');
    }

    private static String recurrenceSignature(
            ToolBlock block, ObjectNode classification) {
        String text = (Objects.toString(block.command, "") + "\n" + block.output)
                .toLowerCase(Locale.ROOT);
        if (text.contains("--slurp") && text.contains("--jq")) {
            return "gh_api_slurp_with_jq";
        }
        if (text.contains("gh pr view") && text.contains("--comments")
                && text.contains("--json")) {
            return "gh_pr_view_comments_with_json";
        }
        if (text.matches("(?s).*accepts\\s+\\d+\\s+arg\\(s\\),\\s+received\\s+\\d+.*")) {
            return "gh_api_wrong_argument_count";
        }
        if (text.contains("no plugin found for prefix")) {
            return "maven_unavailable_plugin_prefix";
        }
        return classification.path("ruleId").asText("unclassified").toLowerCase(Locale.ROOT);
    }

    private static String classificationOrigin(String category, String subtype) {
        if ("product_defect".equals(category)) {
            return "product_code";
        }
        if ("external_service".equals(subtype)) {
            return "external_service";
        }
        if ("missing_tool_dependency".equals(subtype)) {
            return "local_environment";
        }
        if ("test_harness_error".equals(subtype)
                || "leftover_state".equals(subtype)) {
            return "agent_authored_test_harness";
        }
        if ("incompatible_default_jdk".equals(subtype)
                || "missing_campaign_metadata".equals(subtype)) {
            return "shepherd_harness";
        }
        if ("agent_operational_error".equals(category)) {
            return "agent_tool_invocation";
        }
        return "ci_runner";
    }

    private static boolean partialOutputCorroborates(
            SessionResult session, String markdownOutput) {
        String needle = Arrays.stream(markdownOutput.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.startsWith("<"))
                .filter(line -> !line.startsWith("```"))
                .findFirst().orElse("");
        if (needle.length() > 120) {
            needle = needle.substring(0, 120);
        }
        if (needle.length() < 12) {
            return false;
        }
        for (String partial : session.partialOutputs.values()) {
            if (partial.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private void refreshUnclassified() {
        unclassified.clear();
        for (ObjectNode event : events) {
            if (!"unclassified".equals(
                    event.path("classification").path("status").asText())) {
                continue;
            }
            ObjectNode unresolved = event.deepCopy();
            unresolved.put("reason",
                    event.path("classification").path("explanation").asText());
            unclassified.add(unresolved);
        }
    }

    private void parseTransitions(SessionResult session, ToolBlock block) {
        List<JsonNode> roots = parseJsonValues(block.output);
        for (JsonNode root : roots) {
            collectTransitions(session, block, root, null, false);
        }
    }

    private void collectTransitions(
            SessionResult session, ToolBlock block, JsonNode node,
            String inheritedHead, boolean insideSteps) {
        if (node == null) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                collectTransitions(session, block, child, inheritedHead, insideSteps);
            }
            return;
        }
        if (!node.isObject()) {
            return;
        }
        String head = node.has("headRefOid") ? node.path("headRefOid").asText() : inheritedHead;
        if (node.has("headSha")) {
            head = node.path("headSha").asText(head);
        }
        String check = firstText(node, "name", "context", "workflowName");
        String conclusion = firstText(node, "conclusion", "state", "status");
        if (!insideSteps && check != null && conclusion != null
                && Set.of("SUCCESS", "FAILURE", "ERROR", "CANCELLED", "TIMED_OUT",
                "success", "failure", "error", "cancelled", "timed_out")
                .contains(conclusion)) {
            if (isOrchestrationCheck(check)) {
                session.remoteAgentCheckObservations++;
                return;
            }
            ObjectNode transition = JSON.createObjectNode();
            transition.put("timestamp", session.startedAt == null ? null
                    : session.startedAt.plusSeconds(block.relativeSeconds).toString());
            if (head == null || head.isBlank()) {
                transition.putNull("headSha");
            } else {
                transition.put("headSha", head);
            }
            transition.put("check", check);
            transition.put("result", conclusion.toLowerCase(Locale.ROOT));
            transition.put("agentAction", excerpt(block.command, 300));
            transition.put("greenToRed", false);
            session.transitions.add(transition);
        }
        String childHead = head;
        node.fields().forEachRemaining(entry ->
                collectTransitions(session, block, entry.getValue(), childHead,
                        insideSteps || "steps".equals(entry.getKey())));
    }

    private static boolean isOrchestrationCheck(String check) {
        String lower = check.toLowerCase(Locale.ROOT);
        return lower.equals("running copilot cloud agent")
                || lower.matches("addressing comment on pr #\\d+")
                || lower.equals("copilot") || lower.startsWith("copilot /");
    }

    private static String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            if (node.has(field) && node.path(field).isValueNode()
                    && !node.path(field).asText().isBlank()) {
                return node.path(field).asText();
            }
        }
        return null;
    }

    private static List<JsonNode> parseJsonValues(String output) {
        List<JsonNode> values = new ArrayList<>();
        String[] lines = output.split("\\R");
        for (int start = 0; start < lines.length; start++) {
            String trimmed = lines[start].trim();
            if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) {
                continue;
            }
            StringBuilder candidate = new StringBuilder();
            for (int end = start; end < Math.min(lines.length, start + 500); end++) {
                candidate.append(lines[end]).append('\n');
                try {
                    JsonNode value = JSON.readTree(candidate.toString());
                    if (value != null) {
                        values.add(value);
                        start = end;
                        break;
                    }
                } catch (JsonProcessingException ignored) {
                    // Continue until a complete JSON value has been accumulated.
                }
            }
        }
        return values;
    }

    private void parseReviewEvidence(SessionResult session, ToolBlock block) {
        Matcher matcher = REVIEW_ID_OUTPUT.matcher(block.output);
        while (matcher.find()) {
            session.reviewIds.add(matcher.group(1));
        }
        if (block.command == null
                || !block.command.contains("/comments")
                || !block.command.contains("in_reply_to_id == null")) {
            return;
        }
        for (JsonNode value : parseJsonValues(block.output)) {
            if (!value.isArray()) {
                continue;
            }
            for (JsonNode comment : value) {
                if (!comment.has("id") || !comment.has("body")) {
                    continue;
                }
                String commentId = comment.path("id").asText();
                if (!session.reviewCommentIds.add(commentId)) {
                    continue;
                }
                ObjectNode classification = classification(
                        "product_defect", "behavioral_defect", "CCRA_ACTIONABLE_COMMENT",
                        "A top-level Copilot code-review comment requested a code correction.");
                ObjectNode event = addEvent(session, block, "review_finding",
                        "ccra_review", null, classification);
                event.put("reviewCommentId", commentId);
                event.put("underlyingProblemId", "problem-review-" + commentId);
                Matcher review = Pattern.compile(
                        "--argjson\\s+review_id\\s+(\\d+)").matcher(block.command);
                if (review.find()) {
                    String timestamp = session.reviewCompletionTimestamps.get(review.group(1));
                    if (timestamp != null) {
                        event.put("timestamp", timestamp);
                        event.put("timestampSource", "jsonl_tool_execution_complete");
                    }
                }
                event.put("commandOrCheck", comment.path("path").asText("Copilot review"));
                event.path("evidence").deepCopy();
                ((ObjectNode) event.path("evidence")).put(
                        "excerpt", excerpt(comment.path("body").asText(), 1200));
            }
        }
    }

    private void parseStage30ChangeRequests(SessionResult session, ToolBlock block) {
        if (session.shepherdStage != 30) {
            return;
        }
        if (block.command != null && block.command.contains("gh pr review")
                && block.command.contains("--request-changes")) {
            Matcher head = Pattern.compile(
                    "(?:CURRENT|CURRENT_SHA)=['\"]([0-9a-f]{40})").matcher(block.command);
            if (head.find()) {
                block.headSha = head.group(1);
            }
            Matcher previous = Pattern.compile(
                    "\"previousHead\"\\s*:\\s*\"([0-9a-f]{40})\"|unchanged=([0-9a-f]{40})")
                    .matcher(block.output);
            if (previous.find()) {
                block.headSha = previous.group(1) == null
                        ? previous.group(2) : previous.group(1);
            }
            String body = stage30RequestBody(block.command);
            String key = "posted\u0000" + body;
            if (session.changeRequestKeys.add(key)) {
                Stage30Decision decision = stage30Decision(body);
                String subtype = decision.subtype();
                boolean alreadyDetected = decision.classified() && events.stream()
                        .anyMatch(event -> event.path("taskIssue").asInt() == session.issue
                                && "product_defect".equals(event.path("classification")
                                .path("category").asText())
                                && subtype.equals(event.path("classification")
                                .path("subtype").asText()));
                if (alreadyDetected) {
                    return;
                }
                ObjectNode classification = decision.classified()
                        ? classification(
                                "product_defect", subtype, "STAGE_30_CHANGE_REQUEST",
                                "The stage-30 gate requested a substantive code or test change.")
                        : unclassifiedClassification(decision.explanation());
                ObjectNode event = addEvent(session, block, "stage_30_change_request",
                        "stage_30_gate", null, classification);
                event.put("gateSource", decision.source());
                Matcher pr = Pattern.compile(
                        "(?:PR_NUMBER|PR)=['\"]?(\\d+)").matcher(block.command);
                if (pr.find()) {
                    session.prNumber = Integer.parseInt(pr.group(1));
                    event.put("prNumber", session.prNumber);
                }
                Matcher submitted = Pattern.compile(
                        "REVIEW_SUBMITTED_AT=(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z)")
                        .matcher(block.output);
                if (submitted.find()) {
                    event.put("timestamp", submitted.group(1));
                    event.put("timestampSource", "command_output_review_submitted_at");
                }
                Matcher finished = Pattern.compile(
                        "LATEST_FINISH=(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z)")
                        .matcher(block.output);
                if (finished.find()) {
                    event.put("fixTimestampHint", finished.group(1));
                    event.put("fixTimestampSourceHint",
                            "command_output_copilot_work_finished");
                }
                ((ObjectNode) event.path("evidence")).put("excerpt", body);
                event.put("itemCount", changeRequestItemCount(body));
            }
            return;
        }
        if (!block.output.contains("\"CHANGES_REQUESTED\"")
                || !session.changeRequestKeys.isEmpty()) {
            return;
        }
        for (JsonNode root : parseJsonValues(block.output)) {
            Deque<JsonNode> pending = new ArrayDeque<>();
            pending.add(root);
            while (!pending.isEmpty()) {
                JsonNode node = pending.removeFirst();
                if (node.isArray()) {
                    node.forEach(pending::addLast);
                    continue;
                }

                if (!node.isObject()) {
                    continue;
                }
                if ("CHANGES_REQUESTED".equals(node.path("state").asText())
                        && node.has("body")) {
                    String key = node.path("submitted_at").asText()
                            + "\u0000" + node.path("body").asText();
                    if (session.changeRequestKeys.add(key)) {
                        String body = node.path("body").asText();
                        Stage30Decision decision = stage30Decision(body);
                        ObjectNode classification = decision.classified()
                                ? classification(
                                        "product_defect", decision.subtype(),
                                        "STAGE_30_CHANGE_REQUEST",
                                        "The stage-30 gate requested a substantive code or test change.")
                                : unclassifiedClassification(decision.explanation());
                        ObjectNode event = addEvent(session, block, "stage_30_change_request",
                                "stage_30_gate", null, classification);
                        event.put("gateSource", decision.source());
                        if (node.path("submitted_at").isTextual()) {
                            event.put("timestamp", node.path("submitted_at").asText());
                            event.put("timestampSource",
                                    "github_review_submitted_at");
                        }
                        ((ObjectNode) event.path("evidence")).put(
                                "excerpt", excerpt(body, 1200));
                        event.put("itemCount",
                                changeRequestItemCount(body));
                    }
                }

                node.elements().forEachRemaining(pending::addLast);
            }
        }
    }

    static Stage30Decision stage30Decision(String body) {
        List<String> headings = Arrays.stream(body.split("\\R"))
                .map(String::strip)
                .filter(line -> line.matches("#{1,6}\\s+.+"))
                .map(line -> line.replaceFirst("^#{1,6}\\s+", "")
                        .toLowerCase(Locale.ROOT))
                .toList();
        boolean scopeHeading = headings.stream().anyMatch(heading ->
                heading.contains("diff scope violation")
                        || heading.startsWith("scope violation")
                        || heading.contains("issue requirement failure: diff scope")
                        || Pattern.compile("(^|\\s)scope\\s*:").matcher(heading).find());
        if (scopeHeading) {
            return new Stage30Decision(
                    "scope_violation", true, "stage30_heading", "");
        }
        boolean formattingHeading = headings.stream().anyMatch(heading ->
                heading.contains("formatting") || heading.contains("spotless"));
        if (formattingHeading) {
            return new Stage30Decision(
                    "formatting", true, "stage30_heading", "");
        }
        String lower = body.toLowerCase(Locale.ROOT);
        if (lower.contains("diff scope") || lower.contains("scope violation")
                || lower.contains("out-of-scope") || lower.contains("out of scope")
                || lower.contains("unrelated") || lower.contains("diff is confined")
                || lower.contains("wrapper changes")) {
            return new Stage30Decision(
                    "scope_violation", false, "free_text_fallback",
                    "Scope-like free text was present, but the stage-30 request "
                            + "had no recognized scope heading.");
        }
        return new Stage30Decision(
                "completeness_gap", true, "stage30_heading", "");
    }

    private static int changeRequestItemCount(String body) {
        long sections = Arrays.stream(body.split("\\R"))
                .map(String::strip)
                .filter(line -> line.startsWith("## "))
                .count();
        if (sections > 0) {
            return (int) sections;
        }
        long bullets = Arrays.stream(body.split("\\R"))
                .map(String::strip)
                .filter(line -> line.matches("(?:[-*]|\\d+[.)])\\s+.+"))
                .count();
        return (int) Math.max(1, bullets);
    }

    private static String stage30RequestBody(String command) {
        Matcher heredoc = Pattern.compile(
                "(?s)(?:REVIEW_BODY|BODY)=\\$\\(cat\\s+<<'?EOF'?\\R(.*?)\\REOF\\R?\\)")
                .matcher(command);
        if (heredoc.find()) {
            return heredoc.group(1).strip();
        }
        Matcher quoted = Pattern.compile(
                "(?s)(?:REVIEW_BODY|BODY)=['\"](.*?)['\"]\\R.*?gh\\s+pr\\s+review")
                .matcher(command);
        if (quoted.find()) {
            return quoted.group(1).strip();
        }
        return Arrays.stream(command.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.startsWith("@copilot"))
                .filter(line -> !line.startsWith("REVIEW_BODY=")
                        && !line.startsWith("BODY="))
                .filter(line -> !line.startsWith("##"))
                .findFirst().map(line -> excerpt(line, 500))
                .orElse("Stage-30 change request");
    }

    private void parseCommitEvidence(SessionResult session, ToolBlock block) {
        Matcher matcher = COMMIT_OUTPUT.matcher(block.output);
        while (matcher.find()) {
            String sha = matcher.group(1);
            String subject = matcher.group(2).trim();
            block.commitShas.add(sha);
            if (session.commitShas.add(sha) && !subject.equalsIgnoreCase("Initial plan")) {
                session.fixCommits++;
            }
        }
    }

    private void resolveEvents(Map<Integer, TaskResult> tasks, List<SessionResult> sessions) {
        for (ObjectNode event : events) {
            ObjectNode resolution = (ObjectNode) event.path("resolution");
            String category = event.path("classification").path("category").asText();
            int issue = event.path("taskIssue").asInt();
            if ("product_defect".equals(category)) {
                TaskResult task = tasks.get(issue);
                if (event.path("prNumber").isNull()
                        && task != null && task.prNumber != null) {
                    event.put("prNumber", task.prNumber);
                }
                if (task != null && task.gitTask != null) {
                    String detectedHead = event.path("headSha").asText("");
                    String fixedHead = task.gitTask.headSha;
                    if (detectedHead.isBlank()) {
                        resolution.put("status", "unknown");
                        resolution.put("summary",
                                "No detected head SHA was captured, so fix ancestry cannot be verified.");
                        continue;
                    }
                    if (detectedHead.equals(fixedHead)
                            || !isAncestor(config.repo, detectedHead, fixedHead)) {
                        resolution.put("status", "unresolved");
                        resolution.put("headSha", fixedHead);
                        resolution.put("summary",
                                "The candidate fix must differ from and descend from the detected head.");
                        continue;
                    }
                    resolution.put("status", "fixed");
                    resolution.put("headSha", fixedHead);
                    if (event.path("fixTimestampHint").isTextual()) {
                        resolution.put("timestamp", event.path("fixTimestampHint").asText());
                        resolution.put("timestampSource",
                                event.path("fixTimestampSourceHint").asText());
                    } else {
                        long offset = event.path("relativeOffsetSeconds").asLong();
                        sessions.stream()
                                .filter(session -> session.issue == issue)
                                .filter(session -> session.shepherdStage
                                        == event.path("shepherdStage").asInt())
                                .flatMap(session -> session.toolBlocks.stream()
                                        .map(block -> Map.entry(session, block)))
                                .filter(entry -> entry.getValue().relativeSeconds > offset)
                                .filter(entry -> task.gitTask.headSha.equals(
                                        entry.getValue().headSha))
                                .findFirst()
                                .ifPresent(entry -> {
                                    resolution.put("timestamp",
                                            entry.getKey().startedAt.plusSeconds(
                                                    entry.getValue().relativeSeconds).toString());
                                    resolution.put("timestampSource",
                                            "transcript_sub_offset");
                                });
                    }
                    resolution.put("summary",
                            "The task's final PR head contains the accepted remediation.");
                    continue;
                }
            }
            if ("infrastructure".equals(category)
                    && "flaky_test".equals(
                    event.path("classification").path("subtype").asText())) {
                resolution.put("status", "passed_on_rerun");
                copyNullable(event.path("rerunEvidence"), resolution, "rerunHeadSha");
                resolution.put("summary",
                        "The test passed on an unchanged head.");
                continue;
            }
            if ("agent_operational_error".equals(category)) {
                long offset = event.path("relativeOffsetSeconds").asLong();
                boolean recovered = sessions.stream()
                        .filter(session -> session.issue == issue)
                        .filter(session -> session.shepherdStage
                                == event.path("shepherdStage").asInt())
                        .flatMap(session -> session.toolBlocks.stream())
                        .anyMatch(block -> block.relativeSeconds > offset
                                && block.exitCode != null && block.exitCode == 0);
                if (recovered) {
                    resolution.put("status", "recovered");
                    resolution.put("summary",
                            "A later tool invocation completed successfully in the same session.");
                }
            }
            if ("infrastructure".equals(category)
                    && "missing_tool_dependency".equals(
                    event.path("classification").path("subtype").asText())) {
                long offset = event.path("relativeOffsetSeconds").asLong();
                boolean recovered = sessions.stream()
                        .filter(session -> session.issue == issue)
                        .filter(session -> session.shepherdStage
                                == event.path("shepherdStage").asInt())
                        .flatMap(session -> session.toolBlocks.stream())
                        .anyMatch(block -> block.relativeSeconds > offset
                                && block.exitCode != null && block.exitCode == 0
                                && (Objects.toString(block.command, "").contains("playwright")
                                || Objects.toString(block.command, "").contains("acceptance.mjs")));
                if (recovered) {
                    resolution.put("status", "recovered");
                    resolution.put("summary",
                            "A later browser acceptance invocation succeeded in the same session.");
                }
            }
        }
    }

    private static boolean isAncestor(Path repo, String ancestor, String descendant) {
        return command(List.of("git", "-C", repo.toString(), "merge-base",
                "--is-ancestor", ancestor, descendant), null, Map.of()).exitCode == 0;
    }

    private static void countHallucinations(SessionResult session, ToolBlock block) {
        String lower = block.output.toLowerCase(Locale.ROOT);
        if (lower.contains("cannot find symbol") || lower.contains("package ")
                && lower.contains(" does not exist")
                || lower.contains("could not find artifact")
                || lower.contains("enforcer") && lower.contains("failed")) {
            session.productHallucinationSignals++;
        }
        if (lower.contains("no plugin found for prefix") || lower.contains("command not found")
                || lower.contains("unknown option") || lower.contains("unknown flag")) {
            session.toolingHallucinationSignals++;
        }
    }

    private static void countConvergenceSignals(SessionResult session, ToolBlock block) {
        String command = block.command == null ? "" : block.command;
        String output = block.output.toLowerCase(Locale.ROOT);
        if (command.contains("gh run rerun")) {
            session.ciReruns++;
        }
        if (output.contains("review cap") || output.contains("maximum review")) {
            session.reviewCapEvents++;
        }
        if (output.contains("idle") && output.contains("kill")) {
            session.idleKillEvents++;
        }
        if ((output.contains("timed out") || output.contains("timeout"))
                && (output.contains("review") || output.contains("workflow"))) {
            session.timeoutEvents++;
        }
    }

    private void applyFlakyRerunRules(List<SessionResult> sessions) {
        for (SessionResult session : sessions) {
            Map<String, ToolBlock> failed = new LinkedHashMap<>();
            String lastCommit = null;
            for (ToolBlock block : session.toolBlocks) {
                if (!block.testFailureEvents.isEmpty()) {
                    failed.put(normalizeCommand(block.command), block);
                }
                if (!block.commitShas.isEmpty()) {
                    lastCommit = block.commitShas.get(block.commitShas.size() - 1);
                }
                if (block.exitCode != null && block.exitCode == 0
                        && isTestCommand(block.command)
                        && (block.output.contains("BUILD SUCCESS")
                        || block.output.matches("(?s).*Tests run:\\s*\\d+,\\s*Failures:\\s*0,\\s*Errors:\\s*0.*"))) {
                    ToolBlock prior = failed.get(normalizeCommand(block.command));
                    if (prior != null && Objects.equals(lastCommit, prior.commitAtExecution)) {
                        for (ObjectNode event : prior.testFailureEvents) {
                            ObjectNode classification = classification(
                                    "infrastructure", "flaky_test",
                                    "TEST_PASS_ON_UNCHANGED_HEAD_RERUN",
                                    "The same test command passed on rerun without an intervening code change.");
                            String cause = detectFlakinessCause(prior.output);
                            classification.put("flakinessCause", cause);
                            event.set("classification", classification);
                            ObjectNode rerun = event.putObject("rerunEvidence");
                            rerun.put("originalHeadSha", nullToEmpty(prior.headSha));
                            rerun.put("rerunHeadSha", nullToEmpty(block.headSha));
                            rerun.put("interveningCodeChange", false);
                            rerun.put("originalResult", "failed");
                            rerun.put("rerunResult", "passed");
                            session.flakyTestFailures++;
                            String gate = event.path("detectionGate").asText();
                            if ("unit_tests".equals(gate)) {
                                session.flakyUnitTestFailures++;
                            } else if ("arquillian_container_tests".equals(gate)) {
                                session.flakyContainerTestFailures++;
                            }
                            if ("leftover_state".equals(cause)) {
                                session.leftoverStateFailures++;
                            } else if ("unknown".equals(cause)) {
                                session.unknownCauseFlakyFailures++;
                            } else {
                                session.otherKnownCauseFlakyFailures++;
                            }
                        }
                    }
                }
                block.commitAtExecution = lastCommit;
            }
        }
    }

    private static String detectFlakinessCause(String output) {
        String lower = output.toLowerCase(Locale.ROOT);
        if (lower.contains("already exists") || lower.contains("duplicate key")
                || lower.contains("constraint violation") || lower.contains("leftover")
                || lower.contains("previous run") || lower.contains("database is locked")
                || lower.contains("address already in use")) {
            return "leftover_state";
        }
        if (lower.contains("timed out") || lower.contains("race")) {
            return "timing_or_race";
        }
        if (lower.contains("connection") || lower.contains("network")) {
            return "network_or_external_service";
        }
        return "unknown";
    }

    private static String normalizeCommand(String command) {
        if (command == null) {
            return "";
        }
        return command.replaceAll("\\s+", " ")
                .replaceAll("LOG=['\"][^'\"]+['\"]", "LOG=<log>")
                .trim();
    }

    private List<ObjectNode> deduplicateProductDefects() {
        Map<String, ObjectNode> defects = new LinkedHashMap<>();
        for (ObjectNode event : events) {
            if (!"product_defect".equals(
                    event.path("classification").path("category").asText())) {
                continue;
            }
            String fingerprint = defectFingerprint(event);
            ObjectNode defect = defects.get(fingerprint);
            if (defect == null) {
                defect = JSON.createObjectNode();
                defect.put("id", "defect-" + (defects.size() + 1));
                defect.put("taskIssue", event.path("taskIssue").asInt());
                copyNullable(event, defect, "prNumber");
                defect.put("categorySubtype",
                        event.path("classification").path("subtype").asText());
                defect.put("defectClass", event.path("defectClass").asText("behavioral"));
                defect.put("itemCount", event.path("itemCount").asInt(1));
                defect.put("summary", summarizeDefect(event));
                ObjectNode first = defect.putObject("firstDetection");
                first.put("eventId", event.path("id").asText());
                first.put("shepherdStage", event.path("shepherdStage").asInt());
                first.put("detectionGate", event.path("detectionGate").asText());
                copyNullable(event, first, "timestamp");
                first.put("timestampSource",
                        event.path("timestampSource").asText("unavailable"));
                copyNullable(event, first, "headSha");
                defect.putArray("occurrenceEventIds");
                ObjectNode resolution = defect.putObject("resolution");
                String resolutionStatus = event.path("resolution")
                        .path("status").asText("unknown");
                resolution.put("status", resolutionStatus);
                if ("fixed".equals(resolutionStatus)
                        && event.path("resolution").path("headSha").isTextual()) {
                    String commit = event.path("resolution").path("headSha").asText();
                    resolution.put("commit", commit);
                    Instant fixedAt = event.path("resolution").path("timestamp").isTextual()
                            ? Instant.parse(event.path("resolution").path("timestamp").asText())
                            : gitCommitTime(commit);
                    if (fixedAt != null) {
                        resolution.put("fixedAt", fixedAt.toString());
                        resolution.put("timestampSource",
                                event.path("resolution").path("timestampSource")
                                        .asText("git_commit_timestamp"));
                        if (event.path("timestamp").isTextual()) {
                            long seconds = Duration.between(
                                    Instant.parse(event.path("timestamp").asText()),
                                    fixedAt).getSeconds();
                            resolution.put("timeToFixSeconds", seconds);
                            if (seconds < 0) {
                                resolution.put("anomaly", "negative_time_to_fix");
                            }
                        }
                    } else {
                        resolution.putNull("fixedAt");
                        resolution.putNull("timeToFixSeconds");
                    }
                } else {
                    resolution.putNull("commit");
                    resolution.putNull("fixedAt");
                    resolution.putNull("timeToFixSeconds");
                }
                defects.put(fingerprint, defect);
            } else if ("stage_30_change_request".equals(event.path("eventKind").asText())) {
                defect.put("itemCount", Math.max(
                        defect.path("itemCount").asInt(1),
                        event.path("itemCount").asInt(1)));
            }
            ((ArrayNode) defect.path("occurrenceEventIds")).add(event.path("id").asText());
            defect.put("occurrenceCount",
                    defect.path("occurrenceEventIds").size());
        }
        return new ArrayList<>(defects.values());
    }

    private Instant gitCommitTime(String commit) {
        if (config.repo == null || commit == null || commit.isBlank()) return null;
        CommandResult result = command(List.of(
                "git", "-C", config.repo.toString(), "show", "-s",
                "--format=%cI", commit), null, Map.of());
        try {
            return result.exitCode == 0 ? Instant.parse(result.output.trim()) : null;
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static String defectFingerprint(ObjectNode event) {
        String base = event.path("taskIssue").asText() + "|"
                + event.path("attempt").asText() + "|"
                + event.path("shepherdStage").asText() + "|"
                + event.path("classification").path("subtype").asText() + "|"
                + event.path("detectionGate").asText();
        if ("review_finding".equals(event.path("eventKind").asText())) {
            return base + "|" + event.path("underlyingProblemId").asText();
        }
        return base;
    }

    private static String summarizeDefect(ObjectNode event) {
        String excerpt = event.path("evidence").path("excerpt").asText();
        String firstLine = Arrays.stream(excerpt.split("\\R"))
                .map(String::trim)
                .filter(Predicate.not(String::isBlank))
                .filter(line -> !line.startsWith("@copilot"))
                .filter(line -> !line.startsWith("REVIEW_BODY="))
                .filter(line -> !line.startsWith("##"))
                .findFirst()
                .orElse(event.path("eventKind").asText());
        return excerpt(firstLine, 300);
    }

    private ObjectNode aggregateTraceCost(List<SessionResult> sessions) {
        BigDecimal nanoAiu = BigDecimal.ZERO;
        int premium = 0;
        long inference = 0;
        long tools = 0;
        TokenTotals tokens = new TokenTotals();
        ObjectNode stages = JSON.createObjectNode();
        for (int stage : List.of(30, 40)) {
            List<SessionResult> selected = sessions.stream()
                    .filter(value -> value.shepherdStage == stage).toList();
            stages.set(Integer.toString(stage), costForSessions(selected));
        }
        for (SessionResult session : sessions) {
            nanoAiu = nanoAiu.add(session.nanoAiu);
            premium += session.premiumRequests;
            inference += session.inferenceCalls;
            tools += session.toolCalls;
            tokens.add(session.tokens);
        }
        ObjectNode cost = JSON.createObjectNode();
        cost.set("aiu", metric(nanoAiu.divide(BigDecimal.valueOf(1_000_000_000L)),
                "measured", "Final session.usage_checkpoint totalNanoAiu per phase session."));
        cost.set("premiumRequests", metric(premium, "measured",
                "Final totalPremiumRequests checkpoint per phase session."));
        cost.set("tokens", tokens.toJson());
        cost.set("inferenceCalls", metric(inference, "measured", "model.call_start count."));
        cost.set("toolCalls", metric(tools, "measured", "tool.execution_complete count."));
        cost.set("byShepherdStage", stages);
        return cost;
    }

    private static ObjectNode costForSessions(List<SessionResult> sessions) {
        BigDecimal nanoAiu = BigDecimal.ZERO;
        int premium = 0;
        long inference = 0;
        long tools = 0;
        long duration = 0;
        TokenTotals tokens = new TokenTotals();
        for (SessionResult session : sessions) {
            nanoAiu = nanoAiu.add(session.nanoAiu);
            premium += session.premiumRequests;
            inference += session.inferenceCalls;
            tools += session.toolCalls;
            duration += session.durationSeconds;
            tokens.add(session.tokens);
        }
        ObjectNode value = JSON.createObjectNode();
        value.set("aiu", metric(nanoAiu.divide(BigDecimal.valueOf(1_000_000_000L)),
                "measured", "Usage checkpoints."));
        value.set("premiumRequests", metric(premium, "measured", "Usage checkpoints."));
        value.set("tokens", tokens.toJson());
        value.set("inferenceCalls", metric(inference, "measured", "JSONL events."));
        value.set("toolCalls", metric(tools, "measured", "JSONL events."));
        value.set("recordedWallTimeSeconds", metric(duration, "measured", "Transcript headers."));
        return value;
    }

    private ObjectNode analyzePostMortem() throws IOException {
        List<Path> jsonlFiles = new ArrayList<>();
        for (Path directory : config.campaignDirs) {
            try (var stream = Files.list(directory)) {
                jsonlFiles.addAll(stream.filter(path ->
                                path.getFileName().toString().startsWith("post-mortem-session-")
                                        && path.getFileName().toString().endsWith(".jsonl"))
                        .sorted().toList());
            }
        }
        if (jsonlFiles.isEmpty()) {
            return unavailableSection("No post-mortem session JSONL was present.");
        }
        BigDecimal nanoAiu = BigDecimal.ZERO;
        int premium = 0;
        int inference = 0;
        int tools = 0;
        int gaps = 0;
        for (Path path : jsonlFiles) {
            JsonNode checkpoint = null;
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                JsonNode event = JSON.readTree(line);
                String type = event.path("type").asText();
                JsonNode data = event.path("data");
                if ("session.usage_checkpoint".equals(type)) {
                    checkpoint = data;
                } else if ("model.call_start".equals(type)) {
                    inference++;
                } else if ("tool.execution_complete".equals(type)) {
                    tools++;
                    if ("true".equals(data.path("toolTelemetry").path("properties")
                            .path("largeSessionLogWrittenToFile").asText())) {
                        gaps++;
                    }
                }
            }
            if (checkpoint != null) {
                nanoAiu = nanoAiu.add(checkpoint.path("totalNanoAiu").decimalValue());
                premium += checkpoint.path("totalPremiumRequests").asInt();
            }
        }
        ObjectNode result = JSON.createObjectNode();
        result.put("availability", "measured");
        result.set("aiu", metric(nanoAiu.divide(BigDecimal.valueOf(1_000_000_000L)),
                "measured", "Post-mortem session usage checkpoint."));
        result.set("premiumRequests", metric(premium, "measured", "Post-mortem checkpoint."));
        result.set("inferenceCalls", metric(inference, "measured", "Post-mortem JSONL."));
        result.set("toolCalls", metric(tools, "measured", "Post-mortem JSONL."));
        result.set("externalLargeOutputReferences", metric(gaps, "measured", "Post-mortem JSONL."));
        result.set("tokens", unavailableMetric(
                "No post-mortem OTEL artifact is present in the campaign directory."));
        return result;
    }

    private ObjectNode analyzeRepository(Map<Integer, TaskResult> tasks) throws Exception {
        ObjectNode result = JSON.createObjectNode();
        result.put("availability", "measured");
        Map<Integer, GitTask> gitTasks = new TreeMap<>();
        for (TaskResult task : tasks.values()) {
            if (task.prNumber == null) {
                continue;
            }
            GitTask gitTask = resolveGitTask(task.issue, task.prNumber);
            if (gitTask != null) {
                gitTasks.put(task.issue, gitTask);
                task.gitTask = gitTask;
            }
        }
        if (gitTasks.isEmpty()) {
            return unavailableSection("No task PR merge commits could be resolved from local history.");
        }
        List<GitTask> ordered = gitTasks.values().stream()
                .sorted(Comparator.comparing(value -> value.mergeTime)).toList();
        String startSha = ordered.get(0).baseSha;
        String finalSha = ordered.get(ordered.size() - 1).mergeSha;
        String startRoot = inferMavenProjectRoot(config.repo, startSha);
        String finalRoot = inferMavenProjectRoot(config.repo, finalSha);
        result.put("startSha", startSha);
        result.put("finalSha", finalSha);
        result.put("startProjectRoot", startRoot);
        result.put("finalProjectRoot", finalRoot);
        ObjectNode roots = result.putObject("projectRootsBySha");
        roots.put(startSha, startRoot);
        roots.put(finalSha, finalRoot);
        ObjectNode buildConfiguration = result.putObject("buildConfigurationBySha");
        buildConfiguration.set(startSha,
                mavenBuildConfiguration(config.repo, startSha, startRoot));
        buildConfiguration.set(finalSha,
                mavenBuildConfiguration(config.repo, finalSha, finalRoot));

        ArrayNode perTask = result.putArray("tasks");
        DiffMetrics campaignDiff = diffMetrics(startSha, finalSha);
        for (GitTask task : ordered) {
            DiffMetrics diff = diffMetrics(task.baseSha, task.headSha);
            task.diff = diff;
            ObjectNode node = JSON.createObjectNode();
            node.put("taskIssue", task.issue);
            node.put("prNumber", task.prNumber);
            node.put("baseSha", task.baseSha);
            node.put("headSha", task.headSha);
            node.put("mergeSha", task.mergeSha);
            node.set("diff", diff.toJson());
            int comments = tasks.get(task.issue).sessions.stream()
                    .mapToInt(value -> value.reviewCommentIds.size()).sum();
            long changedLines = diff.additions + diff.deletions;
            node.set("reviewCommentsPer100ChangedLines",
                    changedLines == 0
                            ? unavailableMetric("The task has no changed lines.")
                            : metric(BigDecimal.valueOf(comments * 100.0 / changedLines)
                                    .setScale(3, RoundingMode.HALF_UP),
                            "derived", "CCRA comments / changed lines * 100."));
            String taskRoot = inferMavenProjectRoot(config.repo, task.headSha);
            roots.put(task.headSha, taskRoot);
            node.put("projectRoot", taskRoot);
            node.set("testIntegrity", testIntegrity(startSha, task.headSha,
                    startRoot, taskRoot));
            perTask.add(node);
        }
        result.set("campaignDiff", campaignDiff.toJson());
        result.set("baselineVerification", baselineVerification(startSha));
        result.set("startCiAndBuildGates",
                analyzeStartCiAndBuild(config.repo, startSha, startRoot));
        result.set("testIntegrity", testIntegrity(
                startSha, finalSha, startRoot, finalRoot));
        result.set("dry", cpdComparison(startSha, finalSha, startRoot, finalRoot));
        result.set("buildWarnings", config.withBuild
                ? buildWarningComparison(startSha, finalSha, startRoot, finalRoot)
                : unavailableSection("Build analysis was not requested; pass --with-build."));
        if (config.controlStart != null) {
            result.set("startEquivalence", compareStarts(
                    config.controlStart, new RepoRevision(config.repo, startSha)));
        } else {
            result.set("startEquivalence", unavailableSection(
                    "Pass --control-start <repo-path>@<sha> for cross-arm equivalence."));
        }
        return result;
    }

    private GitTask resolveGitTask(int issue, int prNumber) {
        CommandResult log = command(List.of(
                "git", "-C", config.repo.toString(), "log", "--all", "--merges",
                "--format=%H|%P|%cI|%s",
                "--grep=Merge pull request #" + prNumber + " "), null, Map.of());
        if (log.exitCode != 0 || log.output.isBlank()) {
            log = command(List.of(
                    "git", "-C", config.repo.toString(), "log", "--all", "--merges",
                    "--format=%H|%P|%cI|%s",
                    "--grep=Merge pull request #" + prNumber), null, Map.of());
        }
        for (String line : log.output.split("\\R")) {
            String[] parts = line.split("\\|", 4);
            if (parts.length < 4 || !parts[3].contains("#" + prNumber)) {
                continue;
            }
            String[] parents = parts[1].split(" ");
            if (parents.length < 2) {
                continue;
            }
            try {
                return new GitTask(issue, prNumber, parts[0], parents[0], parents[1],
                        Instant.parse(parts[2]));
            } catch (DateTimeParseException ignored) {
                return new GitTask(issue, prNumber, parts[0], parents[0], parents[1],
                        Instant.EPOCH);
            }
        }
        return null;
    }

    private DiffMetrics diffMetrics(String from, String to) {
        String projectRoot = inferMavenProjectRoot(config.repo, to);
        CommandResult numstat = command(List.of(
                "git", "-C", config.repo.toString(), "diff", "--numstat", from, to),
                null, Map.of());
        DiffMetrics result = new DiffMetrics();
        for (String line : numstat.output.split("\\R")) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split("\\t", 3);
            if (fields.length < 3) {
                continue;
            }
            long additions = parseNumstat(fields[0]);
            long deletions = parseNumstat(fields[1]);
            String path = fields[2];
            result.files++;
            result.additions += additions;
            result.deletions += deletions;
            FileKind kind = fileKind(path, projectRoot);
            result.byKind.get(kind).files++;
            result.byKind.get(kind).additions += additions;
            result.byKind.get(kind).deletions += deletions;
            if (isProjectBuildFile(path, projectRoot)) {
                result.buildFilesChanged++;
            }
        }
        CommandResult commits = command(List.of(
                "git", "-C", config.repo.toString(), "rev-list", "--count", from + ".." + to),
                null, Map.of());
        result.commits = parseLong(commits.output.trim());
        CommandResult patch = command(List.of(
                "git", "-C", config.repo.toString(), "diff", "-U0", from, to),
                null, Map.of());
        for (String line : patch.output.split("\\R")) {
            if ((line.startsWith("+") || line.startsWith("-"))
                    && !line.startsWith("+++") && !line.startsWith("---")
                    && line.matches(".*<(dependency|plugin|version)>?.*")) {
                result.dependencyChurnLines++;
            }
        }
        return result;
    }

    private static long parseNumstat(String value) {
        return "-".equals(value) ? 0 : parseLong(value);
    }

    private static FileKind fileKind(String path) {
        return fileKind(path, ".");
    }

    private static FileKind fileKind(String path, String projectRoot) {
        String relative = path;
        if (projectRoot != null && !projectRoot.isBlank() && !".".equals(projectRoot)
                && path.startsWith(projectRoot + "/")) {
            relative = path.substring(projectRoot.length() + 1);
        }
        String lower = relative.toLowerCase(Locale.ROOT);
        if (lower.contains("/test/") || lower.startsWith("src/test")) {
            return FileKind.TEST;
        }
        if (isBuildFile(path) || lower.startsWith(".github/")
                || lower.endsWith(".yml") || lower.endsWith(".yaml")
                || lower.endsWith(".xml") && !lower.startsWith("src/main")) {
            return FileKind.CONFIG;
        }
        return FileKind.PRODUCTION;
    }

    private static boolean isProjectBuildFile(String path, String projectRoot) {
        return path.equals(joinRoot(projectRoot, "pom.xml"))
                || path.equals(joinRoot(projectRoot, "build.gradle"))
                || path.equals(joinRoot(projectRoot, "build.gradle.kts"))
                || path.equals(joinRoot(projectRoot, "settings.gradle"))
                || path.equals(joinRoot(projectRoot, "settings.gradle.kts"))
                || path.equals(joinRoot(projectRoot, "mvnw"))
                || path.equals(joinRoot(projectRoot, "gradlew"));
    }

    private static boolean isBuildFile(String path) {
        String name = Paths.get(path).getFileName().toString();
        return name.equals("pom.xml") || name.equals("build.gradle")
                || name.equals("build.gradle.kts") || name.equals("settings.gradle")
                || name.equals("settings.gradle.kts") || name.equals("mvnw")
                || name.equals("gradlew");
    }

    private ObjectNode testIntegrity(
            String from, String to, String fromRoot, String toRoot) {
        ObjectNode result = JSON.createObjectNode();
        ObjectNode baseline = result.putObject("baseline");
        baseline.put("startSha", from);
        baseline.put("projectRoot", fromRoot);
        String pom = gitShow(config.repo, from, joinRoot(fromRoot, "pom.xml"));
        boolean skipTests = Pattern.compile(
                "<skipTests>\\s*true\\s*</skipTests>", Pattern.CASE_INSENSITIVE)
                .matcher(pom).find();
        baseline.put("testsRunByDefault", !skipTests);
        baseline.put("skipTests", skipTests);
        baseline.put("mavenCompilerSource", compilerSetting(pom, "source"));
        baseline.put("mavenCompilerTarget", compilerSetting(pom, "target"));
        String release = compilerSetting(pom, "release");
        if (release == null) baseline.putNull("mavenCompilerRelease");
        else baseline.put("mavenCompilerRelease", release);
        baseline.put("testExecutionMode",
                skipTests ? "compiled_but_not_executed" : "executed");
        baseline.put("containerTestTarget",
                pom.toLowerCase(Locale.ROOT).contains("payara")
                        ? "remote_payara" : "unknown");

        ObjectNode meaningfulness = result.putObject("meaningfulness");
        if ("control".equals(config.arm) && skipTests) {
            meaningfulness.put("status", "not_meaningful");
            meaningfulness.put("reason",
                    "The control arm did not run tests by default at its start SHA.");
        } else {
            meaningfulness.put("status", "meaningful");
            meaningfulness.put("reason",
                    "Tests run by default or test execution is a disclosed treatment characteristic.");
        }

        CommandResult names = command(List.of(
                "git", "-C", config.repo.toString(), "diff", "--name-status", from, to),
                null, Map.of());
        int testModified = 0;
        int testDeleted = 0;
        for (String line : names.output.split("\\R")) {
            String[] fields = line.split("\\t");
            if (fields.length < 2
                    || fileKind(fields[fields.length - 1], toRoot) != FileKind.TEST) {
                continue;
            }
            if (fields[0].startsWith("D")) {
                testDeleted++;
            } else {
                testModified++;
            }
        }

        CommandResult patch = command(List.of(
                "git", "-C", config.repo.toString(), "diff", "-U0", from, to),
                null, Map.of());
        int disabledAdded = 0;
        int skipAdded = 0;
        int skipRemoved = 0;
        int exclusionsAdded = 0;
        int assertionsAdded = 0;
        int assertionsRemoved = 0;
        int ciCommandsChanged = 0;
        for (String line : patch.output.split("\\R")) {
            if (line.startsWith("+++") || line.startsWith("---")) {
                continue;
            }
            boolean added = line.startsWith("+");
            boolean removed = line.startsWith("-");
            String body = line.length() > 1 ? line.substring(1) : "";
            if (added && body.matches(".*@(Disabled|Ignore)\\b.*")) {
                disabledAdded++;
            }
            if (body.matches("(?i).*(skipTests|maven\\.test\\.skip|skipITs|excludeTests).*")) {
                if (added) {
                    skipAdded++;
                }
                if (removed) {
                    skipRemoved++;
                }
            }
            if (added && body.matches("(?i).*(exclude|exclusion).*test.*")) {
                exclusionsAdded++;
            }
            Matcher assertions = ASSERTION.matcher(body);
            int count = 0;
            while (assertions.find()) {
                count++;
            }
            if (added) {
                assertionsAdded += count;
            } else if (removed) {
                assertionsRemoved += count;
            }
            if ((added || removed) && body.matches(
                    "(?i).*(mvn|maven|gradle|test|verify|surefire|failsafe).*")
                    && patch.output.contains(".github/workflows/")) {
                ciCommandsChanged++;
            }
        }
        ObjectNode changes = result.putObject("changes");
        changes.put("testFilesModified", testModified);
        changes.put("testFilesDeleted", testDeleted);
        changes.put("disabledAnnotationsAdded", disabledAdded);
        changes.put("skipFlagsAdded", skipAdded);
        changes.put("skipFlagsRemoved", skipRemoved);
        changes.put("testExclusionsAdded", exclusionsAdded);
        changes.put("assertionsAdded", assertionsAdded);
        changes.put("assertionsRemoved", assertionsRemoved);
        changes.put("ciTestCommandsChanged", ciCommandsChanged);
        ArrayNode signals = result.putArray("tamperingSignals");
        if (testDeleted > 0) {
            signals.add("test_files_deleted");
        }
        if (disabledAdded > 0) {
            signals.add("disabled_or_ignored_tests_added");
        }
        if (skipAdded > 0 || exclusionsAdded > 0) {
            signals.add("test_execution_reduced");
        }
        return result;
    }

    private static String pomProperty(String pom, String name) {
        Matcher matcher = Pattern.compile("<" + Pattern.quote(name)
                + ">\\s*([^<]+?)\\s*</" + Pattern.quote(name) + ">",
                Pattern.CASE_INSENSITIVE).matcher(pom);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private static String compilerSetting(String pom, String name) {
        String property = pomProperty(pom, "maven.compiler." + name);
        if (property != null) return property;
        Matcher plugin = Pattern.compile(
                "<plugin>.*?<artifactId>\\s*maven-compiler-plugin\\s*</artifactId>"
                        + "(.*?)</plugin>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
                .matcher(pom);
        if (!plugin.find()) return null;
        return xmlValue(plugin.group(1), name);
    }

    private static ObjectNode mavenBuildConfiguration(
            Path repo, String sha, String projectRoot) {
        String pom = gitShow(repo, sha, joinRoot(projectRoot, "pom.xml"));
        ObjectNode value = JSON.createObjectNode();
        value.put("projectRoot", projectRoot);
        putNullable(value, "compilerSource", compilerSetting(pom, "source"));
        putNullable(value, "compilerTarget", compilerSetting(pom, "target"));
        putNullable(value, "compilerRelease", compilerSetting(pom, "release"));
        boolean skipTests = Pattern.compile(
                "<(?:skipTests|maven\\.test\\.skip)>\\s*true\\s*</",
                Pattern.CASE_INSENSITIVE).matcher(pom).find();
        value.put("skipTests", skipTests);
        return value;
    }

    private static void putNullable(ObjectNode node, String name, String value) {
        if (value == null || value.isBlank()) node.putNull(name);
        else node.put(name, value);
    }

    private ObjectNode baselineVerification(String startSha) {
        ObjectNode result = JSON.createObjectNode();
        String enablement = "e7b651f";
        CommandResult ancestor = command(List.of(
                "git", "-C", config.repo.toString(), "merge-base", "--is-ancestor",
                enablement, startSha), null, Map.of());
        result.put("baselineEnablementCommit", enablement);
        result.put("isAncestorOfStartSha", ancestor.exitCode == 0);
        CommandResult stat = command(List.of(
                "git", "-C", config.repo.toString(), "diff", "--stat",
                enablement + ".." + startSha), null, Map.of());
        result.put("diffStat", stat.output.strip());
        if ("treatment".equals(config.arm)) {
            CommandResult parent = command(List.of(
                    "git", "-C", config.repo.toString(), "rev-parse", startSha + "^"),
                    null, Map.of());
            if (parent.exitCode == 0) {
                String controlStart = parent.output.trim();
                result.put("controlArmStartSha", controlStart);
                CommandResult treatment = command(List.of(
                        "git", "-C", config.repo.toString(), "diff", "--stat",
                        controlStart + ".." + startSha), null, Map.of());
                result.put("treatmentCommitDiffStat", treatment.output.strip());
            }
        }
        return result;
    }

    private ObjectNode analyzeStartCiAndBuild(
            Path repo, String sha, String projectRoot) {
        ObjectNode result = JSON.createObjectNode();
        result.put("sha", sha);
        result.put("projectRoot", projectRoot);
        ArrayNode workflows = result.putArray("workflows");
        Map<String, Integer> gateCounts = new LinkedHashMap<>();
        for (String gate : List.of("formatting", "build_contract", "security",
                "static_analysis", "compiler", "unit_tests",
                "container_tests", "ci_other")) {
            gateCounts.put(gate, 0);
        }
        Set<String> ciOtherNames = new TreeSet<>();
        for (String path : gitPaths(repo, sha,
                value -> value.startsWith(".github/workflows/")
                        && (value.endsWith(".yml") || value.endsWith(".yaml")))) {
            String text = gitShow(repo, sha, path);
            ObjectNode workflow = workflows.addObject();
            workflow.put("path", path);
            ArrayNode jobs = workflow.putArray("jobs");
            String job = null;
            int jobIndent = -1;
            boolean inJobs = false;
            ObjectNode currentStep = null;
            StringBuilder currentStepText = new StringBuilder();
            for (String line : text.split("\\R")) {
                int indent = line.length() - line.stripLeading().length();
                if ("jobs:".equals(line.strip())) {
                    inJobs = true;
                    continue;
                }
                Matcher jobLine = Pattern.compile("^\\s{2}([A-Za-z0-9_-]+):\\s*$").matcher(line);
                if (inJobs && jobLine.find()) {
                    recordWorkflowStep(currentStep, currentStepText, gateCounts, ciOtherNames);
                    currentStep = null;
                    currentStepText.setLength(0);
                    job = jobLine.group(1);
                    jobIndent = indent;
                    ObjectNode node = jobs.addObject();
                    node.put("id", job);
                    node.putArray("steps");
                    continue;
                }
                Matcher jobName = Pattern.compile("^\\s+name:\\s*['\"]?(.+?)['\"]?\\s*$")
                        .matcher(line);
                if (job != null && indent == jobIndent + 2 && jobName.find()) {
                    ((ObjectNode) jobs.get(jobs.size() - 1)).put(
                            "name", jobName.group(1).replaceAll("['\"]$", "").trim());
                    continue;
                }
                Matcher name = Pattern.compile(
                        "^\\s*-\\s*name:\\s*['\"]?(.+?)['\"]?\\s*$").matcher(line);
                if (job != null && name.find()) {
                    recordWorkflowStep(currentStep, currentStepText, gateCounts, ciOtherNames);
                    currentStepText.setLength(0);
                    String step = name.group(1).replaceAll("['\"]$", "").trim();
                    ObjectNode jobNode = (ObjectNode) jobs.get(jobs.size() - 1);
                    currentStep = ((ArrayNode) jobNode.path("steps")).addObject();
                    currentStep.put("name", step);
                    currentStep.put("named", true);
                    currentStepText.append(line).append('\n');
                } else if (job != null && indent > jobIndent
                        && (line.stripLeading().startsWith("- run:")
                        || line.stripLeading().startsWith("- uses:"))) {
                    recordWorkflowStep(currentStep, currentStepText, gateCounts, ciOtherNames);
                    currentStepText.setLength(0);
                    String stripped = line.stripLeading().replaceFirst("^-\\s*", "");
                    boolean uses = stripped.startsWith("uses:");
                    String command = stripped.substring(stripped.indexOf(':') + 1).trim();
                    ObjectNode jobNode = (ObjectNode) jobs.get(jobs.size() - 1);
                    currentStep = ((ArrayNode) jobNode.path("steps")).addObject();
                    currentStep.put("name", (uses ? "uses: " : "run: ") + command);
                    currentStep.put("named", false);
                    currentStepText.append(line).append('\n');
                } else if (currentStep != null && indent > jobIndent) {
                    currentStepText.append(line).append('\n');
                }
            }
            recordWorkflowStep(currentStep, currentStepText, gateCounts, ciOtherNames);
        }
        String pomPath = joinRoot(projectRoot, "pom.xml");
        String pom = gitShow(repo, sha, pomPath);
        result.put("buildFile", pomPath);
        ArrayNode bound = result.putArray("validateVerifyPlugins");
        Matcher plugin = Pattern.compile("<plugin>(.*?)</plugin>", Pattern.DOTALL).matcher(pom);
        while (plugin.find()) {
            String body = plugin.group(1);
            Matcher phase = Pattern.compile("<phase>\\s*(validate|verify)\\s*</phase>",
                    Pattern.CASE_INSENSITIVE).matcher(body);
            if (!phase.find()) continue;
            String artifact = xmlValue(body, "artifactId");
            ObjectNode node = bound.addObject();
            node.put("artifactId", artifact);
            node.put("phase", phase.group(1).toLowerCase(Locale.ROOT));
            String gate = ciGate(artifact + " " + body);
            node.put("gate", gate);
            gateCounts.merge(gate, 1, Integer::sum);
            if ("ci_other".equals(gate) && !artifact.isBlank()) {
                ciOtherNames.add("Maven plugin: " + artifact);
            }
        }
        ArrayNode ciOther = result.putArray("ciOtherSteps");
        ciOtherNames.forEach(ciOther::add);
        ObjectNode taxonomy = result.putObject("taxonomy");
        gateCounts.forEach((gate, count) -> {
            ObjectNode node = taxonomy.putObject(gate);
            node.put("status", count == 0 ? "not_present" : "present");
            node.put("entryCount", count);
        });
        return result;
    }

    private static void recordWorkflowStep(
            ObjectNode step, StringBuilder definition,
            Map<String, Integer> gateCounts, Set<String> ciOtherNames) {
        if (step == null) return;
        String text = definition.toString().strip();
        step.put("definition", text);
        String gate = ciGate(step.path("name").asText() + "\n" + text);
        step.put("gate", gate);
        gateCounts.merge(gate, 1, Integer::sum);
        if ("ci_other".equals(gate) && step.path("named").asBoolean()) {
            ciOtherNames.add(step.path("name").asText());
        }
    }

    private static String xmlValue(String xml, String name) {
        Matcher matcher = Pattern.compile("<" + name + ">\\s*([^<]+)\\s*</" + name + ">")
                .matcher(xml);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    static String ciGate(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.contains("write test inventory")
                || lower.contains("verify-build-contract.sh")
                || lower.contains("verify-compatibility-contract.sh")
                || lower.contains("verify-source-gates.sh")
                || lower.contains("test inventory")) {
            return "build_contract";
        }
        if (lower.contains("run dependency security and delta gate")
                || lower.contains("dependency security")) {
            return "security";
        }
        if (lower.contains("format") || lower.contains("spotless")) return "formatting";
        if (lower.contains("pmd") || lower.contains("checkstyle")
                || lower.contains("spotbugs") || lower.contains("enforcer")
                || lower.contains("forbidden-api") || lower.contains("archunit")
                || lower.contains("error prone")) {
            return "static_analysis";
        }
        if (lower.contains("compile") || lower.contains("compiler")
                || lower.contains("build canonical war")) return "compiler";
        if (lower.contains("failsafe") || lower.contains("arquillian")
                || lower.contains("container")
                || lower.contains("run open liberty integration tests")) {
            return "container_tests";
        }
        if (lower.contains("surefire") || lower.contains("unit test")
                || lower.matches(".*\\bmvn\\b.*\\btest\\b.*")) return "unit_tests";
        return "ci_other";
    }

    private ObjectNode compareStarts(RepoRevision control, RepoRevision treatment)
            throws IOException {
        ObjectNode result = JSON.createObjectNode();
        result.put("controlRepo", control.repo().toString());
        result.put("controlSha", control.sha());
        result.put("treatmentRepo", treatment.repo().toString());
        result.put("treatmentSha", treatment.sha());
        Path base = config.outputDir.resolve(".worktrees");
        Files.createDirectories(base);
        Path controlTree = addWorktree(control, base.resolve("control-start"));
        Path treatmentTree = addWorktree(treatment, base.resolve("treatment-start"));
        try {
            CommandResult names = command(List.of(
                    "git", "diff", "--no-index", "--find-renames=100%",
                    "--name-status", controlTree.toString(), treatmentTree.toString()),
                    null, Map.of());
            Map<String, List<String>> classified = new LinkedHashMap<>();
            for (String category : List.of("guardrail_infrastructure", "relocation_only",
                    "formatting_only", "substantive_production", "campaign_inputs", "other")) {
                classified.put(category, new ArrayList<>());
            }
            ArrayNode paths = result.putArray("paths");
            for (String line : names.output.split("\\R")) {
                if (line.isBlank()) continue;
                String[] fields = line.split("\\t");
                String status = fields[0];
                String oldPath;
                String newPath;
                if (status.startsWith("R") || status.startsWith("C")) {
                    oldPath = relativeDiffPath(fields[1], controlTree);
                    newPath = relativeDiffPath(fields[2], treatmentTree);
                } else if (status.startsWith("A")) {
                    oldPath = null;
                    newPath = relativeDiffPath(fields[1], treatmentTree);
                } else if (status.startsWith("D")) {
                    oldPath = relativeDiffPath(fields[1], controlTree);
                    newPath = null;
                } else {
                    oldPath = relativeDiffPath(fields[1], controlTree);
                    newPath = oldPath;
                }
                if (".git".equals(oldPath) || ".git".equals(newPath)) continue;
                String display = oldPath == null || Objects.equals(oldPath, newPath)
                        ? Objects.toString(newPath, oldPath)
                        : oldPath + " -> " + newPath;
                String category;
                if (status.startsWith("R100")) {
                    category = "relocation_only";
                } else if (isCampaignInput(newPath) || isCampaignInput(oldPath)) {
                    category = "campaign_inputs";
                } else if (isGuardrail(newPath) || isGuardrail(oldPath)) {
                    category = "guardrail_infrastructure";
                } else if (oldPath != null && newPath != null
                        && Files.isRegularFile(controlTree.resolve(oldPath))
                        && Files.isRegularFile(treatmentTree.resolve(newPath))
                        && command(List.of("git", "diff", "--no-index", "-w", "--exit-code",
                                controlTree.resolve(oldPath).toString(),
                                treatmentTree.resolve(newPath).toString()),
                                null, Map.of()).exitCode == 0) {
                    category = "formatting_only";
                } else if ((newPath != null && (newPath.startsWith("src/main/")
                        || newPath.contains("/src/main/")))
                        || (oldPath != null && (oldPath.startsWith("src/main/")
                        || oldPath.contains("/src/main/")))) {
                    category = "substantive_production";
                } else {
                    category = "other";
                }
                classified.get(category).add(display);
                ObjectNode node = paths.addObject();
                node.put("status", status);
                if (oldPath == null) node.putNull("oldPath"); else node.put("oldPath", oldPath);
                if (newPath == null) node.putNull("newPath"); else node.put("newPath", newPath);
                node.put("classification", category);
            }
            ObjectNode summary = result.putObject("classificationSummary");
            classified.forEach((category, values) -> {
                ObjectNode node = summary.putObject(category);
                node.put("count", values.size());
                ArrayNode list = node.putArray("paths");
                values.forEach(list::add);
            });
            ArrayNode inputs = result.putArray("campaignInputDiffs");
            for (JsonNode path : paths) {
                if (!"campaign_inputs".equals(path.path("classification").asText())) continue;
                String oldPath = path.path("oldPath").asText(null);
                String newPath = path.path("newPath").asText(null);
                ObjectNode input = inputs.addObject();
                input.put("path", oldPath == null || Objects.equals(oldPath, newPath)
                        ? Objects.toString(newPath, oldPath)
                        : newPath == null ? oldPath : oldPath + " -> " + newPath);
                Path oldFile = oldPath == null ? null : controlTree.resolve(oldPath);
                Path newFile = newPath == null ? null : treatmentTree.resolve(newPath);
                input.put("controlLines", lineCount(oldFile));
                input.put("treatmentLines", lineCount(newFile));
                CommandResult patch = oldFile == null
                        ? new CommandResult(1, "Added only: " + newPath)
                        : newFile == null
                        ? new CommandResult(1, "Removed only: " + oldPath)
                        : command(List.of("git", "diff", "--no-index", "--",
                                oldFile.toString(), newFile.toString()), null, Map.of());
                input.put("lineDiff", excerpt(patch.output, 20000));
            }
            result.put("availability", "measured");
            return result;
        } finally {
            removeWorktree(control.repo(), controlTree);
            removeWorktree(treatment.repo(), treatmentTree);
        }
    }

    private static Path addWorktree(RepoRevision revision, Path directory)
            throws IOException {
        CommandResult result = command(List.of(
                "git", "-C", revision.repo().toString(), "worktree", "add",
                "--detach", directory.toString(), revision.sha()), null, Map.of());
        if (result.exitCode != 0) {
            throw new IOException("Could not create comparison worktree: " + result.output);
        }
        return directory;
    }

    private static void removeWorktree(Path repo, Path worktree) {
        if (worktree != null) {
            command(List.of("git", "-C", repo.toString(), "worktree", "remove",
                    "--force", worktree.toString()), null, Map.of());
        }
    }

    private static String relativeDiffPath(String value, Path root) {
        String normalized = value.replace('\\', '/');
        String prefix = root.toString().replace('\\', '/') + "/";
        int index = normalized.indexOf(prefix);
        if (index >= 0) {
            return normalized.substring(index + prefix.length());
        }
        String marker = root.getFileName().toString() + "/";
        index = normalized.indexOf(marker);
        if (index >= 0) {
            return normalized.substring(index + marker.length());
        }
        return normalized.replaceFirst("^a/", "").replaceFirst("^b/", "");
    }

    private static boolean isCampaignInput(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.contains("plan") || lower.contains("prompt")
                || lower.contains("campaign") && (lower.endsWith(".md")
                || lower.endsWith(".json"));
    }

    private static boolean isGuardrail(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.startsWith(".github/") || lower.contains("shepherd-task")
                || lower.contains("guardrail") || lower.contains("acceptance");
    }

    private static long lineCount(Path path) {
        if (path == null || !Files.isRegularFile(path)) return 0;
        try (var lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines.count();
        } catch (IOException ignored) {
            return 0;
        }
    }

    private String gitShow(String sha, String path) {
        return gitShow(config.repo, sha, path);
    }

    private static String gitShow(Path repo, String sha, String path) {
        CommandResult result = command(List.of(
                "git", "-C", repo.toString(), "show", sha + ":" + path),
                null, Map.of());
        return result.exitCode == 0 ? result.output : "";
    }

    private static String joinRoot(String root, String path) {
        return root == null || root.isBlank() || ".".equals(root)
                ? path : root + "/" + path;
    }

    private String inferMavenProjectRoot(Path repo, String sha) {
        Set<String> pomPaths = gitPaths(repo, sha, path -> path.endsWith("pom.xml"));
        if (pomPaths.isEmpty()) return ".";
        Set<String> workflowPaths = gitPaths(repo, sha,
                path -> path.startsWith(".github/workflows/")
                        && (path.endsWith(".yml") || path.endsWith(".yaml")));
        Map<String, Integer> scores = new HashMap<>();
        for (String workflowPath : workflowPaths) {
            String workflow = gitShow(repo, sha, workflowPath);
            Matcher working = Pattern.compile(
                    "(?m)^\\s*working-directory:\\s*['\"]?([^'\"#\\s]+)")
                    .matcher(workflow);
            while (working.find()) {
                String candidate = working.group(1).replaceFirst("^\\./", "");
                if (pomPaths.contains(joinRoot(candidate, "pom.xml"))) {
                    scores.merge(candidate, 4, Integer::sum);
                }
            }
            Matcher file = Pattern.compile(
                    "(?:mvn|\\./mvnw)\\s+(?:[^\\r\\n]*?\\s)?(?:-f|--file)\\s+([^\\s'\"\\\\]+)")
                    .matcher(workflow);
            while (file.find()) {
                String pom = file.group(1).replaceFirst("^\\./", "");
                if (pomPaths.contains(pom)) {
                    String parent = Paths.get(pom).getParent() == null
                            ? "." : Paths.get(pom).getParent().toString();
                    scores.merge(parent, 5, Integer::sum);
                }
            }
        }
        if (!scores.isEmpty()) {
            return scores.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                            .thenComparingInt(entry -> pathDepth(entry.getKey())))
                    .map(Map.Entry::getKey).findFirst().orElse(".");
        }
        return pomPaths.stream()
                .filter(path -> gitShow(repo, sha, path).contains("<build"))
                .min(Comparator.comparingInt(CampaignEvaluator::pathDepth))
                .map(path -> {
                    Path parent = Paths.get(path).getParent();
                    return parent == null ? "." : parent.toString();
                })
                .orElseGet(() -> pomPaths.stream()
                        .min(Comparator.comparingInt(CampaignEvaluator::pathDepth))
                        .map(path -> {
                            Path parent = Paths.get(path).getParent();
                            return parent == null ? "." : parent.toString();
                        }).orElse("."));
    }

    private static int pathDepth(String path) {
        return ".".equals(path) || path.isBlank() ? 0 : Paths.get(path).getNameCount();
    }

    private static Set<String> gitPaths(
            Path repo, String sha, Predicate<String> predicate) {
        CommandResult result = command(List.of(
                "git", "-C", repo.toString(), "ls-tree", "-r", "--name-only", sha),
                null, Map.of());
        return Arrays.stream(result.output.split("\\R"))
                .filter(predicate)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private ObjectNode cpdComparison(
            String startSha, String finalSha, String startRoot, String finalRoot) {
        ObjectNode result = JSON.createObjectNode();
        result.put("minimumTokens", CPD_MINIMUM_TOKENS);
        CpdResult baseline = runCpd(startSha, startRoot);
        CpdResult current = runCpd(finalSha, finalRoot);
        result.set("baseline", baseline.toJson());
        result.set("final", current.toJson());
        if (baseline.available && current.available) {
            ObjectNode delta = result.putObject("newDuplication");
            delta.put("productionBlocks", Math.max(0,
                    current.productionBlocks - baseline.productionBlocks));
            delta.put("testBlocks", Math.max(0, current.testBlocks - baseline.testBlocks));
            delta.put("duplicatedLines", Math.max(0,
                    current.duplicatedLines - baseline.duplicatedLines));
        } else {
            result.set("newDuplication", unavailableMetric(
                    "PMD CPD did not complete for both revisions."));
        }
        return result;
    }

    private CpdResult runCpd(String sha, String projectRoot) {
        Path worktree = null;
        try {
            worktree = createWorktree("cpd", sha);
            Map<String, String> environment = java17Environment();
            Path projectDirectory = projectDirectory(worktree, projectRoot);
            CommandResult run = mavenCommand(List.of(
                    mavenExecutable(projectDirectory), "-q", "-DskipTests",
                    "-Dpmd.minimumTokens=" + CPD_MINIMUM_TOKENS,
                    "-Dpmd.includeTests=true",
                    "org.apache.maven.plugins:maven-pmd-plugin:3.25.0:cpd"),
                    projectDirectory, environment);
            Path report = projectDirectory.resolve("target/cpd.xml");
            if (run.exitCode != 0 || !Files.isRegularFile(report)) {
                return CpdResult.unavailable(excerpt(run.output, 1200));
            }
            JsonNode ignored = null;
            String xml = Files.readString(report, StandardCharsets.UTF_8);
            Matcher duplication = Pattern.compile(
                    "<duplication\\s+lines=\"(\\d+)\"[^>]*>(.*?)</duplication>",
                    Pattern.DOTALL).matcher(xml);
            int production = 0;
            int tests = 0;
            int lines = 0;
            List<ObjectNode> locations = new ArrayList<>();
            while (duplication.find()) {
                int duplicateLines = Integer.parseInt(duplication.group(1));
                String body = duplication.group(2);
                lines += duplicateLines;
                if (body.contains("/src/test/")) {
                    tests++;
                } else {
                    production++;
                }
                Matcher file = Pattern.compile("<file\\s+([^>]+)/?>").matcher(body);
                while (file.find()) {
                    String attributes = file.group(1);
                    Matcher line = Pattern.compile("\\bline=\"(\\d+)\"").matcher(attributes);
                    Matcher endLine = Pattern.compile("\\bendline=\"(\\d+)\"").matcher(attributes);
                    Matcher path = Pattern.compile("\\bpath=\"([^\"]+)\"").matcher(attributes);
                    if (!line.find() || !endLine.find() || !path.find()) continue;
                    ObjectNode location = JSON.createObjectNode();
                    String filePath = path.group(1);
                    int source = filePath.indexOf("/src/");
                    location.put("path", source >= 0
                            ? filePath.substring(source + 1) : filePath);
                    location.put("startLine", Integer.parseInt(line.group(1)));
                    location.put("endLine", Integer.parseInt(endLine.group(1)));
                    location.put("duplicatedLines", duplicateLines);
                    locations.add(location);
                }
            }
            return CpdResult.available(production, tests, lines, locations);
        } catch (Exception error) {
            return CpdResult.unavailable(error.getMessage());
        } finally {
            removeWorktree(worktree);
        }
    }

    private ObjectNode buildWarningComparison(
            String startSha, String finalSha, String startRoot, String finalRoot) {
        BuildWarnings baseline = compileWarnings(startSha, startRoot);
        BuildWarnings current = compileWarnings(finalSha, finalRoot);
        ObjectNode result = JSON.createObjectNode();
        result.put("jdk", "17");
        result.put("compilerLint", "deprecation,removal");
        result.set("baseline", baseline.toJson());
        result.set("final", current.toJson());
        if (baseline.available && current.available) {
            Set<String> added = new TreeSet<>(current.warnings);
            added.removeAll(baseline.warnings);
            ArrayNode warnings = result.putArray("newWarnings");
            added.forEach(warnings::add);
            result.put("newWarningCount", added.size());
        } else {
            result.set("newWarnings", unavailableMetric(
                    "Both revisions must compile successfully for a warning delta."));
        }
        return result;
    }

    private BuildWarnings compileWarnings(String sha, String projectRoot) {
        Path worktree = null;
        try {
            if (!Files.isDirectory(Paths.get(
                    "/Library/Java/JavaVirtualMachines/microsoft-17.jdk/Contents/Home"))) {
                return new BuildWarnings(false, Set.of(), -1,
                        "Pinned JDK 17 is not installed at the required path.");
            }
            worktree = createWorktree("build", sha);
            Path projectDirectory = projectDirectory(worktree, projectRoot);
            CommandResult result = mavenCommand(List.of(
                    mavenExecutable(projectDirectory), "-DskipTests",
                    "-Dmaven.compiler.showWarnings=true",
                    "-Dmaven.compiler.compilerArgs=-Xlint:deprecation,removal",
                    "test-compile"), projectDirectory,
                    java17Environment());
            String worktreePath = worktree.toString();
            Set<String> warnings = Arrays.stream(result.output.split("\\R"))
                    .map(String::trim)
                    .filter(line -> line.toLowerCase(Locale.ROOT).contains("warning")
                            && (line.toLowerCase(Locale.ROOT).contains("deprecated")
                            || line.toLowerCase(Locale.ROOT).contains("removal")))
                    .map(line -> line.replaceAll(
                            Pattern.quote(worktreePath), "<worktree>"))
                    .collect(Collectors.toCollection(TreeSet::new));
            return new BuildWarnings(result.exitCode == 0, warnings,
                    result.exitCode, excerpt(result.output, 1600));
        } catch (Exception error) {
            return new BuildWarnings(false, Set.of(), -1, error.getMessage());
        } finally {
            removeWorktree(worktree);
        }
    }

    private static Path projectDirectory(Path worktree, String projectRoot) {
        return projectRoot == null || projectRoot.isBlank() || ".".equals(projectRoot)
                ? worktree : worktree.resolve(projectRoot);
    }

    private static String mavenExecutable(Path projectDirectory) {
        return Files.isRegularFile(projectDirectory.resolve("mvnw"))
                ? "./mvnw" : "mvn";
    }

    private Path createWorktree(String purpose, String sha) throws IOException {
        Path base = config.outputDir.resolve(".worktrees");
        Files.createDirectories(base);
        Path directory = base.resolve(purpose + "-" + sha.substring(0, Math.min(12, sha.length()))
                + "-" + System.nanoTime());
        CommandResult result = command(List.of(
                "git", "-C", config.repo.toString(), "worktree", "add",
                "--detach", directory.toString(), sha), null, Map.of());
        if (result.exitCode != 0) {
            throw new IOException("Could not create temporary worktree: " + result.output);
        }
        return directory;
    }

    private void removeWorktree(Path worktree) {
        if (worktree == null || config.repo == null) {
            return;
        }
        command(List.of("git", "-C", config.repo.toString(), "worktree", "remove",
                "--force", worktree.toString()), null, Map.of());
    }

    private static Map<String, String> java17Environment() {
        Map<String, String> environment = new HashMap<>();
        Path javaHome = Paths.get(
                "/Library/Java/JavaVirtualMachines/microsoft-17.jdk/Contents/Home");
        if (Files.isDirectory(javaHome)) {
            environment.put("JAVA_HOME", javaHome.toString());
            environment.put("PATH", javaHome.resolve("bin") + ":"
                    + System.getenv().getOrDefault("PATH", ""));
        }
        return environment;
    }

    private ObjectNode evaluatorMetadata() {
        ObjectNode value = JSON.createObjectNode();
        value.put("name", EVALUATOR_NAME);
        value.put("version", evaluatorVersion);
        value.put("gitCommit", evaluatorCommit);
        value.put("worktreeDirty", evaluatorDirty);
        value.put("sourceDirectory", evaluatorDir.toString());
        value.put("generatedAt", Instant.now().toString());
        value.put("platform", System.getProperty("os.name") + "-"
                + System.getProperty("os.arch"));
        ArrayNode invocation = value.putArray("invocation");
        config.invocation.forEach(invocation::add);
        value.put("arm", config.arm);
        return value;
    }

    private ObjectNode campaignMetadata(JsonNode manifest, List<SessionResult> sessions) {
        ObjectNode value = JSON.createObjectNode();
        value.put("arm", config.arm);
        copy(manifest, value, "campaignId");
        value.put("campaignDirectory", config.campaignDir.toString());
        ArrayNode directories = value.putArray("campaignDirectories");
        config.campaignDirs.forEach(path -> directories.add(path.toString()));
        copy(manifest, value, "repository");
        copy(manifest, value, "baseBranch");
        copy(manifest, value, "lessonPropagation");
        value.set("taskIssues", manifest.path("taskIssues").deepCopy());
        copy(manifest, value, "startedAt");
        copy(manifest, value, "completedAt");
        copy(manifest, value, "status");
        copy(manifest, value, "exitCode");
        long elapsedSpanSeconds = durationBetween(
                manifest.path("startedAt").asText(), manifest.path("completedAt").asText());
        long campaignSeconds = combinedActiveSeconds();
        long sessionSeconds = sessions.stream().mapToLong(session -> session.durationSeconds).sum();
        long jsonlSessionMs = sessions.stream().mapToLong(session -> session.jsonlDurationMs).sum();
        value.put("wallTimeSeconds", campaignSeconds);
        value.put("activeTimeSeconds", campaignSeconds);
        value.put("elapsedSpanSeconds", elapsedSpanSeconds);
        value.put("recordedSessionTimeSeconds", sessionSeconds);
        value.put("jsonlSessionTimeMs", jsonlSessionMs);
        value.put("durationSourceDifferenceMs", jsonlSessionMs - sessionSeconds * 1000);
        value.put("primarySessionDurationSource", "markdown_header_truncated_seconds");
        value.put("transcriptTimestampConversionMethod",
                "Local header timestamps are converted with the UTC offset that best aligns the header start with the first JSONL timestamp.");
        long gaps = combinedGapSeconds();
        value.put("interDirectoryGapSeconds", gaps);
        value.put("orchestrationOverheadSeconds",
                campaignSeconds - sessionSeconds);
        ArrayNode attempts = value.putArray("attemptOutcomes");
        for (Path directory : config.campaignDirs) {
            try {
                JsonNode attempt = JSON.readTree(directory
                        .resolve("shepherd-task-25-given-list-run.json").toFile());
                ObjectNode row = attempts.addObject();
                row.put("campaignDirectory", directory.toString());
                copy(attempt, row, "startedAt");
                copy(attempt, row, "completedAt");
                copy(attempt, row, "status");
                copy(attempt, row, "exitCode");
                row.put("activeTimeSeconds", durationBetween(
                        attempt.path("startedAt").asText(),
                        attempt.path("completedAt").asText()));
            } catch (IOException ignored) {
                // Manifest readability was validated before analysis.
            }
        }
        value.put("evaluatorVersion", evaluatorVersion);
        value.put("evaluatorGitCommit", evaluatorCommit);
        return value;
    }

    private long combinedActiveSeconds() {
        long total = 0;
        for (Path directory : config.campaignDirs) {
            try {
                JsonNode manifest = JSON.readTree(directory
                        .resolve("shepherd-task-25-given-list-run.json").toFile());
                total += durationBetween(manifest.path("startedAt").asText(),
                        manifest.path("completedAt").asText());
            } catch (IOException ignored) {
                // Manifest readability was validated before analysis.
            }
        }
        return total;
    }

    private long combinedGapSeconds() {
        long gaps = 0;
        Instant priorEnd = null;
        for (Path directory : config.campaignDirs) {
            try {
                JsonNode manifest = JSON.readTree(directory
                        .resolve("shepherd-task-25-given-list-run.json").toFile());
                Instant start = parseInstant(manifest.path("startedAt").asText());
                Instant end = parseInstant(manifest.path("completedAt").asText());
                if (priorEnd != null && start != null) {
                    gaps += Duration.between(priorEnd, start).getSeconds();
                }
                if (end != null) priorEnd = end;
            } catch (IOException ignored) {
                // Manifest readability was validated before analysis.
            }
        }
        return gaps;
    }

    private static long durationBetween(String start, String end) {
        try {
            return Duration.between(Instant.parse(start), Instant.parse(end)).toSeconds();
        } catch (DateTimeParseException error) {
            return 0;
        }
    }

    private ObjectNode evidenceQuality(List<SessionResult> sessions) {
        ObjectNode value = JSON.createObjectNode();
        value.put("phaseSessionCount", sessions.size());
        ArrayNode missing = value.putArray("missingExpectedArtifacts");
        sessions.stream().flatMap(session -> session.missingArtifacts.stream())
                .distinct().sorted().forEach(missing::add);
        ArrayNode external = value.putArray("externalizedOutputMarkers");
        for (SessionResult session : sessions) {
            if (session.externalOutputReferences > 0) {
                ObjectNode item = external.addObject();
                item.put("taskIssue", session.issue);
                item.put("shepherdStage", session.shepherdStage);
                item.put("count", session.externalOutputReferences);
                item.put("confirmedEvidenceGaps", session.confirmedEvidenceGaps);
                item.put("interpretation", session.confirmedEvidenceGaps == 0
                        ? "Output was preserved by Markdown or JSONL partial-output evidence."
                        : "No equivalent Markdown or partial-output evidence was found.");
            }
        }
        value.put("confirmedEvidenceGapCount", sessions.stream()
                .mapToInt(session -> session.confirmedEvidenceGaps).sum());
        ObjectNode crossCheck = value.putObject("jsonlPartialOutputCrossCheck");
        crossCheck.put("eventCount", sessions.stream()
                .mapToInt(session -> session.partialOutputEvents).sum());
        crossCheck.put("unicodeCodePointCount", sessions.stream()
                .mapToLong(session -> session.partialOutputUnicodeCodePoints).sum());
        crossCheck.put("utf16CodeUnitCount", sessions.stream()
                .mapToLong(session -> session.partialOutputUtf16CodeUnits).sum());
        crossCheck.put("primaryCharacterUnit", "Unicode code points");
        crossCheck.put("failureEventsCorroborated", events.stream()
                .filter(event -> event.path("evidence")
                        .path("partialOutputCorroborated").asBoolean()).count());
        ObjectNode reasoning = value.putObject("reasoningSummaryPresence");
        reasoning.put("eventCount", sessions.stream()
                .mapToInt(session -> session.reasoningDeltaEvents).sum());
        reasoning.put("characterCount", sessions.stream()
                .mapToLong(session -> session.reasoningDeltaCharacters).sum());
        reasoning.put("usedForClassification", false);
        ArrayNode limitations = value.putArray("limitations");
        ObjectNode remote = limitations.addObject();
        remote.put("code", "REMOTE_AGENT_TELEMETRY_UNAVAILABLE");
        remote.put("description",
                "CCA and CCRA internal token, tool, and model telemetry is not present.");
        ObjectNode payload = limitations.addObject();
        payload.put("code", "JSONL_COMPLETE_RESULTS_REDACTED_PARTIALS_AVAILABLE");
        payload.put("description",
                "Completed tool results are redacted, but partial-result events provide a secondary raw-output source.");
        return value;
    }

    private ArrayNode sessionsJson(List<SessionResult> sessions) {
        ArrayNode array = JSON.createArrayNode();
        sessions.forEach(session -> array.add(session.toJson()));
        return array;
    }

    private ArrayNode tasksJson(Map<Integer, TaskResult> tasks, List<SessionResult> sessions) {
        ArrayNode array = JSON.createArrayNode();
        for (TaskResult task : tasks.values()) {
            ObjectNode node = JSON.createObjectNode();
            node.put("taskIssue", task.issue);
            if (task.prNumber == null) {
                node.putNull("prNumber");
            } else {
                node.put("prNumber", task.prNumber);
            }
            List<SessionResult> taskSessions = sessions.stream()
                    .filter(session -> session.issue == task.issue).toList();
            node.put("sessionCount", taskSessions.size());
            node.put("attempts", taskSessions.stream()
                    .map(session -> session.attempt).distinct().count());
            ArrayNode attemptOutcomes = node.putArray("attemptOutcomes");
            taskSessions.stream().map(session -> session.attempt).distinct().sorted()
                    .forEach(attempt -> {
                        List<SessionResult> selected = taskSessions.stream()
                                .filter(session -> session.attempt == attempt).toList();
                        ObjectNode outcome = attemptOutcomes.addObject();
                        outcome.put("attempt", attempt);
                        boolean convergenceFailure = selected.stream()
                                .anyMatch(session -> session.convergenceFailure);
                        outcome.put("outcome", convergenceFailure
                                ? "convergence_failure" : "completed");
                        outcome.put("recordedSessionTimeSeconds", selected.stream()
                                .mapToLong(session -> session.durationSeconds).sum());
                        if (convergenceFailure) {
                            outcome.put("reason", selected.stream()
                                    .filter(session -> session.convergenceFailure)
                                    .map(session -> session.convergenceFailureReason)
                                    .filter(Objects::nonNull)
                                    .findFirst().orElse("No remediation push."));
                        }
                    });
            node.put("recordedSessionTimeSeconds",
                    taskSessions.stream().mapToLong(value -> value.durationSeconds).sum());
            node.put("jsonlSessionTimeMs",
                    taskSessions.stream().mapToLong(value -> value.jsonlDurationMs).sum());
            node.put("ccaWaitPollCount",
                    taskSessions.stream().mapToInt(value -> value.ccaWaitPollCount).sum());
            node.put("ccaWaitElapsedSeconds",
                    taskSessions.stream().mapToLong(value -> value.ccaWaitElapsedSeconds).sum());
            node.put("ccaWaitConfiguredCeilingSeconds",
                    taskSessions.stream().mapToLong(
                            value -> value.ccaWaitConfiguredCeilingSeconds).sum());
            node.put("nonzeroToolExits",
                    taskSessions.stream().mapToInt(value -> value.nonzeroToolExits).sum());
            node.put("reviewRounds",
                    taskSessions.stream().mapToInt(value -> value.reviewIds.size()).sum());
            node.put("ccraActionableComments",
                    taskSessions.stream().mapToInt(value -> value.reviewCommentIds.size()).sum());
            node.put("stage30ChangeRequests", (int) events.stream()
                    .filter(event -> event.path("taskIssue").asInt() == task.issue)
                    .filter(event -> "stage_30_change_request".equals(
                            event.path("eventKind").asText())).count());
            node.put("flakyTestFailures",
                    taskSessions.stream().mapToInt(value -> value.flakyTestFailures).sum());
            node.put("flakyUnitTestFailures",
                    taskSessions.stream().mapToInt(value -> value.flakyUnitTestFailures).sum());
            node.put("flakyArquillianContainerTestFailures",
                    taskSessions.stream().mapToInt(value -> value.flakyContainerTestFailures).sum());
            node.put("leftoverStateFailures",
                    taskSessions.stream().mapToInt(value -> value.leftoverStateFailures).sum());
            array.add(node);
        }
        return array;
    }

    private ArrayNode aggregateTransitions(List<SessionResult> sessions) {
        List<ObjectNode> all = sessions.stream()
                .flatMap(session -> session.transitions.stream())
                .collect(Collectors.toCollection(ArrayList::new));
        Map<String, String> previous = new HashMap<>();
        for (ObjectNode transition : all) {
            String key = transition.path("check").asText();
            String current = transition.path("result").asText();
            String prior = previous.put(key, current);
            transition.put("greenToRed", "success".equals(prior)
                    && Set.of("failure", "error", "cancelled", "timed_out").contains(current));
        }
        return array(all);
    }

    private ObjectNode aggregateHallucinations() {
        ObjectNode result = JSON.createObjectNode();
        List<ObjectNode> tooling = events.stream()
                .filter(event -> "malformed_invocation".equals(
                        event.path("classification").path("subtype").asText())
                        || "nonexistent_plugin".equals(
                        event.path("classification").path("subtype").asText()))
                .filter(event -> "nonzero_tool_exit".equals(event.path("eventKind").asText()))
                .toList();
        ObjectNode product = result.putObject("productCode");
        product.put("eventCount", 0);
        product.put("distinctSignatureCount", 0);
        product.putArray("evidenceIds");
        ObjectNode tools = result.putObject("tooling");
        tools.put("eventCount", tooling.size());
        tools.put("distinctSignatureCount", tooling.stream()
                .map(event -> event.path("recurrenceSignature").asText())
                .distinct().count());
        ArrayNode evidence = tools.putArray("evidenceIds");
        tooling.forEach(event -> evidence.add(event.path("id").asText()));
        return result;
    }

    private ArrayNode aggregateUnderlyingProblems() {
        Map<String, ObjectNode> problems = new LinkedHashMap<>();
        for (ObjectNode event : events) {
            String id = event.path("underlyingProblemId").asText();
            ObjectNode problem = problems.computeIfAbsent(id, ignored -> {
                ObjectNode value = JSON.createObjectNode();
                value.put("id", id);
                value.put("recurrenceSignature", event.path("recurrenceSignature").asText());
                value.putArray("eventIds");
                return value;
            });
            ((ArrayNode) problem.path("eventIds")).add(event.path("id").asText());
        }
        return array(new ArrayList<>(problems.values()));
    }

    private ArrayNode aggregateRecurrences() {
        Map<String, ObjectNode> signatures = new TreeMap<>();
        for (ObjectNode event : events) {
            String signature = event.path("recurrenceSignature").asText();
            ObjectNode value = signatures.computeIfAbsent(signature, ignored -> {
                ObjectNode node = JSON.createObjectNode();
                node.put("signature", signature);
                node.put("occurrenceCount", 0);
                node.putArray("eventIds");
                node.putArray("sessions");
                return node;
            });
            value.put("occurrenceCount", value.path("occurrenceCount").asInt() + 1);
            ((ArrayNode) value.path("eventIds")).add(event.path("id").asText());
            ArrayNode sessionIds = (ArrayNode) value.path("sessions");
            String session = event.path("sessionId").asText();
            boolean present = false;
            for (JsonNode existing : sessionIds) {
                present |= session.equals(existing.asText());
            }
            if (!present) sessionIds.add(session);
        }
        return array(new ArrayList<>(signatures.values()));
    }

    private ObjectNode aggregateConvergence(List<SessionResult> sessions) {
        ObjectNode result = JSON.createObjectNode();
        result.put("reviewRounds", sessions.stream()
                .mapToInt(value -> value.reviewIds.size()).sum());
        result.put("ccraActionableComments", sessions.stream()
                .mapToInt(value -> value.reviewCommentIds.size()).sum());
        result.put("fixCommits", sessions.stream()
                .mapToInt(value -> value.fixCommits).sum());
        result.put("stage30Remediations", events.stream()
                .filter(event -> "stage_30_change_request".equals(
                        event.path("eventKind").asText())).count());
        List<ObjectNode> ci = sessions.stream()
                .flatMap(value -> value.transitions.stream())
                .filter(value -> "Shepherd task Cargo Tracker".equals(
                        value.path("check").asText())).toList();
        long distinctHeads = ci.stream().map(value -> value.path("headSha").asText())
                .filter(Predicate.not(String::isBlank)).distinct().count();
        result.put("ciRuns", distinctHeads);
        result.put("ciRerunsSameHead", 0);
        result.put("ciObservationCount", ci.size());
        result.put("ciRunCountingMethod",
                "Distinct substantive check head SHAs; repeated transcript observations are not reruns.");
        result.put("reviewCapEvents", sessions.stream()
                .mapToInt(value -> value.reviewCapEvents).sum());
        result.put("timeoutEvents", sessions.stream()
                .mapToInt(value -> value.timeoutEvents).sum());
        result.put("idleKillEvents", sessions.stream()
                .mapToInt(value -> value.idleKillEvents).sum());
        ObjectNode ccaWait = result.putObject("ccaWait");
        long waitSeconds = sessions.stream()
                .mapToLong(value -> value.ccaWaitElapsedSeconds).sum();
        long sessionSeconds = sessions.stream().mapToLong(value -> value.durationSeconds).sum();
        ccaWait.put("pollCount", sessions.stream()
                .mapToInt(value -> value.ccaWaitPollCount).sum());
        ccaWait.put("elapsedSeconds", waitSeconds);
        ccaWait.put("configuredPollCeilingSeconds", sessions.stream()
                .mapToLong(value -> value.ccaWaitConfiguredCeilingSeconds).sum());
        ccaWait.put("elapsedMethod",
                "Transcript offset from remote background-shell start to its completed read_bash block.");
        ccaWait.put("shareOfRecordedSessionTime",
                sessionSeconds == 0 ? 0.0 : waitSeconds / (double) sessionSeconds);
        ccaWait.put("interpretation",
                "Elapsed local waiting proxy; not CCA execution cost or remote-agent telemetry.");
        ObjectNode flakiness = result.putObject("flakiness");
        flakiness.put("flakyTestFailures", sessions.stream()
                .mapToInt(value -> value.flakyTestFailures).sum());
        flakiness.put("flakyUnitTestFailures", sessions.stream()
                .mapToInt(value -> value.flakyUnitTestFailures).sum());
        flakiness.put("flakyArquillianContainerTestFailures", sessions.stream()
                .mapToInt(value -> value.flakyContainerTestFailures).sum());
        flakiness.put("leftoverStateFailures", Math.max(
                sessions.stream().mapToInt(value -> value.leftoverStateFailures).sum(),
                (int) events.stream().filter(event -> "leftover_state".equals(
                        event.path("classification").path("subtype").asText())).count()));
        flakiness.put("otherKnownCauseFailures", sessions.stream()
                .mapToInt(value -> value.otherKnownCauseFlakyFailures).sum());
        flakiness.put("unknownCauseFailures", sessions.stream()
                .mapToInt(value -> value.unknownCauseFlakyFailures).sum());
        return result;
    }

    private ObjectNode aggregateEnvironment(List<SessionResult> sessions) {
        ObjectNode result = JSON.createObjectNode();
        ArrayNode sessionValues = result.putArray("sessions");
        for (SessionResult session : sessions) {
            ObjectNode value = sessionValues.addObject();
            value.put("taskIssue", session.issue);
            value.put("shepherdStage", session.shepherdStage);
            ArrayNode homes = value.putArray("javaHomesUsed");
            session.javaHomes.forEach(homes::add);
            ArrayNode levels = value.putArray("javacSourceTargetReleaseLevels");
            session.javacLevels.forEach(levels::add);
            ArrayNode models = value.putArray("modelIds");
            session.models.forEach(models::add);
            ArrayNode reasoning = value.putArray("reasoningLevels");
            session.reasoningLevels.forEach(reasoning::add);
            ArrayNode cliVersions = value.putArray("copilotCliVersions");
            session.copilotCliVersions.forEach(cliVersions::add);
            value.set("javaInvocations", array(session.javaInvocations));
        }
        return result;
    }

    private ObjectNode aggregateRunInvariants(JsonNode manifest, List<SessionResult> sessions) {
        ObjectNode result = JSON.createObjectNode();
        result.put("shepherdTaskVersion",
                manifest.path("shepherdTaskVersion").asText("unavailable"));
        result.put("campaignCreatedWithVersion",
                manifest.path("campaignCreatedWithVersion").asText("unavailable"));
        ArrayNode models = result.putArray("models");
        sessions.stream().flatMap(session -> session.models.stream())
                .distinct().sorted().forEach(models::add);
        ArrayNode reasoning = result.putArray("reasoningLevels");
        sessions.stream().flatMap(session -> session.reasoningLevels.stream())
                .distinct().sorted().forEach(reasoning::add);
        ArrayNode cli = result.putArray("copilotCliVersions");
        sessions.stream().flatMap(session -> session.copilotCliVersions.stream())
                .distinct().sorted().forEach(cli::add);
        ObjectNode hashes = result.putObject("skillNameHashes");
        Map<String, Set<String>> bySkill = new TreeMap<>();
        for (SessionResult session : sessions) {
            session.skillNameHashes.forEach((name, hash) ->
                    bySkill.computeIfAbsent(name, ignored -> new TreeSet<>()).add(hash));
        }
        bySkill.forEach((name, values) -> {
            ArrayNode list = hashes.putArray(name);
            values.forEach(list::add);
        });
        ObjectNode lengths = result.putObject("skillContentLengths");
        Map<String, Set<Long>> lengthsBySkill = new TreeMap<>();
        for (SessionResult session : sessions) {
            session.skillContentLengths.forEach((name, length) ->
                    lengthsBySkill.computeIfAbsent(
                            name, ignored -> new TreeSet<>()).add(length));
        }
        lengthsBySkill.forEach((name, values) -> {
            ArrayNode list = lengths.putArray(name);
            values.forEach(list::add);
        });
        result.set("skillContentVerification", readSkillContentVerification());
        ArrayNode skills = result.putArray("loadedSkills");
        sessions.stream().flatMap(session -> session.loadedSkills.stream())
                .distinct().sorted().forEach(skills::add);
        result.put("loadedSkillCount", skills.size());
        result.put("allSessionsSameModel",
                sessions.stream().map(session -> session.models).distinct().count() == 1);
        result.put("allSessionsSameReasoningLevel",
                sessions.stream().map(session -> session.reasoningLevels).distinct().count() == 1);
        ArrayNode drift = result.putArray("drift");
        addInvariantDrift(drift, "copilotCliVersions", sessions.stream()
                .collect(Collectors.groupingBy(session -> session.attempt,
                        TreeMap::new,
                        Collectors.flatMapping(session -> session.copilotCliVersions.stream(),
                                Collectors.toCollection(TreeSet::new)))));
        Map<Integer, Set<String>> stage30Lengths = new TreeMap<>();
        for (SessionResult session : sessions) {
            Long length = session.skillContentLengths.get(
                    "shepherd-task-30-from-assignment-to-ready");
            if (length != null) {
                stage30Lengths.computeIfAbsent(session.attempt,
                        ignored -> new TreeSet<>()).add(Long.toString(length));
            }
        }
        addInvariantDrift(drift, "stage30SkillContentLength", stage30Lengths);
        result.put("driftDetected", drift.size() > 0);
        result.put("referenceInvariantObservation",
                "Control CLI 1.0.89 / stage-30 length 25804; treatment CLI 1.0.91 / stage-30 length 27427.");
        return result;
    }

    private static void addInvariantDrift(
            ArrayNode rows, String invariant, Map<Integer, Set<String>> byAttempt) {
        Set<Set<String>> distinct = new HashSet<>(byAttempt.values());
        if (distinct.size() <= 1) return;
        ObjectNode row = rows.addObject();
        row.put("invariant", invariant);
        row.put("warning", true);
        ObjectNode values = row.putObject("valuesByAttempt");
        byAttempt.forEach((attempt, observed) -> {
            ArrayNode list = values.putArray(Integer.toString(attempt));
            observed.forEach(list::add);
        });
    }

    private ObjectNode aggregateCiAnalysis(
            ObjectNode repository, List<SessionResult> sessions) {
        ObjectNode result = JSON.createObjectNode();
        Set<String> observed = sessions.stream().flatMap(session -> session.transitions.stream())
                .map(node -> node.path("check").asText())
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toCollection(TreeSet::new));
        ArrayNode allObserved = result.putArray("observedCheckNames");
        observed.forEach(allObserved::add);
        ArrayNode substantive = result.putArray("substantiveCheckNames");
        observed.forEach(substantive::add);
        ArrayNode classifiedChecks = result.putArray("classifiedChecks");
        observed.forEach(name -> {
            ObjectNode check = classifiedChecks.addObject();
            check.put("name", name);
            check.put("gate", ciGate(name));
            check.put("substantive", true);
        });
        ArrayNode excluded = result.putArray("excludedOrchestrationChecks");
        excluded.add("Running Copilot cloud agent");
        excluded.add("Addressing comment on PR #N");
        excluded.add("copilot");
        result.put("remoteAgentCheckObservations", sessions.stream()
                .mapToInt(session -> session.remoteAgentCheckObservations).sum());
        result.put("testsSkippedMessages", sessions.stream()
                .mapToInt(session -> session.testsSkippedMessages).sum());
        int ciSkipped = sessions.stream()
                .mapToInt(session -> session.ciTestsSkippedMessages).sum();
        result.put("ciTestsSkippedMessages", ciSkipped);
        ObjectNode execution = result.putObject("testExecution");
        boolean ciLogsCaptured = sessions.stream().anyMatch(session -> session.ciLogCaptured);
        boolean fullCiLogsCaptured = sessions.stream().anyMatch(
                session -> session.ciFullLogCaptured);
        boolean failedCiLogsOnly = ciLogsCaptured && !fullCiLogsCaptured
                && sessions.stream().anyMatch(session -> session.ciFailedLogOnlyCaptured);
        List<CiTestSummary> summaries = sessions.stream()
                .flatMap(session -> session.ciTestSummaries.stream()).toList();
        result.put("ciLogsCaptured", ciLogsCaptured);
        result.put("fullCiLogsCaptured", fullCiLogsCaptured);
        ArrayNode perRun = result.putArray("testSummariesByRunAndTask");
        for (SessionResult session : sessions) {
            for (CiTestSummary summary : session.ciTestSummaries) {
                ObjectNode node = perRun.addObject();
                node.put("taskIssue", session.issue);
                node.put("attempt", session.attempt);
                node.put("ciRun", summary.run());
                node.put("provider", summary.provider());
                node.put("testsRun", summary.testsRun());
                node.put("failures", summary.failures());
                node.put("errors", summary.errors());
                node.put("skipped", summary.skipped());
            }
        }
        if (failedCiLogsOnly) {
            execution.put("availability", "not_captured");
            execution.putNull("testsExecuted");
            execution.putNull("testsRun");
            execution.put("reason",
                    "Only gh run view --log-failed output was captured; passing-run "
                            + "Surefire/Failsafe summaries were not captured.");
        } else if (!summaries.isEmpty()) {
            execution.put("availability", "measured");
            execution.put("testsExecuted", summaries.stream()
                    .mapToInt(CiTestSummary::testsRun).sum() > 0);
            execution.put("testsRun", summaries.stream()
                    .mapToInt(CiTestSummary::testsRun).sum());
            execution.put("failures", summaries.stream()
                    .mapToInt(CiTestSummary::failures).sum());
            execution.put("errors", summaries.stream()
                    .mapToInt(CiTestSummary::errors).sum());
            execution.put("skipped", summaries.stream()
                    .mapToInt(CiTestSummary::skipped).sum());
            execution.put("source", "Surefire/Failsafe summaries parsed from captured CI logs.");
        } else if (ciSkipped > 0) {
            execution.put("availability", "measured");
            execution.put("testsExecuted", false);
            execution.put("testsRun", 0);
            execution.put("source", "CI transcript log contains 'Tests are skipped.'");
        } else if (ciLogsCaptured) {
            execution.put("availability", "measured");
            execution.putNull("testsExecuted");
            execution.putNull("testsRun");
            execution.put("source",
                    "CI logs were captured but contained no Surefire/Failsafe summary.");
        } else {
            execution.put("availability", "unavailable");
            execution.putNull("testsExecuted");
            execution.putNull("testsRun");
            execution.put("reason", "No conclusive CI test execution evidence was found.");
        }
        JsonNode baseline = repository.path("testIntegrity").path("baseline");
        if (baseline.has("testsRunByDefault")) {
            result.put("testsRunByDefault", baseline.path("testsRunByDefault").asBoolean());
            result.put("controlCiInterpretation",
                    "control".equals(config.arm) && !baseline.path("testsRunByDefault").asBoolean()
                            ? "compile_only_zero_tests_by_default"
                            : "tests_enabled_or_treatment_defined");
        } else {
            if (ciSkipped > 0) {
                result.put("testsRunByDefault", false);
            } else {
                result.putNull("testsRunByDefault");
            }
            result.put("controlCiInterpretation", ciSkipped > 0
                    ? "compile_only_zero_tests_from_ci_log_evidence"
                    : "unavailable_without_repository_or_ci_log_evidence");
        }
        return result;
    }

    private ObjectNode readSkillContentVerification() {
        ObjectNode result = JSON.createObjectNode();
        ObjectNode perDirectory = result.putObject("perDirectory");
        int verified = 0;
        for (Path directory : config.campaignDirs) {
            Path evidence = directory.resolve(
                    "shepherd-task-skill-content-hashes.json");
            ObjectNode entry = perDirectory.putObject(directory.toString());
            if (!Files.isRegularFile(evidence)) {
                entry.put("status", "unverified");
                entry.put("availability", "unavailable");
                continue;
            }
            try {
                JsonNode artifact = JSON.readTree(evidence.toFile());
                entry.put("status", "verified");
                entry.put("availability", "measured");
                entry.put("artifact", evidence.getFileName().toString());
                entry.set("data", artifact);
                verified++;
            } catch (IOException error) {
                entry.put("status", "invalid");
                entry.put("availability", "unavailable");
                entry.put("reason", error.getMessage());
            }
        }
        if (verified == 0) {
            result.put("status", "unverified");
            result.put("availability", "unavailable");
            result.put("expectedArtifact",
                    "shepherd-task-skill-content-hashes.json");
            result.put("fallback",
                    "Telemetry provides skill name hashes and content lengths only.");
            return result;
        }
        if (verified == config.campaignDirs.size()) {
            result.put("status", "verified");
            result.put("availability", "measured");
        } else {
            result.put("status", "partially_verified");
            result.put("availability", "measured");
        }
        return result;
    }

    private ArrayNode interpretationNotes() {
        ArrayNode notes = JSON.createArrayNode();
        notes.add("Detection stage should be presented per defect and descriptively; a small number of product defects per run does not support significance claims.");
        notes.add("Local AIU and tokens include Shepherd waiting/polling activity; CCA wait is reported separately because remote CCA internals are unavailable.");
        notes.add("Enabling tests in the treatment is a disclosed intervention, not evaluator-detected control-arm tampering.");
        notes.add("If the treatment raises the Java release level, disclose that it also removes the JDK-25/source-7 operational failure mode.");
        return notes;
    }

    private ArrayNode aggregateHumanInterventions(List<SessionResult> sessions) {
        ArrayNode values = JSON.createArrayNode();
        for (SessionResult session : sessions) {
            for (ObjectNode intervention : session.humanInterventions) {
                ObjectNode value = intervention.deepCopy();
                value.put("taskIssue", session.issue);
                value.put("shepherdStage", session.shepherdStage);
                values.add(value);
            }
        }
        return values;
    }

    private ObjectNode reconciliation(
            JsonNode manifest, ObjectNode root, Map<Integer, TaskResult> tasks,
            List<SessionResult> sessions) {
        if (!REFERENCE_CAMPAIGN_ID.equals(manifest.path("campaignId").asText())) {
            return unavailableSection("No built-in acceptance profile exists for this campaign.");
        }
        ObjectNode result = JSON.createObjectNode();
        result.put("profile", "reference-control-campaign");
        ArrayNode rows = result.putArray("metrics");
        addReconciliation(rows, "tasksMerged", 5,
                (int) tasks.values().stream().filter(task -> task.prNumber != null).count(),
                "Resolved stage-40 PRs from transcripts.");
        addReconciliation(rows, "wallClockSeconds", 9240,
                root.path("campaign").path("wallTimeSeconds").asLong(),
                "Manifest completedAt minus startedAt.");
        addReconciliation(rows, "recordedSessionSeconds", 7806,
                root.path("campaign").path("recordedSessionTimeSeconds").asLong(),
                "Sum of ten transcript header durations.");
        addReconciliation(rows, "sessions", 10, sessions.size(),
                "Phase task transcript count.");
        addReconciliation(rows, "ccraRounds", 6,
                root.path("convergence").path("reviewRounds").asInt(),
                "Unique completed Copilot review IDs.");
        addReconciliation(rows, "ccraActionableComments", 2,
                root.path("convergence").path("ccraActionableComments").asInt(),
                "Unique top-level comments in completed CCRA review batches.");
        addReconciliation(rows, "stage30ChangeRequests", 1,
                root.path("convergence").path("stage30Remediations").asInt(),
                "Unique CHANGES_REQUESTED review objects in stage 30.");
        addReconciliation(rows, "aiu", new BigDecimal("786.754"),
                root.path("cost").path("aiu").path("value").decimalValue()
                        .setScale(3, RoundingMode.HALF_UP),
                "Usage checkpoint total, rounded to three decimals.");
        addReconciliation(rows, "premiumRequests", 10,
                root.path("cost").path("premiumRequests").path("value").asInt(),
                "Usage checkpoint totals.");
        addReconciliation(rows, "inputTokens", 8_143_155,
                root.path("cost").path("tokens").path("input").path("value").asLong(),
                "Last cumulative export per metric attribute set per file.");
        addReconciliation(rows, "cacheReadTokens", 7_588_116,
                root.path("cost").path("tokens").path("cacheRead").path("value").asLong(),
                "Last cumulative export per metric attribute set per file.");
        addReconciliation(rows, "cacheWriteTokens", 554_442,
                root.path("cost").path("tokens").path("cacheWrite").path("value").asLong(),
                "Last cumulative export per metric attribute set per file.");
        addReconciliation(rows, "outputTokens", 102_885,
                root.path("cost").path("tokens").path("output").path("value").asLong(),
                "Last cumulative export per metric attribute set per file.");
        addReconciliation(rows, "reasoningTokens", 18_919,
                root.path("cost").path("tokens").path("reasoning").path("value").asLong(),
                "Last cumulative export per metric attribute set per file.");
        addReconciliation(rows, "nonzeroToolExits", 18,
                sessions.stream().mapToInt(value -> value.nonzeroToolExits).sum(),
                "Nonzero transcript shell terminators.");
        boolean matched = true;
        for (JsonNode row : rows) {
            matched &= row.path("matches").asBoolean();
        }
        result.put("allExpectedValuesMatched", matched);
        return result;
    }

    private ArrayNode acceptanceChecks(ObjectNode root) {
        ArrayNode checks = JSON.createArrayNode();
        JsonNode repository = root.path("repositoryAnalysis");
        addAcceptance(checks, "maven_project_root_recorded",
                repository.path("startProjectRoot").isTextual()
                        && repository.path("finalProjectRoot").isTextual(),
                repository.path("projectRootsBySha"));
        addAcceptance(checks, "cross_repo_start_equivalence",
                config.controlStart == null
                        || "measured".equals(repository.path("startEquivalence")
                        .path("availability").asText()),
                repository.path("startEquivalence").path("classificationSummary"));
        addAcceptance(checks, "ci_and_build_gates_classified",
                repository.path("startCiAndBuildGates").path("taxonomy")
                        .has("formatting")
                        && repository.path("startCiAndBuildGates").path("taxonomy")
                        .has("build_contract")
                        && repository.path("startCiAndBuildGates").path("taxonomy")
                        .has("static_analysis"),
                repository.path("startCiAndBuildGates").path("taxonomy"));
        boolean allProductClassified = events.stream()
                .filter(event -> "product_defect".equals(event.path("classification")
                        .path("category").asText()))
                .allMatch(event -> event.path("detectionGate").isTextual()
                        && event.path("defectClass").isTextual());
        addAcceptance(checks, "product_defect_gate_and_class_non_null",
                allProductClassified, allProductClassified);
        boolean consistentClasses = StreamSupport.stream(
                        root.path("productDefects").spliterator(), false)
                .allMatch(defect -> defectClass(
                        defect.path("categorySubtype").asText()).equals(
                        defect.path("defectClass").asText()));
        addAcceptance(checks, "defect_class_subtype_consistent",
                consistentClasses, consistentClasses);
        List<ObjectNode> guardrailUnclassified = unclassified.stream()
                .filter(event -> !"job_fallback".equals(
                        event.path("gateSource").asText()))
                .filter(event -> {
                    String evidence = event.path("evidence").path("excerpt")
                            .asText("").toLowerCase(Locale.ROOT);
                    return evidence.contains("spotless")
                            || evidence.contains("formatting")
                            || evidence.contains("source-gates")
                            || evidence.contains("source gates");
                }).toList();
        addAcceptance(checks, "guardrail_failures_not_unclassified",
                guardrailUnclassified.isEmpty(), array(guardrailUnclassified));
        addAcceptance(checks, "ci_test_log_availability_semantics",
                !"unavailable".equals(root.path("ciAnalysis").path("testExecution")
                        .path("availability").asText())
                        || !root.path("ciAnalysis").path("ciLogsCaptured").asBoolean(),
                root.path("ciAnalysis").path("testExecution"));
        addAcceptance(checks, "combined_attempts_preserved",
                config.mode != Mode.COMBINE
                        || root.path("campaign").path("campaignDirectories").size()
                        == config.campaignDirs.size(),
                root.path("campaign").path("campaignDirectories"));
        addAcceptance(checks, "combined_active_time_separates_gap",
                config.mode != Mode.COMBINE
                        || root.path("campaign").path("activeTimeSeconds").asLong()
                        + root.path("campaign").path("interDirectoryGapSeconds").asLong()
                        == root.path("campaign").path("elapsedSpanSeconds").asLong(),
                root.path("campaign"));
        return checks;
    }

    private static void addAcceptance(
            ArrayNode checks, String name, boolean pass, Object observed) {
        ObjectNode check = checks.addObject();
        check.put("name", name);
        check.put("status", pass ? "pass" : "fail");
        check.set("observed", observed instanceof JsonNode node
                ? node.deepCopy() : JSON.valueToTree(observed));
    }

    private static void addReconciliation(
            ArrayNode rows, String metric, Object expected, Object actual, String method) {
        ObjectNode row = rows.addObject();
        row.put("metric", metric);
        row.set("expected", JSON.valueToTree(expected));
        row.set("actual", JSON.valueToTree(actual));
        row.put("matches", Objects.equals(expected.toString(), actual.toString()));
        row.put("explanation", method);
    }

    private void writeSummaryCsv(
            JsonNode manifest, ObjectNode root, Map<Integer, TaskResult> tasks,
            List<SessionResult> sessions) throws IOException {
        List<String> columns = List.of(
                "schema_version", "evaluator_version", "evaluator_git_commit", "arm",
                "campaign_id", "row_type", "task_issue", "pr_number",
                "attempts",
                "first_detection_gate", "compiler_defects", "static_analysis_defects",
                "formatting_defects",
                "unit_tests_defects", "arquillian_container_tests_defects",
                "ci_defects", "ccra_review_defects", "stage_30_gate_defects",
                "functional_acceptance_defects",
                "session_count", "recorded_session_seconds", "jsonl_session_ms",
                "duration_source_difference_ms", "cca_wait_polls",
                "cca_wait_elapsed_seconds", "cca_wait_ceiling_seconds",
                "nonzero_tool_exits",
                "review_rounds", "ccra_actionable_comments", "stage30_change_requests",
                "product_defects", "flaky_test_failures", "leftover_state_failures",
                "behavioral_defects", "completeness_defects", "style_defects",
                "static_analysis_finding_defects",
                "aiu", "premium_requests", "input_tokens", "cache_read_tokens",
                "cache_write_tokens", "uncached_input_tokens", "output_tokens", "reasoning_tokens",
                "product_code_events", "shepherd_harness_events",
                "local_environment_events", "agent_tool_invocation_events",
                "agent_authored_test_harness_events", "ci_runner_events",
                "external_service_events",
                "product_code_problems", "shepherd_harness_problems",
                "local_environment_problems", "agent_tool_invocation_problems",
                "agent_authored_test_harness_problems", "ci_runner_problems",
                "external_service_problems",
                "files_changed", "additions", "deletions", "test_tampering_meaningfulness");
        StringBuilder csv = new StringBuilder();
        csv.append(columns.stream().map(CampaignEvaluator::csv).collect(Collectors.joining(",")))
                .append('\n');
        for (TaskResult task : tasks.values()) {
            List<SessionResult> selected = sessions.stream()
                    .filter(value -> value.issue == task.issue).toList();
            Map<String, Object> row = baseCsvRow(manifest, "task");
            row.put("task_issue", task.issue);
            row.put("pr_number", task.prNumber);
            row.put("attempts", selected.stream()
                    .map(session -> session.attempt).distinct().count());
            fillSessionCsv(row, selected);
            if (task.gitTask != null && task.gitTask.diff != null) {
                row.put("files_changed", task.gitTask.diff.files);
                row.put("additions", task.gitTask.diff.additions);
                row.put("deletions", task.gitTask.diff.deletions);
            }
            row.put("test_tampering_meaningfulness",
                    testMeaningfulness(root, task.issue));
            fillDefectCsvColumns(row, Set.of(task.issue));
            appendCsv(csv, columns, row);
        }
        Map<String, Object> campaign = baseCsvRow(manifest, "campaign");
        fillSessionCsv(campaign, sessions);
        campaign.put("attempts", config.campaignDirs.size());
        JsonNode campaignDiff = root.path("repositoryAnalysis").path("campaignDiff");
        if (!campaignDiff.isMissingNode()) {
            campaign.put("files_changed", nullableText(campaignDiff, "files"));
            campaign.put("additions", nullableText(campaignDiff, "additions"));
            campaign.put("deletions", nullableText(campaignDiff, "deletions"));
        }
        campaign.put("test_tampering_meaningfulness",
                root.path("repositoryAnalysis").path("testIntegrity")
                        .path("meaningfulness").path("status").asText(""));
        fillDefectCsvColumns(campaign, sessions.stream()
                .map(value -> value.issue).collect(Collectors.toSet()));
        appendCsv(csv, columns, campaign);
        Files.writeString(config.outputDir.resolve("summary.csv"), csv,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private Map<String, Object> baseCsvRow(JsonNode manifest, String rowType) {
        Map<String, Object> row = new HashMap<>();
        row.put("schema_version", "1.2");
        row.put("evaluator_version", evaluatorVersion);
        row.put("evaluator_git_commit", evaluatorCommit);
        row.put("arm", config.arm);
        row.put("campaign_id", manifest.path("campaignId").asText());
        row.put("row_type", rowType);
        return row;
    }

    private void fillSessionCsv(Map<String, Object> row, List<SessionResult> sessions) {
        row.put("session_count", sessions.size());
        row.put("recorded_session_seconds",
                sessions.stream().mapToLong(value -> value.durationSeconds).sum());
        long jsonlMs = sessions.stream().mapToLong(value -> value.jsonlDurationMs).sum();
        row.put("jsonl_session_ms", jsonlMs);
        row.put("duration_source_difference_ms", jsonlMs
                - sessions.stream().mapToLong(value -> value.durationSeconds).sum() * 1000);
        row.put("cca_wait_polls",
                sessions.stream().mapToInt(value -> value.ccaWaitPollCount).sum());
        row.put("cca_wait_elapsed_seconds",
                sessions.stream().mapToLong(value -> value.ccaWaitElapsedSeconds).sum());
        row.put("cca_wait_ceiling_seconds",
                sessions.stream().mapToLong(
                        value -> value.ccaWaitConfiguredCeilingSeconds).sum());
        row.put("nonzero_tool_exits",
                sessions.stream().mapToInt(value -> value.nonzeroToolExits).sum());
        row.put("review_rounds",
                sessions.stream().mapToInt(value -> value.reviewIds.size()).sum());
        row.put("ccra_actionable_comments",
                sessions.stream().mapToInt(value -> value.reviewCommentIds.size()).sum());
        Set<Integer> issues = sessions.stream().map(value -> value.issue).collect(Collectors.toSet());
        row.put("stage30_change_requests", events.stream()
                .filter(event -> issues.contains(event.path("taskIssue").asInt()))
                .filter(event -> "stage_30_change_request".equals(
                        event.path("eventKind").asText())).count());
        row.put("product_defects", distinctDefectCount(issues, null));
        for (String defectClass : List.of(
                "behavioral", "completeness", "style", "static_analysis_finding")) {
            row.put(defectClass + "_defects",
                    distinctDefectCount(issues, defectClass));
        }
        row.put("flaky_test_failures",
                sessions.stream().mapToInt(value -> value.flakyTestFailures).sum());
        row.put("leftover_state_failures",
                Math.max(sessions.stream().mapToInt(
                                value -> value.leftoverStateFailures).sum(),
                        events.stream()
                                .filter(event -> issues.contains(
                                        event.path("taskIssue").asInt()))
                                .filter(event -> "leftover_state".equals(
                                        event.path("classification")
                                                .path("subtype").asText()))
                                .count()));
        BigDecimal aiu = sessions.stream().map(value -> value.nanoAiu)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(1_000_000_000L));
        row.put("aiu", aiu.toPlainString());
        row.put("premium_requests",
                sessions.stream().mapToInt(value -> value.premiumRequests).sum());
        TokenTotals tokens = new TokenTotals();
        sessions.forEach(value -> tokens.add(value.tokens));
        row.put("input_tokens", tokens.input);
        row.put("cache_read_tokens", tokens.cacheRead);
        row.put("cache_write_tokens", tokens.cacheWrite);
        row.put("uncached_input_tokens",
                tokens.input - tokens.cacheRead - tokens.cacheWrite);
        row.put("output_tokens", tokens.output);
        row.put("reasoning_tokens", tokens.reasoning);
        for (String origin : List.of(
                "product_code", "shepherd_harness", "local_environment",
                "agent_tool_invocation", "agent_authored_test_harness",
                "ci_runner", "external_service")) {
            row.put(origin + "_events", events.stream()
                    .filter(event -> issues.contains(event.path("taskIssue").asInt()))
                    .filter(event -> origin.equals(event.path("classification")
                            .path("origin").asText())).count());
            row.put(origin + "_problems", events.stream()
                    .filter(event -> issues.contains(event.path("taskIssue").asInt()))
                    .filter(event -> origin.equals(event.path("classification")
                            .path("origin").asText()))
                    .collect(Collectors.toMap(
                            event -> event.path("underlyingProblemId").asText(),
                            event -> event, (left, right) -> left))
                    .size());
        }
    }

    private long distinctDefectCount(Set<Integer> issues, String defectClass) {
        return events.stream()
                .filter(event -> issues.contains(event.path("taskIssue").asInt()))
                .filter(event -> "product_defect".equals(
                        event.path("classification").path("category").asText()))
                .filter(event -> defectClass == null
                        || defectClass.equals(event.path("defectClass").asText()))
                .map(CampaignEvaluator::defectFingerprint)
                .distinct().count();
    }

    private void fillDefectCsvColumns(Map<String, Object> row, Set<Integer> issues) {
        List<ObjectNode> product = events.stream()
                .filter(event -> issues.contains(event.path("taskIssue").asInt()))
                .filter(event -> "product_defect".equals(
                        event.path("classification").path("category").asText()))
                .sorted(Comparator.comparing(event -> event.path("timestamp").asText()))
                .toList();
        row.put("first_detection_gate", product.isEmpty() ? ""
                : product.get(0).path("detectionGate").asText());
        for (String gate : List.of("compiler", "formatting", "static_analysis", "unit_tests",
                "arquillian_container_tests", "ci", "ccra_review",
                "stage_30_gate", "functional_acceptance")) {
            row.put(gate + "_defects", product.stream()
                    .filter(event -> gate.equals(event.path("detectionGate").asText()))
                    .map(CampaignEvaluator::defectFingerprint)
                    .distinct()
                    .count());
        }
    }

    private void writeDefectsCsv(JsonNode manifest, List<ObjectNode> defects)
            throws IOException {
        List<String> columns = List.of(
                "schema_version", "evaluator_version", "evaluator_git_commit", "arm",
                "campaign_id", "defect_id", "task_issue", "pr_number",
                "defect_class", "category_subtype", "item_count",
                "detection_gate", "where", "shepherd_stage",
                "detected_at", "detected_head_sha", "fix_commit", "fixed_at",
                "detected_timestamp_source", "fix_timestamp_source",
                "time_to_fix_seconds", "time_anomaly", "resolution_status",
                "occurrence_count", "evidence_ids");
        StringBuilder output = new StringBuilder();
        output.append(columns.stream().map(CampaignEvaluator::csv)
                .collect(Collectors.joining(","))).append('\n');
        for (ObjectNode defect : defects) {
            Map<String, Object> row = baseCsvRow(manifest, "defect");
            row.put("defect_id", defect.path("id").asText());
            row.put("task_issue", defect.path("taskIssue").asInt());
            row.put("pr_number", defect.path("prNumber").isNull() ? ""
                    : defect.path("prNumber").asText());
            row.put("category_subtype", defect.path("categorySubtype").asText());
            row.put("defect_class", defect.path("defectClass").asText());
            row.put("item_count", defect.path("itemCount").asInt(1));
            JsonNode first = defect.path("firstDetection");
            row.put("detection_gate", first.path("detectionGate").asText());
            String firstEventId = first.path("eventId").asText();
            events.stream().filter(event -> firstEventId.equals(event.path("id").asText()))
                    .findFirst().ifPresent(event ->
                            row.put("where", event.path("where").asText()));
            row.put("shepherd_stage", first.path("shepherdStage").asInt());
            row.put("detected_at", first.path("timestamp").asText(""));
            row.put("detected_head_sha", first.path("headSha").asText(""));
            row.put("detected_timestamp_source",
                    first.path("timestampSource").asText(""));
            JsonNode resolution = defect.path("resolution");
            row.put("fix_commit", resolution.path("commit").asText(""));
            row.put("fixed_at", resolution.path("fixedAt").asText(""));
            row.put("fix_timestamp_source",
                    resolution.path("timestampSource").asText(""));
            row.put("time_to_fix_seconds", resolution.path("timeToFixSeconds").asText(""));
            row.put("time_anomaly", resolution.path("anomaly").asText(""));
            row.put("resolution_status", resolution.path("status").asText(""));
            row.put("occurrence_count", defect.path("occurrenceCount").asInt());
            row.put("evidence_ids", String.join(";",
                    toStrings(defect.path("occurrenceEventIds"))));
            appendCsv(output, columns, row);
        }
        Files.writeString(config.outputDir.resolve("defects.csv"), output,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static List<String> toStrings(JsonNode array) {
        List<String> values = new ArrayList<>();
        array.forEach(value -> values.add(value.asText()));
        return values;
    }

    private static Object nullableText(JsonNode node, String field) {
        return node.has(field) && !node.path(field).isNull()
                ? node.path(field).asText() : null;
    }

    private static String testMeaningfulness(ObjectNode root, int issue) {
        for (JsonNode task : root.path("repositoryAnalysis").path("tasks")) {
            if (task.path("taskIssue").asInt() == issue) {
                return task.path("testIntegrity").path("meaningfulness")
                        .path("status").asText("");
            }
        }
        return "";
    }

    private static void appendCsv(
            StringBuilder output, List<String> columns, Map<String, Object> values) {
        output.append(columns.stream()
                        .map(column -> csv(Objects.toString(values.get(column), "")))
                        .collect(Collectors.joining(",")))
                .append('\n');
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void writeReport(
            JsonNode manifest, ObjectNode root, Map<Integer, TaskResult> tasks,
            List<SessionResult> sessions) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Shepherd Campaign Evaluation\n\n")
                .append("- **Arm:** `").append(config.arm).append("`\n")
                .append("- **Campaign:** `").append(manifest.path("campaignId").asText()).append("`\n")
                .append("- **Evaluator:** `").append(evaluatorVersion).append("` at `")
                .append(evaluatorCommit).append("`\n")
                .append("- **Evaluator worktree dirty:** ").append(evaluatorDirty).append("\n")
                .append("- **Generated:** ").append(Instant.now()).append("\n\n");
        if ("unversioned".equals(evaluatorVersion)
                || "unversioned".equals(evaluatorCommit)) {
            report.append("> [!WARNING]\n> This evaluation was explicitly allowed to run without versioned evaluator provenance.\n\n");
        }

        report.append("## Headline findings\n\n")
                .append("| Task | PR | Attempts/outcomes | First product-defect detection | Product defects | Nonzero exits | CCRA rounds | CCRA comments | Flaky tests |\n")
                .append("|---:|---:|---|---|---:|---:|---:|---:|---:|\n");
        for (TaskResult task : tasks.values()) {
            List<JsonNode> taskDefects = StreamSupport.stream(
                            root.path("productDefects").spliterator(), false)
                    .filter(defect -> defect.path("taskIssue").asInt() == task.issue)
                    .toList();
            String first = taskDefects.isEmpty() ? "none detected"
                    : taskDefects.get(0).path("firstDetection")
                    .path("detectionGate").asText();
            List<SessionResult> selected = sessions.stream()
                    .filter(value -> value.issue == task.issue).toList();
            String outcomes = selected.stream().map(session -> session.attempt)
                    .distinct().sorted()
                    .map(attempt -> attempt + ":"
                            + (selected.stream()
                            .filter(session -> session.attempt == attempt)
                            .anyMatch(session -> session.convergenceFailure)
                            ? "convergence_failure" : "completed"))
                    .collect(Collectors.joining(", "));
            report.append("| ").append(task.issue).append(" | ")
                    .append(task.prNumber == null ? "unavailable" : task.prNumber)
                    .append(" | ").append(outcomes)
                    .append(" | ").append(first).append(" | ")
                    .append(taskDefects.size()).append(" | ")
                    .append(selected.stream().mapToInt(value -> value.nonzeroToolExits).sum())
                    .append(" | ")
                    .append(selected.stream().mapToInt(value -> value.reviewIds.size()).sum())
                    .append(" | ")
                    .append(selected.stream().mapToInt(value -> value.reviewCommentIds.size()).sum())
                    .append(" | ")
                    .append(selected.stream().mapToInt(value -> value.flakyTestFailures).sum())
                    .append(" |\n");
        }

        report.append("\n## Cost and timing\n\n")
                .append("- Campaign active time: ").append(formatDuration(
                        root.path("campaign").path("activeTimeSeconds").asLong())).append("\n")
                .append("- Inter-directory gap: ").append(formatDuration(
                        root.path("campaign").path("interDirectoryGapSeconds").asLong())).append("\n")
                .append("- First-start to last-end span: ").append(formatDuration(
                        root.path("campaign").path("elapsedSpanSeconds").asLong()))
                .append(" (not campaign active time)\n")
                .append("- Recorded session time: ").append(formatDuration(
                        root.path("campaign").path("recordedSessionTimeSeconds").asLong())).append("\n")
                .append("- JSONL exact session time: ")
                .append(root.path("campaign").path("jsonlSessionTimeMs").asLong())
                .append(" ms (")
                .append(root.path("campaign").path("durationSourceDifferenceMs").asLong())
                .append(" ms above second-truncated Markdown headers)\n")
                .append("- Orchestration overhead: ").append(formatDuration(
                        root.path("campaign").path("orchestrationOverheadSeconds").asLong())).append("\n")
                .append("- CCA wait proxy: ")
                .append(root.path("convergence").path("ccaWait").path("pollCount").asInt())
                .append(" polls / ")
                .append(formatDuration(root.path("convergence").path("ccaWait")
                        .path("elapsedSeconds").asLong())).append(" elapsed; ")
                .append(formatDuration(root.path("convergence").path("ccaWait")
                        .path("configuredPollCeilingSeconds").asLong()))
                .append(" configured ceiling\n")
                .append("- AIU: ").append(root.path("cost").path("aiu").path("value").asText()).append("\n")
                .append("- Premium requests: ")
                .append(root.path("cost").path("premiumRequests").path("value").asText()).append("\n");

        report.append("\n## Evidence and run invariants\n\n")
                .append("- CI tests run: ")
                .append(root.path("ciAnalysis").path("testExecution")
                        .path("testsRun").isNumber()
                        ? root.path("ciAnalysis").path("testExecution")
                                .path("testsRun").asText()
                        : "unavailable")
                .append(" (`")
                .append(root.path("ciAnalysis").path("testExecution")
                        .path("availability").asText("unavailable"))
                .append("`)\n")
                .append("- Partial output: ")
                .append(root.path("evidenceQuality")
                        .path("jsonlPartialOutputCrossCheck")
                        .path("unicodeCodePointCount").asLong())
                .append(" Unicode code points / ")
                .append(root.path("evidenceQuality")
                        .path("jsonlPartialOutputCrossCheck")
                        .path("utf16CodeUnitCount").asLong())
                .append(" UTF-16 code units\n")
                .append("- Skill content verification: `")
                .append(root.path("runInvariants").path("skillContentVerification")
                        .path("status").asText("unverified"))
                .append("`; telemetry hashes identify skill names, not content\n");
        if (root.path("runInvariants").path("driftDetected").asBoolean()) {
            report.append("\n> [!WARNING]\n> Combined campaign directories contain invariant drift.\n\n")
                    .append("| Invariant | Values by attempt |\n|---|---|\n");
            for (JsonNode drift : root.path("runInvariants").path("drift")) {
                report.append("| ").append(drift.path("invariant").asText())
                        .append(" | ").append(drift.path("valuesByAttempt").toString())
                        .append(" |\n");
            }
        }

        report.append("\n## Acceptance checks\n\n")
                .append("| Check | Status | Observed |\n|---|---|---|\n");
        for (JsonNode check : root.path("acceptanceChecks")) {
            report.append("| ").append(check.path("name").asText()).append(" | **")
                    .append(check.path("status").asText()).append("** | `")
                    .append(excerpt(check.path("observed").toString(), 500)
                            .replace("|", "\\|"))
                    .append("` |\n");
        }

        report.append("\n## CI gate inventory\n\n")
                .append("| Gate | Status | Entries |\n|---|---|---:|\n");
        root.path("repositoryAnalysis").path("startCiAndBuildGates")
                .path("taxonomy").fields().forEachRemaining(entry ->
                        report.append("| ").append(entry.getKey()).append(" | ")
                                .append(entry.getValue().path("status").asText())
                                .append(" | ")
                                .append(entry.getValue().path("entryCount").asInt())
                                .append(" |\n"));
        report.append("\nNamed steps requiring `ci_other` review:\n\n");
        for (JsonNode step : root.path("repositoryAnalysis")
                .path("startCiAndBuildGates").path("ciOtherSteps")) {
            report.append("- `").append(step.asText()).append("`\n");
        }

        JsonNode equivalence = root.path("repositoryAnalysis").path("startEquivalence");
        report.append("\n## Start equivalence and confounds\n\n");
        if ("measured".equals(equivalence.path("availability").asText())) {
            report.append("| Classification | Count | Paths |\n|---|---:|---|\n");
            equivalence.path("classificationSummary").fields().forEachRemaining(entry ->
                    report.append("| ").append(entry.getKey()).append(" | ")
                            .append(entry.getValue().path("count").asInt()).append(" | ")
                            .append(entry.getValue().path("paths").toString()
                                    .replace("|", "\\|")).append(" |\n"));
            report.append("\n### Campaign-input line differences\n\n");
            for (JsonNode input : equivalence.path("campaignInputDiffs")) {
                report.append("- **").append(input.path("path").asText()).append("**: ")
                        .append(input.path("controlLines").asLong()).append(" control lines vs ")
                        .append(input.path("treatmentLines").asLong())
                        .append(" treatment lines. This is a campaign-input confound.\n")
                        .append("\n```diff\n")
                        .append(input.path("lineDiff").asText())
                        .append("\n```\n");
            }
            for (JsonNode path : equivalence.path("classificationSummary")
                    .path("relocation_only").path("paths")) {
                report.append("- Relocation confound: `").append(path.asText()).append("`.\n");
            }
        } else {
            report.append("Cross-repository start comparison was not requested.\n");
        }

        report.append("\n## Defect interpretation\n\n")
                .append("See `defects.csv` for one row per deduplicated defect, including ")
                .append("defect class, item count, local/CI location, timestamp source, ancestry-verified fix, and anomalies.\n\n")
                .append("Style and static-analysis findings are detectable only where the corresponding gate exists; ")
                .append("they are excluded from arm-comparison conclusions when either arm reports that gate as `not_present`.\n");

        report.append("\n## Post-mortem agent cost\n\n")
                .append("- AIU: ")
                .append(root.path("postMortemAgent").path("aiu").path("value").asText("unavailable"))
                .append("\n- Premium requests: ")
                .append(root.path("postMortemAgent").path("premiumRequests")
                        .path("value").asText("unavailable"))
                .append("\n- Tokens: ")
                .append(root.path("postMortemAgent").path("tokens")
                        .path("availability").asText("unavailable"))
                .append("\n");

        report.append("\n## Reference reconciliation\n\n");
        JsonNode reconciliation = root.path("reconciliation");
        if (reconciliation.has("metrics")) {
            report.append("| Metric | Expected | Actual | Match | Explanation |\n")
                    .append("|---|---:|---:|:---:|---|\n");
            for (JsonNode row : reconciliation.path("metrics")) {
                report.append("| ").append(row.path("metric").asText()).append(" | ")
                        .append(row.path("expected").asText()).append(" | ")
                        .append(row.path("actual").asText()).append(" | ")
                        .append(row.path("matches").asBoolean() ? "yes" : "no").append(" | ")
                        .append(row.path("explanation").asText()).append(" |\n");
            }
        } else {
            report.append("No built-in acceptance profile applies to this campaign.\n");
        }

        report.append("\n## Trust assessment\n\n")
                .append("- **Trustworthy:** manifest timing, session counts, transcript durations, ")
                .append("exact JSONL durations, nonzero exit counts, JSONL AIU/premium/model/tool counts, ")
                .append("JSONL partial-output cross-checks, run invariants, and cumulative OTEL tokens.\n")
                .append("- **Approximate:** command-to-head correlation when a transcript does not emit a full SHA, ")
                .append("agent-action labels, rule-based defect deduplication, and CCA wait as a latency proxy.\n")
                .append("- **Manual review:** all entries in `unclassified.md`, confirmed evidence gaps, ")
                .append("and remote CCA/CCRA internal cost because those internals are absent.\n");

        report.append("\n## Experiment interpretation\n\n");
        for (JsonNode note : root.path("interpretationNotes")) {
            report.append("- ").append(note.asText()).append("\n");
        }
        report.append("- Test-tampering metrics are comparable only within an arm where tests run by default; the control arm is `not_meaningful`, so this is not a between-arm tampering comparison.\n");
        report.append("\n## Remaining unclassified\n\n");
        if (unclassified.isEmpty()) {
            report.append("- None.\n");
        } else {
            for (ObjectNode event : unclassified) {
                report.append("- `").append(event.path("id").asText()).append("`: ")
                        .append(event.path("reason").asText().replaceAll("\\R+", " "))
                        .append("\n");
            }
        }

        Files.writeString(config.outputDir.resolve("report.md"), report,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void writeUnclassified() throws IOException {
        StringBuilder output = new StringBuilder();
        output.append("# Unclassified Campaign Evidence\n\n")
                .append("- **Arm:** `").append(config.arm).append("`\n")
                .append("- **Evaluator:** `").append(evaluatorVersion).append("` at `")
                .append(evaluatorCommit).append("`\n\n");
        if (unclassified.isEmpty()) {
            output.append("No events require manual classification.\n");
        } else {
            for (ObjectNode event : unclassified) {
                output.append("## ").append(event.path("id").asText())
                        .append(" — task #").append(event.path("taskIssue").asInt())
                        .append(", stage ").append(event.path("shepherdStage").asInt())
                        .append("\n\n")
                        .append("- Kind: `").append(event.path("eventKind").asText()).append("`\n")
                        .append("- Detection gate: `").append(event.path("detectionGate").asText()).append("`\n")
                        .append("- Reason: ").append(event.path("reason").asText()).append("\n")
                        .append("- Artifact: `").append(event.path("evidence").path("artifact").asText())
                        .append(":").append(event.path("evidence").path("lineStart").asInt())
                        .append("`\n\n```text\n")
                        .append(event.path("evidence").path("excerpt").asText())
                        .append("\n```\n\n");
            }
        }
        Files.writeString(config.outputDir.resolve("unclassified.md"), output,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String formatDuration(long seconds) {
        return String.format(Locale.ROOT, "%dh %02dm %02ds",
                seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    private static ObjectNode metric(Object value, String availability, String method) {
        ObjectNode metric = JSON.createObjectNode();
        metric.set("value", JSON.valueToTree(value));
        metric.put("availability", availability);
        metric.put("method", method);
        metric.putArray("evidenceIds");
        return metric;
    }

    private static ObjectNode unavailableMetric(String reason) {
        ObjectNode metric = JSON.createObjectNode();
        metric.putNull("value");
        metric.put("availability", "unavailable");
        metric.put("reason", reason);
        metric.putArray("evidenceIds");
        return metric;
    }

    private static ObjectNode unavailableSection(String reason) {
        ObjectNode value = JSON.createObjectNode();
        value.put("availability", "unavailable");
        value.put("reason", reason);
        return value;
    }

    private static ArrayNode array(List<ObjectNode> nodes) {
        ArrayNode array = JSON.createArrayNode();
        nodes.forEach(array::add);
        return array;
    }

    private static void copy(JsonNode from, ObjectNode to, String field) {
        if (from.has(field)) {
            to.set(field, from.path(field).deepCopy());
        } else {
            to.putNull(field);
        }
    }

    private static void copyNullable(JsonNode from, ObjectNode to, String field) {
        if (from.has(field) && !from.path(field).isNull()) {
            to.set(field, from.path(field).deepCopy());
        } else {
            to.putNull(field);
        }
    }

    private static String excerpt(String value, int maximum) {
        if (value == null) {
            return "";
        }
        String normalized = value.strip();
        if (normalized.length() <= maximum) {
            return normalized;
        }
        return normalized.substring(0, maximum) + "\n…";
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static void writeJson(Path path, JsonNode value) throws IOException {
        JSON.writeValue(path.toFile(), value);
        Files.writeString(path, System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.APPEND);
    }

    private static CommandResult command(
            List<String> arguments, Path directory, Map<String, String> environment) {
        ProcessBuilder builder = new ProcessBuilder(arguments);
        builder.redirectErrorStream(true);
        if (directory != null) {
            builder.directory(directory.toFile());
        }
        builder.environment().putAll(environment);
        try {
            Process process = builder.start();
            String output;
            try (var input = process.getInputStream()) {
                output = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
            int exitCode = process.waitFor();
            return new CommandResult(exitCode, output);
        } catch (IOException error) {
            return new CommandResult(-1, error.getMessage());
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            return new CommandResult(-1, "Interrupted while running " + arguments);
        }
    }

    private static CommandResult mavenCommand(
            List<String> arguments, Path directory, Map<String, String> environment) {
        String logName = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm", Locale.ROOT)
                .format(ZonedDateTime.now()) + "-job-logs.txt";
        String shellCommand = "set -o pipefail; "
                + arguments.stream().map(CampaignEvaluator::shellQuote)
                .collect(Collectors.joining(" "))
                + " 2>&1 | tee " + shellQuote(logName);
        CommandResult run = command(
                List.of("/bin/bash", "-c", shellCommand), directory, environment);
        Path log = directory.resolve(logName);
        if (Files.isRegularFile(log)) {
            try {
                return new CommandResult(run.exitCode,
                        Files.readString(log, StandardCharsets.UTF_8));
            } catch (IOException ignored) {
                // The streamed command output remains available.
            }
        }
        return run;
    }

    private static String shellQuote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private record CommandResult(int exitCode, String output) {}
    private record MetricPoint(long endNanos, BigDecimal value) {}
    record CiFailure(String job, String step, String source) {}
    record Stage30Decision(
            String subtype, boolean classified, String source, String explanation) {}
    private record CiTestSummary(
            String run, String provider, int testsRun,
            int failures, int errors, int skipped) {}
    private record RepoRevision(Path repo, String sha) {}
    private enum Mode { SINGLE, COMBINE, COMPARE_EVALS }

    private static final class Config {
        final Path campaignDir;
        final List<Path> campaignDirs;
        final List<Path> evaluationDirs;
        final Mode mode;
        final String arm;
        final Path repo;
        final RepoRevision controlStart;
        final Path outputDir;
        final Path evaluatorDir;
        final boolean withBuild;
        final boolean allowUnversioned;
        final List<String> invocation;

        private Config(
                Path campaignDir, List<Path> campaignDirs, List<Path> evaluationDirs,
                Mode mode, String arm, Path repo, RepoRevision controlStart, Path outputDir,
                Path evaluatorDir, boolean withBuild, boolean allowUnversioned,
                List<String> invocation) {
            this.campaignDir = campaignDir;
            this.campaignDirs = campaignDirs;
            this.evaluationDirs = evaluationDirs;
            this.mode = mode;
            this.arm = arm;
            this.repo = repo;
            this.controlStart = controlStart;
            this.outputDir = outputDir;
            this.evaluatorDir = evaluatorDir;
            this.withBuild = withBuild;
            this.allowUnversioned = allowUnversioned;
            this.invocation = invocation;
        }

        static Config parse(String[] args) {
            if (args.length == 0) {
                throw new UsageException("A campaign directory or explicit mode is required.");
            }
            List<Path> directories = new ArrayList<>();
            Mode mode = Mode.SINGLE;
            String arm = null;
            Path repo = null;
            RepoRevision controlStart = null;
            Path output = null;
            Path evaluatorDir = null;
            boolean withBuild = false;
            boolean allowUnversioned = false;
            for (int index = 0; index < args.length; index++) {
                String argument = args[index];
                switch (argument) {
                    case "--combine" -> mode = Mode.COMBINE;
                    case "--compare-evals" -> mode = Mode.COMPARE_EVALS;
                    case "--arm" -> arm = requireValue(args, ++index, "--arm");
                    case "--repo" -> repo = Paths.get(requireValue(args, ++index, "--repo"))
                            .toAbsolutePath().normalize();
                    case "--control-start" -> controlStart = parseRepoRevision(
                            requireValue(args, ++index, "--control-start"));
                    case "--out" -> output = Paths.get(requireValue(args, ++index, "--out"))
                            .toAbsolutePath().normalize();
                    case "--evaluator-dir" -> evaluatorDir = Paths.get(
                            requireValue(args, ++index, "--evaluator-dir"))
                            .toAbsolutePath().normalize();
                    case "--with-build" -> withBuild = true;
                    case "--allow-unversioned" -> allowUnversioned = true;
                    default -> {
                        if (argument.startsWith("--")) {
                            throw new UsageException("Unknown option: " + argument);
                        }
                        if (mode == Mode.SINGLE && !directories.isEmpty()) {
                            throw new UsageException("Only one campaign directory may be supplied.");
                        }
                        directories.add(Paths.get(argument).toAbsolutePath().normalize());
                    }
                }
            }
            if (directories.isEmpty()) {
                throw new UsageException("At least one directory is required.");
            }
            if (mode != Mode.COMPARE_EVALS
                    && !Set.of("control", "treatment").contains(arm)) {
                throw new UsageException("--arm must be control or treatment.");
            }
            if (output == null) {
                Path first = directories.get(0);
                output = first.resolveSibling(first.getFileName()
                        + (mode == Mode.COMPARE_EVALS ? "-comparison" : "-eval"));
            }
            if (evaluatorDir == null) {
                String source = System.getProperty("jbang.source");
                if (source != null && !source.isBlank()) {
                    evaluatorDir = Paths.get(source).toAbsolutePath().normalize().getParent();
                }
            }
            if (evaluatorDir == null) {
                throw new UsageException("Evaluator source directory was not supplied by the launcher.");
            }
            Path campaign = mode == Mode.COMPARE_EVALS ? directories.get(0) : directories.get(0);
            List<Path> campaigns = mode == Mode.COMPARE_EVALS ? List.of() : List.copyOf(directories);
            List<Path> evaluations = mode == Mode.COMPARE_EVALS
                    ? List.copyOf(directories) : List.of();
            return new Config(campaign, campaigns, evaluations, mode, arm, repo,
                    controlStart, output, evaluatorDir,
                    withBuild, allowUnversioned,
                    List.of(args.clone()));
        }

        private static RepoRevision parseRepoRevision(String value) {
            int separator = value.lastIndexOf('@');
            if (separator <= 0 || separator == value.length() - 1) {
                throw new UsageException("--control-start must be <repo-path>@<sha>.");
            }
            Path repo = Paths.get(value.substring(0, separator))
                    .toAbsolutePath().normalize();
            String sha = value.substring(separator + 1);
            if (!sha.matches("[0-9a-fA-F]{7,40}")) {
                throw new UsageException("--control-start SHA is invalid: " + sha);
            }
            return new RepoRevision(repo, sha);
        }

        private static String requireValue(String[] args, int index, String option) {
            if (index >= args.length) {
                throw new UsageException("Missing value for " + option);
            }
            return args[index];
        }
    }

    private static final class UsageException extends RuntimeException {
        UsageException(String message) {
            super(message);
        }
    }

    private static final class SessionResult {
        final int issue;
        final int shepherdStage;
        final Path markdown;
        final Path jsonl;
        final Path otel;
        final Path campaignDirectory;
        final int attempt;
        final TokenTotals tokens = new TokenTotals();
        final Set<String> models = new TreeSet<>();
        final Set<String> reasoningLevels = new TreeSet<>();
        final Set<String> copilotCliVersions = new TreeSet<>();
        final Set<String> loadedSkills = new TreeSet<>();
        final Set<String> javaHomes = new TreeSet<>();
        final Set<String> javacLevels = new TreeSet<>();
        final Set<String> reviewIds = new LinkedHashSet<>();
        final Set<String> reviewCommentIds = new LinkedHashSet<>();
        final Set<String> changeRequestKeys = new LinkedHashSet<>();
        final Set<String> commitShas = new LinkedHashSet<>();
        final List<String> missingArtifacts = new ArrayList<>();
        final List<String> eventIds = new ArrayList<>();
        final List<ObjectNode> transitions = new ArrayList<>();
        final List<ObjectNode> humanInterventions = new ArrayList<>();
        final List<ToolBlock> toolBlocks = new ArrayList<>();
        final List<ObjectNode> javaInvocations = new ArrayList<>();
        final List<CiTestSummary> ciTestSummaries = new ArrayList<>();
        final List<String> toolCompletionTimestamps = new ArrayList<>();
        final Map<String, Integer> eventTypeCounts = new TreeMap<>();
        final Map<String, String> partialOutputs = new HashMap<>();
        final Map<String, String> reviewCompletionTimestamps = new HashMap<>();
        final Map<String, String> externalizedToolCalls = new LinkedHashMap<>();
        final Map<String, String> skillNameHashes = new TreeMap<>();
        final Map<String, Long> skillContentLengths = new TreeMap<>();
        String sessionId;
        Integer prNumber;
        LocalDateTime headerLocalStart;
        Instant firstJsonlTimestamp;
        Instant startedAt;
        String timestampConversionMethod;
        long durationSeconds;
        long jsonlDurationMs;
        long partialOutputUnicodeCodePoints;
        long partialOutputUtf16CodeUnits;
        long reasoningDeltaCharacters;
        long ccaWaitElapsedSeconds;
        long ccaWaitConfiguredCeilingSeconds;
        BigDecimal nanoAiu = BigDecimal.ZERO;
        int premiumRequests;
        int inferenceCalls;
        int toolCalls;
        int externalOutputReferences;
        int confirmedEvidenceGaps;
        int partialOutputEvents;
        int reasoningDeltaEvents;
        int toolSuccessTrue;
        int toolSuccessFalse;
        int ccaWaitPollCount;
        int remoteAgentCheckObservations;
        int testsSkippedMessages;
        int ciTestsSkippedMessages;
        int nonzeroToolExits;
        int jsonlParseErrors;
        int otelParseErrors;
        int otelMonotonicityViolations;
        int productHallucinationSignals;
        int toolingHallucinationSignals;
        int fixCommits;
        int ciReruns;
        int reviewCapEvents;
        int timeoutEvents;
        int idleKillEvents;
        int flakyTestFailures;
        int flakyUnitTestFailures;
        int flakyContainerTestFailures;
        int leftoverStateFailures;
        int otherKnownCauseFlakyFailures;
        int unknownCauseFlakyFailures;
        boolean ciLogCaptured;
        boolean ciFailedLogOnlyCaptured;
        boolean ciFullLogCaptured;
        boolean convergenceFailure;
        String convergenceFailureReason;

        SessionResult(
                int issue, int shepherdStage, Path markdown, Path jsonl, Path otel,
                Path campaignDirectory, int attempt) {
            this.issue = issue;
            this.shepherdStage = shepherdStage;
            this.markdown = markdown;
            this.jsonl = jsonl;
            this.otel = otel;
            this.campaignDirectory = campaignDirectory;
            this.attempt = attempt;
        }

        ObjectNode toJson() {
            ObjectNode value = JSON.createObjectNode();
            value.put("taskIssue", issue);
            value.put("shepherdStage", shepherdStage);
            value.put("attempt", attempt);
            value.put("campaignDirectory", campaignDirectory.toString());
            if (sessionId == null) {
                value.putNull("sessionId");
            } else {
                value.put("sessionId", sessionId);
            }
            value.put("transcript", markdown.getFileName().toString());
            value.put("jsonl", jsonl.getFileName().toString());
            value.put("otel", otel.getFileName().toString());
            if (prNumber == null) {
                value.putNull("prNumber");
            } else {
                value.put("prNumber", prNumber);
            }
            value.put("startedAt", startedAt == null ? null : startedAt.toString());
            value.put("timestampConversionMethod", timestampConversionMethod);
            value.put("durationSeconds", durationSeconds);
            value.put("jsonlDurationMs", jsonlDurationMs);
            value.set("aiu", metric(nanoAiu.divide(BigDecimal.valueOf(1_000_000_000L)),
                    "measured", "Final session usage checkpoint."));
            value.set("premiumRequests", metric(premiumRequests,
                    "measured", "Final session usage checkpoint."));
            value.set("tokens", tokens.toJson());
            value.put("inferenceCalls", inferenceCalls);
            value.put("toolCalls", toolCalls);
            value.put("nonzeroToolExits", nonzeroToolExits);
            value.put("externalizedOutputMarkers", externalOutputReferences);
            value.put("confirmedEvidenceGaps", confirmedEvidenceGaps);
            value.put("partialOutputEvents", partialOutputEvents);
            value.put("partialOutputUnicodeCodePoints",
                    partialOutputUnicodeCodePoints);
            value.put("partialOutputUtf16CodeUnits", partialOutputUtf16CodeUnits);
            value.put("reasoningDeltaEvents", reasoningDeltaEvents);
            value.put("reasoningDeltaCharacters", reasoningDeltaCharacters);
            value.put("toolExecutionCompleteSuccessTrue", toolSuccessTrue);
            value.put("toolExecutionCompleteSuccessFalse", toolSuccessFalse);
            value.put("toolSuccessIsShellFailureSignal", false);
            value.put("ccaWaitPollCount", ccaWaitPollCount);
            value.put("ccaWaitElapsedSeconds", ccaWaitElapsedSeconds);
            value.put("ccaWaitConfiguredCeilingSeconds",
                    ccaWaitConfiguredCeilingSeconds);
            value.put("remoteAgentCheckObservations", remoteAgentCheckObservations);
            value.put("testsSkippedMessages", testsSkippedMessages);
            value.put("ciTestsSkippedMessages", ciTestsSkippedMessages);
            value.put("attemptOutcome",
                    convergenceFailure ? "convergence_failure" : "completed");
            if (convergenceFailure) {
                value.put("attemptOutcomeReason", convergenceFailureReason);
            }
            ArrayNode loaded = value.putArray("loadedSkills");
            loadedSkills.forEach(loaded::add);
            ObjectNode hashes = value.putObject("skillNameHashes");
            skillNameHashes.forEach(hashes::put);
            ObjectNode lengths = value.putObject("skillContentLengths");
            skillContentLengths.forEach(lengths::put);
            ArrayNode invocations = value.putArray("javaInvocations");
            javaInvocations.forEach(invocations::add);
            value.put("otelMonotonicityViolations", otelMonotonicityViolations);
            value.put("flakyTestFailures", flakyTestFailures);
            value.put("flakyUnitTestFailures", flakyUnitTestFailures);
            value.put("flakyArquillianContainerTestFailures",
                    flakyContainerTestFailures);
            value.put("leftoverStateFailures", leftoverStateFailures);
            ArrayNode ids = value.putArray("eventIds");
            eventIds.forEach(ids::add);
            return value;
        }
    }

    private static final class ToolBlock {
        final String tool;
        final int startLine;
        final long relativeSeconds;
        final List<ObjectNode> testFailureEvents = new ArrayList<>();
        final List<String> commitShas = new ArrayList<>();
        int endLine;
        String command;
        String output = "";
        String raw = "";
        Integer exitCode;
        String headSha;
        String commitAtExecution;
        String jsonlCompletedAt;

        ToolBlock(String tool, int startLine, long relativeSeconds) {
            this.tool = tool;
            this.startLine = startLine;
            this.relativeSeconds = relativeSeconds;
        }
    }

    private static final class TaskResult {
        final int issue;
        final List<SessionResult> sessions = new ArrayList<>();
        Integer prNumber;
        GitTask gitTask;

        TaskResult(int issue) {
            this.issue = issue;
        }
    }

    private static final class GitTask {
        final int issue;
        final int prNumber;
        final String mergeSha;
        final String baseSha;
        final String headSha;
        final Instant mergeTime;
        DiffMetrics diff;

        GitTask(
                int issue, int prNumber, String mergeSha, String baseSha,
                String headSha, Instant mergeTime) {
            this.issue = issue;
            this.prNumber = prNumber;
            this.mergeSha = mergeSha;
            this.baseSha = baseSha;
            this.headSha = headSha;
            this.mergeTime = mergeTime;
        }
    }

    private enum FileKind {
        PRODUCTION, TEST, CONFIG
    }

    private static final class DiffMetrics {
        long files;
        long additions;
        long deletions;
        long commits;
        long buildFilesChanged;
        long dependencyChurnLines;
        final Map<FileKind, DiffMetrics> byKind = new LinkedHashMap<>();

        DiffMetrics() {
            for (FileKind kind : FileKind.values()) {
                byKind.put(kind, new DiffMetrics(false));
            }
        }

        private DiffMetrics(boolean initializeChildren) {
            if (initializeChildren) {
                for (FileKind kind : FileKind.values()) {
                    byKind.put(kind, new DiffMetrics(false));
                }
            }
        }

        ObjectNode toJson() {
            ObjectNode value = JSON.createObjectNode();
            value.put("files", files);
            value.put("additions", additions);
            value.put("deletions", deletions);
            value.put("commits", commits);
            value.put("buildFilesChanged", buildFilesChanged);
            value.put("dependencyChurnLines", dependencyChurnLines);
            ObjectNode split = value.putObject("split");
            for (Map.Entry<FileKind, DiffMetrics> entry : byKind.entrySet()) {
                ObjectNode kind = split.putObject(
                        entry.getKey().name().toLowerCase(Locale.ROOT));
                kind.put("files", entry.getValue().files);
                kind.put("additions", entry.getValue().additions);
                kind.put("deletions", entry.getValue().deletions);
            }
            return value;
        }
    }

    private static final class TokenTotals {
        static final Map<String, String> METRICS = Map.of(
                "gen_ai.client.inference.usage.input_tokens", "input",
                "gen_ai.client.inference.usage.output_tokens", "output",
                "gen_ai.client.inference.usage.cache_read.input_tokens", "cacheRead",
                "gen_ai.client.inference.usage.cache_write.input_tokens", "cacheWrite",
                "gen_ai.client.inference.usage.reasoning.output_tokens", "reasoning");
        long input;
        long output;
        long cacheRead;
        long cacheWrite;
        long reasoning;

        void add(String kind, long value) {
            switch (kind) {
                case "input" -> input += value;
                case "output" -> output += value;
                case "cacheRead" -> cacheRead += value;
                case "cacheWrite" -> cacheWrite += value;
                case "reasoning" -> reasoning += value;
                default -> throw new IllegalArgumentException("Unknown token metric " + kind);
            }
        }

        void add(TokenTotals other) {
            input += other.input;
            output += other.output;
            cacheRead += other.cacheRead;
            cacheWrite += other.cacheWrite;
            reasoning += other.reasoning;
        }

        ObjectNode toJson() {
            ObjectNode value = JSON.createObjectNode();
            value.set("input", metric(input, "measured",
                    "Last cumulative OTEL export per metric attribute set per file."));
            value.set("output", metric(output, "measured",
                    "Last cumulative OTEL export per metric attribute set per file."));
            value.set("cacheRead", metric(cacheRead, "measured",
                    "Last cumulative OTEL export per metric attribute set per file."));
            value.set("cacheWrite", metric(cacheWrite, "measured",
                    "Last cumulative OTEL export per metric attribute set per file."));
            value.set("reasoning", metric(reasoning, "measured",
                    "Last cumulative OTEL export per metric attribute set per file."));
            value.set("uncachedInput", metric(input - cacheRead - cacheWrite, "derived",
                    "Input tokens include cache-read and cache-write tokens; this subtracts both."));
            value.put("inputIncludesCacheTokens", true);
            return value;
        }
    }

    private static final class CpdResult {
        final boolean available;
        final int productionBlocks;
        final int testBlocks;
        final int duplicatedLines;
        final List<ObjectNode> locations;
        final String reason;

        private CpdResult(
                boolean available, int productionBlocks, int testBlocks,
                int duplicatedLines, List<ObjectNode> locations, String reason) {
            this.available = available;
            this.productionBlocks = productionBlocks;
            this.testBlocks = testBlocks;
            this.duplicatedLines = duplicatedLines;
            this.locations = locations;
            this.reason = reason;
        }

        static CpdResult available(
                int production, int tests, int lines, List<ObjectNode> locations) {
            return new CpdResult(true, production, tests, lines, locations, null);
        }

        static CpdResult unavailable(String reason) {
            return new CpdResult(false, 0, 0, 0, List.of(), reason);
        }

        ObjectNode toJson() {
            ObjectNode value = JSON.createObjectNode();
            value.put("availability", available ? "measured" : "unavailable");
            if (available) {
                value.put("productionBlocks", productionBlocks);
                value.put("testBlocks", testBlocks);
                value.put("duplicatedLines", duplicatedLines);
                value.set("locations", array(locations));
            } else {
                value.put("reason", reason);
                value.putNull("productionBlocks");
                value.putNull("testBlocks");
                value.putNull("duplicatedLines");
            }
            return value;
        }
    }

    private static final class BuildWarnings {
        final boolean available;
        final Set<String> warnings;
        final int exitCode;
        final String outputExcerpt;

        BuildWarnings(
                boolean available, Set<String> warnings,
                int exitCode, String outputExcerpt) {
            this.available = available;
            this.warnings = warnings;
            this.exitCode = exitCode;
            this.outputExcerpt = outputExcerpt;
        }

        ObjectNode toJson() {
            ObjectNode value = JSON.createObjectNode();
            value.put("availability", available ? "measured" : "unavailable");
            value.put("exitCode", exitCode);
            ArrayNode values = value.putArray("warnings");
            warnings.forEach(values::add);
            value.put("outputExcerpt", outputExcerpt);
            return value;
        }
    }
}
