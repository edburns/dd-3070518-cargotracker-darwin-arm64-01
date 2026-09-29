#!/usr/bin/env bash
set -euo pipefail
timestamp=20260928-1936
log_dir_full=/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936
session_share_path="$log_dir_full/create-issues-session-$timestamp.md"
session_jsonl_path="$log_dir_full/create-issues-session-$timestamp.jsonl"
session_otel_path="$log_dir_full/create-issues-otel-$timestamp.jsonl"
prompt_file=/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/20260928-1936-invoke-shepherd-task-20-create-issues-from-plan-skill.md
prompt=$(cat "$prompt_file")
echo "[shepherd-task] Logging create-issues run to: $log_dir_full"
export COPILOT_OTEL_FILE_EXPORTER_PATH="$session_otel_path"
set +e
printf '%s' "$prompt" | copilot --yolo --output-format json --share "$session_share_path" | "/Users/edburns/.copilot/plugins/shepherd-task/scripts/redact-secrets.sh" - > "$session_jsonl_path"
pipeline_status=("${PIPESTATUS[@]}")
copilot_exit=${pipeline_status[1]}
redact_exit=${pipeline_status[2]}
set -e
"/Users/edburns/.copilot/plugins/shepherd-task/scripts/redact-secrets.sh" "$log_dir_full" >/dev/null
unset COPILOT_OTEL_FILE_EXPORTER_PATH
if [[ $copilot_exit -ne 0 || $redact_exit -ne 0 ]]; then echo "[shepherd-task] FAILED: copilot or redaction exited with code $copilot_exit/$redact_exit" >&2; exit 1; fi
"/Users/edburns/.copilot/plugins/shepherd-task/scripts/assert-stage20-result.sh" "$log_dir_full/stage-20-result.json" 5
echo "[shepherd-task] Create-issues session complete."
