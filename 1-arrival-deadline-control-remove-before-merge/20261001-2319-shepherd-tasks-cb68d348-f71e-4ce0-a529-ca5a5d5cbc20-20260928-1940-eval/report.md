# Shepherd Campaign Evaluation

- **Arm:** `control`
- **Campaign:** `cb68d348-f71e-4ce0-a529-ca5a5d5cbc20`
- **Evaluator:** `0.3.1` at `6d85fabeddd0dd834345781a6446182af5ee8d74`
- **Evaluator worktree dirty:** false
- **Generated:** 2026-10-01T22:11:54.682722Z

## Headline findings

| Task | PR | First product-defect detection | Product defects | Nonzero exits | CCRA rounds | CCRA comments | Flaky tests |
|---:|---:|---|---:|---:|---:|---:|---:|
| 2 | 7 | none detected | 0 | 3 | 1 | 0 | 0 |
| 3 | 8 | stage_30_gate | 1 | 1 | 1 | 0 | 0 |
| 4 | 9 | none detected | 0 | 3 | 1 | 0 | 0 |
| 5 | 10 | ccra_review | 2 | 5 | 2 | 2 | 0 |
| 6 | 11 | none detected | 0 | 6 | 1 | 0 | 0 |

## Cost and timing

- Campaign wall clock: 2h 34m 00s
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
