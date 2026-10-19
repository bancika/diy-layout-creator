# Cloud sync job

`org.diylc.swing.plugins.cloud.sync.CloudSyncJob` mirrors `.diy` files that users publish on their
own web pages into the DIYLC cloud, on behalf of users who agreed to it. Each run scrapes the
configured pages for `.diy` links and downloads every file. A file that is new is uploaded as a
project with a rendered thumbnail. A file whose content changed replaces the files of the project
it was uploaded as. Unchanged files are skipped, and files that disappear from a page are left in
the cloud.

## Configuration

Copy `cloud-sync.example.json` to `cloud-sync.json` (git-ignored, because it holds login tokens).
For each user:

- `username`, `token`, `machineId`: the cloud API authenticates with the username (or email), not
  the numeric ID, plus a login token issued for that machine ID.
- `userId`: names the user's folder under `storageDir`.
- `sourceUrl`: optional page cited as the source in project descriptions, instead of the link to
  each file.
- `category`, `keywords`: defaults for the user's pages. Category is the name shown in the upload
  dialog; the leading `- ` of subcategories is optional.
- `pages`: page URLs. Use an object with `url`, `category` and `keywords` to override the defaults
  for one page.

The project name comes from the file name, and the description from the file's own description
followed by a `Source:` link to the user's `sourceUrl`, or to the file when there is none. When
the source changes, the next run rewrites the `Source:` line of every project it cites, keeping the
rest of the description and the other project details as they are in the cloud.

## Running

```bash
mvn -q -pl diylc-swing -am package -DskipTests   # builds diylc-swing/target/diylc.jar
cloud-sync/cloud-sync.sh --dry-run                # download and render only
cloud-sync/cloud-sync.sh                          # sync for real
```

`storage/<userId>/` mirrors the downloaded files with their thumbnails (`*.diy.png`), and keeps
`sync-state.json`, which maps each source URL to its cloud project ID, the hash that was last
uploaded and the source its description cites. If the state is lost, the next run takes over the user's existing projects by name
instead of uploading duplicates. The exit status is non-zero if any page or file failed.
