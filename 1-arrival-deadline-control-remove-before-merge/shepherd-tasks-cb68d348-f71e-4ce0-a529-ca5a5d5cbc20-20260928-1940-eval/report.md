# Shepherd Campaign Evaluation

- **Arm:** `control`
- **Campaign:** `cb68d348-f71e-4ce0-a529-ca5a5d5cbc20`
- **Evaluator:** `0.4.3` at `499d2b0d8cb98aa27215ebec6ef41572b6ed1368`
- **Evaluator worktree dirty:** false
- **Generated:** 2026-10-02T23:13:06.284122Z

## Headline findings

| Task | PR | Attempts/outcomes | First product-defect detection | Product defects | Nonzero exits | CCRA rounds | CCRA comments | Flaky tests |
|---:|---:|---|---|---:|---:|---:|---:|---:|
| 2 | 7 | 1:completed | none detected | 0 | 3 | 1 | 0 | 0 |
| 3 | 8 | 1:completed | stage_30_gate | 1 | 1 | 1 | 0 | 0 |
| 4 | 9 | 1:completed | none detected | 0 | 3 | 1 | 0 | 0 |
| 5 | 10 | 1:completed | ccra_review | 2 | 5 | 2 | 2 | 0 |
| 6 | 11 | 1:completed | none detected | 0 | 6 | 1 | 0 | 0 |

## Cost and timing

- Campaign active time: 2h 34m 00s
- Inter-directory gap: 0h 00m 00s
- First-start to last-end span: 2h 34m 00s (not campaign active time)
- Recorded session time: 2h 10m 06s
- JSONL exact session time: 7809160 ms (3160 ms above second-truncated Markdown headers)
- Orchestration overhead: 0h 23m 54s
- CCA wait proxy: 17 polls / 0h 49m 18s elapsed; 0h 51m 00s configured ceiling
- AIU: 786.75444
- Premium requests: 10

## Evidence and run invariants

- CI tests run: 0 (`measured`)
- Partial output: 1692406 Unicode code points / 1692410 UTF-16 code units
- Skill content verification: `unverified`; telemetry hashes identify skill names, not content

## Acceptance checks

| Check | Status | Observed |
|---|---|---|
| maven_project_root_recorded | **pass** | `{"9deec1b5cbdf1666df1323473b3ca0f06ca2cfce":".","4cd369908f089830bbab701632d321f0b122aa43":".","702ee54ca1fa603560d54b86e29b654151869a77":".","db3827c4c49282e36ed3a56970fb2052f0dfe695":".","b58e11722a0ca422d35adec70c9f4bd54041c85f":".","d2d6cb14480ddbf48f38f19bfd4eaf5e292fafbf":".","a56afb39ab5771afd255b8962ea4e10a39af0347":"."}` |
| cross_repo_start_equivalence | **pass** | `` |
| ci_and_build_gates_classified | **pass** | `{"formatting":{"status":"not_present","entryCount":0},"build_contract":{"status":"not_present","entryCount":0},"static_analysis":{"status":"not_present","entryCount":0},"compiler":{"status":"not_present","entryCount":0},"unit_tests":{"status":"not_present","entryCount":0},"container_tests":{"status":"not_present","entryCount":0},"ci_other":{"status":"present","entryCount":3}}` |
| product_defect_gate_and_class_non_null | **pass** | `true` |
| defect_class_subtype_consistent | **pass** | `true` |
| guardrail_failures_not_unclassified | **pass** | `[]` |
| ci_test_log_availability_semantics | **pass** | `{"availability":"measured","testsExecuted":false,"testsRun":0,"source":"CI transcript log contains 'Tests are skipped.'"}` |
| combined_attempts_preserved | **pass** | `["/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/shepherd-tasks-cb68d348-f71e-4ce0-a529-ca5a5d5cbc20-20260928-1940"]` |
| combined_active_time_separates_gap | **pass** | `{"arm":"control","campaignId":"cb68d348-f71e-4ce0-a529-ca5a5d5cbc20","campaignDirectory":"/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/shepherd-tasks-cb68d348-f71e-4ce0-a529-ca5a5d5cbc20-20260928-1940","campaignDirectories":["/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/shepherd-tasks-cb68d348-f71e-4ce0-a529-ca5a5d5cbc20-20260928-
…` |

## CI gate inventory

| Gate | Status | Entries |
|---|---|---:|
| formatting | not_present | 0 |
| build_contract | not_present | 0 |
| static_analysis | not_present | 0 |
| compiler | not_present | 0 |
| unit_tests | not_present | 0 |
| container_tests | not_present | 0 |
| ci_other | present | 3 |

Named steps requiring `ci_other` review:

- `Build Cargo Tracker with Open Liberty`

## Start equivalence and confounds

Cross-repository start comparison was not requested.

## Defect interpretation

See `defects.csv` for one row per deduplicated defect, including defect class, item count, local/CI location, timestamp source, ancestry-verified fix, and anomalies.

Style and static-analysis findings are detectable only where the corresponding gate exists; they are excluded from arm-comparison conclusions when either arm reports that gate as `not_present`.

## Post-mortem agent cost

- AIU: 109.9212
- Premium requests: 1
- Tokens: unavailable

## Reference reconciliation

| Metric | Expected | Actual | Match | Explanation |
|---|---:|---:|:---:|---|
| tasksMerged | 5 | 5 | yes | Resolved stage-40 PRs from transcripts. |
| wallClockSeconds | 9240 | 9240 | yes | Manifest completedAt minus startedAt. |
| recordedSessionSeconds | 7806 | 7806 | yes | Sum of ten transcript header durations. |
| sessions | 10 | 10 | yes | Phase task transcript count. |
| ccraRounds | 6 | 6 | yes | Unique completed Copilot review IDs. |
| ccraActionableComments | 2 | 2 | yes | Unique top-level comments in completed CCRA review batches. |
| stage30ChangeRequests | 1 | 1 | yes | Unique CHANGES_REQUESTED review objects in stage 30. |
| aiu | 786.754 | 786.754 | yes | Usage checkpoint total, rounded to three decimals. |
| premiumRequests | 10 | 10 | yes | Usage checkpoint totals. |
| inputTokens | 8143155 | 8143155 | yes | Last cumulative export per metric attribute set per file. |
| cacheReadTokens | 7588116 | 7588116 | yes | Last cumulative export per metric attribute set per file. |
| cacheWriteTokens | 554442 | 554442 | yes | Last cumulative export per metric attribute set per file. |
| outputTokens | 102885 | 102885 | yes | Last cumulative export per metric attribute set per file. |
| reasoningTokens | 18919 | 18919 | yes | Last cumulative export per metric attribute set per file. |
| nonzeroToolExits | 18 | 18 | yes | Nonzero transcript shell terminators. |

## Trust assessment

- **Trustworthy:** manifest timing, session counts, transcript durations, exact JSONL durations, nonzero exit counts, JSONL AIU/premium/model/tool counts, JSONL partial-output cross-checks, run invariants, and cumulative OTEL tokens.
- **Approximate:** command-to-head correlation when a transcript does not emit a full SHA, agent-action labels, rule-based defect deduplication, and CCA wait as a latency proxy.
- **Manual review:** all entries in `unclassified.md`, confirmed evidence gaps, and remote CCA/CCRA internal cost because those internals are absent.

## Experiment interpretation

- Detection stage should be presented per defect and descriptively; a small number of product defects per run does not support significance claims.
- Local AIU and tokens include Shepherd waiting/polling activity; CCA wait is reported separately because remote CCA internals are unavailable.
- Enabling tests in the treatment is a disclosed intervention, not evaluator-detected control-arm tampering.
- If the treatment raises the Java release level, disclose that it also removes the JDK-25/source-7 operational failure mode.
- Test-tampering metrics are comparable only within an arm where tests run by default; the control arm is `not_meaningful`, so this is not a between-arm tampering comparison.

## Remaining unclassified

- None.
