# DIY Layout Creator — repository map

The guidelines that govern all code changes live in **[`diylc/CLAUDE.md`](diylc/CLAUDE.md)**. Read
that file before editing anything under `diylc/`.

## What is where

| Path | Contents |
| --- | --- |
| `diylc/` | The application. Maven reactor with `diylc-core`, `diylc-library`, `diylc-swing`. This is where nearly all work happens. |
| `diylc-regression-data/` | Sample `.diy` projects plus reference PNG and netlist outputs used by the regression suite, and the historical reports. |
| `diylc-server-api/` | PHP backend for cloud and collaboration features. Runs in an isolated environment on diy-fever.com and cannot be tested locally — do not modify unless explicitly asked. |
| `snap/` | Snap packaging metadata. |

## House rules that apply repository-wide

- Match the conventions of the surrounding code; consistency beats novelty.
- Comment only what the code cannot say for itself. No comments that restate the line below them.
- Every new source file begins with the GPL header from `diylc/HEADER.txt`.
- Do not commit or push. Leave changes in the working tree for review.

The project is GPLv3; see `COPYING`.
