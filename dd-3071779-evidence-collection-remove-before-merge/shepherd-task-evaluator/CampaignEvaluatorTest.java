///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+
//SOURCES CampaignEvaluator.java
//DEPS com.fasterxml.jackson.core:jackson-databind:2.18.2
//DEPS com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class CampaignEvaluatorTest {
    private static int assertions;

    public static void main(String[] args) {
        failingStepControlsGateAndClass();
        jobFallbackIsExplicit();
        contractStepsHaveSpecificSubtypes();
        additionalGatesAreMapped();
        scopeRequiresARecognizedHeading();
        System.out.println("CampaignEvaluatorTest: " + assertions + " assertions passed");
    }

    private static void failingStepControlsGateAndClass() {
        String output = "build\tRun unit tests\t2026-10-02T12:00:00Z "
                + "Tests run: 5, Failures: 1, Errors: 0, Skipped: 0\n"
                + "build\tWrite metadata\t2026-10-02T12:00:01Z "
                + "##[error]Process completed with exit code 1.";
        List<CampaignEvaluator.CiFailure> failures =
                CampaignEvaluator.failedCiFailures(output, true);
        equal(1, failures.size(), "one failed step");
        CampaignEvaluator.CiFailure failure = failures.get(0);
        equal("build", failure.job(), "job name");
        equal("Run unit tests", failure.step(), "step name");
        equal("step_log", failure.source(), "gate source");

        String gate = CampaignEvaluator.ciGate(failure.step());
        ObjectNode classification =
                CampaignEvaluator.ciStepClassification(failure.step());
        CampaignEvaluator.canonicalizeProductClassification(
                classification, gate, "failed_ci_check", output);
        equal("unit_tests", gate, "unit-test gate");
        equal("behavioral_defect", classification.path("subtype").asText(),
                "unit-test subtype");
        equal("behavioral", CampaignEvaluator.defectClass(
                classification.path("subtype").asText()), "unit-test class");
    }

    private static void jobFallbackIsExplicit() {
        List<CampaignEvaluator.CiFailure> failures =
                CampaignEvaluator.failedCiFailures(
                        "build\tfail\t51s\thttps://example.invalid", false);
        equal(1, failures.size(), "one fallback job");
        equal(null, failures.get(0).step(), "no inferred step");
        equal("job_fallback", failures.get(0).source(), "fallback source");
        equal("ci_other", CampaignEvaluator.ciGate("build"),
                "generic build job is not a build-contract step");
    }

    private static void contractStepsHaveSpecificSubtypes() {
        ObjectNode inventory =
                CampaignEvaluator.ciStepClassification("Write test inventory");
        equal("test_inventory", inventory.path("subtype").asText(),
                "inventory subtype");

        ObjectNode build = CampaignEvaluator.ciStepClassification(
                "Run ./scripts/ci/verify-build-contract.sh");
        equal("contract_violation", build.path("subtype").asText(),
                "build contract subtype");
        equal("build", build.path("contract").asText(), "build contract name");

        ObjectNode compatibility = CampaignEvaluator.ciStepClassification(
                "Run ./scripts/ci/verify-compatibility-contract.sh");
        equal("compatibility", compatibility.path("contract").asText(),
                "compatibility contract name");

        ObjectNode source = CampaignEvaluator.ciStepClassification(
                "Run ./scripts/ci/verify-source-gates.sh");
        equal("source_gates", source.path("contract").asText(),
                "source-gates contract name");
    }

    private static void additionalGatesAreMapped() {
        equal("container_tests", CampaignEvaluator.ciGate(
                "Run Open Liberty integration tests"), "Open Liberty gate");
        equal("security", CampaignEvaluator.ciGate(
                "Run dependency security and delta gate"), "security gate");
    }

    private static void scopeRequiresARecognizedHeading() {
        assertScope("## Issue completion gate: diff scope violation");
        assertScope("## Scope violation: unrelated files");
        assertScope("## Issue requirement failure: diff scope");
        assertScope("## Scope: generated wrapper files");

        CampaignEvaluator.Stage30Decision fallback =
                CampaignEvaluator.stage30Decision(
                        "## Missing focused test\nRemove unrelated wrapper changes.");
        equal(false, fallback.classified(), "free-text scope is uncertain");
        equal("free_text_fallback", fallback.source(), "free-text source");

        CampaignEvaluator.Stage30Decision completeness =
                CampaignEvaluator.stage30Decision(
                        "## Missing required runtime evidence\nAdd the acceptance results.");
        equal(true, completeness.classified(), "completeness is classified");
        equal("completeness_gap", completeness.subtype(), "completeness subtype");
    }

    private static void assertScope(String heading) {
        CampaignEvaluator.Stage30Decision decision =
                CampaignEvaluator.stage30Decision(heading + "\nDetails.");
        equal(true, decision.classified(), heading + " classified");
        equal("scope_violation", decision.subtype(), heading + " subtype");
        equal("stage30_heading", decision.source(), heading + " source");
    }

    private static void equal(Object expected, Object actual, String label) {
        assertions++;
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(
                    label + ": expected " + expected + ", got " + actual);
        }
    }
}
