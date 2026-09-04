---
name: regression-test
description: Run the DIYLC end-to-end regression suite, which renders ~700 sample .diy projects and compares PNG exports and netlists against reference outputs. Use after any change to rendering, component drawing, serialization, netlist or analyzer logic, or when asked to check for regressions or regenerate reference files.
---

# Running the DIYLC regression suite

`RegressionTestRunner` (in `diylc-swing/src/test/java/org/diylc/`) loads every sample project under
`diylc-regression-data/input`, exports a PNG and extracts netlists, and compares both against
reference files produced by a known-good build. It is the only automated coverage of component
appearance and of the file format end to end — unit tests do not touch either.

**Always ask before running the full suite**, and never run `PREPARE` without explicit instruction.

## Modes

- **`TEST`** — compares against the reference files and writes a CSV report plus visual diff images.
  This is what you almost always want.
- **`PREPARE`** — *regenerates* the reference PNGs and netlists from the current build. This
  overwrites the golden copy and is correct only once a human has inspected the differences and
  decided they are intended. Never run it on your own initiative.

## Easiest path

If the maintainer is at their IDE, the `.run/RegressionTestRunner.run.xml` IntelliJ configuration
already has the arguments and JVM flags. Offer that first.

## From the terminal

The runner needs the `--add-opens` / `--add-exports` flags and a classpath spanning all three
modules; `mvn exec:java` will not work because it runs in Maven's own classloader and fails to find
Reflections. Build a classpath and fork a JVM:

```bash
cd diylc
SP=/tmp   # or the session scratchpad

mvn -q -pl diylc-swing -am test-compile
mvn -q -pl diylc-swing   dependency:build-classpath -Dmdep.outputFile=$SP/cp-swing.txt -Dmdep.includeScope=test
mvn -q -pl diylc-core    dependency:build-classpath -Dmdep.outputFile=$SP/cp-core.txt  -Dmdep.includeScope=test
mvn -q -pl diylc-library dependency:build-classpath -Dmdep.outputFile=$SP/cp-lib.txt   -Dmdep.includeScope=test

java \
  --add-exports java.desktop/com.apple.eawt.event=ALL-UNNAMED \
  --add-exports java.desktop/com.apple.eio=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.text=ALL-UNNAMED \
  --add-opens java.desktop/java.awt=ALL-UNNAMED \
  --add-opens java.desktop/java.awt.font=ALL-UNNAMED \
  --add-opens java.desktop/java.awt.geom=ALL-UNNAMED \
  -cp "diylc-swing/target/test-classes:diylc-swing/target/classes:diylc-core/target/classes:diylc-library/target/classes:$(cat $SP/cp-swing.txt):$(cat $SP/cp-core.txt):$(cat $SP/cp-lib.txt)" \
  org.diylc.RegressionTestRunner \
  "$(cd .. && pwd)/diylc-regression-data" TEST [filter]
```

Sanity-check the log line `Presenter - Current DIYLC version: 6.4.0` near the end. If it reports
`5.0.0`, the `--add-opens` flags are missing and the run is invalid.

### Arguments

1. Path to `diylc-regression-data`
2. `TEST` or `PREPARE`
3. Optional filter — a substring matched against file paths. **Use it.** The full suite is ~700
   projects; while narrowing down an issue, run one file or one directory at a time
   (e.g. `splitter2`, or `guitar`).

## Reading the results

- A CSV report is written to `diylc-regression-data/reports/`, named with the timestamp and version,
  listing each file as OK or failed with the reason (`Images do not match`, `Netlists do not match
  with switches`, `Netlists do not match without switches`).
- Rendered output and **visual diff images**, with red rectangles around every differing region, go
  to `diylc-regression-data/output/` (not under version control).
- Reports are kept as historical evidence. If a run was exploratory rather than a real verification,
  delete the generated CSV afterwards so the reports directory stays meaningful — and check
  `git status` before finishing, since the runner writes into the repo.

## Interpreting a failure

A difference is not automatically a bug, but it is always a change the maintainer must see. Report
which files differ and what kind of difference it is; where an image diff exists, point at the file
so it can be looked at. Do not conclude "no regressions" from a filtered run — say exactly which
subset was run.
