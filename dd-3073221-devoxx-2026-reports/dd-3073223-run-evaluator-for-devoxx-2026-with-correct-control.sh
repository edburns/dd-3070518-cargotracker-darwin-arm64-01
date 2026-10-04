#!/usr/bin/env bash

set -euo pipefail

export IDEA_HOME="/Applications/IntelliJ IDEA CE.app/Contents/MacOS"
export APPCAT_HOME="/Users/edburns/.appcat"
export JAVA_HOME="/Library/Java/JavaVirtualMachines/microsoft-25.jdk/Contents/Home"
export ANT_HOME="${HOME}/Downloads/apache-ant-1.10.13"
export M2_HOME="${HOME}/Downloads/apache-maven-3.9.8"
export PATH="${APPCAT_HOME}:${M2_HOME}/bin:${ANT_HOME}/bin:${JAVA_HOME}/bin:${IDEA_HOME}:${PATH}"

WORKAREAS="/Users/edburns/workareas"
EVALUATOR_REPO="${WORKAREAS}/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control"
EVALUATOR="${EVALUATOR_REPO}/dd-3071779-evidence-collection-remove-before-merge/shepherd-task-evaluator/evaluate-campaign"
CONTROL_START="${WORKAREAS}/dd-3072797-tricked-out-cargotracker-run-04/1-arrival-deadline-control-remove-before-merge/dd-3072973-cargotracker-control-01@325b7e1"

banner() {
  printf '\n'
  printf '%s\n' '################################################################################'
  printf '### %-74s ###\n' "$1"
  printf '%s\n' '################################################################################'
  printf '\n'
}

section() {
  printf '\n'
  printf '%s\n' '================================================================================'
  printf '==  %s\n' "$1"
  printf '%s\n' '================================================================================'
}

banner 'TREATMENT RUNS'

section 'Campaign run 03: dd-3072707-tricket-out-cargotracker-run-03'

TREATMENT_03_REPO="${WORKAREAS}/dd-3072707-tricket-out-cargotracker-run-03"
TREATMENT_03_ROOT="${TREATMENT_03_REPO}/1-arrival-deadline-control-remove-before-merge"
TREATMENT_03_RUN_01="${TREATMENT_03_ROOT}/shepherd-tasks-a63d175f-0ce7-4430-850d-c134a707f88c-20261002-1414"
TREATMENT_03_RUN_02="${TREATMENT_03_ROOT}/shepherd-tasks-a63d175f-0ce7-4430-850d-c134a707f88c-20261002-2058"
TREATMENT_03_OUT="${TREATMENT_03_ROOT}/shepherd-tasks-a63d175f-0ce7-4430-850d-c134a707f88c-combined-eval"

printf '%s\n' '  run 01: shepherd-tasks-a63d175f-0ce7-4430-850d-c134a707f88c-20261002-1414'
printf '%s\n' '  run 02: shepherd-tasks-a63d175f-0ce7-4430-850d-c134a707f88c-20261002-2058'
printf '  output: %s\n\n' "${TREATMENT_03_OUT}"

"${EVALUATOR}" \
  --combine "${TREATMENT_03_RUN_01}" "${TREATMENT_03_RUN_02}" \
  --arm treatment \
  --repo "${TREATMENT_03_REPO}" \
  --control-start "${CONTROL_START}" \
  --with-build \
  --out "${TREATMENT_03_OUT}"

section 'Campaign run 04: dd-3072797-tricked-out-cargotracker-run-04'

TREATMENT_04_REPO="${WORKAREAS}/dd-3072797-tricked-out-cargotracker-run-04"
TREATMENT_04_ROOT="${TREATMENT_04_REPO}/1-arrival-deadline-control-remove-before-merge"
TREATMENT_04_RUN_01="${TREATMENT_04_ROOT}/shepherd-tasks-26cfa4aa-1dc7-46f3-a995-310bc2c5f60b-20261002-1502"
TREATMENT_04_RUN_02="${TREATMENT_04_ROOT}/shepherd-tasks-26cfa4aa-1dc7-46f3-a995-310bc2c5f60b-20261002-2042"
TREATMENT_04_OUT="${TREATMENT_04_ROOT}/shepherd-tasks-26cfa4aa-1dc7-46f3-a995-310bc2c5f60b-combined-eval"

printf '%s\n' '  run 01: shepherd-tasks-26cfa4aa-1dc7-46f3-a995-310bc2c5f60b-20261002-1502'
printf '%s\n' '  run 02: shepherd-tasks-26cfa4aa-1dc7-46f3-a995-310bc2c5f60b-20261002-2042'
printf '  output: %s\n\n' "${TREATMENT_04_OUT}"

"${EVALUATOR}" \
  --combine "${TREATMENT_04_RUN_01}" "${TREATMENT_04_RUN_02}" \
  --arm treatment \
  --repo "${TREATMENT_04_REPO}" \
  --control-start "${CONTROL_START}" \
  --with-build \
  --out "${TREATMENT_04_OUT}"

banner 'CONTROL RUNS'

section 'Campaign run 01: dd-3072973-cargotracker-control-01'

CONTROL_01_REPO="${WORKAREAS}/dd-3072797-tricked-out-cargotracker-run-04/1-arrival-deadline-control-remove-before-merge/dd-3072973-cargotracker-control-01"
CONTROL_01_ROOT="${CONTROL_01_REPO}/1-arrival-deadline-control-remove-before-merge"
CONTROL_01_RUN_01="${CONTROL_01_ROOT}/shepherd-tasks-ce607171-1d05-40bd-b1ca-65fd9ffb27df-20261003-0042"
CONTROL_01_OUT="${CONTROL_01_RUN_01}-eval"

printf '%s\n' '  run 01: shepherd-tasks-ce607171-1d05-40bd-b1ca-65fd9ffb27df-20261003-0042'
printf '  output: %s\n\n' "${CONTROL_01_OUT}"

"${EVALUATOR}" \
  "${CONTROL_01_RUN_01}" \
  --arm control \
  --repo "${CONTROL_01_REPO}" \
  --with-build \
  --out "${CONTROL_01_OUT}"

banner 'ALL CAMPAIGN REPORTS COMPLETED'

printf '%s\n' 'Generated reports:'
printf '  Treatment run 03: %s\n' "${TREATMENT_03_OUT}"
printf '  Treatment run 04: %s\n' "${TREATMENT_04_OUT}"
printf '  Control run 01:   %s\n' "${CONTROL_01_OUT}"
