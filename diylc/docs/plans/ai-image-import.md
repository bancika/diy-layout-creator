# AI Image Import

Turning one image of a circuit (a render or a photograph of a schematic, PCB, perfboard, stripboard,
breadboard, turret/tag board, point-to-point build or guitar wiring) into a DIYLC project, using the
exact component types and values shown and every connection that is visible.

## 1. Goal

The user picks one image from a new **AI** menu, optionally adds a few words ("only the board, the
pots are 500k"), confirms the credit cost, and gets back an ordinary edit-script proposal in the AI
Assistant that, when applied, rebuilds what the image shows. What cannot be seen (the underside of a
board, a hidden wire, an unreadable band) is not invented silently: it is left out or filled with a
placeholder, and the reply lists it.

Success is measured on connectivity first, values second and geometry third: a reconstruction with
the right nets in a slightly wrong place is useful, a pretty one with wrong nets is not.

## 2. Current state

### Client (`diylc-core/.../plugins/chatbot`, `diylc-swing`)

- `ChatbotService.promptChatbot()` serialises the project to an `AiProject` (`AiProjectBuilder`:
  components with `bounds`, `terminals`, `connectedTo`, plus `nets`, `switches`, `selection`),
  writes it and the optional guitar `clientAnalysis` to temp files, and posts them through
  `IChatbotAPI` (`PhpFlatProxy`, multipart; a `File` parameter arrives in PHP as `$_FILES[name]`).
- The response is `{"string": ...}` or a JSON `ChatbotResponse` carrying an `AiEditScript`; the
  client stamps it with `fingerprint(aiProject)`.
- `ChatbotPane.ask()` runs the call on a background task and `renderProposal()` shows the edit
  script with an *Apply Changes* link; `applyProposal()` refuses if the fingerprint changed, then
  runs `AiEditScriptEditor` as one undoable `IProjectEditor` and shows its warnings.
- Placement is in grid units (`gridX/gridY`, 1.0 = one `gridSpacing`): `position` for
  `SINGLE_CLICK` types, `fromPos/toPos` for `POINT_BY_POINT`, `fromTerminal/toTerminal` for wires.
- *AI Analyzer* (`AiAnalyzeAction`) is the first item of the **Analyze** menu
  (`AnalyzeMenuPlugin`). The AI Assistant pane is toggled from **Config**. Top-level menus appear in
  the order they are first injected (`MainFrame.findOrCreateMenu`), which follows plugin install
  order: File, Analyze, Edit, Config, Layers, then `ChatbotPlugin`, then Cloud and Help.

### Server (`diylc-ai-server/v1/ai`)

- `promptChatbot.php`: text-only precheck → for `edit`/`new`,
  `CircuitContextBuilder::buildForEditIntent` (construction type, a component-selection call,
  filtered `catalog_full.json`, `system_edit.md` with per-construction-type connection rules) → main
  call on `ai_model_high` in JSON mode → `logChatRequest` → `updateSubscription` → echo.
- `LLMClient::complete()` takes messages whose `content` is a **string only**;
  `normalizeMessages()` drops anything else. No provider can be sent an image today.
- **Credits**: `diylc_ai_subscription.credits` starts at the tier's `credits` (Free 25, Supporter
  100, Pro 500) and `updateSubscription()` takes **one credit per request** whatever it cost, adding
  the tokens to `total_tokens`. `checkActiveSubscription()` only refuses at zero credits. A request
  that throws is not charged. Model prices live in `diylc_ai_model_cost`.
- Tiers today: Free runs `gpt-6-luna` everywhere ($0.10 / $0.50 per M tokens in/out); Supporter and
  Pro use `gpt-6.1-sol` ($2 / $10) for medium and high; `max_tokens` 8k / 16k / 30k.
- `diylc_ai_chat` has no column for attachments. Uploads go through PHP's `upload_max_filesize` /
  `post_max_size`, which default to 2M/8M.
- `getSubscriptionInfo.php` returns a class-keyed object
  (`org.diylc.plugins.chatbot.model.SubscriptionEntity`) that `PhpFlatProxy` deserialises with
  XStream on the client.

## 3. Design decisions

| # | Decision | Rationale |
| --- | --- | --- |
| D1 | A dedicated endpoint `v1/ai/importImage.php` and `IChatbotAPI.importImage(..., circuit, image, prompt)` | The precheck does not fit: intent is always "rebuild this" and the construction type comes from the image. The endpoint reuses `ApiRequestHandler`, `CircuitContextBuilder`, logging (`type = 'import'`), chat history and billing. |
| D2 | Two-stage generation: a **perception** call that returns a structured inventory, then the existing selection + edit-script generation driven by it | One call that both reads a photo and writes DIYLC geometry against a catalog does both badly. The inventory is inspectable, loggable, testable alone, and is what the client checks the result against (D9). |
| D3 | Perception reports positions in the image's physical frame: **hole column/row** on pitched boards (perf, strip, bread), millimetres from a board corner on PCBs, turret and tag boards, a 0-1000 relative frame for schematics, point-to-point and guitar wiring | On 0.1" boards one hole is one grid step, so leads land on exact grid points and stripboard connectivity, which depends on exact holes and cuts, comes out right. Elsewhere topology matters more than position. |
| D4 | The image goes to the perception and edit-script calls; the selection call gets only the inventory | The edit model needs placement and orientation; the selector only needs the parts list and stays cheap. |
| D5 | A new tier column `ai_model_vision`, used for both image calls. Null means the tier has no image import, and the endpoint says so before spending anything | Keeps the vision model an explicit per-tier choice, independent of the chat models (DeepSeek, for one, cannot see images). |
| D6 | The client downscales and re-encodes before upload: longest edge at most 2000 px, JPEG or PNG whichever is smaller, never more than 1.5 MB | Stays under default PHP and provider limits and cuts tokens; past that resolution the models gain little. |
| D7 | Unreadable value: the type's default, flagged in the reply. Unidentifiable part: left out and listed. Invisible connection: not drawn | The user asked for the exact parts shown; a confident guess that looks right is worse than a visible gap. |
| D8 | Every uploaded image is stored on the server and linked from its `diylc_ai_chat` row (§6) | Imports cannot be debugged from the inventory alone. Retention is a separate cleanup job owned by the maintainer. |
| D9 | The response carries the perception `nets` (`ChatbotResponse.expectedNets`, ignored by older clients through `@JsonIgnoreProperties`); after *Apply*, the client compares them with the rebuilt project's nets and lists mismatches like apply warnings | The server cannot run DIYLC, the client can, and it gives the user a concrete list of what to check. |
| D10 | Import goes into the current project, beside existing content (the existing `bounds` rule in `system_edit.md`); the result appears in the AI Assistant pane as a normal proposal | Same mechanism as the `new` intent today, so fingerprinting, preview and undo work unchanged. |
| D11 | Chat history stores the turn as `"AI Import - <explanation>"`; the image is never replayed | Same reduction as edit responses. |
| D12 | Import is priced in credits per tier (§7), checked before the first LLM call, and shown to the user before they confirm | An import costs several chat requests' worth of tokens; one credit would make it the cheapest way to burn the budget. |
| D13 | DIYLC always draws a board from the component side. Perception reports the side the image shows and positions **as seen**; when that is the copper side, the server mirrors the inventory into the component-side frame with plain code before anything else uses it (§5.4) | Mirroring is arithmetic; asking the model to do it while it is also counting holes adds a second place to make mistakes. Only the reported side is left to the model. |

## 4. Perception output

One JSON object, defined by `system_import_perceive.md`. Component ids become DIYLC component
names, so the client can match nets by name (D9).

```json
{
  "constructionType": "veroboard",
  "view": "component-side",
  "board": { "columns": 24, "rows": 15, "stripDirection": "horizontal",
             "cuts": [{"col": 7, "row": 3}] },
  "components": [
    { "id": "R1", "kind": "resistor", "package": "axial", "marking": "brown black orange gold",
      "value": "10k", "confidence": "high",
      "pins": [{"name": "1", "at": {"col": 3, "row": 5}}, {"name": "2", "at": {"col": 7, "row": 5}}] }
  ],
  "connections": [
    { "kind": "jumper", "from": {"col": 4, "row": 2}, "to": {"col": 4, "row": 9}, "color": "red" },
    { "kind": "wire", "from": "R1.2", "to": "VOL.3", "color": "green" }
  ],
  "nets": [["R1.2", "C1.1", "VOL.3"]],
  "unseen": ["Underside not shown: trace cuts beyond those visible may exist", "C3 value unreadable"]
}
```

- `kind` is a short free vocabulary (resistor, film cap, electrolytic, diode, LED, transistor, IC,
  pot, jack, switch, pickup, tube socket, ...) that the selection call maps to catalog classes.
- `marking` always holds the raw reading (bands, printed text) next to the interpreted `value`, so a
  misread band is visible in the log and can be corrected.
- For schematics, `at` is `{"x": .., "y": ..}` in the 0-1000 frame and `board` is absent.
- `view` is `component-side`, `copper-side` or `unknown` (and absent for schematics and wiring
  without a board). Positions are always as seen in the image; pins are always named by their real
  identity (IC pin 1 from the square pad or notch, transistor E/B/C), never by where they appear.
  A component mounted on the side the image shows, such as a pot or jack soldered to the copper
  side of a pedal board, gets `"mountedOn": "copper-side"`.
- `unknown` is treated as `component-side`, and `unseen` says so.

## 5. Server flow (`importImage.php`)

1. `ApiRequestHandler::init(false)`. `checkActiveSubscription()` also selects `t.ai_model_vision`
   and `t.import_credits`; refuse when `ai_model_vision` is null or `credits < import_credits`.
2. Read `$_FILES['image']`, check MIME (JPEG/PNG/WebP) and size, **store it (§6)**, base64 it.
   `circuit` is the current project as today, empty for a new project.
3. **Perceive** on `ai_model_vision` with `system_import_perceive.md`; user content = image +
   optional prompt; JSON mode; reasoning from the tier. One retry on invalid JSON.
4. **Normalise to the component side** (D13) — when `view` is `copper-side`, mirror every position
   left to right: `col' = columns - 1 - col` in the hole frame, `x' = width - x` in millimetres.
   Mirroring across the other axis would only rotate the result by 180 degrees with the same
   connectivity, so one fixed axis is enough. Pin names stay as they are. Components with
   `mountedOn: copper-side` are passed on with an instruction to set `Board Side` to bottom on
   types that have that property. Then set `view` to `component-side`, so the later steps and the
   logged inventory see only one frame. A plain function, checked against a hand-written inventory.
5. **Select** — `CircuitContextBuilder::buildForImport($inventory, ...)`: construction type from the
   inventory, selection prompt built from the inventory's `kind`/`package`/`value` list, datasheet
   models requested for every part with a readable part number. Runs on `ai_model_precheck`.
6. **Generate** on `ai_model_vision` with `system_edit.md` plus a new `{{IMPORT_RULES}}` block (map
   hole coordinates to grid from the board's origin, keep inventory ids as `componentName`, follow
   D7, list placeholders in the message); user content = image + inventory JSON + filtered catalog +
   current circuit JSON when non-empty.
7. Return `{"string": message, "editScript": ..., "expectedNets": ...}`. Log both image calls
   (inventory in `full_response`) and the image path; charge `import_credits` (§7).
8. A truncated response (`finish_reason` length / `max_tokens`) is an error with a clear message
   ("too many parts for one import on your tier"), never a partial script, and is not charged.

`LLMClient`: a message `content` may be a string **or** a list of blocks
`[{"type":"text","text":..}, {"type":"image","mediaType":..,"data":base64}]`;
`normalizeMessages()` keeps lists and each payload builder translates image blocks for its
provider (`image` with a base64 `source` for Anthropic, `input_image` with a data URL for Responses,
`image_url` for Chat Completions). `full_request` is logged with the base64 replaced by the stored
file name, so `diylc_ai_chat` does not grow by megabytes per row.

## 6. Image storage

- Files go under one folder outside the web root, configured once (e.g. `ai_image_dir` in a
  properties file next to `db.properties`), in `YYYY/MM/` subfolders, named
  `<sha256 of the bytes>.<ext>`. The same image uploaded twice is stored once.
- `diylc_ai_chat` gains a nullable `image_path varchar(255)` holding the path relative to that
  folder; `logChatRequest()` takes it as an optional trailing argument, so existing callers are
  untouched.
- The file is written **before** the first LLM call, so failed imports keep their image too, and
  the error path's `logChatRequest` gets the same path.
- The cleanup job may delete files and null the column; nothing on the server reads the file back
  after the request, so a missing file never breaks anything.
- If the folder is not writable the import still runs and `error_log` records it; a debugging aid
  must not fail a paid request.
- The confirmation dialog tells the user the image is kept for a limited time for troubleshooting.

## 7. Credits

### Estimate

Per import, at the Supporter/Pro price of `gpt-6.1-sol` ($2 / $10 per M), with reasoning counted
as output. **These are estimates to replace with measured numbers** from the benchmark (§9), whose
token counts land in `diylc_ai_chat` and price out against `diylc_ai_model_cost`.

| Call | Input tokens | Output tokens | Cost |
| --- | --- | --- | --- |
| Typical AI edit today (main call) | ~20k | ~8k | ~$0.12 |
| Import, perception | ~5k (image ~2-4k + prompt) | ~10-15k | ~$0.11-0.16 |
| Import, selection (precheck model) | ~15k | ~1k | negligible |
| Import, generation | ~35-40k (image + inventory + larger catalog) | ~15-30k | ~$0.22-0.38 |
| **Import, pedal-sized circuit** | | | **~$0.35-0.45** |
| **Import, amp-sized circuit** (both calls near `max_tokens`) | | | **~$0.70-0.80** |

So an import costs roughly **3-6 typical edits**, and an edit already costs more than a chat
answer, which shares the same one-credit price. Output tokens dominate; the image itself is a minor
part. On Free (`gpt-6-luna`) the same import costs about a cent, but a small model's vision is
unlikely to be good enough, which is a reason to leave `ai_model_vision` null on Free at first.

### Rule

- A new tier column `import_credits` (int), charged instead of 1 by passing a `$credits` argument
  to `updateSubscription()` (defaulting to 1, so other callers are unchanged).
- Start at **5** on Supporter and Pro and adjust once the benchmark gives real per-import costs. At
  5, Supporter's 100 credits buy at most 20 imports and Pro's 500 at most 100, which bounds the
  monthly exposure per user at roughly $8 and $40 at the upper estimate.
- Refuse up front when `credits < import_credits`, with the renewal date as today's zero-credit
  message does.
- Failed and truncated imports are not charged, as failed requests are not today.
- Token-weighted charging for every request is the better long-term model, but it changes chat
  pricing too; it is out of scope here.

### Showing the cost

The client needs the price for the menu label and the confirmation dialog. Adding a field to
`SubscriptionEntity` would change the class-keyed XStream payload that already-shipped clients
deserialise, and XStream rejects unknown fields by default, so instead: a small `getImportInfo.php`
returning
`{"org.diylc.plugins.chatbot.model.ImportInfo": {"available": .., "importCredits": .., "remainingCredits": ..}}`
and `IChatbotAPI.getImportInfo(username, token, machineId)`. The client derives imports left as
`remainingCredits / importCredits`, rounded down.

The menu item carries the count, in imports rather than credits, since that is what the user
decides on:

| State | Label | On click |
| --- | --- | --- |
| Not logged in, or info not loaded yet | *AI Image Import* | Log-in message, as for the analyzer |
| Tier without import (`available` false) | *AI Image Import (not on your tier)* | Message naming the tiers that have it |
| Available | *AI Image Import (X left)* | The import flow; at 0 left, the renewal-date message instead |

The item stays enabled in every state, so a click always explains itself. Credits are shared with
chat and the analyzer, so X can drop without an import: the info is re-fetched in the background on
`CLOUD_LOGGED_IN` / `CLOUD_LOGGED_OUT` (already subscribed by `ChatbotPlugin`) and after every AI
request completes, whether chat, analyzer or import. The label is updated on the EDT with
`putValue(Action.NAME, ...)`; `MainFrame` translates only the initial name, so updates format the
translated key themselves, e.g. `String.format(LangUtil.translate("AI Image Import (%s left)"), n)`.

## 8. Client changes

- **AI menu.** `ChatbotPlugin` injects a new top-level **AI** menu (placed between Layers and Cloud
  by install order) with *AI Analyzer* moved there from Analyze, and *AI Image Import*, labelled
  with the imports left (§7). The AI Assistant toggle stays in Config.
- *AI Image Import* (new `AiImportImageAction` beside `AiAnalyzeAction`; `ChatbotPlugin` keeps the
  last `ImportInfo` and updates the label):
  1. not logged in, not on the tier, or 0 left → the message from the table in §7;
  2. file chooser (jpg, jpeg, png, webp);
  3. `getImportInfo` again on a background task, so the dialog shows the current balance;
  4. a confirmation dialog with a thumbnail of the image, an optional instructions field, and the
     line **"This import uses %s credits. You have %s remaining."**, plus the storage notice (§6);
  5. on OK, show the AI Assistant pane if hidden and hand the import to it.
- `ChatbotPane.importImage(File, String)`: appends a user turn with the file name and instructions,
  runs `ChatbotService.importImage()` on a background task with the usual waiting message, and
  renders the result with `renderProposal()` unchanged; then refreshes the subscription line so the
  new credit balance shows.
- `ChatbotService.importImage(File, String)`: same structure as `promptChatbot` (logged-in check,
  `AiProject`, temp files deleted in `finally`, fingerprint) plus the D6 downscale into a temp file
  via `ImageIO`. `getImportInfo()` alongside `getSubscriptionInfo()`.
- `IChatbotAPI.importImage(...)`, `IChatbotAPI.getImportInfo(...)`, model class `ImportInfo`,
  `ChatbotResponse.expectedNets` (`List<List<String>>`).
- After a successful apply with `expectedNets`: compare with the new project's nets (matched by
  component name and pin) and append mismatches to the apply report. A small helper in
  `org.diylc.plugins.chatbot.service`, unit-tested.
- New strings (menu items and the three label states, dialog, cost line, storage notice, mismatch
  report) go into `English.txt`
  and every other file in `lang/`. Changelog `NEW_FEATURE` entries for the import and for the AI
  menu.

## 9. Evaluation

The regression data is a ready-made benchmark: just under 100 `.diy` projects across every
construction type, each with a reference PNG and a reference netlist in `diylc-regression-data/`.

- A harness uploads each reference PNG to `importImage.php` against an empty project, applies the
  script headlessly, extracts the netlist and scores it against the reference: net recall and
  precision over component-pin pairs, type match, value match. It also records tokens and cost per
  import, which is what sets `import_credits` (§7).
- A second, hand-collected set of 15-20 real photographs (stripboard, perfboard, PCB, turret amp,
  guitar cavity, hand-drawn and printed schematics) with hand-built reference `.diy` files covers
  photos. Photos with an unclear licence stay outside the repository.
- A prompt change is kept only if it does not lower the score on either set.

## 10. Next steps

1. **Schema** — `diylc_ai_subscription_tier.ai_model_vision` (nullable) and `import_credits`;
   `diylc_ai_chat.image_path`; the image folder and its config; PHP upload limits on the host.
2. **Server transport** — multimodal `LLMClient` content blocks for all three providers; a
   `smoke_test.php` case that sends one small PNG.
3. **Perception prompt** — `system_import_perceive.md` and §4's schema, tuned by running it alone
   over the rendered benchmark images and reading the inventories.
4. **Endpoints** — `importImage.php` with image storage, `buildForImport`, `{{IMPORT_RULES}}`,
   credit check and charge; `getImportInfo.php`.
5. **Benchmark harness** (§9); first scored run with measured costs; set `import_credits`;
   iterate on steps 3-4.
6. **Client** — API methods and models, service method with downscaling, AI menu, import action
   and dialog, chat-pane entry point, localisation, changelog. Compile `diylc-core` and
   `diylc-swing`.
7. **Net check** — `expectedNets` and the post-apply mismatch report, with unit tests.
8. **Later** — two images (component and copper side) for boards, a follow-up "fix the listed
   mismatches" round that resends the image with the list, multi-layer PCBs.

## 11. Known limits

- One image shows one side. Underboard wiring, stripboard cuts and PCB traces on the hidden side
  go to `unseen`, not guessed.
- A copper-side image shows where leads go but not what the parts are: values, polarity and
  transistor pinouts come only from silkscreen or the user's instructions, and otherwise follow D7.
- Resistor colour bands and small SMD markings are the least reliable reading; `marking` keeps the
  raw reading so they can be checked.
- Large builds can exceed one response; D6's resolution and the tier's `max_tokens` bound what one
  import can rebuild, and the failure is explicit and free (§5.8).
- Perspective in photos distorts hole counts near the edges; renders and straight-on photos work
  best, and the reply says so when the image was taken at an angle.
