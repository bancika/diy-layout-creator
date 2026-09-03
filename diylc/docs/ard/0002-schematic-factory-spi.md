# 0002 — Per-type `ISchematicFactory`, declared on `@ComponentDescriptor`, with a generic fallback

**Status:** accepted

## Context

Every physical component type needs to become one or more schematic symbols. The mapping is
type-specific and sometimes complex:

- A resistor → one resistor symbol, pin 0 → pin 0, pin 1 → pin 1.
- A dual-triode tube socket → two triode symbols, with the heater pins shared to one section.
- A DIP IC → a box, or (for a dual op-amp) two op-amp symbols.

There are hundreds of component types. Most are simple; a handful are not. We need something
scalable that keeps per-type logic with the type, and that still produces *something* for a
type nobody has written a mapping for.

## Decision

A strategy interface plus an annotation attribute plus a fallback:

```java
interface ISchematicFactory {
  List<SchematicSymbolMapping> createSchematicSymbols(IDIYComponent<?> physicalComponent);
}
```

- `SchematicSymbolMapping` = a fully configured symbol instance + `Map<Integer,Integer>`
  pin mapping (physical control-point index → schematic control-point index) + optional
  section label. The factory returns *ready* symbols; the builder only positions them.
- Types opt in via `@ComponentDescriptor(schematicFactory = XxxSchematicFactory.class)`.
  `ComponentProcessor` copies it to `ComponentType.getSchematicFactoryClass()`. The annotation
  default is `ISchematicFactory.class` itself, used purely as a "none declared" marker.
- **`GenericBoxSchematicFactory`** is the fallback for any type without a declared factory: it
  inspects the sticky, node-named control points and builds a `SchematicBox` with one labeled
  terminal per node, copying name and value.
- `AbstractSimpleSchematicFactory` is a base for the common 1:1 case — subclasses just supply
  `createSymbol()` and (optionally) `electricalPinCount()`.

The interface and `SchematicSymbolMapping` live in `diylc-core` (the annotation references
the interface). Concrete factories live in `diylc-library` next to the symbol classes they
instantiate.

## Consequences

- **+** Per-type logic lives with the type. Adding coverage is: write a factory class, add one
  annotation attribute. No central registry to edit.
- **+** Full control for hard cases: the factory returns concrete instances, so it decides the
  symbol class, property values, and which sections get shared pins.
- **+** Every component gets a symbol — no gaps, no crashes on unmapped types.
- **−** The factory must be stateless (it is instantiated once and reused). This is a
  convention, not enforced.
- **−** `diylc-core` gains an `org.diylc.schematic` package that `diylc-library` also uses
  (split package across jars). DIYLC already does this for `org.diylc.components`, so it is
  consistent, but it is a smell.
- Pin mappings from the factory are recomputed on demand, never serialized (see
  [0001](0001-schematic-embedded-in-project.md)).
