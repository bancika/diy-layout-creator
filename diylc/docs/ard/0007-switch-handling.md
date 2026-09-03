# 0007 — Switches are drawn as symbols, not treated as connections

**Status:** accepted

## Context

`SchematicBuilder.isEligible()` excludes `IContinuity` implementors from the schematic — wires,
traces, solder bridges, board strips are connections, not symbols.

But **`ISwitch extends IContinuity`**. So the naive check silently dropped every switch from
the schematic, even though the netlist still contained the switch's terminals with real
connections. Symptom: switches present in the generated netlist, invisible on the diagram.

The netlist is built with `NetlistBuilder.extractNetlists(includeSwitches = false, ...)`. In
that mode `NetlistBuilder` itself treats a switch as a regular component (a node per terminal),
using the guard `c instanceof IContinuity && !(c instanceof ISwitch)` to decide what is a pure
connection. The schematic filter has to match that logic.

## Decision

`isEligible()` returns `true` for `ISwitch` *before* the `IContinuity` check:

```java
if (component instanceof ICommonNode) return true;
if (component instanceof ISwitch)     return true;   // drawn as a symbol
if (component instanceof IContinuity) return false;  // wires, traces, ...
```

There is no dedicated switch factory yet, so a switch falls through to
`GenericBoxSchematicFactory` — a labeled box with one terminal per lug — and is wired like any
other symbol.

## Consequences

- **+** Switches appear on the schematic with their connections, matching the netlist.
- **+** The schematic filter and `NetlistBuilder`'s own switch logic now agree.
- **−** A switch renders as a generic box, not a proper switch symbol. A
  `SwitchSchematicFactory` → `SwitchLatchingSymbol` (or a rotary/multi-pole symbol) is future
  work.
- **General principle:** anywhere the code says "exclude `IContinuity`", check whether it
  should special-case `ISwitch`.
