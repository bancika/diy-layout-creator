# Configuration compatibility fixtures

Each file here is a real `config.xml` written by the DIYLC release its name refers to, kept so that
`org.diylc.ConfigCompatibilityTests` can prove that the current code still reads it. The test picks
up every `.xml` file in this directory, so adding a snapshot is enough to have it covered.

## Adding a snapshot

1. Copy `config.xml` out of the DIYLC user directory (`<user.home>/diylc` on all platforms; dated
   `config.xml.<timestamp>` files next to it are backups the application makes when it fails to
   read the configuration).
2. Name it `config-<version>.xml` after the DIYLC version that wrote it.
3. Delete the entries that carry personal data or machine-specific noise before committing:
   `cloud.token`, `cloud.Username`, `userId`, `lastPath`, `recentFiles`, `announcement.lastReadDate`
   and `FontOptimizer.slowFonts`. Nothing else should be touched — the point of the fixture is that
   it is byte-for-byte what an old release produced.
4. Building blocks and variants are the valuable part of a snapshot, since they embed serialized
   components; prefer a configuration that has plenty of both. Check that no building block holds an
   image the maintainer would not want published.

## When the test fails

A failure means an existing user would lose their configuration on upgrade: `ConfigurationManager`
cannot parse the file, so it backs it up and starts empty. Fix the compatibility break rather than
the fixture — keep the old field and migrate lazily in the getter, or register an XStream alias for
a class that had to move. See the file-format section of `diylc/CLAUDE.md`.
