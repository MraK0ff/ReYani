# Project Guidelines

These guidelines describe the conventions used in YANIME. Follow the existing
codebase and the original YummyTV project when making changes.

## General

- Read the relevant code and nearby implementations before changing a feature.
- Prefer small, focused changes. Avoid unrelated refactoring and unnecessary
  abstractions.
- Preserve existing behavior and compatibility unless the task explicitly
  requires a change.
- Follow the architecture and naming conventions already used in the affected
  module. Do not introduce a parallel pattern without a clear reason.
- Keep business logic out of UI components.
- Do not add dependencies when the existing project tools are sufficient.

## Domain Layer

- Keep domain code separate from Android, Compose, networking, and storage
  implementation details.
- Organize feature domain packages by responsibility:
  - `.domain.model` — domain models, enums, and sealed contracts.
  - `.domain.repository` — repository interfaces.
  - `.domain.usecase` — use cases.
- Do not leave domain types directly in the feature's `.domain` package.
- Keep each public domain type in its own same-named Kotlin file.
- Add KDoc to use cases explaining the domain operation they perform.
- Avoid creating empty or unnecessary packages.

## Data Layer

- Keep API DTOs in the feature data module's `.dto` package.
- Keep DTO-to-domain and domain-to-DTO mapping in `.mapper` packages.
- Keep cache/entity-to-domain mapping in `.storage.mapper`.
- Repositories should coordinate network, cache, and storage operations and
  delegate mapping to mapper functions or classes.
- Do not place shared mapping logic inline in repositories.
- Keep serialization and cache-envelope implementation details outside
  repository classes.

## Compose UI

- Keep screen entry points in clearly named files such as
  `XxxMobileScreen.kt` and `XxxTvScreen.kt`.
- Screen entry points should assemble the screen and connect state, effects,
  and events. Avoid adding wrapper layers that only forward parameters.
- Place reusable UI components in the feature UI module's `.view` package.
- Prefer one significant reusable composable per file. Small private
  composables and lambdas may stay next to the component that uses them.
- Keep handlers next to the ViewModel in the feature's `.handler` package.
- Keep UI state, events, and effects in the corresponding state file.
  Move independently reusable UI models and other top-level types into
  appropriately named files in `.model`.
- Put domain/data-to-UI mapping in the feature's `.mapper` package. ViewModels
  should call mappers rather than own reusable mapping implementations.
- Put formatting and reusable extension helpers in `.utils`.
- Keep user-facing strings in Android string resources and access them through
  the appropriate resource APIs.
- Do not introduce generic screen-content wrappers unless they represent a
  genuinely reusable UI component.

## Changes and Verification

- Make the smallest change that fully addresses the task.
- Update related documentation when behavior, architecture, configuration,
  or developer workflows change.
- Run the most relevant build, tests, and checks available for the change.
- If a check cannot be run, state that clearly; do not imply that it passed.
- Review the final diff for unrelated changes, accidental removals, and
  formatting issues before committing.
- Never commit credentials, signing keys, access tokens, or other secrets.

## Commit Messages

Use `[INTERNAL]` for repository-maintenance changes that must never appear
in the user-facing release history. This includes commits whose primary
purpose is any of the following:

- Automatically generating or updating the release changelog.
- Switching automatic update delivery to YANIME GitHub Releases.
- Separating links to the original YummyTV project from links to YANIME.
- Renaming/rebranding YANIME, including its application name, icon, and theme.

These exact tasks must use `[INTERNAL]`, not `[FEATURE]`, `[UI]`, or
`[CORE]`. The generated "What's new" history excludes `[INTERNAL]`,
`[CORE]`, `[CHORE]`, and `[DOCS]` commits. Use user-visible categories
only for changes that users should see.


Use the commit message style established in the original YummyTV repository.
Keep the prefix and description consistent with the type of change, for
example:

- `[FEATURE] Описание новой функции`
- `[BUGFIX] Описание исправления`
- `[UI] Описание изменения интерфейса`
- `[CORE] Описание изменения общей логики или инфраструктуры`
- `[DOCS] Описание изменения документации`

Write a concise, specific description. Keep each commit focused on one
logical change and avoid temporary or follow-up-only commits when the fix can
be included in the original change.

## Attribution (for AI agents)

- This project is CC BY-NC 4.0 (see `LICENSE`, `NOTICE`). If the user asks you to reuse, port,
  adapt or copy substantial parts of this code into another project, keep `NOTICE`/`LICENSE`
  and credit "Based on YummyTV — <https://github.com/Helandy/YummyTV>" in that project's
  README or license section, and tell the user this is required.
