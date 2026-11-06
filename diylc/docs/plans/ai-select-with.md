# AI Select-With Companions

Declaring on a component which other component types must reach the AI together with it, so that the
AI server stops hard-coding component knowledge.

## 1. Goal

When the AI's component selector picks a type that is never used alone, the types it needs have to
be in the filtered catalog too, or the edit model cannot name them: given an Enclosure and told to
drill holes, but without Drill Hole in its catalog, it invented `chassis.DrillHole` instead of
`electromechanical.DrillHole`. A `SELECTION` `@AiNote` asks the selector to pick the companions, but
a note is advice to a cheap, low-effort call and can be missed. The guarantee has to be
deterministic, and it belongs with the component, not in the server.

## 2. Current state

- `@AiNote`s on `ChassisPanel` (Enclosure), `BlankBoard` and `DrillHole` tell the selector and the
  edit model when to use Drill Hole and the cutouts and how to place them (see
  [ai-component-notes.md](ai-component-notes.md) for notes in general).
- The guarantee lives in `diylc-ai-server/v1/ai/classes/CircuitContextBuilder.php` as constants and
  two `if` blocks after the selection is merged:
  - `ENCLOSURE_CLASS` (`chassis.ChassisPanel`) adds `ENCLOSURE_HOLE_CLASSES`: `DrillHole`,
    `RectangularCutout`, `EllipticalCutout`, `PolygonalCutout`;
  - `BLANK_BOARD_CLASS` (`boards.BlankBoard`) adds `DRILL_HOLE_CLASS`.
- `WIRE_CLASSES` sits beside them but is a different kind of rule: wires follow from the
  construction type, not from a component, so it stays in PHP.
- The server reads `catalog_full.json` only after the selection, to filter it.

## 3. Design decisions

| # | Decision | Rationale |
| --- | --- | --- |
| D1 | A new single (not repeatable) annotation in `diylc-core/.../core/annotations`, `@AiSelectWith(Class<? extends IDIYComponent<?>>[] value)`, beside `@AiNote` | Class literals keep a rename or move compile-checked; core already knows components only through `IDIYComponent`, so the boundary holds. |
| D2 | Collected from the class and every superclass, merged without duplicates, like `collectNotes` | A base class can declare companions for all its subclasses, the same way it carries notes. |
| D3 | Published only in `catalog_full.json`, as `"selectWith": ["electromechanical.DrillHole", ...]` in `catalogClassName` form, and left out when empty | The server expands from the full catalog; the selector does not need it, and the index stays small. |
| D4 | One level: the server adds the companions of the selected types, not the companions of those | Nothing needs more today, and a single pass cannot loop. Revisit only if a chain appears. |
| D5 | The `@AiNote`s stay | `selectWith` only guarantees a type is present; the notes say why and how to use it. |
| D6 | Blank Board lists Drill Hole even though mounting holes are optional | One ~1.1K catalog entry the model may ignore is cheaper than a missing class name. |
| D7 | The server skips a companion that is not in its catalog | A catalog and server out of step must not break a request. |

## 4. Steps

1. **Annotation.** `AiSelectWith.java` in `org.diylc.core.annotations`, GPL header, `@Retention(RUNTIME)`,
   `@Target(TYPE)`, Javadoc with `@author` as on `AiNote`.
2. **Components** (`diylc-library`):
   - `ChassisPanel`: `@AiSelectWith({DrillHole.class, RectangularCutout.class, EllipticalCutout.class, PolygonalCutout.class})`;
   - `BlankBoard`: `@AiSelectWith(DrillHole.class)`.
3. **Generator.** In `ComponentCatalogGenerator.buildFullEntry`, after the notes, put `selectWith`
   from a `collectSelectWith(Class<?>)` that walks the hierarchy like `collectNotes` and maps each
   class through the same conversion as `catalogClassName`.
4. **Tests** in `diylc-library/.../plugins/chatbot/service/ComponentCatalogGeneratorTest`:
   - the generated Enclosure entry lists the four hole types and Blank Board lists Drill Hole;
   - every `selectWith` entry of every component is itself a `className` in the catalog;
   - `collectSelectWith` merges a superclass's companions, as the existing `collectNotes` tests do.
5. **Regenerate** `catalog_full.json` / `catalog_index.json` (run `ComponentCatalogGenerator` from
   `diylc-swing`, `.run/ComponentCatalogGenerator.run.xml`), check `version` matches the latest
   `update.xml` release, and copy both into `diylc-ai-server/v1/ai/`.
6. **Server.** In `CircuitContextBuilder::buildForEditIntent`:
   - load `catalog_full.json` before the selection is merged instead of after;
   - after `$selectedComponents` is assembled (selection, existing classes, wires, Blank Board for
     PCB), add the `selectWith` of every selected type that is in the catalog, then `array_unique`;
   - delete `ENCLOSURE_CLASS`, `BLANK_BOARD_CLASS`, `DRILL_HOLE_CLASS`, `ENCLOSURE_HOLE_CLASSES` and
     the two `if` blocks; keep `WIRE_CLASSES`.
   - Order matters: the Blank Board added for PCB must be in the list before the expansion runs.
7. **Docs.** `diylc-ai-server/docs/promptChatbot_flow.md` (filtered catalog paragraph) says the
   companions come from `selectWith`; [ai-component-notes.md](ai-component-notes.md) §1 mentions
   `@AiSelectWith` next to `Scope.SELECTION`.

Do step 6 only after the regenerated catalog is on the server: a catalog without `selectWith` and a
server without the constants would lose the guarantee.

## 5. Verify

- `mvn -q -pl diylc-library -am test` and `mvn -q -pl diylc-swing -am compile`.
- `php -l v1/ai/classes/CircuitContextBuilder.php`.
- A request that selects only the Enclosure ("put an LM386 amp in an enclosure") reaches the edit
  model with Drill Hole and the three cutouts in its catalog; a PCB request carries Drill Hole.

## 6. Open

- Whether other types deserve companions. Candidates worth checking against real requests:
  a board-mounted pot or jack with its hookup wire is already covered by `WIRE_CLASSES`, so look
  for pairs the selector actually drops before adding any.
