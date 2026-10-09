# SignPath code signing (Windows)

DIYLC signs its Windows artifacts through the [SignPath Foundation](https://signpath.org/) OSS
program. The private key lives in SignPath's HSM and never reaches this repository or a developer
machine; signing happens inside GitHub Actions, which SignPath has registered as a trusted build
system so that it can verify each artifact came from this repository.

The XML files here are the **artifact configurations**. They are not read by the Maven build — they
are the reviewed source of truth for what has to be pasted into the SignPath web UI. Keep them in
sync with the UI whenever the Windows packaging changes.

| File | Slug in SignPath | Workflow | Signs |
| --- | --- | --- | --- |
| `installer.xml` | `windows-installer` | `sign-windows.yml` | the Inno Setup installer, once per release |
| `launcher.xml` | `windows-launcher` | `sign-launcher.yml` | the launch4j launcher, only when it is rebuilt |

## Why the launcher is signed separately and committed

Inno Setup compresses its payload into a custom container that cannot be signed through, so
`diylc-x64.exe` must already carry its signature when the installer is compiled. That launcher is a
prebuilt binary with six changes in its entire history, so signing it on every release would buy
nothing and cost an extra approval each time. It is signed on its own by `sign-launcher.yml` and the
signed copy is committed, which leaves releases with a single signing request.

Two consequences worth remembering:

- **Re-sign the launcher when the production certificate replaces the test certificate.** A
  committed test-signed launcher is worse than an unsigned one, because it names a publisher that
  Windows does not trust.
- **Re-sign it whenever launch4j regenerates it.** Nothing downstream fails if you forget, so
  `sign-windows.yml` checks the committed launcher with `is-signed.py` and refuses to produce a
  release if it is unsigned.

The win64 zip needs no signing request of its own: it packs the launcher that is already signed. The
bundled Temurin JRE is left alone too, since Adoptium ships those binaries Authenticode-signed.

## One-time setup in SignPath

1. The predefined **GitHub.com** trusted build system has to be added to the organization and linked
   to the project. This needs organization administrator rights, so in an OSS organization the
   SignPath Foundation does it on request.
2. Create the two artifact configurations above with the slugs from the table.
3. Create an API token for the CI user and store it in this repository as the `SIGNPATH_API_TOKEN`
   secret. The organization ID and project slug are not secret and sit in the workflow files.
