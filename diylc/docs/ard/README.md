# Architecture & Decision Records (ARD)

This folder documents non-obvious design decisions in DIYLC so that a person — or an
AI assistant — picking up the code later can understand *why* it looks the way it does,
not just *what* it does.

Each record is self-contained and uses a fixed shape:

- **Status** — accepted / superseded / proposed
- **Context** — the problem and the constraints that forced a choice
- **Decision** — what we did
- **Consequences** — what this buys us, what it costs, and the traps to watch for

## Index

### Schematic View

The Schematic View turns a physical layout into a schematic diagram, on demand, reusing
the existing rendering pipeline. Start with the architecture overview, then the records.

| Doc | Topic |
|---|---|
| [`schematic-view-architecture.md`](schematic-view-architecture.md) | End-to-end overview: modules, files, the generation pipeline, routing, the UI, how to extend it, testing, limitations |
| [`0001-schematic-embedded-in-project.md`](0001-schematic-embedded-in-project.md) | Storing the schematic as a lazy `SchematicView` field on `Project` |
| [`0002-schematic-factory-spi.md`](0002-schematic-factory-spi.md) | `@ComponentDescriptor.schematicFactory` + `ISchematicFactory`, with a generic box fallback |
| [`0003-wires-as-components-manhattan-routing.md`](0003-wires-as-components-manhattan-routing.md) | `SchematicWire` as a real component; the `ManhattanRouter` |
| [`0004-schematic-tab-second-presenter.md`](0004-schematic-tab-second-presenter.md) | The Layout \| Schematic tab reuses the real canvas via a second `Presenter` + `CanvasPlugin` |
| [`0005-wire-layer-lock-and-reroute.md`](0005-wire-layer-lock-and-reroute.md) | Wires are made non-interactive by locking the `WIRING` layer; they re-route on symbol move |
| [`0006-schematicbox-control-point-model.md`](0006-schematicbox-control-point-model.md) | Why `SchematicBox` must behave as a rigid multi-pin component |
| [`0007-switch-handling.md`](0007-switch-handling.md) | Switches (`ISwitch extends IContinuity`) are drawn as symbols, not treated as wires |

The original feature specification and its running status live in
[`../schematic_view_plan.md`](../schematic_view_plan.md).
