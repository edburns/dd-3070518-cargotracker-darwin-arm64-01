# Copilot CLI Session

> [!NOTE]
> - **Session ID:** `d2f1472e-19f4-4218-a44d-72e9cbda0d3c`  
> - **Started:** 9/28/2026, 7:36:57 PM  
> - **Duration:** 2m 52s  
> - **Exported:** 9/28/2026, 7:39:49 PM  

---

<sub>5s</sub>

### User

Invoke skill `shepherd-task-20-create-issues-from-plan` with these inputs:

- CAMPAIGN_ID: cb68d348-f71e-4ce0-a529-ca5a5d5cbc20
- LESSON_PROPAGATION: off
- REPO: edburns/dd-3070518-cargotracker-darwin-arm64-01
- BASE_BRANCH: experiment/shepherd-control
- PARENT_ISSUE: 1
- PLAN_DIRECTORY: 1-arrival-deadline-control-remove-before-merge
- PLAN_FILE_NAME: add-change-arrival-deadline-feature-ignorance-reduction-plan.md
- QUESTIONS_SECTION: ## Phase 3 — Ignorance reduction: questions to answer before writing code
- IMPLEMENTATION_SECTION: ## Phase 4 — Implementation (five serial issues)
- EXPECTED_TASK_COUNT: 5
- BASE_REMOTE: origin
- LOG_DIRECTORY: /Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936
- DRAFT_VALIDATOR: /Users/edburns/.copilot/plugins/shepherd-task/scripts/validate-stage20-drafts.sh
- ISSUE_BODY_VERIFIER: /Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-github-issue-body.sh
- CHILD_LINK_VERIFIER: /Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-stage20-child-links.sh

Fixture pagination response contract (mandatory):

- `gh api ... --paginate --slurp` returns a JSON array of page payloads, so a
  one-page response has the shape `[[{...}]]`, not `[{...}]`.
- Before indexing child issue fields such as `.id`, normalize the response to
  one flat issue array exactly once.
- In Bash, use:
  `jq 'if length == 0 then [] elif all(.[]; type == "array") then add else . end'`.
- In PowerShell, capture the `gh` output and `$LASTEXITCODE` first, then pass
  the complete JSON through the same `jq` normalization before
  `ConvertFrom-Json`.
- Use the normalized flat array for the pre-creation baseline, final child
  count/order checks, and failure reconciliation. Do not apply `add` a second
  time to an already-flat array.

---

<sub>7s</sub>

### `skill`

**shepherd-task-20-create-issues-from-plan**

<details>
<summary>Arguments</summary>

```json
{
  "skill": "shepherd-task-20-create-issues-from-plan"
}
```

</details>

```
Skill "shepherd-task-20-create-issues-from-plan" loaded successfully. Follow the instructions in the skill context.
```


---

<sub>10s</sub>

### Copilot

I’m validating the campaign inputs, branch, plan structure, GitHub parent, and bundled drafting conventions before any repository mutation.

---

<sub>19s</sub>

### `view`

**view the file at /Users/edburns/.copilot/skills/shepherd-task-20-create-issues-from-plan/examples/01-1682-java-tool-ergonomics.md.**

/Users/edburns/.copilot/skills/shepherd-task-20-create-issues-from-plan/examples/01-1682-java-tool-ergonomics.md

<details>
<summary>15 lines</summary>

```
# Example 01: 1682-java-tool-ergonomics-prompts-remove-before-merge

Read `copilot-sdk/1682-java-tool-ergonomics-prompts-remove-before-merge/dd-3018003-ignorance-reduction-for-implementation-plan.md` (the plan) to get the context.

✅✅Pay very close attention to each **Resolution:** to the questions in `Phase 3 — Ignorance reduction: questions to answer before writing code`.

In this session, we are creating issues of type **Task** as children of Epic https://github.com/github/copilot-sdk/issues/1682 . These issues correspond to the sub-sections in `## Phase 4 — Implementation (the build order)`.

I want you to use `gh` to create issues as sub-issues of 1682.

- I will dispatch these issues by assigning them to an agent. Leave them unassigned for now.

- The description you put in the issues you create is the specification for the work the agent assigned to the issue must do. Therefore, you need to be very specific and targeted when writing the description.

- Set the type to **Task**.

- Each issue will be worked on and resolved in serial. You can assume:
  - All issues will operate in this topic branch of `upstream`: `edburns/1682-java-tool-ergonomics`.

  - The assignee agent has access to the topic branch and, most importantly, to the prompts in the directory `1682-java-tool-ergonomics-prompts-remove-before-merge`, which includes the plan. Therefore, you can instruct the agent to access the plan by reference, you don't need to reference it by value directly in the issue.

- The sub-sections of `## Phase 4 — Implementation (the build order)` have essential details for each corresponding **Task**.

- Instruct the agent to read the entire plan before working.

- Instruct the agent to carefully re-read the relevant sections of the plan. ❌❌You can't just say, "read the relevant sections of the plan". ✅✅In the description of the issue, list exactly what sections of the plan apply to this particular issue.
- Though the sub-sections of `## Phase 4 — Implementation (the build order)` have essential details for each corresponding **Task**, I want you to carefully consider if there are additional gating tests you can add so that as the agent proceeds from one task to the next, the likelihood of rework is reduced.
```

</details>


---

<sub>19s</sub>

### `view`

**view the file at /Users/edburns/.copilot/skills/shepherd-task-20-create-issues-from-plan/examples/02-1810-java-tool-as-lambda.md.**

/Users/edburns/.copilot/skills/shepherd-task-20-create-issues-from-plan/examples/02-1810-java-tool-as-lambda.md

<details>
<summary>12 lines</summary>

```
# Example 02: 1810-java-tool-ergonomics-tool-as-lambda-remove-before-merge

Carefully read the plan `copilot-sdk/1810-java-tool-ergonomics-tool-as-lambda-remove-before-merge/1810-ignorance-reduction-for-implementation-plan.md`.

We have completed Phases 1 - 3. We are now on Phase 4.

For each sub item in Phase 4, file a child issue of Feature #1810.

Use `gh` to look at Epic #1682 and its child issues for the standard of verbosity and concreteness for issue descriptions.

The issue should be assignable to a coding agent and there must be sufficient detail in the description for the agent to proceed to completion.

Pull out the specific relevant content from Phase 3 as it applies to this current issue in Phase 4.

Tell the agent to take `edburns/1810-java-tool-ergonomics-tool-as-lambda` as the base branch. When I assign the issue to the coding agent, I will set this as the base branch.

Tell the agent to read `1810-java-tool-ergonomics-tool-as-lambda-remove-before-merge/1810-ignorance-reduction-for-implementation-plan.md`, which is present on the branch, for context.

Tell the agent which sub-item within Phase 4 they are solving.

Tell the agent that each issue will be assigned, resolved and merged to the base branch in the order in which they appear in the plan.

No work is to start until I assign each issue to the coding agent, in turn.
```

</details>


---

<sub>19s</sub>

### `view`

**view the file at /Users/edburns/.copilot/skills/shepherd-task-20-create-issues-from-plan/examples/03-dd-3017826-java-real-estate-demo.md.**

/Users/edburns/.copilot/skills/shepherd-task-20-create-issues-from-plan/examples/03-dd-3017826-java-real-estate-demo.md

<details>
<summary>14 lines</summary>

```
# Example 03: dd-3017826-java-real-estate-demo-remove-before-merge

Read `dd-3017826-java-real-estate-demo-remove-before-merge\dd-3017826-java-real-estate-demo-ignorance-reduction-plan.md` (the plan) to get the context.

✅✅Pay very close attention to each **Resolution:** to the questions in `Phase 2 — Ignorance reduction: questions to answer before writing code`.

In this session, we are creating sub-issues as children of https://github.com/edburns/Build26-BRK206-your-agent-anywhere-multiclient-multidevice-with-github-copilot-sdk/issues/2 . These issues correspond to the sub-sections in `## Phase 3 — Implementation (build order)`.

I want you to use `gh` to create issues as sub-issues of #2.

- I will dispatch these issues by assigning them to an agent. Leave them unassigned for now.

- The description you put in the issues you create is the specification for the work the agent assigned to the issue must do. Therefore, you need to be very specific and targeted when writing the description.

- Each issue will be worked on and resolved in serial. You can assume:
  - All issues will operate in this topic branch of `origin`: `edburns/2-build-out-demo`.

  - The assignee agent has access to the topic branch and, most importantly, to the prompts in the directory `dd-3017826-java-real-estate-demo-remove-before-merge`, which includes the plan. Therefore, you can instruct the agent to access the plan by reference, you don't need to reference it by value directly in the issue.

- The sub-sections of `## Phase 3 — Implementation (build order)` have essential details for each corresponding sub-issue.

- Instruct the agent to read the entire plan before working.

- Instruct the agent to carefully re-read the relevant sections of the plan. ❌❌You can't just say, "read the relevant sections of the plan". ✅✅In the description of the issue, list exactly what sections of the plan apply to this particular issue.
- Though the sub-sections of `## Phase 3 — Implementation (build order)` have essential details for each corresponding sub-issue, I want you to carefully consider if there are additional gating tests you can add so that as the agent proceeds from one task to the next, the likelihood of rework is reduced.
```

</details>


---

<sub>19s</sub>

### `view`

**view the file at /Users/edburns/.copilot/skills/shepherd-task-20-create-issues-from-plan/examples/04-28-python-agent-demo.md.**

/Users/edburns/.copilot/skills/shepherd-task-20-create-issues-from-plan/examples/04-28-python-agent-demo.md

<details>
<summary>16 lines</summary>

```
# Example 04: 28-python-agent-demo-remove-before-merge

Read `28-python-agent-demo-remove-before-merge/28-python-agent-demo-ignorance-reduction-plan.md` (the plan) to get the context.

✅✅Pay very close attention to each **Resolution:** to the questions in `Phase 2 — Ignorance reduction: questions to answer before writing code`. These pertain to the implementation tasks in Phase 3. ✅✅✅ Include relevant spike findings as self-contained prose in the issue description; do not direct the agent to spike directories or source files.

In this session, we are creating sub-issues as children of https://github.com/edburns/Build26-BRK206-your-agent-anywhere-multiclient-multidevice-with-github-copilot-sdk/issues/28 . These issues correspond to the sub-sections in `## Phase 3 — Implementation (build order)`.

I want you to use `gh` to create issues as sub-issues of #28

- I will dispatch these issues by assigning them to an agent. Leave them unassigned for now.

- The description you put in the issues you create is the specification for the work the agent assigned to the issue must do. Therefore, you need to be very specific and targeted when writing the description.

- Again, ✅✅For each **Resolution:** to the questions in `Phase 2 — Ignorance reduction: questions to answer before writing code`, include the relevant Resolution values in the issue description. These pertain to the implementation tasks in Phase 3. ✅✅✅ State relevant spike findings directly in the issue body with enough detail to implement them without reading spike code.

- For tasks dealing with the UI, make sure to refer to the relevant screengrabs, but only ones within `28-python-agent-demo-remove-before-merge/`.

- Each issue will be worked on and resolved in serial. You can assume:
  - All issues will operate in this topic branch of `origin`: `edburns/28-python-agent-demo`.

  - The assignee agent has access to the topic branch and, most importantly, to the prompts in the directory `28-python-agent-demo-remove-before-merge/`, which includes the plan. Therefore, you can instruct the agent to access the plan by reference, you don't need to reference it by value directly in the issue.

- The sub-sections of `## Phase 3 — Implementation (build order)` have essential details for each corresponding sub-issue.

- Instruct the agent to read the entire plan before working.

- Instruct the agent to carefully re-read the relevant sections of the plan. ❌❌You can't just say, "read the relevant sections of the plan". ✅✅In the description of the issue, list exactly what sections of the plan apply to this particular issue.
- Though the sub-sections of `## Phase 3 — Implementation (build order)` have essential details for each corresponding sub-issue, I want you to carefully consider if there are additional gating tests you can add so that as the agent proceeds from one task to the next, the likelihood of rework is reduced.
```

</details>


---

<sub>19s</sub>

### `view`

**view the file at /Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/add-change-arrival-deadline-feature-ignorance-reduction-plan.md.**

/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/add-change-arrival-deadline-feature-ignorance-reduction-plan.md

<details>
<summary>679 lines</summary>

````
# Implementation plan: Change Arrival Deadline Date (`eclipse-ee4j/cargotracker#64`)

Human DRI: Ed Burns
Starting commit: `9b9f311b2a3a2854bdac947593950d9edb6bca7d` (`Make the system ready for implementation`)
Working directory: repository root of the current campaign worktree
Runtime baseline: Java 17, Java EE 7 (`javax.*`), Open Liberty 26.0.0.8, PrimeFaces 8.0
Baseline run instructions: `README.md`
Baseline preparation: fixed source branch and immutable SHA validated by the campaign fixture
Historical issue: `eclipse-ee4j/cargotracker#64`

Related directories and files:

- `src/main/java/org/eclipse/cargotracker/application/`
- `src/main/java/org/eclipse/cargotracker/interfaces/booking/facade/`
- `src/main/java/org/eclipse/cargotracker/interfaces/booking/web/`
- `src/main/webapp/admin/dialogs/`
- `src/main/webapp/admin/tables/listNotRouted.xhtml`
- `src/test/java/org/eclipse/cargotracker/application/BookingServiceTest.java`

---

## Goal

Add an Administration dashboard operation that lets a shipping administrator
change the arrival deadline of a cargo listed in the **Not Routed Cargo** table.
The operation must preserve Cargo Tracker's layered architecture:

1. The application service owns the domain mutation.
2. The booking facade shields the web layer from domain types.
3. A JSF backing bean loads and submits the editable date.
4. A PrimeFaces dynamic dialog presents the editor.
5. The existing Not Routed Cargo table opens the dialog and refreshes after a
   successful update.

### User-visible acceptance behavior

Using the stable sample cargo `DEF789`:

1. Start the application with Java 17:

   ```bash
   ./mvnw clean package -Popenliberty liberty:run
   ```

2. Open `http://localhost:8080/cargo-tracker/`.
3. Select **Administration**.
4. Find `DEF789` in the **Not Routed Cargo** table.
5. The Deadline cell displays its date together with an edit icon.
6. Hovering over the deadline displays:
   `Click to change cargo arrival deadline date.`
7. Selecting the deadline opens a modal dialog titled **Change Deadline**.
8. The dialog displays the cargo's origin and destination as read-only
   context.
9. The date editor is initialized to the cargo's current arrival deadline.
10. Selecting a different date and pressing **Update** closes the dialog and
    refreshes the Administration view.
11. The new date is shown in the Not Routed Cargo table.
12. Reloading the page continues to show the new date for the lifetime of the
    running in-memory sample application.
13. Pressing **Cancel** closes the dialog without changing the deadline.

### Domain acceptance behavior

Changing the deadline must:

- locate the cargo by `TrackingId`;
- preserve its existing origin;
- preserve its existing destination;
- replace only the arrival deadline in its `RouteSpecification`;
- apply the specification through `Cargo.specifyNewRoute(...)`;
- preserve the currently assigned itinerary rather than silently discarding
  it;
- allow the domain model to recalculate routing status and delivery-derived
  values against the new route specification;
- persist the changed cargo through `CargoRepository.store(...)`.

### Hard scope constraints

- Begin from commit `9b9f311b2a3a2854bdac947593950d9edb6bca7d`.
- Preserve Java EE 7 and the `javax.*` namespace.
- Preserve the Java 7 source/target level used by this historical codebase.
- Run the application on JDK 17 using the existing Open Liberty profile.
- Do not migrate the application to Jakarta EE 8+, Jakarta EE 9+, Spring, or a
  different UI framework.
- Do not replace the in-memory Derby configuration or the Open Liberty runtime.
- Do not redesign unrelated cargo booking, routing, destination editing,
  messaging, batch, REST, or persistence behavior.
- Do not copy commits or files from feature-bearing branches. This plan is the
  implementation specification.
- Implement the five build issues below in order. Each issue must be complete
  and gated before the next issue begins.

---

## Completed phases

### Phase 1 ✅ — Establish a runnable feature-absent baseline

- Commit `9b9f311b2a3a2854bdac947593950d9edb6bca7d` is based on the historical
  feature-absent commit and contains only the compatibility work needed to run
  the sample on JDK 17 and Open Liberty.
- `./mvnw clean package -Popenliberty liberty:run` starts the application.
- The home page and Administration flows return HTTP 200.
- JSF view metadata is placed at `UIViewRoot` scope for MyFaces compatibility.
- The internal routing REST client works without a Jersey/MOXy classloading
  conflict.
- The scheduled batch job has the local authorization it needs.

### Phase 2 ✅ — Verify the before and after user experience

- Before implementation, `DEF789` appears in the Not Routed Cargo table with a
  plain-text deadline and no edit operation.
- The neighboring Destination column demonstrates the existing PrimeFaces
  dynamic-dialog interaction pattern.
- The desired after behavior has been manually exercised: open the deadline
  editor, choose a new date, update, refresh the table, and observe the
  persisted value.
- The historical architectural boundaries and affected files have been
  identified.

---

## Phase 3 — Ignorance reduction: questions to answer before writing code

Resolve these questions before production implementation begins. The
recommendations intentionally define the desired design closely enough that an
implementing agent should not need to invent a different architecture.

### 3.1 — Which cargos expose the edit operation?

**Question:** Should deadline editing be exposed for all cargos or only for
cargos displayed in the Not Routed Cargo table?

The requested feature originates in the Administration dashboard's Not Routed
Cargo table. Other tables represent routed, misrouted, claimed, or otherwise
progressed cargo. Adding the affordance to every table would expand the feature
and require additional business rules about changing deadlines after handling
has begun.

| Option | UI scope | Trade-off |
|--------|----------|-----------|
| A | Not Routed Cargo table only | Matches the requested feature and the established destination-edit affordance. |
| B | Every Administration cargo table | Broader capability, but introduces lifecycle and authorization questions outside the request. |
| C | Cargo details page only | Avoids table complexity but does not meet the requested dashboard interaction. |

The application-service operation itself does not need to encode a UI-table
restriction. It should accept a tracking ID and apply the domain mutation to
the located cargo. The presentation layer determines where the operation is
offered.

**Recommendation:** Option A. Add the edit affordance only to
`src/main/webapp/admin/tables/listNotRouted.xhtml`. Keep the application
operation generally usable for a valid cargo.

**Resolution:**

Select Option A. Expose the edit affordance only in
`src/main/webapp/admin/tables/listNotRouted.xhtml`. The application and facade
operations remain generally callable for any cargo that can be found by
tracking ID; they do not encode knowledge of dashboard table membership.

### 3.2 — What is the exact domain mutation?

**Question:** Should the feature mutate the existing `RouteSpecification`, add
a setter to `Cargo`, or replace the specification using the existing domain
operation?

`RouteSpecification` is a value object describing origin, destination, and
arrival deadline. The existing `changeDestination(...)` implementation already
establishes the correct pattern: create a replacement specification, call
`Cargo.specifyNewRoute(...)`, and store the aggregate.

Proposed application-service shape:

```java
void changeDeadline(TrackingId trackingId, Date deadline);
```

Proposed implementation:

```java
Cargo cargo = cargoRepository.find(trackingId);
RouteSpecification specification = new RouteSpecification(
        cargo.getOrigin(),
        cargo.getRouteSpecification().getDestination(),
        deadline);

cargo.specifyNewRoute(specification);
cargoRepository.store(cargo);
```

Calling `specifyNewRoute(...)` is significant. It lets the aggregate recalculate
delivery and routing status relative to the new specification. Direct field
mutation or a persistence-only update would bypass that behavior.

**Recommendation:** Replace the `RouteSpecification` through
`Cargo.specifyNewRoute(...)`. Preserve origin, destination, and itinerary.
Persist using the existing repository. Do not add a deadline setter to the
domain model.

**Resolution:**

Use the same aggregate-update pattern as `changeDestination(...)`. Add
`BookingService.changeDeadline(TrackingId, Date)` and implement it by loading
the cargo, constructing a new `RouteSpecification` from the existing origin,
existing destination, and supplied deadline, calling
`cargo.specifyNewRoute(...)`, and storing the cargo through
`cargoRepository.store(...)`. Do not add mutable deadline setters to the domain
objects.

### 3.3 — What should happen to an existing itinerary and delivery state?

**Question:** When a routed cargo's deadline changes, should its itinerary be
cleared, retained, or recomputed?

Although the UI initially exposes the feature only for unrouted cargo, the
application operation should have deterministic domain behavior if invoked for
a routed cargo. The existing `changeDestination(...)` behavior preserves the
assigned itinerary and lets `Cargo.specifyNewRoute(...)` recalculate whether
that itinerary still satisfies the new specification.

The core application test should deliberately invoke the operation after:

1. booking a cargo;
2. requesting route candidates;
3. assigning an itinerary;
4. changing its destination;
5. changing its deadline.

This sequence verifies that the feature uses the aggregate correctly rather
than assuming the cargo always has an empty itinerary.

**Recommendation:** Preserve the itinerary. Let the domain model recompute
routing and delivery-derived state. Assert all unaffected fields explicitly in
`BookingServiceTest`.

**Resolution:**

Retain the existing itinerary. Do not clear, replace, or reroute it as part of
the deadline change. `Cargo.specifyNewRoute(...)` recalculates the delivery
snapshot and routing status against the replacement specification. In the
established sequential application test, the assigned itinerary remains
unchanged and the cargo remains `MISROUTED` after the deadline changes.

### 3.4 — What type crosses the facade boundary?

**Question:** Should the booking facade accept a `Date`, a formatted string, or
a newly introduced request DTO?

The existing facade already uses `java.util.Date` for
`bookNewCargo(...)`. Introducing another representation for this one operation
would create unnecessary conversion code and depart from the historical
application style.

Proposed facade shape:

```java
void changeDeadline(String trackingId, Date arrivalDeadline);
```

The implementation converts only the identifier:

```java
bookingService.changeDeadline(
        new TrackingId(trackingId),
        arrivalDeadline);
```

**Recommendation:** Use `String` for the tracking ID and `java.util.Date` for
the deadline. Do not expose `TrackingId`, `Cargo`, or `RouteSpecification` to
the JSF layer and do not introduce a new DTO solely for this command.

**Resolution:**

Add `void changeDeadline(String trackingId, Date arrivalDeadline)` to
`BookingServiceFacade`. `DefaultBookingServiceFacade` converts the string to
`new TrackingId(trackingId)` and passes the same `Date` to
`BookingService.changeDeadline(...)`. No new command DTO or formatted-string
service parameter is introduced.

### 3.5 — How is the DTO's formatted deadline converted for editing?

**Question:** `CargoRoute` exposes its deadline as formatted strings, while
`p:datePicker` binds naturally to `java.util.Date`. How should the backing bean
initialize the editor?

At the starting commit:

- `CargoRoute.getArrivalDeadline()` returns
  `MM/dd/yyyy hh:mm a z`.
- `CargoRoute.getArrivalDeadlineDate()` returns only the date component.
- The table displays `getArrivalDeadlineDate()`.

Options:

| Option | Approach | Trade-off |
|--------|----------|-----------|
| A | Parse `cargo.getArrivalDeadlineDate()` with `MM/dd/yyyy` | Small, localized change; preserves the existing DTO contract. |
| B | Add a `Date` property to `CargoRoute` | Cleaner typing, but broadens a DTO used throughout the application. |
| C | Reload the domain object in the backing bean | Violates the facade boundary. |

The formatter/parser must be created per operation or per view bean; do not add
a shared mutable `SimpleDateFormat`.

**Recommendation:** Option A. Load `CargoRoute` through
`BookingServiceFacade.loadCargoForRouting(trackingId)` and parse
`cargo.getArrivalDeadlineDate()` using `new SimpleDateFormat("MM/dd/yyyy")`.
Surface an explicit failure if the existing DTO value cannot be parsed; do not
silently submit a null date.

**Resolution:**

Use Option A and keep date conversion inside the view-scoped editor bean. The
existing implementation loads the `CargoRoute`, creates
`new SimpleDateFormat("MM/dd/yyyy")`, and parses the leading date portion of
`cargo.getArrivalDeadline()`. Because that value begins with `MM/dd/yyyy`,
`SimpleDateFormat.parse(...)` obtains the same date that
`getArrivalDeadlineDate()` displays. A per-load formatter is used, so no shared
mutable formatter is added.

### 3.6 — Which JSF bean scopes and interaction pattern should be used?

**Question:** Should deadline editing introduce a new navigation page, use an
inline editor, or mirror the existing Change Destination dynamic-dialog
pattern?

The baseline already contains:

- `ChangeDestination`, a CDI `@Named` and JSF `@ViewScoped` editor bean;
- `ChangeDestinationDialog`, a session-scoped JSF managed bean that opens and
  closes a PrimeFaces dynamic dialog;
- `changeDestination.xhtml`, a dialog view;
- a `dialogReturn` Ajax listener that refreshes `tableNotRouted`.

Using the same pattern minimizes changes and provides a consistent user
experience.

Proposed bean names:

```text
changeArrivalDeadlineDate
changeArrivalDeadlineDateDialog
```

**Recommendation:** Add a serializable CDI `@Named @ViewScoped`
`ChangeArrivalDeadlineDate` editor and a serializable
`@ManagedBean(name = "changeArrivalDeadlineDateDialog") @SessionScoped`
launcher. Mirror the existing destination-dialog lifecycle rather than
introducing a new navigation or inline-edit framework.

**Resolution:**

Mirror the existing Change Destination interaction. Implement
`ChangeArrivalDeadlineDate` as a serializable CDI `@Named @ViewScoped` bean and
`ChangeArrivalDeadlineDateDialog` as a serializable
`@ManagedBean(name = "changeArrivalDeadlineDateDialog") @SessionScoped` bean.
Use a PrimeFaces dynamic dialog rather than navigation to a full page or inline
cell editing.

### 3.7 — What is the dynamic-dialog contract?

**Question:** What path, request parameters, dimensions, and close result should
the PrimeFaces dialog use?

The launcher needs one parameter, `trackingId`, supplied as a
`Map<String, List<String>>`. The dialog metadata binds the parameter and invokes
the editor bean's `load()` action.

Proposed launcher contract:

```java
PrimeFaces.current().dialog().openDynamic(
        "/admin/dialogs/changeArrivalDeadlineDate.xhtml",
        options,
        params);
```

Required options:

| Option | Value |
|--------|-------|
| `modal` | `true` |
| `draggable` | `true` |
| `resizable` | `false` |
| `contentWidth` | `410` |
| `contentHeight` | `280` |

Required completion behavior:

- successful update: `closeDynamic("DONE")`;
- cancel: `closeDynamic("")`;
- caller listens for `dialogReturn` and updates `tableNotRouted`.

Because Open Liberty uses MyFaces, `<f:metadata>` must be a direct child of the
view root, before `<h:head>` and `<h:body>`. It must not be nested inside
`<h:body>`.

**Recommendation:** Use the contract above and preserve the metadata placement
required by the prepared baseline.

**Resolution:**

Open `/admin/dialogs/changeArrivalDeadlineDate.xhtml` with a single
`trackingId` request parameter and these options: modal and draggable are
`true`, resizable is `false`, content width is `410`, and content height is
`280`. Successful submission closes with `"DONE"`; cancellation closes with
the empty string. The caller handles `dialogReturn` and updates
`tableNotRouted`. Place the dialog's `<f:metadata>` directly under the root
`<html>` element, before `<h:head>` and `<h:body>`, so the known MyFaces
`UIViewRoot` requirement is satisfied.

### 3.8 — What date validation is required?

**Question:** Must the new deadline be non-null, in the future, after the
current date, or after itinerary completion?

The requested feature is an administrative correction to an existing arrival
deadline. No new domain policy about future dates is part of the request.
Inventing such a rule could reject dates accepted by existing cargo booking or
`RouteSpecification` behavior.

The UI must nevertheless prevent a null submission because the operation
requires a concrete replacement deadline.

**Recommendation:** Require a date value in the JSF form and display a normal
Faces validation message when it is absent. Do not add a new minimum-date,
future-date, or itinerary-date business rule. Continue to rely on the existing
domain model for its established invariants.

**Resolution:**

Require a non-null date selection, but add no new chronological business rule.
In particular, do not require the replacement deadline to be after today,
after the old deadline, or after every itinerary leg. Pass the selected
`java.util.Date` to the existing domain construction path and let the current
`RouteSpecification` invariants apply.

### 3.9 — How will the feature be tested on the prepared historical baseline?

**Question:** Which automated and runtime tests are mandatory, given that the
historical JUnit/Arquillian suite is configured for a remote Payara 4
container, while the prepared production baseline runs on JDK 17/Open Liberty?

The starting POM deliberately leaves `skipTests=true`. The Open Liberty profile
builds and compiles all test sources but does not provide a Liberty Arquillian
adapter. Modernizing the entire integration-test runtime is outside this
feature's scope.

The feature still needs layered evidence:

1. Extend `BookingServiceTest` with the domain/application assertions that
   specify the deadline mutation.
2. Ensure all test sources compile as part of
   `./mvnw clean package -Popenliberty`.
3. Add focused JUnit tests for facade and backing-bean delegation where they
   can run without a container, using hand-written fakes rather than adding a
   mocking framework.
4. Perform mandatory end-to-end verification against the running Open Liberty
   application.
5. Preserve the existing Payara Arquillian test path; do not delete, disable,
   or rewrite it to manufacture a passing result.

**Spike needed:** Before Issue 1 implementation, run the starting commit's
standard Open Liberty package command and record whether tests are compiled but
skipped. Confirm the new `BookingServiceTest` method can be added without
expanding the runtime modernization scope.

**Recommendation:** Treat the JDK 17/Open Liberty build plus HTTP/UI acceptance
as the mandatory executable gate. Keep the historical Arquillian test as a
precise application-layer specification and run it only when its documented
Payara environment is available.

**Resolution:**

Extend the existing sequential Arquillian `BookingServiceTest` with
`testChangeDeadline()` after `testChangeDestination()`. The test changes the
deadline by one month, reloads the cargo through JPA, and asserts the complete
set of preserved and recalculated domain state described above. The prepared
Open Liberty build compiles this test but retains the historical default
`skipTests=true`; executing that Arquillian suite still requires its documented
remote Payara environment. Therefore the mandatory executable gates are the
JDK 17 Open Liberty package/start command, direct HTTP checks, and the complete
`DEF789` browser acceptance flow. No Arquillian-runtime modernization or new
mocking dependency is part of this feature.

---

## Phase 4 — Implementation (five serial issues)

Implement these issues in order. Each issue should be a separate commit. Do not
start an issue until the previous issue's gating criteria are satisfied.

### 4.1 — Issue 1: Add the application-layer deadline change operation

**What to build**

Add the core use case to the application layer. This issue must contain no JSF
or PrimeFaces changes.

**Files to modify**

- `src/main/java/org/eclipse/cargotracker/application/BookingService.java`
- `src/main/java/org/eclipse/cargotracker/application/internal/DefaultBookingService.java`
- `src/test/java/org/eclipse/cargotracker/application/BookingServiceTest.java`

**Required API**

```java
void changeDeadline(TrackingId trackingId, Date deadline);
```

**Required implementation behavior**

1. Load the cargo using `cargoRepository.find(trackingId)`.
2. Obtain the current destination from
   `cargo.getRouteSpecification().getDestination()`.
3. Construct a replacement `RouteSpecification` from:
   - `cargo.getOrigin()`;
   - the current destination;
   - the new deadline.
4. Apply it using `cargo.specifyNewRoute(routeSpecification)`.
5. Persist using `cargoRepository.store(cargo)`.
6. Log the tracking ID and new deadline at `Level.INFO`, following the style of
   `changeDestination(...)`.

Do not:

- add a setter to `Cargo` or `RouteSpecification`;
- modify the origin or destination;
- clear or replace the itinerary directly;
- update persistence entities behind the aggregate's back.

**Tests to write first**

Append a sequential `testChangeDeadline()` case to `BookingServiceTest` after
`testChangeDestination()`. Build a new deadline one month after the test's
original `deadline`, invoke the service, reload the cargo with
`Cargo.findByTrackingId`, and assert:

- origin remains Chicago;
- destination remains Helsinki;
- stored deadline is the same calendar day as the requested new deadline;
- assigned itinerary remains unchanged;
- transport status remains `NOT_RECEIVED`;
- last known location remains `Location.UNKNOWN`;
- current voyage remains `Voyage.NONE`;
- cargo is not marked misdirected;
- estimated time of arrival is `Delivery.ETA_UNKOWN`;
- next expected activity is `Delivery.NO_ACTIVITY`;
- cargo is not unloaded at destination;
- routing status reflects the domain model's recalculation and remains
  `MISROUTED` for the established test sequence.

**Gating criteria**

- The test source compiles.
- `./mvnw clean package -Popenliberty` succeeds on JDK 17.
- No web, facade, REST, Liberty, or persistence configuration files change in
  this issue.

### 4.2 — Issue 2: Expose deadline changes through the booking facade

**What to build**

Expose the use case to presentation clients without leaking domain identifier
types into the web layer.

**Files to modify**

- `src/main/java/org/eclipse/cargotracker/interfaces/booking/facade/BookingServiceFacade.java`
- `src/main/java/org/eclipse/cargotracker/interfaces/booking/facade/internal/DefaultBookingServiceFacade.java`

**Optional focused test file**

- `src/test/java/org/eclipse/cargotracker/interfaces/booking/facade/internal/DefaultBookingServiceFacadeTest.java`

**Required API**

```java
void changeDeadline(String trackingId, Date arrivalDeadline);
```

**Required implementation**

```java
bookingService.changeDeadline(
        new TrackingId(trackingId),
        arrivalDeadline);
```

The facade must not:

- load and mutate `Cargo` itself;
- call `CargoRepository.store(...)`;
- parse a formatted date;
- introduce JSF or PrimeFaces types.

**Tests**

Where a container-free test is added, use a hand-written `BookingService` fake
or spy and prove that:

- the same `Date` object/value reaches the application service;
- the tracking-ID string is converted to an equivalent `TrackingId`;
- the facade delegates exactly once;
- no repository work is duplicated in the facade.

Do not add Mockito or another dependency solely for this test.

**Gating criteria**

- Existing facade consumers still compile.
- `./mvnw clean package -Popenliberty` succeeds on JDK 17.
- The application-layer test added in Issue 1 remains unchanged and compiling.

### 4.3 — Issue 3: Implement the deadline editor backing model

**What to build**

Add the view-scoped backing bean that loads a cargo's current deadline and
submits a replacement deadline through the booking facade. Do not add the
dialog launcher or XHTML in this issue.

**File to create**

- `src/main/java/org/eclipse/cargotracker/interfaces/booking/web/ChangeArrivalDeadlineDate.java`

**Required bean shape**

```java
@Named
@ViewScoped
public class ChangeArrivalDeadlineDate implements Serializable {
    private static final long serialVersionUID = 1L;

    private String trackingId;
    private CargoRoute cargo;
    private Date arrivalDeadlineDate;

    @Inject
    private BookingServiceFacade bookingServiceFacade;
}
```

Required properties and methods:

- `getTrackingId()` / `setTrackingId(String)`
- `getCargo()`
- `getArrivalDeadlineDate()` / `setArrivalDeadlineDate(Date)`
- `load()`
- `changeArrivalDeadline()`

**Load behavior**

1. Call `bookingServiceFacade.loadCargoForRouting(trackingId)`.
2. Store the returned `CargoRoute`.
3. Parse `cargo.getArrivalDeadlineDate()` using `MM/dd/yyyy`.
4. Store the resulting `Date` in `arrivalDeadlineDate`.
5. Do not query a repository or domain object directly.
6. Do not ignore a parsing failure or merely print its stack trace. Surface a
   clear application/view error consistent with existing JSF behavior.

**Submit behavior**

1. Refuse a null date through JSF validation or explicit bean validation.
2. Call
   `bookingServiceFacade.changeDeadline(trackingId, arrivalDeadlineDate)`.
3. Close the dynamic dialog with:

   ```java
   PrimeFaces.current().dialog().closeDynamic("DONE");
   ```

4. Do not close the dialog if the facade call fails.

**Tests to write**

Add a container-free JUnit test if practical, using a hand-written fake facade,
that proves:

- `load()` requests the correct tracking ID;
- `load()` converts an `MM/dd/yyyy` DTO date into the editable `Date`;
- `changeArrivalDeadline()` delegates the selected date and tracking ID;
- a malformed DTO deadline is surfaced rather than converted to null;
- a null selected date is rejected.

Do not add a mocking framework solely for these tests.

**Gating criteria**

- The bean is serializable and uses the established CDI/JSF annotations.
- The bean references only facade DTOs, not domain model classes.
- `./mvnw clean package -Popenliberty` succeeds on JDK 17.

### 4.4 — Issue 4: Implement the PrimeFaces deadline dialog

**What to build**

Add the session-scoped dialog launcher and the dynamic dialog view. The dialog
must work when addressed directly with a `trackingId` query parameter, but it
is not yet linked from the dashboard in this issue.

**Files to create**

- `src/main/java/org/eclipse/cargotracker/interfaces/booking/web/ChangeArrivalDeadlineDateDialog.java`
- `src/main/webapp/admin/dialogs/changeArrivalDeadlineDate.xhtml`

**Launcher requirements**

Use:

```java
@ManagedBean(name = "changeArrivalDeadlineDateDialog")
@SessionScoped
```

Implement:

- `showDialog(String trackingId)`
- `handleReturn(SelectEvent event)`
- `cancel()`

`showDialog(...)` must:

- set the options documented in Question 3.7;
- pass `trackingId` as a dynamic-dialog request parameter;
- open `/admin/dialogs/changeArrivalDeadlineDate.xhtml`.

`cancel()` must close the dialog without invoking the facade.

**XHTML requirements**

The page title must be:

```xhtml
<title>Change Deadline</title>
```

Place metadata directly beneath the root `<html>` element and before
`<h:head>`:

```xhtml
<f:metadata>
    <f:viewParam name="trackingId"
                 value="#{changeArrivalDeadlineDate.trackingId}"/>
    <f:viewAction action="#{changeArrivalDeadlineDate.load}"/>
</f:metadata>
```

The form must display:

- `Origin:` and `changeArrivalDeadlineDate.cargo.originName`;
- `Destination:` and
  `changeArrivalDeadlineDate.cargo.finalDestinationName`;
- `Deadline:` and a `p:datePicker` bound to
  `changeArrivalDeadlineDate.arrivalDeadlineDate`;
- **Cancel**, invoking
  `changeArrivalDeadlineDateDialog.cancel()`;
- **Update**, invoking
  `changeArrivalDeadlineDate.changeArrivalDeadline()`.

The date picker must require a value. The Update action must reload or refresh
the calling Administration view after a successful dialog close, following the
existing destination-dialog behavior.

**Runtime tests**

With the application running, request:

```text
http://localhost:8080/cargo-tracker/admin/dialogs/changeArrivalDeadlineDate.xhtml?trackingId=DEF789
```

Verify:

- HTTP 200;
- title is **Change Deadline**;
- origin and destination render;
- the existing deadline is selected;
- no `TagException`, `Parent UIComponent`, `FacesException`, or server error is
  present;
- Cancel does not change the persisted deadline;
- Update changes the deadline.

**Gating criteria**

- `./mvnw clean package -Popenliberty liberty:run` succeeds on JDK 17.
- Direct dialog loading and both actions work.
- Destination editing continues to work.
- Stop Liberty cleanly before completing the issue.

### 4.5 — Issue 5: Integrate deadline editing into the Administration dashboard

**What to build**

Replace the plain deadline text in the Not Routed Cargo table with the
PrimeFaces command-link affordance that opens the completed dialog and refreshes
the table after return.

**File to modify**

- `src/main/webapp/admin/tables/listNotRouted.xhtml`

**Required UI shape**

Within the existing Deadline column, add a `p:commandLink` that:

- calls
  `changeArrivalDeadlineDateDialog.showDialog(cargoNotRouted.trackingId)`;
- retains the displayed
  `cargoNotRouted.arrivalDeadlineDate`;
- adds the existing Font Awesome edit icon style;
- uses a stable component ID such as `arrivalDeadlineToUpdate`;
- listens for `dialogReturn`;
- invokes
  `changeArrivalDeadlineDateDialog.handleReturn`;
- updates `tableNotRouted`;
- provides the tooltip:
  `Click to change cargo arrival deadline date.`

Follow the adjacent Destination column's established structure and styling. Do
not alter tracking-ID routing or destination editing.

**End-to-end acceptance test**

1. Start from a clean build on JDK 17:

   ```bash
   ./mvnw clean package -Popenliberty liberty:run
   ```

2. Confirm the home page returns HTTP 200.
3. Open Administration and locate `DEF789`.
4. Record the original deadline.
5. Confirm the deadline now has an edit icon and tooltip.
6. Open the deadline dialog.
7. Confirm origin and destination identify the same cargo.
8. Choose a visibly different date.
9. Press **Update**.
10. Confirm the dialog closes and the Not Routed Cargo table refreshes.
11. Confirm the table shows the selected date.
12. Reload the browser and confirm the selected date remains.
13. Reopen the dialog and confirm the editor initializes to the changed date.
14. Press **Cancel** and confirm no additional change occurs.
15. Verify the Destination edit dialog still opens.
16. Verify selecting `DEF789` for routing still loads without an error page.

**Log acceptance**

The final run must contain none of:

- `<f:metadata> Parent UIComponent`;
- `TagException`;
- `VerifyError`;
- `FacesException`;
- `CWWKZ0002E` or `CWWKZ0003E`;
- recurring batch authorization failures;
- new FFDC files attributable to this feature.

Transient JMS activation-order warnings are acceptable only if all message
endpoints subsequently activate, as established by the prepared baseline.

**Final regression and scope checks**

- `./mvnw clean package -Popenliberty` succeeds.
- The existing test sources and the new deadline test compile.
- No Java EE namespace migration occurred.
- No Open Liberty, Derby, Jackson, JSF metadata, batch authorization, or REST
  compatibility fix from the starting commit was reverted.
- The feature affects only the intended application, facade, web, dialog,
  table, and test surfaces.
- Stop Liberty cleanly.

---

## Phase 5 — Documentation and implementation handoff

- Update `README.md` only if user-facing Administration capabilities are
  enumerated there; add one concise sentence that administrators can change an
  unrouted cargo's arrival deadline.
- Record the exact JDK 17 run command in the final issue or pull-request
  description:

  ```bash
  ./mvnw clean package -Popenliberty liberty:run
  ```

- Include `DEF789` and the before/after deadline values in the acceptance
  evidence.
- State explicitly that data is in-memory and resets when the application is
  rebuilt/restarted.

---

## Cross-cutting concerns

| Concern | Required handling |
|---------|--------------------|
| Domain-driven design | Mutate the aggregate through `Cargo.specifyNewRoute(...)`; do not bypass it with persistence-level field updates. |
| Layering | Web bean → booking facade → booking service → cargo repository. |
| Date handling | Use `java.util.Date` at service/facade boundaries and `MM/dd/yyyy` for the date-only editor representation. |
| Time zones | Acceptance compares the calendar date displayed by the application; do not introduce a new timezone policy in this feature. |
| Error handling | Do not silently swallow parse, lookup, validation, or persistence failures. A failed update must not look successful. |
| JSF compatibility | Keep `<f:metadata>` at view-root scope for MyFaces. |
| Bean lifecycle | Editor is CDI `@ViewScoped`; dynamic-dialog launcher mirrors the existing session-scoped JSF managed bean. |
| Accessibility | Preserve visible labels; the date editor must have an associated label and validation feedback. |
| Backward compatibility | Existing destination editing, routing, tracking, REST, messaging, batch, and startup behavior must remain intact. |
| Test discipline | Add tests before production code where practical; every issue must preserve all prior gates. |
| Experiment integrity | Implement from this specification starting at `9b9f311b2a3a2854bdac947593950d9edb6bca7d`; do not cherry-pick or inspect feature-bearing commits. |
````

</details>


---

<sub>19s</sub>

### `view`

**view the file at /Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/shepherd-campaign.json.**

/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/shepherd-campaign.json

<details>
<summary>16 lines</summary>

```
{
  "schemaVersion": 1,
  "campaignId": "cb68d348-f71e-4ce0-a529-ca5a5d5cbc20",
  "campaignIssueNumber": 1,
  "campaignShortname": "arrival-deadline-control",
  "repository": "edburns/dd-3070518-cargotracker-darwin-arm64-01",
  "baseBranch": "experiment/shepherd-control",
  "lessonPropagation": "off",
  "campaignMetadataDirectory": "1-arrival-deadline-control-remove-before-merge",
  "lessonsFile": "campaign-lessons.md",
  "createdBy": {
    "shepherdTaskVersion": "1.0.4",
    "stageOutcomeProtocolVersion": 1
  },
  "createdAt": "2026-09-28T23:36:51Z"
}
```

</details>


---

<sub>19s</sub>

### `view`

**view the file at /Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/campaign-lessons.md.**

/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/campaign-lessons.md

<details>
<summary>5 lines</summary>

```
# Campaign lessons

This file contains validated, reusable lessons for subsequent issues in this campaign.
The issue specification and repository instructions remain authoritative.

## Validated lessons

No validated lessons have been recorded yet.
```

</details>


---

<sub>19s</sub>

### `bash`

**Validate stage 20 inputs and GitHub state**

$ export IDEA_HOME="/Applications/IntelliJ IDEA CE.app/Contents/MacOS" APPCAT_HOME=/Users/edburns/.appcat JAVA_HOME="/Library/Java/JavaVirtualMachines/microsoft-25.jdk/Contents/Home" ANT_HOME="$HOME/Downloads/apache-ant-1.10.13" M2_HOME="$HOME/Downloads/apache-maven-3.9.8"; export PATH="$APPCAT_HOME:$M2_HOME/bin:$ANT_HOME/bin:$JAVA_HOME/bin:$IDEA_HOME:$PATH"; set -euo pipefail
REPO='edburns/dd-3070518-cargotracker-darwin-arm64-01'; BASE='experiment/shepherd-control'; REMOTE='origin'; PARENT='1'; LOG='/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936'; DV='/Users/edburns/.copilot/plugins/shepherd-task/scripts/validate-stage20-drafts.sh'; BV='/Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-github-issue-body.sh'; CV='/Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-stage20-child-links.sh'
printf 'parent_numeric=%s\n' "$(printf '%s' "$PARENT" | grep -Eq '^[1-9][0-9]*$' && echo yes || echo no)"
test -d "$LOG" && test -w "$LOG"; mkdir -p "$LOG/issue-bodies"; printf 'log_writable=yes\n'
for s in "$DV" "$BV" "$CV"; do test -x "$s"; printf 'script_ok=%s\n' "$s"; done
default_branch=$(gh api "repos/$REPO" --jq '.default_branch'); owner_type=$(gh api "repos/$REPO" --jq '.owner.type'); owner_login=$(gh api "repos/$REPO" --jq '.owner.login'); printf 'default_branch=%s\nowner_type=%s\nowner_login=%s\n' "$default_branch" "$owner_type" "$owner_login"
test "$BASE" != main && test "$BASE" != "$default_branch"
git show-ref --verify --quiet "refs/heads/$BASE" || git show-ref --verify --quiet "refs/remotes/$REMOTE/$BASE"; printf 'base_exists=yes\n'
remote_url=$(git remote get-url "$REMOTE"); printf 'remote_url=%s\n' "$remote_url"; printf '%s' "$remote_url" | grep -Eq 'github\.com[:/]edburns/dd-3070518-cargotracker-darwin-arm64-01(\.git)?$'
gh api "repos/$REPO/issues/$PARENT" --jq '{number,state,title,repository_url}'
if [ "$owner_type" = Organization ]; then gh api "orgs/$owner_login/issue-types"; elif [ "$owner_type" = User ]; then printf 'selected_issue_type=UNTYPED\n'; else exit 70; fi
printf 'plan_headings_questions='; git show "$BASE:1-arrival-deadline-control-remove-before-merge/add-change-arrival-deadline-feature-ignorance-reduction-plan.md" | grep -Fxc '## Phase 3 — Ignorance reduction: questions to answer before writing code'
printf 'plan_headings_impl='; git show "$BASE:1-arrival-deadline-control-remove-before-merge/add-change-arrival-deadline-feature-ignorance-reduction-plan.md" | grep -Fxc '## Phase 4 — Implementation (five serial issues)'
printf 'manifest='; git show "$BASE:1-arrival-deadline-control-remove-before-merge/shepherd-campaign.json" | jq -c .
git cat-file -e "$BASE:1-arrival-deadline-control-remove-before-merge/campaign-lessons.md"; printf 'campaign_lessons_exists=yes\n'

<details>
<summary>17 lines</summary>

```
parent_numeric=yes
log_writable=yes
script_ok=/Users/edburns/.copilot/plugins/shepherd-task/scripts/validate-stage20-drafts.sh
script_ok=/Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-github-issue-body.sh
script_ok=/Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-stage20-child-links.sh
default_branch=20260902-2104Z-commit-e7b651f-liberty
owner_type=User
owner_login=edburns
base_exists=yes
remote_url=git@github.com:edburns/dd-3070518-cargotracker-darwin-arm64-01.git
{"number":1,"repository_url":"https://api.github.com/repos/edburns/dd-3070518-cargotracker-darwin-arm64-01","state":"open","title":"[Campaign][off] Cargo Tracker arrival-deadline control"}
selected_issue_type=UNTYPED
plan_headings_questions=1
plan_headings_impl=1
manifest={"schemaVersion":1,"campaignId":"cb68d348-f71e-4ce0-a529-ca5a5d5cbc20","campaignIssueNumber":1,"campaignShortname":"arrival-deadline-control","repository":"edburns/dd-3070518-cargotracker-darwin-arm64-01","baseBranch":"experiment/shepherd-control","lessonPropagation":"off","campaignMetadataDirectory":"1-arrival-deadline-control-remove-before-merge","lessonsFile":"campaign-lessons.md","createdBy":{"shepherdTaskVersion":"1.0.4","stageOutcomeProtocolVersion":1},"createdAt":"2026-09-28T23:36:51Z"}
campaign_lessons_exists=yes
<shellId: 0 completed with exit code 0>
```

</details>


---

<sub>23s</sub>

### Copilot

Validation passed: the parent is open, the topic branch exists and is non-default, campaign metadata matches, and this user-owned repository will use untyped issues. I’m now capturing the normalized child baseline and resolving the five task specifications from the plan.

---

<sub>28s</sub>

### `glob`

**1-arrival-deadline-control-remove-before-merge/**/***

"1-arrival-deadline-control-remove-before-merge/**/*"

<details>
<summary>10 matches</summary>

```
/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/campaign-lessons.md
/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/.stage20-after.32706
/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/shepherd-test-experiment.json
/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/add-change-arrival-deadline-feature-ignorance-reduction-plan.md
/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/shepherd-campaign.json
/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/.stage20-before.32706
/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/create-issues-otel-20260928-1936.jsonl
/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/create-issues-session-20260928-1936.jsonl
/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/20260928-1936-invoke-shepherd-task-20-create-issues-from-plan-skill.sh
/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/20260928-1936-invoke-shepherd-task-20-create-issues-from-plan-skill.md
```

</details>


---

<sub>28s</sub>

### `bash`

**Capture normalized child baseline**

$ export IDEA_HOME="/Applications/IntelliJ IDEA CE.app/Contents/MacOS" APPCAT_HOME=/Users/edburns/.appcat JAVA_HOME="/Library/Java/JavaVirtualMachines/microsoft-25.jdk/Contents/Home" ANT_HOME="$HOME/Downloads/apache-ant-1.10.13" M2_HOME="$HOME/Downloads/apache-maven-3.9.8"; export PATH="$APPCAT_HOME:$M2_HOME/bin:$ANT_HOME/bin:$JAVA_HOME/bin:$IDEA_HOME:$PATH"; set -euo pipefail
REPO='edburns/dd-3070518-cargotracker-darwin-arm64-01'; PARENT=1; LOG='/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936'
raw=$(gh api "repos/$REPO/issues/$PARENT/sub_issues" --paginate --slurp)
normalized=$(printf '%s' "$raw" | jq 'if length == 0 then [] elif all(.[]; type == "array") then add else . end')
tmp="$LOG/.pre-creation-children.json.tmp"; printf '%s\n' "$normalized" > "$tmp"; mv "$tmp" "$LOG/pre-creation-children.json"
printf 'baseline_shape=%s baseline_count=%s\n' "$(jq -r type "$LOG/pre-creation-children.json")" "$(jq length "$LOG/pre-creation-children.json")"
jq '[.[] | {id,number,title}]' "$LOG/pre-creation-children.json"

```
baseline_shape=array baseline_count=0
[]
<shellId: 1 completed with exit code 0>
```


---

<sub>28s</sub>

### `bash`

**Count direct implementation tasks**

$ set -euo pipefail
PLAN='1-arrival-deadline-control-remove-before-merge/add-change-arrival-deadline-feature-ignorance-reduction-plan.md'; BASE='experiment/shepherd-control'
git show "$BASE:$PLAN" | awk '
/^## Phase 4 — Implementation \(five serial issues\)$/ {in_impl=1; next}
in_impl && /^## / {in_impl=0}
in_impl && /^### 4\.[0-9]+ — / {print}
'

<details>
<summary>6 lines</summary>

```
### 4.1 — Issue 1: Add the application-layer deadline change operation
### 4.2 — Issue 2: Expose deadline changes through the booking facade
### 4.3 — Issue 3: Implement the deadline editor backing model
### 4.4 — Issue 4: Implement the PrimeFaces deadline dialog
### 4.5 — Issue 5: Integrate deadline editing into the Administration dashboard
<shellId: 2 completed with exit code 0>
```

</details>


---

<sub>28s</sub>

### `bash`

**Extract resolution blocks**

$ set -euo pipefail
PLAN='1-arrival-deadline-control-remove-before-merge/add-change-arrival-deadline-feature-ignorance-reduction-plan.md'; BASE='experiment/shepherd-control'
git show "$BASE:$PLAN" | awk '
/^## Phase 3 — Ignorance reduction: questions to answer before writing code$/ {in_q=1; next}
in_q && /^## / {exit}
in_q && /^### 3\.[0-9]+ — / {if (heading != "") {print "---"}; heading=$0; print heading; next}
in_q && /^\*\*Resolution:\*\*/ {in_r=1; print; next}
in_r && /^### 3\.[0-9]+ — / {in_r=0}
in_r {print}
'

<details>
<summary>260 lines</summary>

````
### 3.1 — Which cargos expose the edit operation?
**Resolution:**

Select Option A. Expose the edit affordance only in
`src/main/webapp/admin/tables/listNotRouted.xhtml`. The application and facade
operations remain generally callable for any cargo that can be found by
tracking ID; they do not encode knowledge of dashboard table membership.

---
### 3.2 — What is the exact domain mutation?

**Question:** Should the feature mutate the existing `RouteSpecification`, add
a setter to `Cargo`, or replace the specification using the existing domain
operation?

`RouteSpecification` is a value object describing origin, destination, and
arrival deadline. The existing `changeDestination(...)` implementation already
establishes the correct pattern: create a replacement specification, call
`Cargo.specifyNewRoute(...)`, and store the aggregate.

Proposed application-service shape:

```java
void changeDeadline(TrackingId trackingId, Date deadline);
```

Proposed implementation:

```java
Cargo cargo = cargoRepository.find(trackingId);
RouteSpecification specification = new RouteSpecification(
        cargo.getOrigin(),
        cargo.getRouteSpecification().getDestination(),
        deadline);

cargo.specifyNewRoute(specification);
cargoRepository.store(cargo);
```

Calling `specifyNewRoute(...)` is significant. It lets the aggregate recalculate
delivery and routing status relative to the new specification. Direct field
mutation or a persistence-only update would bypass that behavior.

**Recommendation:** Replace the `RouteSpecification` through
`Cargo.specifyNewRoute(...)`. Preserve origin, destination, and itinerary.
Persist using the existing repository. Do not add a deadline setter to the
domain model.

**Resolution:**

Use the same aggregate-update pattern as `changeDestination(...)`. Add
`BookingService.changeDeadline(TrackingId, Date)` and implement it by loading
the cargo, constructing a new `RouteSpecification` from the existing origin,
existing destination, and supplied deadline, calling
`cargo.specifyNewRoute(...)`, and storing the cargo through
`cargoRepository.store(...)`. Do not add mutable deadline setters to the domain
objects.

---
### 3.3 — What should happen to an existing itinerary and delivery state?

**Question:** When a routed cargo's deadline changes, should its itinerary be
cleared, retained, or recomputed?

Although the UI initially exposes the feature only for unrouted cargo, the
application operation should have deterministic domain behavior if invoked for
a routed cargo. The existing `changeDestination(...)` behavior preserves the
assigned itinerary and lets `Cargo.specifyNewRoute(...)` recalculate whether
that itinerary still satisfies the new specification.

The core application test should deliberately invoke the operation after:

1. booking a cargo;
2. requesting route candidates;
3. assigning an itinerary;
4. changing its destination;
5. changing its deadline.

This sequence verifies that the feature uses the aggregate correctly rather
than assuming the cargo always has an empty itinerary.

**Recommendation:** Preserve the itinerary. Let the domain model recompute
routing and delivery-derived state. Assert all unaffected fields explicitly in
`BookingServiceTest`.

**Resolution:**

Retain the existing itinerary. Do not clear, replace, or reroute it as part of
the deadline change. `Cargo.specifyNewRoute(...)` recalculates the delivery
snapshot and routing status against the replacement specification. In the
established sequential application test, the assigned itinerary remains
unchanged and the cargo remains `MISROUTED` after the deadline changes.

---
### 3.4 — What type crosses the facade boundary?

**Question:** Should the booking facade accept a `Date`, a formatted string, or
a newly introduced request DTO?

The existing facade already uses `java.util.Date` for
`bookNewCargo(...)`. Introducing another representation for this one operation
would create unnecessary conversion code and depart from the historical
application style.

Proposed facade shape:

```java
void changeDeadline(String trackingId, Date arrivalDeadline);
```

The implementation converts only the identifier:

```java
bookingService.changeDeadline(
        new TrackingId(trackingId),
        arrivalDeadline);
```

**Recommendation:** Use `String` for the tracking ID and `java.util.Date` for
the deadline. Do not expose `TrackingId`, `Cargo`, or `RouteSpecification` to
the JSF layer and do not introduce a new DTO solely for this command.

**Resolution:**

Add `void changeDeadline(String trackingId, Date arrivalDeadline)` to
`BookingServiceFacade`. `DefaultBookingServiceFacade` converts the string to
`new TrackingId(trackingId)` and passes the same `Date` to
`BookingService.changeDeadline(...)`. No new command DTO or formatted-string
service parameter is introduced.

---
### 3.5 — How is the DTO's formatted deadline converted for editing?

**Question:** `CargoRoute` exposes its deadline as formatted strings, while
`p:datePicker` binds naturally to `java.util.Date`. How should the backing bean
initialize the editor?

At the starting commit:

- `CargoRoute.getArrivalDeadline()` returns
  `MM/dd/yyyy hh:mm a z`.
- `CargoRoute.getArrivalDeadlineDate()` returns only the date component.
- The table displays `getArrivalDeadlineDate()`.

Options:

| Option | Approach | Trade-off |
|--------|----------|-----------|
| A | Parse `cargo.getArrivalDeadlineDate()` with `MM/dd/yyyy` | Small, localized change; preserves the existing DTO contract. |
| B | Add a `Date` property to `CargoRoute` | Cleaner typing, but broadens a DTO used throughout the application. |
| C | Reload the domain object in the backing bean | Violates the facade boundary. |

The formatter/parser must be created per operation or per view bean; do not add
a shared mutable `SimpleDateFormat`.

**Recommendation:** Option A. Load `CargoRoute` through
`BookingServiceFacade.loadCargoForRouting(trackingId)` and parse
`cargo.getArrivalDeadlineDate()` using `new SimpleDateFormat("MM/dd/yyyy")`.
Surface an explicit failure if the existing DTO value cannot be parsed; do not
silently submit a null date.

**Resolution:**

Use Option A and keep date conversion inside the view-scoped editor bean. The
existing implementation loads the `CargoRoute`, creates
`new SimpleDateFormat("MM/dd/yyyy")`, and parses the leading date portion of
`cargo.getArrivalDeadline()`. Because that value begins with `MM/dd/yyyy`,
`SimpleDateFormat.parse(...)` obtains the same date that
`getArrivalDeadlineDate()` displays. A per-load formatter is used, so no shared
mutable formatter is added.

---
### 3.6 — Which JSF bean scopes and interaction pattern should be used?

**Question:** Should deadline editing introduce a new navigation page, use an
inline editor, or mirror the existing Change Destination dynamic-dialog
pattern?

The baseline already contains:

- `ChangeDestination`, a CDI `@Named` and JSF `@ViewScoped` editor bean;
- `ChangeDestinationDialog`, a session-scoped JSF managed bean that opens and
  closes a PrimeFaces dynamic dialog;
- `changeDestination.xhtml`, a dialog view;
- a `dialogReturn` Ajax listener that refreshes `tableNotRouted`.

Using the same pattern minimizes changes and provides a consistent user
experience.

Proposed bean names:

```text
changeArrivalDeadlineDate
changeArrivalDeadlineDateDialog
```

**Recommendation:** Add a serializable CDI `@Named @ViewScoped`
`ChangeArrivalDeadlineDate` editor and a serializable
`@ManagedBean(name = "changeArrivalDeadlineDateDialog") @SessionScoped`
launcher. Mirror the existing destination-dialog lifecycle rather than
introducing a new navigation or inline-edit framework.

**Resolution:**

Mirror the existing Change Destination interaction. Implement
`ChangeArrivalDeadlineDate` as a serializable CDI `@Named @ViewScoped` bean and
`ChangeArrivalDeadlineDateDialog` as a serializable
`@ManagedBean(name = "changeArrivalDeadlineDateDialog") @SessionScoped` bean.
Use a PrimeFaces dynamic dialog rather than navigation to a full page or inline
cell editing.

---
### 3.7 — What is the dynamic-dialog contract?

**Question:** What path, request parameters, dimensions, and close result should
the PrimeFaces dialog use?

The launcher needs one parameter, `trackingId`, supplied as a
`Map<String, List<String>>`. The dialog metadata binds the parameter and invokes
the editor bean's `load()` action.

Proposed launcher contract:

```java
PrimeFaces.current().dialog().openDynamic(
        "/admin/dialogs/changeArrivalDeadlineDate.xhtml",
        options,
        params);
```

Required options:

| Option | Value |
|--------|-------|
| `modal` | `true` |
| `draggable` | `true` |
| `resizable` | `false` |
| `contentWidth` | `410` |
| `contentHeight` | `280` |

Required completion behavior:

- successful update: `closeDynamic("DONE")`;
- cancel: `closeDynamic("")`;
- caller listens for `dialogReturn` and updates `tableNotRouted`.

Because Open Liberty uses MyFaces, `<f:metadata>` must be a direct child of the
view root, before `<h:head>` and `<h:body>`. It must not be nested inside
`<h:body>`.

**Recommendation:** Use the contract above and preserve the metadata placement
required by the prepared baseline.

**Resolution:**

Open `/admin/dialogs/changeArrivalDeadlineDate.xhtml` with a single
`trackingId` request parameter and these options: modal and draggable are
`true`, resizable is `false`, content width is `410`, and content height is
`280`. Successful submission closes with `"DONE"`; cancellation closes with
the empty string. The caller handles `dialogReturn` and updates
`tableNotRouted`. Place the dialog's `<f:metadata>` directly under the root
`<html>` element, before `<h:head>` and `<h:body>`, so the known MyFaces
`UIViewRoot` requirement is satisfied.

---
### 3.8 — What date validation is required?

**Question:** Must the new deadline be non-null, in the future, after the
current date, or after itinerary completion?

The requested feature is an administrative correction to an existing arrival
deadline. No new domain policy about future dates is part of the request.
Inventing such a rule could reject dates accepted by existing cargo booking or
`RouteSpecification` behavior.

The UI must nevertheless prevent a null submission because the operation
requires a concrete replacement deadline.

**Recommendation:** Require a date value in the JSF form and display a normal
Faces validation message when it is absent. Do not add a new minimum-date,
future-date, or itinerary-date business rule. Continue to rely on the existing
domain model for its established invariants.

**Resolution:**

Require a non-null date selection, but add no new chronological business rule.
In particular, do not require the replacement deadline to be after today,
after the old deadline, or after every itinerary leg. Pass the selected
`java.util.Date` to the existing domain construction path and let the current
`RouteSpecification` invariants apply.

---
### 3.9 — How will the feature be tested on the prepared historical baseline?

**Question:** Which automated and runtime tests are mandatory, given that the
historical JUnit/Arquillian suite is configured for a remote Payara 4
container, while the prepared production baseline runs on JDK 17/Open Liberty?

The starting POM deliberately leaves `skipTests=true`. The Open Liberty profile
builds and compiles all test sources but does not provide a Liberty Arquillian
adapter. Modernizing the entire integration-test runtime is outside this
feature's scope.

The feature still needs layered evidence:

1. Extend `BookingServiceTest` with the domain/application assertions that
   specify the deadline mutation.
2. Ensure all test sources compile as part of
   `./mvnw clean package -Popenliberty`.
3. Add focused JUnit tests for facade and backing-bean delegation where they
   can run without a container, using hand-written fakes rather than adding a
   mocking framework.
4. Perform mandatory end-to-end verification against the running Open Liberty
   application.
5. Preserve the existing Payara Arquillian test path; do not delete, disable,
   or rewrite it to manufacture a passing result.

**Spike needed:** Before Issue 1 implementation, run the starting commit's
standard Open Liberty package command and record whether tests are compiled but
skipped. Confirm the new `BookingServiceTest` method can be added without
expanding the runtime modernization scope.

**Recommendation:** Treat the JDK 17/Open Liberty build plus HTTP/UI acceptance
as the mandatory executable gate. Keep the historical Arquillian test as a
precise application-layer specification and run it only when its documented
Payara environment is available.

**Resolution:**

Extend the existing sequential Arquillian `BookingServiceTest` with
`testChangeDeadline()` after `testChangeDestination()`. The test changes the
deadline by one month, reloads the cargo through JPA, and asserts the complete
set of preserved and recalculated domain state described above. The prepared
Open Liberty build compiles this test but retains the historical default
`skipTests=true`; executing that Arquillian suite still requires its documented
remote Payara environment. Therefore the mandatory executable gates are the
JDK 17 Open Liberty package/start command, direct HTTP checks, and the complete
`DEF789` browser acceptance flow. No Arquillian-runtime modernization or new
mocking dependency is part of this feature.

---
<shellId: 3 completed with exit code 0>
````

</details>


---

<sub>32s</sub>

### `view`

**view the file at /Users/edburns/.copilot/plugins/shepherd-task/scripts/validate-stage20-drafts.sh.**

/Users/edburns/.copilot/plugins/shepherd-task/scripts/validate-stage20-drafts.sh

<details>
<summary>65 lines</summary>

```
#!/usr/bin/env bash
# shepherd-task-version: 1.0.4

set -euo pipefail

[[ $# -eq 3 ]] || {
    echo "Usage: $0 <body-directory> <expected-count> <off|campaign>" >&2
    exit 1
}

body_directory="$1"
expected_count="$2"
lesson_propagation="$3"

[[ -d "$body_directory" ]] || {
    echo "Stage-20 body directory does not exist: $body_directory" >&2
    exit 1
}
[[ "$expected_count" =~ ^[1-9][0-9]*$ ]] || {
    echo "Expected count must be a positive integer." >&2
    exit 1
}
[[ "$lesson_propagation" == "off" || "$lesson_propagation" == "campaign" ]] || {
    echo "Lesson propagation must be off or campaign." >&2
    exit 1
}

body_files=()
for body_file in "$body_directory"/*-body.md; do
    [[ -f "$body_file" ]] || continue
    [[ "$body_file" != *-observed-body.md ]] || continue
    body_files+=("$body_file")
done
[[ ${#body_files[@]} -eq $expected_count ]] || {
    echo "Expected $expected_count persisted stage-20 body files; found ${#body_files[@]}." >&2
    exit 1
}

required_headings=(
    '## Campaign context and required reading'
    '## Branch and execution order'
    '## Implement'
    '## Completion gates'
    '## Out of scope'
)

for body_file in "${body_files[@]}"; do
    [[ -s "$body_file" && "$(awk 'END { print NR }' "$body_file")" -gt 1 ]] || {
        echo "Persisted issue body must contain physical Markdown lines: $body_file" >&2
        exit 1
    }
    first_nonblank="$(awk 'NF { print; exit }' "$body_file")"
    [[ "$first_nonblank" =~ ^##[[:space:]]+[^[:space:]] ]] || {
        echo "Persisted issue body must begin with a level-two heading: $body_file" >&2
        exit 1
    }
    for heading in "${required_headings[@]}"; do
        grep -Fqx -- "$heading" "$body_file" || {
            echo "Persisted issue body is missing the physical heading '$heading': $body_file" >&2
            exit 1
        }
    done
    if [[ "$lesson_propagation" == "campaign" ]]; then
        grep -Fqx -- '## Campaign lessons (REQUIRED)' "$body_file" || {
            echo "Treatment issue body is missing its campaign-lessons heading: $body_file" >&2
            exit 1
        }
    elif grep -Fqx -- '## Campaign lessons (REQUIRED)' "$body_file"; then
        echo "Control issue body unexpectedly contains a campaign-lessons heading: $body_file" >&2
        exit 1
    fi
done
```

</details>


---

<sub>32s</sub>

### `view`

**view the file at /Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-stage20-child-links.sh.**

/Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-stage20-child-links.sh

<details>
<summary>30 lines</summary>

```
#!/usr/bin/env bash
# shepherd-task-version: 1.0.4

set -euo pipefail

if [[ $# -ne 3 ]]; then
    echo "Usage: $0 <PRE_CREATION_CHILDREN_JSON> <FINAL_CHILDREN_JSON> <CREATION_LEDGER_JSON>" >&2
    exit 2
fi

for command_name in jq; do
    command -v "$command_name" >/dev/null 2>&1 || {
        echo "Error: Required command '$command_name' was not found." >&2
        exit 1
    }
done

script_dir="$(cd "$(dirname "$0")" && pwd)"
filter_path="$script_dir/verify-stage20-child-links.jq"
[[ -f "$filter_path" ]] || {
    echo "Error: Stage-20 child-link verification filter was not found: $filter_path" >&2
    exit 1
}

for input_path in "$@"; do
    [[ -f "$input_path" ]] || {
        echo "Error: Stage-20 child-link verification input was not found: $input_path" >&2
        exit 1
    }
done

jq -n \
    --slurpfile baseline "$1" \
    --slurpfile final "$2" \
    --slurpfile ledger "$3" \
    -f "$filter_path"
```

</details>


---

<sub>32s</sub>

### `view`

**view the file at /Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-github-issue-body.sh.**

/Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-github-issue-body.sh

<details>
<summary>131 lines</summary>

```
#!/usr/bin/env bash
# shepherd-task-version: 1.0.4
set -euo pipefail

fail() {
    printf 'verify-github-issue-body: %s\n' "$*" >&2
    exit 1
}

[[ $# -ge 3 && $# -le 6 ]] ||
    fail "usage: $0 OWNER/REPO ISSUE_NUMBER EXPECTED_BODY_PATH [MAX_ATTEMPTS] [DELAY_SECONDS] [DIAGNOSTIC_PATH]"

repository="$1"
issue_number="$2"
expected_body_path="$3"
max_attempts="${4:-6}"
delay_seconds="${5:-5}"
diagnostic_path="${6:-}"
gh_command="${GH_COMMAND:-gh}"

[[ "$repository" =~ ^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$ ]] ||
    fail "invalid repository: $repository"
[[ "$issue_number" =~ ^[1-9][0-9]*$ ]] ||
    fail "invalid issue number: $issue_number"
[[ "$max_attempts" =~ ^[1-9][0-9]*$ ]] ||
    fail "MAX_ATTEMPTS must be a positive integer"
[[ "$delay_seconds" =~ ^[0-9]+$ ]] ||
    fail "DELAY_SECONDS must be a non-negative integer"
[[ -f "$expected_body_path" ]] ||
    fail "expected issue body file not found: $expected_body_path"

temp_directory="$(mktemp -d)"
trap 'rm -rf "$temp_directory"' EXIT
response_path="$temp_directory/response.json"
actual_path="$temp_directory/actual.txt"
actual_normalized="$temp_directory/actual-normalized.txt"
expected_normalized="$temp_directory/expected-normalized.txt"

normalize_file() {
    jq -b -Rsj 'gsub("\r\n|\r"; "\n")' "$1" >"$2"
}

equivalent_files() {
    local actual="$1"
    local expected="$2"
    local candidate="$temp_directory/candidate.txt"

    cmp -s -- "$actual" "$expected" && return 0
    cp "$actual" "$candidate"
    printf '\n' >>"$candidate"
    cmp -s -- "$candidate" "$expected" && return 0
    cp "$expected" "$candidate"
    printf '\n' >>"$candidate"
    cmp -s -- "$actual" "$candidate"
}

sha256_file() {
    if command -v sha256sum >/dev/null 2>&1; then
        sha256sum "$1" | awk '{print $1}'
    else
        shasum -a 256 "$1" | awk '{print $1}'
    fi
}

write_diagnostic() {
    local reason="$1"
    local attempts="$2"
    [[ -n "$diagnostic_path" ]] || return 0

    mkdir -p "$(dirname "$diagnostic_path")"
    local expected_length actual_length expected_hash actual_hash first_offset
    expected_length="$(wc -c <"$expected_normalized" | tr -d ' ')"
    actual_length="$(wc -c <"$actual_normalized" | tr -d ' ')"
    expected_hash="$(sha256_file "$expected_normalized")"
    actual_hash="$(sha256_file "$actual_normalized")"
    first_offset="$( (cmp -l -- "$actual_normalized" "$expected_normalized" 2>/dev/null || true) | awk 'NR == 1 { print $1 - 1 }')"
    [[ -n "$first_offset" ]] || first_offset="null"

    jq -n \
        --arg repository "$repository" \
        --argjson issueNumber "$issue_number" \
        --arg endpoint "repos/$repository/issues/$issue_number" \
        --argjson attempts "$attempts" \
        --arg observedAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
        --arg reason "$reason" \
        --argjson expectedLength "$expected_length" \
        --argjson actualLength "$actual_length" \
        --arg expectedSha256 "$expected_hash" \
        --arg actualSha256 "$actual_hash" \
        --argjson firstDifferenceOffset "$first_offset" \
        '{
            schemaVersion: 1,
            repository: $repository,
            issueNumber: $issueNumber,
            endpoint: $endpoint,
            attempts: $attempts,
            observedAt: $observedAt,
            reason: $reason,
            expectedLength: $expectedLength,
            actualLength: $actualLength,
            expectedSha256: $expectedSha256,
            actualSha256: $actualSha256,
            firstDifferenceOffset: $firstDifferenceOffset
        }' >"$diagnostic_path"
}

normalize_file "$expected_body_path" "$expected_normalized"
last_reason=""

for ((attempt = 1; attempt <= max_attempts; attempt++)); do
    set +e
    "$gh_command" api "repos/$repository/issues/$issue_number" >"$response_path" 2>"$temp_directory/error.txt"
    exit_code=$?
    set -e

    if [[ $exit_code -ne 0 ]]; then
        last_reason="GitHub REST request failed with exit code $exit_code."
        error_message="$(cat "$temp_directory/error.txt")"
        authentication_error_pattern='HTTP[[:space:]]+(401|403)|[Aa]uthentication|[Nn][Oo][Tt][[:space:]]authorized|[Rr]esource[[:space:]]not[[:space:]]accessible'
        if [[ "$error_message" =~ $authentication_error_pattern ]]; then
            : >"$actual_normalized"
            write_diagnostic "$last_reason" "$attempt"
            fail "unable to fetch issue #$issue_number from $repository: $error_message"
        fi
    elif ! jq -e 'type == "object" and has("body")' "$response_path" >/dev/null 2>&1; then
        last_reason="GitHub REST response was invalid JSON."
    else
        jq -b -j '.body // ""' "$response_path" >"$actual_path"
        normalize_file "$actual_path" "$actual_normalized"
        if equivalent_files "$actual_normalized" "$expected_normalized"; then
            cat "$response_path"
            exit 0
        fi
        last_reason="GitHub issue body differs from the persisted draft."
    fi

    if [[ $attempt -lt $max_attempts ]]; then
        printf 'verify-github-issue-body: %s Retrying issue #%s body verification (%s/%s).\n' \
            "$last_reason" "$issue_number" "$attempt" "$max_attempts" >&2
        ((delay_seconds > 0)) && sleep "$delay_seconds"
    fi
done

[[ -f "$actual_normalized" ]] || : >"$actual_normalized"
write_diagnostic "$last_reason" "$max_attempts"
diagnostic_suffix=""
[[ -z "$diagnostic_path" ]] || diagnostic_suffix=" Diagnostic: $diagnostic_path"
fail "issue #$issue_number body verification failed after $max_attempts attempts. $last_reason$diagnostic_suffix"
```

</details>


---

<sub>35s</sub>

### Copilot

All nine gating questions have substantive resolutions, and exactly five direct implementation tasks were found. The normalized pre-creation child baseline is empty; I’m persisting all five complete issue drafts for pre-mutation validation.

---

<sub>1m 11s</sub>

### `apply_patch`

<details>
<summary>Arguments</summary>

````json
"*** Begin Patch\n*** Add File: 1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/issue-bodies/01-4.1-body.md\n+## Campaign context and required reading\n+\n+On the `experiment/shepherd-control` branch, the directory `1-arrival-deadline-control-remove-before-merge` contains the plan (`add-change-arrival-deadline-feature-ignorance-reduction-plan.md`) and supporting resources (diagrams, decision records). Spike subdirectories are research artifacts — read the plan's Resolution sections for findings, not the spike source code.\n+\n+Read the entire plan before working. Then re-read these exact sections:\n+\n+- `## Goal`, including Domain acceptance behavior and Hard scope constraints.\n+- `### 3.2 — What is the exact domain mutation?`\n+- `### 3.3 — What should happen to an existing itinerary and delivery state?`\n+- `### 3.9 — How will the feature be tested on the prepared historical baseline?`\n+- `### 4.1 — Issue 1: Add the application-layer deadline change operation`\n+- `## Cross-cutting concerns`\n+\n+The binding resolutions are:\n+\n+- Replace the route specification through `Cargo.specifyNewRoute(...)`; preserve the existing origin, destination, and itinerary; then store the aggregate. Do not add a mutable deadline setter.\n+- A routed cargo retains its itinerary. The aggregate recalculates delivery and routing state against the replacement specification; in the established sequential test, routing remains `MISROUTED`.\n+- Extend the sequential Arquillian specification with `testChangeDeadline()` after `testChangeDestination()`. The Open Liberty package gate compiles tests but the historical POM keeps `skipTests=true`; running Arquillian still requires its documented remote Payara environment.\n+\n+Research established that JDK 17 with the existing Open Liberty profile is the mandatory build baseline, that test sources compile while Arquillian execution remains skipped by default, and that modernizing the test runtime is unnecessary and out of scope.\n+\n+## Branch and execution order\n+\n+Use `experiment/shepherd-control` from remote `origin` as the PR base branch. This is task 1 of 5. Tasks are assigned, completed, and merged serially in plan order. Do not begin until this issue is assigned; task 2 must not begin until this task is merged and its gates pass.\n+\n+Start from the prepared feature-absent baseline. Do not cherry-pick, inspect, or copy feature-bearing commits or files.\n+\n+## Implement\n+\n+Add the application-layer operation:\n+\n+```java\n+void changeDeadline(TrackingId trackingId, Date deadline);\n+```\n+\n+Modify only:\n+\n+- `src/main/java/org/eclipse/cargotracker/application/BookingService.java`\n+- `src/main/java/org/eclipse/cargotracker/application/internal/DefaultBookingService.java`\n+- `src/test/java/org/eclipse/cargotracker/application/BookingServiceTest.java`\n+\n+Write the test first. Append `testChangeDeadline()` immediately after `testChangeDestination()` in the established sequential test. Create a deadline one month after the test's original `deadline`, invoke the service, reload through `Cargo.findByTrackingId`, and assert:\n+\n+- origin remains Chicago;\n+- destination remains Helsinki;\n+- the persisted deadline has the same calendar day as requested;\n+- the assigned itinerary is unchanged;\n+- transport status is `NOT_RECEIVED`;\n+- last known location is `Location.UNKNOWN`;\n+- current voyage is `Voyage.NONE`;\n+- the cargo is not misdirected;\n+- ETA is `Delivery.ETA_UNKOWN`;\n+- next expected activity is `Delivery.NO_ACTIVITY`;\n+- the cargo is not unloaded at destination;\n+- routing status remains `MISROUTED`.\n+\n+Implement the operation by loading with `cargoRepository.find(trackingId)`, constructing a new `RouteSpecification` from `cargo.getOrigin()`, the existing specification's destination, and the supplied deadline, applying it with `cargo.specifyNewRoute(...)`, and persisting with `cargoRepository.store(cargo)`. Log the tracking ID and deadline at `Level.INFO` in the style of `changeDestination(...)`.\n+\n+## Completion gates\n+\n+- `BookingServiceTest` compiles and the new test is positioned after `testChangeDestination()`.\n+- `./mvnw clean package -Popenliberty` succeeds on JDK 17.\n+- Review the diff and confirm only the three listed application/test files changed.\n+- Confirm the implementation calls `specifyNewRoute(...)` and stores exactly once rather than mutating persistence state directly.\n+- Confirm the test asserts both the changed deadline and every preserved/recalculated field listed above, preventing later facade/UI tasks from masking a domain regression.\n+\n+## Out of scope\n+\n+Do not change JSF, PrimeFaces, facade, REST, Liberty, Derby, persistence configuration, dependencies, Java EE namespaces, or the Java source/target level. Do not clear or replace the itinerary, reroute cargo, add setters to domain value objects, invent chronological validation, or modernize Arquillian/Payara.\n*** Add File: 1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/issue-bodies/02-4.2-body.md\n+## Campaign context and required reading\n+\n+On the `experiment/shepherd-control` branch, the directory `1-arrival-deadline-control-remove-before-merge` contains the plan (`add-change-arrival-deadline-feature-ignorance-reduction-plan.md`) and supporting resources (diagrams, decision records). Spike subdirectories are research artifacts — read the plan's Resolution sections for findings, not the spike source code.\n+\n+Read the entire plan before working. Then re-read these exact sections:\n+\n+- `## Goal`, including Hard scope constraints.\n+- `### 3.2 — What is the exact domain mutation?`\n+- `### 3.4 — What type crosses the facade boundary?`\n+- `### 3.9 — How will the feature be tested on the prepared historical baseline?`\n+- `### 4.2 — Issue 2: Expose deadline changes through the booking facade`\n+- `## Cross-cutting concerns`\n+\n+The binding resolutions are:\n+\n+- The application service owns the aggregate mutation and uses `Cargo.specifyNewRoute(...)`; the facade must only delegate.\n+- Add `void changeDeadline(String trackingId, Date arrivalDeadline)` to `BookingServiceFacade`. Convert only the identifier with `new TrackingId(trackingId)` and pass the same `Date` to `BookingService.changeDeadline(...)`. Do not introduce a DTO or formatted-string command parameter.\n+- The JDK 17 Open Liberty package is the mandatory executable build gate. Do not add a mocking framework or modernize the historical Arquillian runtime.\n+\n+Research established that `java.util.Date` is already the facade convention used by cargo booking, so introducing a second date representation would add conversion risk without improving the historical architecture.\n+\n+## Branch and execution order\n+\n+Use `experiment/shepherd-control` from remote `origin` as the PR base branch. This is task 2 of 5. Tasks are assigned, completed, and merged serially in plan order. Do not begin until this issue is assigned and task 1 has been merged with all gates passing. Task 3 must wait for this task.\n+\n+## Implement\n+\n+Modify:\n+\n+- `src/main/java/org/eclipse/cargotracker/interfaces/booking/facade/BookingServiceFacade.java`\n+- `src/main/java/org/eclipse/cargotracker/interfaces/booking/facade/internal/DefaultBookingServiceFacade.java`\n+\n+Optionally add:\n+\n+- `src/test/java/org/eclipse/cargotracker/interfaces/booking/facade/internal/DefaultBookingServiceFacadeTest.java`\n+\n+Add this facade API:\n+\n+```java\n+void changeDeadline(String trackingId, Date arrivalDeadline);\n+```\n+\n+Implement it as a single delegation:\n+\n+```java\n+bookingService.changeDeadline(\n+        new TrackingId(trackingId),\n+        arrivalDeadline);\n+```\n+\n+If a focused container-free test is practical with the existing test setup, use a hand-written fake or spy to prove the tracking string becomes an equivalent `TrackingId`, the same date object/value reaches the application service, delegation occurs exactly once, and the facade performs no repository work. Do not weaken or rewrite the application-layer test from task 1.\n+\n+## Completion gates\n+\n+- Existing facade consumers and all test sources compile.\n+- `./mvnw clean package -Popenliberty` succeeds on JDK 17.\n+- The facade signature uses `String` and `java.util.Date`, with no domain aggregate or JSF/PrimeFaces types exposed.\n+- The implementation delegates once and contains no cargo loading, repository storage, or formatted-date parsing.\n+- The task 1 application test remains unchanged and compiling.\n+- If the optional focused test is omitted, document why the current historical test setup makes it impractical; do not add dependencies merely to enable it.\n+\n+## Out of scope\n+\n+Do not load or mutate `Cargo` in the facade, call `CargoRepository`, parse date strings, add a command DTO, introduce JSF/PrimeFaces code, change the domain mutation from task 1, add test dependencies, or modify application runtime/configuration files.\n*** Add File: 1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/issue-bodies/03-4.3-body.md\n+## Campaign context and required reading\n+\n+On the `experiment/shepherd-control` branch, the directory `1-arrival-deadline-control-remove-before-merge` contains the plan (`add-change-arrival-deadline-feature-ignorance-reduction-plan.md`) and supporting resources (diagrams, decision records). Spike subdirectories are research artifacts — read the plan's Resolution sections for findings, not the spike source code.\n+\n+Read the entire plan before working. Then re-read these exact sections:\n+\n+- `## Goal`, including User-visible acceptance behavior and Hard scope constraints.\n+- `### 3.4 — What type crosses the facade boundary?`\n+- `### 3.5 — How is the DTO's formatted deadline converted for editing?`\n+- `### 3.6 — Which JSF bean scopes and interaction pattern should be used?`\n+- `### 3.7 — What is the dynamic-dialog contract?`\n+- `### 3.8 — What date validation is required?`\n+- `### 3.9 — How will the feature be tested on the prepared historical baseline?`\n+- `### 4.3 — Issue 3: Implement the deadline editor backing model`\n+- `## Cross-cutting concerns`\n+\n+The binding resolutions are:\n+\n+- Keep domain types behind the facade; submit the tracking ID as `String` and deadline as `java.util.Date`.\n+- Load `CargoRoute` through `BookingServiceFacade.loadCargoForRouting(trackingId)`. Use a per-load `new SimpleDateFormat(\"MM/dd/yyyy\")` to parse the leading date portion of `cargo.getArrivalDeadline()`; do not add a shared mutable formatter.\n+- Implement a serializable CDI `@Named @ViewScoped` editor bean named `changeArrivalDeadlineDate`.\n+- Successful submission closes dynamically with `\"DONE\"`.\n+- Reject null, but add no minimum-date, future-date, old-deadline, or itinerary-date rule.\n+- Build on JDK 17/Open Liberty; use hand-written test fakes if practical and add no mocking dependency.\n+\n+Research established that `CargoRoute.getArrivalDeadline()` begins with `MM/dd/yyyy` before its time and zone, so `SimpleDateFormat.parse(...)` yields the date shown by the table. Parsing failures must remain visible; silently producing null would turn malformed server data into a misleading validation or update flow.\n+\n+## Branch and execution order\n+\n+Use `experiment/shepherd-control` from remote `origin` as the PR base branch. This is task 3 of 5. Tasks are assigned, completed, and merged serially in plan order. Do not begin until assigned and tasks 1 and 2 are merged with gates passing. Task 4 must wait for this task.\n+\n+## Implement\n+\n+Create:\n+\n+- `src/main/java/org/eclipse/cargotracker/interfaces/booking/web/ChangeArrivalDeadlineDate.java`\n+\n+Use this state and established annotations:\n+\n+```java\n+@Named\n+@ViewScoped\n+public class ChangeArrivalDeadlineDate implements Serializable {\n+    private static final long serialVersionUID = 1L;\n+\n+    private String trackingId;\n+    private CargoRoute cargo;\n+    private Date arrivalDeadlineDate;\n+\n+    @Inject\n+    private BookingServiceFacade bookingServiceFacade;\n+}\n+```\n+\n+Provide `getTrackingId()`, `setTrackingId(String)`, `getCargo()`, `getArrivalDeadlineDate()`, `setArrivalDeadlineDate(Date)`, `load()`, and `changeArrivalDeadline()`.\n+\n+`load()` must request the cargo for the current tracking ID through the facade, retain the returned DTO, parse its formatted deadline with a newly created `SimpleDateFormat(\"MM/dd/yyyy\")`, and assign the editable date. Surface parsing failure through the repository's established application/view error behavior; do not swallow it, print a stack trace, or continue with null.\n+\n+`changeArrivalDeadline()` must reject null, call `bookingServiceFacade.changeDeadline(trackingId, arrivalDeadlineDate)`, and only after successful return call `PrimeFaces.current().dialog().closeDynamic(\"DONE\")`. A facade exception must leave the dialog open and remain visible.\n+\n+If practical, add a container-free JUnit test with a hand-written facade fake covering the correct load ID, valid date conversion, submit delegation, malformed input failure, and null rejection.\n+\n+## Completion gates\n+\n+- The new bean is serializable and uses the existing CDI `@Named` and JSF `@ViewScoped` conventions.\n+- It references facade interfaces/DTOs but no domain model or repository classes.\n+- Valid `MM/dd/yyyy` data loads to the same displayed calendar day.\n+- A malformed deadline produces an explicit failure and never silently becomes null.\n+- Null submission is rejected; successful submission delegates once and closes with `\"DONE\"` only afterward.\n+- `./mvnw clean package -Popenliberty` succeeds on JDK 17, and prior task tests remain compiling.\n+\n+## Out of scope\n+\n+Do not create the launcher or XHTML yet. Do not query repositories/domain objects, broaden `CargoRoute`, add shared `SimpleDateFormat` state, introduce a new timezone policy, add chronological business rules, swallow failures, add a mocking framework, or change prior application/facade contracts.\n*** Add File: 1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/issue-bodies/04-4.4-body.md\n+## Campaign context and required reading\n+\n+On the `experiment/shepherd-control` branch, the directory `1-arrival-deadline-control-remove-before-merge` contains the plan (`add-change-arrival-deadline-feature-ignorance-reduction-plan.md`) and supporting resources (diagrams, decision records). Spike subdirectories are research artifacts — read the plan's Resolution sections for findings, not the spike source code.\n+\n+Read the entire plan before working. Then re-read these exact sections:\n+\n+- `## Goal`, especially User-visible acceptance behavior and Hard scope constraints.\n+- `### 3.5 — How is the DTO's formatted deadline converted for editing?`\n+- `### 3.6 — Which JSF bean scopes and interaction pattern should be used?`\n+- `### 3.7 — What is the dynamic-dialog contract?`\n+- `### 3.8 — What date validation is required?`\n+- `### 3.9 — How will the feature be tested on the prepared historical baseline?`\n+- `### 4.4 — Issue 4: Implement the PrimeFaces deadline dialog`\n+- `## Cross-cutting concerns`\n+\n+The binding resolutions are:\n+\n+- Mirror Change Destination: a serializable session-scoped JSF managed launcher opens a PrimeFaces dynamic dialog backed by the task 3 CDI view-scoped editor.\n+- Open `/admin/dialogs/changeArrivalDeadlineDate.xhtml` with one `trackingId` parameter. Set modal and draggable to `true`, resizable to `false`, width `410`, and height `280`.\n+- Success closes with `\"DONE\"`; Cancel closes with `\"\"`.\n+- Put `<f:metadata>` directly beneath root `<html>`, before `<h:head>` and `<h:body>`, to satisfy MyFaces `UIViewRoot`.\n+- Require a date but impose no chronological business rule.\n+- Mandatory runtime evidence uses JDK 17/Open Liberty and direct HTTP/UI checks; preserve the remote Payara Arquillian path.\n+\n+Research established that nested view metadata fails under this prepared MyFaces baseline, while root-level metadata correctly binds the query parameter and invokes the load action. The existing destination dialog is the production interaction pattern; reproduce its lifecycle and styling without copying research spike code.\n+\n+## Branch and execution order\n+\n+Use `experiment/shepherd-control` from remote `origin` as the PR base branch. This is task 4 of 5. Tasks are assigned, completed, and merged serially in plan order. Do not begin until assigned and tasks 1-3 are merged with gates passing. Task 5 must wait for this task.\n+\n+## Implement\n+\n+Create:\n+\n+- `src/main/java/org/eclipse/cargotracker/interfaces/booking/web/ChangeArrivalDeadlineDateDialog.java`\n+- `src/main/webapp/admin/dialogs/changeArrivalDeadlineDate.xhtml`\n+\n+The launcher must be serializable and use:\n+\n+```java\n+@ManagedBean(name = \"changeArrivalDeadlineDateDialog\")\n+@SessionScoped\n+```\n+\n+Implement `showDialog(String trackingId)`, `handleReturn(SelectEvent event)`, and `cancel()`. `showDialog(...)` passes `trackingId` as a `Map<String, List<String>>` request parameter and opens the required path with the exact options above. `cancel()` closes dynamically with the empty string and never calls the facade.\n+\n+The XHTML title is `Change Deadline`. Place this metadata before the head:\n+\n+```xhtml\n+<f:metadata>\n+    <f:viewParam name=\"trackingId\"\n+                 value=\"#{changeArrivalDeadlineDate.trackingId}\"/>\n+    <f:viewAction action=\"#{changeArrivalDeadlineDate.load}\"/>\n+</f:metadata>\n+```\n+\n+Render read-only Origin and Destination values from `cargo.originName` and `cargo.finalDestinationName`. Bind a required, labeled `p:datePicker` to `arrivalDeadlineDate` and provide validation feedback. Cancel invokes the launcher; Update invokes the editor's `changeArrivalDeadline()`. Preserve the return-handler contract needed to refresh `tableNotRouted` once task 5 connects the caller.\n+\n+## Completion gates\n+\n+- `./mvnw clean package -Popenliberty liberty:run` succeeds on JDK 17.\n+- A direct request to `/cargo-tracker/admin/dialogs/changeArrivalDeadlineDate.xhtml?trackingId=DEF789` returns HTTP 200 with title, origin, destination, and selected existing deadline.\n+- Runtime logs contain no `TagException`, `Parent UIComponent`, `FacesException`, or server error for the dialog.\n+- Cancel leaves the deadline unchanged; Update changes it and closes successfully.\n+- Destination editing still works.\n+- Metadata is physically located before `<h:head>` and `<h:body>`.\n+- Stop Liberty cleanly before completion.\n+\n+## Out of scope\n+\n+Do not integrate the dashboard command link yet. Do not add navigation or inline editing, move metadata into the body, change the task 3 parsing/facade behavior, create chronological policy, modernize JSF/Java EE, replace Open Liberty/Derby, or alter destination editing.\n*** Add File: 1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/issue-bodies/05-4.5-body.md\n+## Campaign context and required reading\n+\n+On the `experiment/shepherd-control` branch, the directory `1-arrival-deadline-control-remove-before-merge` contains the plan (`add-change-arrival-deadline-feature-ignorance-reduction-plan.md`) and supporting resources (diagrams, decision records). Spike subdirectories are research artifacts — read the plan's Resolution sections for findings, not the spike source code.\n+\n+Read the entire plan before working. Then re-read these exact sections:\n+\n+- `## Goal`, including all User-visible acceptance behavior, Domain acceptance behavior, and Hard scope constraints.\n+- `### 3.1 — Which cargos expose the edit operation?`\n+- `### 3.6 — Which JSF bean scopes and interaction pattern should be used?`\n+- `### 3.7 — What is the dynamic-dialog contract?`\n+- `### 3.8 — What date validation is required?`\n+- `### 3.9 — How will the feature be tested on the prepared historical baseline?`\n+- `### 4.5 — Issue 5: Integrate deadline editing into the Administration dashboard`\n+- `## Phase 5 — Documentation and implementation handoff`\n+- `## Cross-cutting concerns`\n+\n+The binding resolutions are:\n+\n+- Expose editing only in `src/main/webapp/admin/tables/listNotRouted.xhtml`; application/facade operations remain generally callable for valid tracking IDs.\n+- Mirror the existing Change Destination dynamic-dialog behavior.\n+- The dialog returns `\"DONE\"` after update or `\"\"` on cancel; the caller handles `dialogReturn` and updates `tableNotRouted`.\n+- Require a non-null date without adding future-date, old-deadline, today, or itinerary chronology rules.\n+- The mandatory gate is JDK 17/Open Liberty plus direct HTTP and the complete `DEF789` browser flow; preserve the historical skipped-by-default Arquillian setup.\n+\n+Research established that the adjacent Destination column is the compatible PrimeFaces command-link and dialog-return pattern. The final acceptance is calendar-date based, and in-memory data persists only for the running sample application and resets after rebuild/restart.\n+\n+## Branch and execution order\n+\n+Use `experiment/shepherd-control` from remote `origin` as the PR base branch. This is task 5 of 5. Tasks are assigned, completed, and merged serially in plan order. Do not begin until assigned and tasks 1-4 are merged with all gates passing.\n+\n+## Implement\n+\n+Modify:\n+\n+- `src/main/webapp/admin/tables/listNotRouted.xhtml`\n+\n+Within the existing Deadline column, replace plain deadline text with a `p:commandLink` that:\n+\n+- calls `changeArrivalDeadlineDateDialog.showDialog(cargoNotRouted.trackingId)`;\n+- continues displaying `cargoNotRouted.arrivalDeadlineDate`;\n+- uses the existing Font Awesome edit-icon styling;\n+- has stable ID `arrivalDeadlineToUpdate` or an equally stable equivalent;\n+- contains a `dialogReturn` listener invoking `changeArrivalDeadlineDateDialog.handleReturn`;\n+- updates `tableNotRouted`;\n+- exposes tooltip text exactly `Click to change cargo arrival deadline date.`\n+\n+Follow the adjacent Destination column's structure and styling. Do not alter destination editing, tracking-ID routing, or other Administration tables.\n+\n+Update `README.md` only if it already enumerates user-facing Administration capabilities; if so, add one concise sentence about changing an unrouted cargo's arrival deadline.\n+\n+Perform the full `DEF789` acceptance flow: record the original deadline; confirm icon and tooltip; open the dialog and verify cargo context; choose a visibly different date; Update; verify close and table refresh; reload and verify persistence; reopen and verify initialization to the changed date; Cancel and verify no change; then verify the Destination dialog and routing selection still load.\n+\n+## Completion gates\n+\n+- `./mvnw clean package -Popenliberty liberty:run` succeeds on JDK 17 and the home page returns HTTP 200.\n+- The complete `DEF789` acceptance flow passes, with before/after dates recorded in PR evidence and an explicit note that in-memory data resets on rebuild/restart.\n+- Logs contain none of `<f:metadata> Parent UIComponent`, `TagException`, `VerifyError`, `FacesException`, `CWWKZ0002E`, `CWWKZ0003E`, recurring batch authorization failures, or new feature-attributable FFDC files. Transient JMS activation-order warnings are acceptable only if all endpoints later activate.\n+- Destination editing and routing selection remain functional.\n+- `./mvnw clean package -Popenliberty` succeeds after runtime verification; test sources compile.\n+- No Java EE namespace/runtime/configuration compatibility work from the baseline is reverted.\n+- Stop Liberty cleanly.\n+\n+## Out of scope\n+\n+Do not expose deadline editing in routed, misrouted, claimed, details, or other tables. Do not redesign routing, destination editing, tracking, REST, messaging, batch, persistence, Liberty, Derby, or JSF metadata. Do not migrate to Jakarta EE/Spring, add chronological rules, or change the five-task architecture.\n*** End Patch\n"
````

</details>

```
Added 5 file(s): /Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/issue-bodies/01-4.1-body.md, /Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/issue-bodies/02-4.2-body.md, /Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/issue-bodies/03-4.3-body.md, /Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/issue-bodies/04-4.4-body.md, /Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936/issue-bodies/05-4.5-body.md
```


---

<sub>1m 17s</sub>

### `bash`

**Validate drafts and initialize ledger**

$ export IDEA_HOME="/Applications/IntelliJ IDEA CE.app/Contents/MacOS" APPCAT_HOME=/Users/edburns/.appcat JAVA_HOME="/Library/Java/JavaVirtualMachines/microsoft-25.jdk/Contents/Home" ANT_HOME="$HOME/Downloads/apache-ant-1.10.13" M2_HOME="$HOME/Downloads/apache-maven-3.9.8"; export PATH="$APPCAT_HOME:$M2_HOME/bin:$ANT_HOME/bin:$JAVA_HOME/bin:$IDEA_HOME:$PATH"; set -euo pipefail
LOG='/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936'
DV='/Users/edburns/.copilot/plugins/shepherd-task/scripts/validate-stage20-drafts.sh'
"$DV" "$LOG/issue-bodies" 5 off
atomic_write() { local target="$1" content="$2" tmp; tmp="$(mktemp "${target}.tmp.XXXXXX")"; printf '%s\n' "$content" > "$tmp"; mv "$tmp" "$target"; }
atomic_write "$LOG/creation-ledger.json" '[]'
atomic_write "$LOG/stage-20-result.json" '{"schemaVersion":1,"status":"in_progress","ledgerFile":"creation-ledger.json","operationError":null}'
printf 'Draft validation passed; ledger and result initialized.\n'

```
Draft validation passed; ledger and result initialized.
<shellId: 4 completed with exit code 0>
```


---

<sub>1m 21s</sub>

### Copilot

The five persisted drafts passed the supplied validator, and the authoritative ledger/result artifacts are initialized. I’m beginning the one-shot create, exact-body verification, and child-link sequence in plan order.

---

<sub>1m 42s</sub>

### `bash`

**Create verify and link five issues**

$ export IDEA_HOME="/Applications/IntelliJ IDEA CE.app/Contents/MacOS" APPCAT_HOME=/Users/edburns/.appcat JAVA_HOME="/Library/Java/JavaVirtualMachines/microsoft-25.jdk/Contents/Home" ANT_HOME="$HOME/Downloads/apache-ant-1.10.13" M2_HOME="$HOME/Downloads/apache-maven-3.9.8"; export PATH="$APPCAT_HOME:$M2_HOME/bin:$ANT_HOME/bin:$JAVA_HOME/bin:$IDEA_HOME:$PATH"; set -uo pipefail
REPO='edburns/dd-3070518-cargotracker-darwin-arm64-01'
PARENT=1
LOG='/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936'
LEDGER="$LOG/creation-ledger.json"
RESULT="$LOG/stage-20-result.json"
VERIFIER='/Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-github-issue-body.sh'

titles=(
  '4.1 — Add the application-layer deadline change operation'
  '4.2 — Expose deadline changes through the booking facade'
  '4.3 — Implement the deadline editor backing model'
  '4.4 — Implement the PrimeFaces deadline dialog'
  '4.5 — Integrate deadline editing into the Administration dashboard'
)
subsections=('4.1' '4.2' '4.3' '4.4' '4.5')
body_files=(
  "$LOG/issue-bodies/01-4.1-body.md"
  "$LOG/issue-bodies/02-4.2-body.md"
  "$LOG/issue-bodies/03-4.3-body.md"
  "$LOG/issue-bodies/04-4.4-body.md"
  "$LOG/issue-bodies/05-4.5-body.md"
)

atomic_write() {
  local target="$1" content="$2" tmp
  tmp="$(mktemp "${target}.tmp.XXXXXX")" || return 1
  printf '%s\n' "$content" > "$tmp" || return 1
  mv "$tmp" "$target"
}

update_ledger_flag() {
  local number="$1" field="$2" value="$3" updated
  updated="$(
    jq \
      --argjson number "$number" \
      --arg field "$field" \
      --argjson value "$value" \
      'map(if .number == $number then .[$field] = $value else . end)' \
      "$LEDGER"
  )" || return 1
  atomic_write "$LEDGER" "$updated"
}

reconcile_and_fail() {
  local operation="$1" error="$2" raw normalized reconciled result_json
  if raw=$(gh api "repos/$REPO/issues/$PARENT/sub_issues" --paginate --slurp 2>&1); then
    if normalized=$(printf '%s' "$raw" | jq 'if length == 0 then [] elif all(.[]; type == "array") then add else . end' 2>/dev/null); then
      reconciled=$(jq --argjson children "$normalized" 'map(.linked = ([ $children[].id ] | index(.id) != null))' "$LEDGER" 2>/dev/null) || reconciled=$(cat "$LEDGER")
      atomic_write "$LEDGER" "$reconciled" || true
    fi
  fi
  result_json=$(jq -n --arg op "$operation" --arg err "$error" '{schemaVersion:1,status:"failed",ledgerFile:"creation-ledger.json",operationError:($op + ": " + $err)}')
  atomic_write "$RESULT" "$result_json" || true
  printf 'STAGE20_FAILED operation=%s error=%s\n' "$operation" "$error" >&2
  jq -r '.[] | "#\(.number) | \(.title) | \(.url) | \(.bodyFile) | body_verified=\(.body_verified) | linked=\(.linked)"' "$LEDGER" >&2
  jq -r --arg repo "$REPO" '.[] | "gh issue delete \(.number) --repo \"" + $repo + "\" --yes"' "$LEDGER" >&2
  exit 1
}

for i in 0 1 2 3 4; do
  title="${titles[$i]}"
  subsection="${subsections[$i]}"
  body="${body_files[$i]}"
  rel_body="issue-bodies/$(basename "$body")"
  create_err="$LOG/.create-error-$i.txt"
  if ! issue_json=$(gh api "repos/$REPO/issues" -X POST -f title="$title" -F "body=@$body" --jq '{id,number,node_id,html_url,title}' 2>"$create_err"); then
    error=$(cat "$create_err")
    reconcile_and_fail "create issue for subsection $subsection" "$error"
  fi
  id=$(printf '%s' "$issue_json" | jq -er '.id') || reconcile_and_fail "parse created issue for subsection $subsection" "Missing issue id"
  number=$(printf '%s' "$issue_json" | jq -er '.number') || reconcile_and_fail "parse created issue for subsection $subsection" "Missing issue number"
  url=$(printf '%s' "$issue_json" | jq -er '.html_url') || reconcile_and_fail "parse created issue for subsection $subsection" "Missing issue URL"
  actual_title=$(printf '%s' "$issue_json" | jq -er '.title') || reconcile_and_fail "parse created issue for subsection $subsection" "Missing issue title"
  updated=$(jq \
    --arg implementationSubsection "$subsection" \
    --arg bodyFile "$rel_body" \
    --argjson id "$id" \
    --argjson number "$number" \
    --arg title "$actual_title" \
    --arg url "$url" \
    '. + [{implementationSubsection:$implementationSubsection,bodyFile:$bodyFile,id:$id,number:$number,title:$title,url:$url,body_verified:false,linked:false}]' \
    "$LEDGER") || reconcile_and_fail "append ledger for issue $number" "Unable to update creation ledger"
  atomic_write "$LEDGER" "$updated" || reconcile_and_fail "persist ledger for issue $number" "Unable to write creation ledger"
  if ! "$VERIFIER" "$REPO" "$number" "$body" 6 5 "$LOG/issue-$number-body-verification-failure.json" >/dev/null; then
    reconcile_and_fail "verify body for issue $number" "Persisted GitHub body did not match draft"
  fi
  update_ledger_flag "$number" body_verified true || reconcile_and_fail "persist body verification for issue $number" "Unable to update creation ledger"
  linked=false
  link_error=''
  for attempt in 1 2 3; do
    if printf '{"sub_issue_id": %s}' "$id" | gh api "repos/$REPO/issues/$PARENT/sub_issues" -X POST --input - >/dev/null 2>"$LOG/.link-error-$number.txt"; then
      linked=true
      break
    fi
    link_error=$(cat "$LOG/.link-error-$number.txt")
    sleep 1
  done
  if [ "$linked" != true ]; then
    reconcile_and_fail "link issue $number to parent $PARENT" "$link_error"
  fi
  update_ledger_flag "$number" linked true || reconcile_and_fail "persist link status for issue $number" "Unable to update creation ledger"
  printf 'Created, verified, and linked #%s: %s\n' "$number" "$actual_title"
done
rm -f "$LOG"/.create-error-*.txt "$LOG"/.link-error-*.txt
printf 'Creation sequence complete.\n'

<details>
<summary>7 lines</summary>

```
Created, verified, and linked #2: 4.1 — Add the application-layer deadline change operation
Created, verified, and linked #3: 4.2 — Expose deadline changes through the booking facade
Created, verified, and linked #4: 4.3 — Implement the deadline editor backing model
Created, verified, and linked #5: 4.4 — Implement the PrimeFaces deadline dialog
Created, verified, and linked #6: 4.5 — Integrate deadline editing into the Administration dashboard
Creation sequence complete.
<shellId: 5 completed with exit code 0>
```

</details>


---

<sub>1m 46s</sub>

### Copilot

All five issues were created, body-verified, and linked in order. I’m running the authoritative child-link verifier plus final body, open-state, and unassigned postconditions before marking stage 20 complete.

---

<sub>2m 43s</sub>

### `bash`

**Verify final stage 20 postconditions**

$ export IDEA_HOME="/Applications/IntelliJ IDEA CE.app/Contents/MacOS" APPCAT_HOME=/Users/edburns/.appcat JAVA_HOME="/Library/Java/JavaVirtualMachines/microsoft-25.jdk/Contents/Home" ANT_HOME="$HOME/Downloads/apache-ant-1.10.13" M2_HOME="$HOME/Downloads/apache-maven-3.9.8"; export PATH="$APPCAT_HOME:$M2_HOME/bin:$ANT_HOME/bin:$JAVA_HOME/bin:$IDEA_HOME:$PATH"; set -uo pipefail
REPO='edburns/dd-3070518-cargotracker-darwin-arm64-01'; PARENT=1
LOG='/Users/edburns/workareas/dd-3070518-cargotracker-darwin-arm64-01-shepherd-control/1-arrival-deadline-control-remove-before-merge/prompts/shepherd-task-20-20260928-1936'
LEDGER="$LOG/creation-ledger.json"; RESULT="$LOG/stage-20-result.json"
BODY_VERIFIER='/Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-github-issue-body.sh'
CHILD_VERIFIER='/Users/edburns/.copilot/plugins/shepherd-task/scripts/verify-stage20-child-links.sh'
atomic_write() { local target="$1" content="$2" tmp; tmp="$(mktemp "${target}.tmp.XXXXXX")" || return 1; printf '%s\n' "$content" > "$tmp" || return 1; mv "$tmp" "$target"; }
reconcile_and_fail() {
  local operation="$1" error="$2" raw normalized reconciled result_json
  if raw=$(gh api "repos/$REPO/issues/$PARENT/sub_issues" --paginate --slurp 2>&1); then
    if normalized=$(printf '%s' "$raw" | jq 'if length == 0 then [] elif all(.[]; type == "array") then add else . end' 2>/dev/null); then
      reconciled=$(jq --argjson children "$normalized" 'map(.linked = ([ $children[].id ] | index(.id) != null))' "$LEDGER" 2>/dev/null) || reconciled=$(cat "$LEDGER")
      atomic_write "$LEDGER" "$reconciled" || true
    fi
  fi
  result_json=$(jq -n --arg op "$operation" --arg err "$error" '{schemaVersion:1,status:"failed",ledgerFile:"creation-ledger.json",operationError:($op + ": " + $err)}')
  atomic_write "$RESULT" "$result_json" || true
  printf 'STAGE20_FAILED operation=%s error=%s\n' "$operation" "$error" >&2
  jq -r '.[] | "#\(.number) | \(.title) | \(.url) | \(.bodyFile) | body_verified=\(.body_verified) | linked=\(.linked)"' "$LEDGER" >&2
  jq -r --arg repo "$REPO" '.[] | "gh issue delete \(.number) --repo \"" + $repo + "\" --yes"' "$LEDGER" >&2
  exit 1
}
raw=$(gh api "repos/$REPO/issues/$PARENT/sub_issues" --paginate --slurp 2>&1) || reconcile_and_fail 'fetch final child snapshot' "$raw"
normalized=$(printf '%s' "$raw" | jq 'if length == 0 then [] elif all(.[]; type == "array") then add else . end') || reconcile_and_fail 'normalize final child snapshot' 'jq normalization failed'
atomic_write "$LOG/final-children.json" "$normalized" || reconcile_and_fail 'persist final child snapshot' 'Unable to write final-children.json'
child_output=$("$CHILD_VERIFIER" "$LOG/pre-creation-children.json" "$LOG/final-children.json" "$LEDGER" 2>&1) || reconcile_and_fail 'verify child links' "$child_output"
printf '%s\n' "$child_output"
while IFS=$'\t' read -r number body_file; do
  absolute_body="$LOG/$body_file"
  if ! "$BODY_VERIFIER" "$REPO" "$number" "$absolute_body" 6 5 "$LOG/issue-$number-final-body-verification-failure.json" >/dev/null; then
    reconcile_and_fail "final body verification for issue $number" 'Persisted GitHub body did not match draft'
  fi
  issue_state=$(gh api "repos/$REPO/issues/$number" --jq '[.state, (.assignees | length)] | @tsv' 2>&1) || reconcile_and_fail "fetch final state for issue $number" "$issue_state"
  state=$(printf '%s' "$issue_state" | cut -f1); assignees=$(printf '%s' "$issue_state" | cut -f2)
  [ "$state" = open ] || reconcile_and_fail "verify issue $number is open" "Observed state: $state"
  [ "$assignees" = 0 ] || reconcile_and_fail "verify issue $number is unassigned" "Observed assignee count: $assignees"
done < <(jq -r '.[] | [.number,.bodyFile] | @tsv' "$LEDGER")
complete='{"schemaVersion":1,"status":"complete","ledgerFile":"creation-ledger.json","operationError":null}'
atomic_write "$RESULT" "$complete" || reconcile_and_fail 'persist completion result' 'Unable to write stage-20-result.json'
printf 'FINAL_LEDGER\n'
jq -r '.[] | [.implementationSubsection,(.number|tostring),.title,.url] | @tsv' "$LEDGER"
printf 'RESULT\n'; cat "$RESULT"

<details>
<summary>24 lines</summary>

```
{
  "baselineCount": 0,
  "ledgerCount": 5,
  "finalCount": 5,
  "newIssueIds": [
    5622753605,
    5622753866,
    5622754190,
    5622754445,
    5622754668
  ]
}
verify-github-issue-body: GitHub REST request failed with exit code 1. Retrying issue #3 body verification (1/6).
verify-github-issue-body: GitHub REST request failed with exit code 1. Retrying issue #3 body verification (2/6).
verify-github-issue-body: GitHub REST request failed with exit code 1. Retrying issue #3 body verification (3/6).
FINAL_LEDGER
4.1	2	4.1 — Add the application-layer deadline change operation	https://github.com/edburns/dd-3070518-cargotracker-darwin-arm64-01/issues/2
4.2	3	4.2 — Expose deadline changes through the booking facade	https://github.com/edburns/dd-3070518-cargotracker-darwin-arm64-01/issues/3
4.3	4	4.3 — Implement the deadline editor backing model	https://github.com/edburns/dd-3070518-cargotracker-darwin-arm64-01/issues/4
4.4	5	4.4 — Implement the PrimeFaces deadline dialog	https://github.com/edburns/dd-3070518-cargotracker-darwin-arm64-01/issues/5
4.5	6	4.5 — Integrate deadline editing into the Administration dashboard	https://github.com/edburns/dd-3070518-cargotracker-darwin-arm64-01/issues/6
RESULT
{"schemaVersion":1,"status":"complete","ledgerFile":"creation-ledger.json","operationError":null}
<shellId: 6 completed with exit code 0>
```

</details>


---

<sub>2m 47s</sub>

### `rg`

**shepherd-task-25-given-list|given-list**

"shepherd-task-25-given-list|given-list" in *.{md,sh,ps1,json} (~/.copilot/plugins/shepherd-task)

<details>
<summary>110 matches</summary>

```
[grep content: 94 matches across 49 file(s) under /Users/edburns/.copilot/plugins/shepherd-task]

figure (7 match(es)):
  05- post-mortem.md:3:The given-list exit path invokes stage 50 for both successful and failed runs.
  02- shepherd-task.md:4:existing given-list run directory. It derives repository, base branch, campaign
  02- shepherd-task.md:10:    participant GL as Stage 25 given-list runner
  01- shepherd-task-25-given-list.md:1:# Figure 01 — Stage 25 given-list batch orchestration
  01- shepherd-task-25-given-list.md:3:Stage 25 (`shepherd-task-25-given-list`) owns one serial run. It validates the durable campaign
  01- shepherd-task-25-given-list.md:12:    participant GL as Stage 25: shepherd-task-25-given-list
  01- shepherd-task-25-given-list.md:14:    participant RM as given-list run manifest
making-of.md:248: `shepherd-task-25-given-list-run.json`. The run begins as `running` and is

workshop.md (3 match(es)):
  215: & 'C:\Users\edburns\.copilot\plugins\shepherd-task\scripts\shepherd-task-25-given-list.ps1' `
  229: /Users/edburns/.copilot/plugins/shepherd-task/scripts/shepherd-task-25-given-list.sh 2\,3 1-math-control-remove-before-merge
  241: By the time you have invoked `shepherd-task-25-given-list` the work proceeds in an entirely human hands-off manner. See `awesome-copilot-01/plugins/shepherd-task/README.md` Sections **Stage 30 readiness boundary** through **Workflow approval helper** and **Post-mortem behavior**.
scripts/shepherd-task.ps1:22:     Existing shepherd-task-25-given-list run directory.
scripts/shepherd-task-monitor.sh:9: # Run this in a SEPARATE terminal while shepherd-task-25-given-list.sh is running.

README.md (16 match(es)):
  33: - one or more `shepherd-task-25-given-list` runs.
  54: | 25         |                                                    | `shepherd-task-25-given-list`                     | Runs selected child issues serially, invokes `shepherd-task` separately for each issue to perform stages 30 and 40, and always invokes stage 50 |
  57: | 50         | `shepherd-task-50-create-post-mortem`              |                                                   | Writes an evidence-based report for the given-list run                               |
  256: ./plugins/shepherd-task/scripts/shepherd-task-25-given-list.sh \
  264: .\plugins\shepherd-task\scripts\shepherd-task-25-given-list.ps1 `
  295: - [Figure 01 — stage 25 given-list batch orchestration](figure-01-shepherd-task-25-given-list.md)
  307:   <given-list-run-directory> \
  445: Every given-list invocation creates
  446: `shepherd-task-25-given-list-run.json`:
  536:     ├── shepherd-task-25-given-list-run.json
  548: Each given-list invocation has its own run directory. A campaign may have
  553: The given-list exit path invokes stage 50 after success or failure. Stage 50
  578:   <given-list-run-directory>
  598: - A given-list run stops on the first failed issue but still runs stage 50 and
  600: - Resume a campaign by starting a new given-list run with the remaining issues.
  617: | `scripts/shepherd-task-25-given-list.*` | Run stage 25: create a run and dispatch issues serially |

scripts/shepherd-task (5 match(es)):
  25- given-list.ps1:65:$runManifestPath = Join-Path $logDirFull 'shepherd-task-25-given-list-run.json'
  25- given-list.ps1:117:    Write-Host "Logging shepherd-task-25-given-list run to: $logDirFull"
  25- given-list.sh:6:#   ./shepherd-task-25-given-list.sh <TASK_ISSUES> <CAMPAIGN_METADATA_DIRECTORY>
  25- given-list.sh:80:RUN_MANIFEST="$LOG_DIR_FULL/shepherd-task-25-given-list-run.json"
  25- given-list.sh:116:echo "Logging shepherd-task-25-given-list run to: $LOG_DIR_FULL"
scripts/shepherd-task-monitor.ps1:10:     Run this in a SEPARATE terminal while shepherd-task-25-given-list.ps1 is running.
test/simple-math/10-simple-math-fixture-contract.ps1:112:     "scripts\\shepherd-task-25-given-list\.ps1"

skills/shepherd-task (4 match(es)):
  20- create-issues-from-plan/SKILL.md:325:2. Comma-separated child issue numbers for `shepherd-task-25-given-list`.
  20- create-issues-from-plan/SKILL.md:326:3. Suggested campaign-aware given-list invocation using the ordered issue numbers and `PLAN_DIRECTORY`; stage 25 derives `LESSON_PROPAGATION` from the campaign manifest.
  50- create-post-mortem/SKILL.md:13:This skill is designed to be invoked from `shepherd-task-25-given-list.ps1` / `shepherd-task-25-given-list.sh` in a `finally` / `trap EXIT` path so it runs for **all outcomes**, not only after success.
  50- create-post-mortem/SKILL.md:60:2. If `shepherd-task-25-given-list-run.json` exists, verify its campaign ID,
test/simple-math/07-driver-encoding-contract.ps1:296:         'shepherd-task-25-given-list.ps1',
test/simple-math/08-psncpps-contract.sh:17: stage25="$scripts_directory/shepherd-task-25-given-list.sh"
test/simple-math/07-driver-encoding-contract.sh:46:     'shepherd-task-25-given-list.sh'
test/simple-math/02-create-issues.sh:25: stage25="$scripts_directory/shepherd-task-25-given-list.sh"
test/simple-math/10-simple-math-fixture-contract.sh:77: [[ "$(grep -Fc 'scripts/shepherd-task-25-given-list.sh' "$driver")" -eq 1 ]] ||
test/simple-math/06-stage40-review-contract.ps1:14: $stage25Path = Join-Path $repoRoot 'plugins\shepherd-task\scripts\shepherd-task-25-given-list.ps1'

test/simple-math/run-campaign.sh (2 match(es)):
  202:         local manifest="$directory/shepherd-task-25-given-list-run.json"
  336:     local stage25_script="$shepherd_plugin/scripts/shepherd-task-25-given-list.sh"

test/lesson-propagation-default-contract.ps1 (4 match(es)):
  9: $stage25 = Join-Path $scriptsDirectory 'shepherd-task-25-given-list.ps1'
  160:         (Join-Path $harnessDirectory 'shepherd-task-25-given-list.ps1'),
  196:             Join-Path $harnessDirectory 'shepherd-task-25-given-list.ps1'
  214:         Join-Path $runDirectories[0].FullName 'shepherd-task-25-given-list-run.json'
test/version-lineup-contract.sh:64: grep -Fq 'stageOutcomeProtocolVersion:' "$plugin_root/scripts/shepherd-task-25-given-list.sh"
test/simple-math/02-create-issues.ps1:261:     (Join-Path $PSScriptRoot '..' '..' 'scripts' 'shepherd-task-25-given-list.ps1')
test/lesson-propagation-default-contract.sh:9: STAGE25="$SCRIPTS_DIR/shepherd-task-25-given-list.sh"
test/simple-math/08-psncpps-contract.ps1:13: $stage25 = Join-Path $scriptsDirectory 'shepherd-task-25-given-list.ps1'

test/simple-math/run-campaign.ps1 (2 match(es)):
  339:                 $manifestPath = Join-Path $_.FullName 'shepherd-task-25-given-list-run.json'
  468:         'scripts\shepherd-task-25-given-list.ps1'
test/simple-math-treatment-control/08-psncpps-contract.ps1:13: $stage25 = Join-Path $scriptsDirectory 'shepherd-task-25-given-list.ps1'

test/simple-math-treatment-control/20260831-run-treatment-control-experiment.ps1 (3 match(es)):
  150:                 $manifestPath = Join-Path $_.FullName 'shepherd-task-25-given-list-run.json'
  496:         -Path (Join-Path $ShepherdPlugin 'scripts\shepherd-task-25-given-list.ps1') `
  506:         -Path (Join-Path $ShepherdPlugin 'scripts\shepherd-task-25-given-list.ps1') `
test/cargotracker-add-change-arrival-deadline-feature-treatment-control/06-stage40-review-contract.sh:10: STAGE25="$REPO_ROOT/plugins/shepherd-task/scripts/shepherd-task-25-given-list.sh"

test/simple-math-treatment-control/README.md (2 match(es)):
  407: & "$ShepherdPlugin/scripts/shepherd-task-25-given-list.ps1" `
  416: & "$ShepherdPlugin/scripts/shepherd-task-25-given-list.ps1" `
test/simple-math-treatment-control/06-stage40-review-contract.ps1:14: $stage25Path = Join-Path $repoRoot 'plugins\shepherd-task\scripts\shepherd-task-25-given-list.ps1'
test/simple-math/06-stage40-review-contract.sh:10: STAGE25="$REPO_ROOT/plugins/shepherd-task/scripts/shepherd-task-25-given-list.sh"

test/cargotracker-add-change-arrival-deadline-feature-treatment-control/20260902-run-treatment-control-experiment.ps1 (3 match(es)):
  149:                 $manifestPath = Join-Path $_.FullName 'shepherd-task-25-given-list-run.json'
  502:         -Path (Join-Path $ShepherdPlugin 'scripts\shepherd-task-25-given-list.ps1') `
  512:         -Path (Join-Path $ShepherdPlugin 'scripts\shepherd-task-25-given-list.ps1') `
test/cargotracker-add-change-arrival-deadline-feature/07-driver-encoding-contract.ps1:296:         'shepherd-task-25-given-list.ps1',
test/simple-math-treatment-control/02-create-issues.ps1:252:     (Join-Path $PSScriptRoot '..' '..' 'scripts' 'shepherd-task-25-given-list.ps1')
test/simple-math-treatment-control/06-stage40-review-contract.sh:10: STAGE25="$REPO_ROOT/plugins/shepherd-task/scripts/shepherd-task-25-given-list.sh"

test/cargotracker-add-change-arrival-deadline-feature-treatment-control/README.md (2 match(es)):
  305: & "$ShepherdPlugin\scripts\shepherd-task-25-given-list.ps1" `
  310: & "$ShepherdPlugin\scripts\shepherd-task-25-given-list.ps1" `
test/cargotracker-add-change-arrival-deadline-feature/07-driver-encoding-contract.sh:52:     'shepherd-task-25-given-list.sh'
test/cargotracker-add-change-arrival-deadline-feature/02-create-issues.ps1:264:     (Join-Path $PSScriptRoot '..' '..' 'scripts' 'shepherd-task-25-given-list.ps1')

test/cargotracker-add-change-arrival-deadline-feature-treatment-control/202609023-1638Z-run-treatment-control-experiment-resumeable.ps1 (3 match(es)):
  180:             'shepherd-task-25-given-list-run.json'
  395:         'shepherd-task-25-given-list-run.json'
  436:                 'scripts\shepherd-task-25-given-list.ps1') `
test/cargotracker-add-change-arrival-deadline-feature-treatment-control/06-stage40-review-contract.ps1:14: $stage25Path = Join-Path $repoRoot 'plugins\shepherd-task\scripts\shepherd-task-25-given-list.ps1'
test/cargotracker-add-change-arrival-deadline-feature-treatment-control/02-create-issues.ps1:255:     (Join-Path $PSScriptRoot '..' '..' 'scripts' 'shepherd-task-25-given-list.ps1')
test/cargotracker-add-change-arrival-deadline-feature-treatment-control/08-psncpps-contract.ps1:13: $stage25 = Join-Path $scriptsDirectory 'shepherd-task-25-given-list.ps1'
test/cargotracker-add-change-arrival-deadline-feature/06-stage40-review-contract.sh:10: STAGE25="$REPO_ROOT/plugins/shepherd-task/scripts/shepherd-task-25-given-list.sh"
test/cargotracker-add-change-arrival-deadline-feature/02-create-issues.sh:226:     "$scripts_directory/shepherd-task-25-given-list.sh" \
test/cargotracker-add-change-arrival-deadline-feature/10-cargotracker-fixture-contract.ps1:201:     "scripts\\shepherd-task-25-given-list\.ps1"
test/cargotracker-add-change-arrival-deadline-feature/06-stage40-review-contract.ps1:14: $stage25Path = Join-Path $repoRoot 'plugins\shepherd-task\scripts\shepherd-task-25-given-list.ps1'
test/cargotracker-add-change-arrival-deadline-feature/08-psncpps-contract.sh:8: stage25="$scripts_directory/shepherd-task-25-given-list.sh"

test/cargotracker-add-change-arrival-deadline-feature/run-campaign.sh (2 match(es)):
  190:         local manifest="$directory/shepherd-task-25-given-list-run.json"
  336: stage25_script="$shepherd_plugin/scripts/shepherd-task-25-given-list.sh"

test/cargotracker-add-change-arrival-deadline-feature/run-campaign.ps1 (2 match(es)):
  341:                 $manifestPath = Join-Path $_.FullName 'shepherd-task-25-given-list-run.json'
  473:         'scripts\shepherd-task-25-given-list.ps1'
test/cargotracker-add-change-arrival-deadline-feature/10-cargotracker-fixture-contract.sh:136: [[ "$(grep -Fc 'shepherd-task-25-given-list.sh' "$driver")" -eq 1 ]] ||
test/cargotracker-add-change-arrival-deadline-feature/08-psncpps-contract.ps1:13: $stage25 = Join-Path $scriptsDirectory 'shepherd-task-25-given-list.ps1'
```

</details>


---

<sub>2m 50s</sub>

### Copilot

Created and linked five ordered, unassigned child issues under #1. Stage 20 completed successfully; issues were created **without an issue type**.

| Subsection | Issue | Title | URL |
|---|---:|---|---|
| 4.1 | #2 | Add the application-layer deadline change operation | https://github.com/edburns/dd-3070518-cargotracker-darwin-arm64-01/issues/2 |
| 4.2 | #3 | Expose deadline changes through the booking facade | https://github.com/edburns/dd-3070518-cargotracker-darwin-arm64-01/issues/3 |
| 4.3 | #4 | Implement the deadline editor backing model | https://github.com/edburns/dd-3070518-cargotracker-darwin-arm64-01/issues/4 |
| 4.4 | #5 | Implement the PrimeFaces deadline dialog | https://github.com/edburns/dd-3070518-cargotracker-darwin-arm64-01/issues/5 |
| 4.5 | #6 | Integrate deadline editing into the Administration dashboard | https://github.com/edburns/dd-3070518-cargotracker-darwin-arm64-01/issues/6 |

**Given-list:** `2,3,4,5,6`

```bash
/Users/edburns/.copilot/plugins/shepherd-task/scripts/shepherd-task-25-given-list.sh \
  2,3,4,5,6 \
  1-arrival-deadline-control-remove-before-merge
```

---

<sub>Generated by [GitHub Copilot CLI](https://github.com/features/copilot/cli)</sub>
