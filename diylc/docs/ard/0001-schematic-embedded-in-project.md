# 0001 — Store the schematic as a lazy `SchematicView` on `Project`

**Status:** accepted

## Context

The schematic derived from a layout needs to live somewhere. Options:

1. A sidecar file (`project.diy.schematic`).
2. A separate top-level entity the app manages in parallel.
3. A field on `Project`, serialized inside the `.diy` file.

Constraints:

- One file per project is a strong existing expectation.
- The vast majority of users will never open the Schematic View; their files must not change.
- Schematic edits (moving a symbol) should participate in undo/redo and the modified flag with
  no bespoke plumbing.
- Older DIYLC versions must still open new files.

## Decision

Add `private SchematicView schematicView` to `Project`, serialized via XStream with the rest
of the project.

- The field stays `null` until the user first opens the view. `Project.getSchematicView()`
  returns it as-is; `Project.getOrCreateSchematicView()` lazily allocates it.
- `SchematicView` holds the schematic `components` (symbols + wires), a
  `Map<UUID, List<UUID>> physicalToSchematicMap` (physical component id → schematic symbol
  ids, 1:N), and canvas `Size`s.
- `Project.clone()` and `Project.equals()` include the schematic view.

## Consequences

- **+** Single file. No new file formats, no sync-of-two-files problem.
- **+** Undo/redo and the dirty flag work for free: the app already snapshots `Project` via
  `clone()` and diffs via `equals()`.
- **+** Backward compatible: a `null` field serializes to nothing; XStream on old versions
  ignores the unknown element.
- **−** Opening the Schematic View calls `getOrCreateSchematicView()`, which flips the project
  to *modified* even if the user changes nothing. This is intentional (the plan wants the
  schematic to persist once generated) but is a surprising side effect.
- **−** `SchematicView` in `equals()` slightly enlarges the cost of every undo-snapshot
  comparison once the view exists.
- Pin mappings are deliberately **not** stored in `SchematicView` — they are a pure function
  of the physical component and are recomputed by re-running the factory. This keeps the
  serialized form small and avoids a second source of truth.
