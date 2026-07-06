# Git Workflow

## Commit Message Format

Every commit message for the HZCU Summer Semester project must follow the course day-prefix rule plus Angular commit style.

Use this format:

```text
[Day XX] type(scope): subject
```

Example:

```text
[Day 01] docs(reference): update proj reference
```

Rules:

- `Day XX` is the day number for the module or deliverable being committed.
- The day number follows the reference schedule, not the current calendar date.
- Put the day prefix before the Angular commit message.
- Use Angular-style `type(scope): subject` after the prefix.
- Use the course format with a space and two digits, for example `[Day 01]`, not `[Day1]`.
- If a commit touches work from one module, use that module's day.
- If a commit touches multiple modules, use the earliest relevant day unless the user gives a different day.
- Before creating a commit, confirm the final commit message with the user.

Common types:

| Type | Use for |
| --- | --- |
| feat | User-visible feature or required project capability |
| fix | Bug fix |
| docs | Documentation, reports, README, PPT source material |
| test | Behavior/regression tests |
| refactor | Code restructuring without behavior change |
| chore | Build, configuration, dependencies, repository maintenance |

Reference mapping:

| Day | Work area |
| --- | --- |
| Day 01 | Repository setup, environment setup, requirements, use cases, MySQL E-R design, MongoDB collection design |
| Day 02 | Database scripts, views, procedures, triggers, MongoDB initialization, Maven project skeleton, base DAO, UserDAO |
| Day 03 | User module, core business module, MongoDB DAO, details module, order/record module |
| Day 04 | Action logs, comments, initial statistics aggregation |
| Day 05 | Recommendation module, cross-database queries, DTOs |
| Day 06 | Statistics reports, monthly report procedure calls, system operation logs |
| Day 07 | Index/query/batch/security optimization, integration, stable runnable version |
| Day 08 | Behavior/regression tests, transaction rollback tests, stress testing, refactoring |
| Day 09 | Technical design document, user manual |
| Day 10 | Defense PPT, final document updates, README completion |
| Day 11 | Rehearsal notes, code review checklist, final bug fixes, final Git record cleanup |
| Day 15 | Project summary report |
