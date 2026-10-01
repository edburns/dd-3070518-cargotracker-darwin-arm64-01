# Shepherd Task Evaluator

`shepherd-task-evaluator` converts one local Shepherd campaign directory into
structured trace, convergence, cost, environment, and repository findings.

## Requirements

- macOS on Apple Silicon
- JBang
- Git
- Maven when repository or build analysis is requested
- JDK 17 at
  `/Library/Java/JavaVirtualMachines/microsoft-17.jdk/Contents/Home` for
  `--with-build`

## Run

```sh
./evaluate-campaign <campaign-dir> --arm <control|treatment> \
  [--repo <path>] [--out <dir>] [--with-build] [--allow-unversioned]
```

The default output directory is `<campaign-dir>-eval`.

```text
findings.json
summary.csv
defects.csv
report.md
unclassified.md
```

The launcher is POSIX shell and does not require Bash-specific arrays or
features. The campaign directory is read-only. Repository analysis creates
detached temporary Git worktrees and removes them after each operation.
Evaluator provenance is resolved from this launcher's source directory. Normal
runs require a semantic version and a 40-character Git commit, and outputs
record whether the evaluator worktree is dirty. `--allow-unversioned` is only
for explicit development runs and emits a report warning.

## Analysis behavior

- Phase 1 artifacts are Shepherd stage 30; phase 2 artifacts are stage 40.
- Markdown transcripts are the primary classification evidence. JSONL
  `tool.execution_partial_result` output is retained as an independent
  cross-check; completion `success` is never treated as the shell exit status.
- JSONL supplies exact millisecond session duration alongside the
  second-truncated Markdown duration, usage, model/tool calls, loaded skills,
  skill-name hashes, skill-content lengths, and reasoning-summary presence.
- Large-session-output markers are reported separately from confirmed evidence
  gaps. A marker is a gap only when neither Markdown nor JSONL partial output
  preserves equivalent evidence.
- OTEL token metrics use the final cumulative export for each metric and
  attribute set in each file.
- Post-mortem agent cost is excluded from campaign cost and reported
  separately.
- Product defects, agent operational errors, and infrastructure failures use
  deterministic rules. Each classification also records an orthogonal origin
  such as `shepherd_harness`, `local_environment`,
  `agent_tool_invocation`, `agent_authored_test_harness`, or `product_code`.
  Uncertain events are written to `unclassified.md`.
- Every event has an underlying-problem ID and normalized recurrence signature.
  Companion events emitted by one failed command share a problem ID.
- `defects.csv` is the concatenable headline dataset, with one row per
  deduplicated product defect and its first detection, fix, and timing.
- Defect timestamps prefer GitHub-reported times in command output, then
  correlated JSONL tool-execution times, and use transcript offsets only as a
  last resort. The selected source is recorded in `findings.json`.
- Unit-test and Arquillian/container-test detection stages remain distinct.
- A failed test that passes on an unchanged-head rerun is infrastructure
  flakiness. Detectable persisted-state contamination is separately labeled
  `leftover_state`.
- Remote coding-agent wait polls are correlated to their originating
  background shell. Poll count, elapsed transcript time, and the configured
  maximum wait ceiling are reported separately. Local runtime polls are
  excluded, and none of these values is presented as remote-agent AIU or token
  cost.
- `Running Copilot cloud agent` is excluded from substantive CI checks and
  recorded as remote-agent orchestration.
- Run invariants include Shepherd and Copilot CLI versions, model and reasoning
  level, loaded-skill list, skill-name hashes, and observed content lengths.
  `skillNameHash` does not verify skill content.
- Test-integrity changes are always measured from the selected arm's start
  SHA. A control baseline with tests skipped by default is labeled
  `not_meaningful`, not treated as tampering.
- PMD CPD uses a recorded minimum-token threshold of 100.
- `--with-build` compiles the start and final revisions with pinned JDK 17 and
  `-Xlint:deprecation,removal`.
- OTEL input tokens already include cache-read and cache-write tokens. The
  evaluator reports a derived uncached-input count instead of adding the three
  values together.

Metrics use explicit availability values:

```text
measured | derived | approximate | unavailable | not_applicable
```

An unavailable value is represented as JSON `null` with a reason; it is never
silently represented as zero.

## Skill-content verification

Before starting a counted campaign, hash the active Shepherd skill directories
and write `shepherd-task-skill-content-hashes.json` next to the campaign
manifest. For example:

```sh
find "$HOME/.copilot/skills" -type f -path '*/shepherd-task-*/*' -print0 |
  sort -z |
  xargs -0 shasum -a 256 > shepherd-task-skill-files.sha256
```

The JSON artifact should record the algorithm, capture time, and per-skill or
per-file hashes. The evaluator preserves the artifact under
`runInvariants.skillContentVerification`. If it is absent, as in the reference
campaign, content identity is reported as unverified; telemetry content lengths
remain available but are not treated as proof.

`summary.csv` contains separate event-count columns for every classification
origin so harness and local-environment events can be excluded when comparing
arms.
