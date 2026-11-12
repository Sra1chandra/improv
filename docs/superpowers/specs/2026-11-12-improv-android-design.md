# S.andy — Android improv story partner

Date: 2026-11-12  
Status: draft, awaiting user review  
Stack: Kotlin, Jetpack Compose, Room, OpenAI Chat Completions  
Min SDK: 26  
Application id: `app.sandy.improv`  
Audience: one person, this phone only

## What it is

A personal Android app for **yes-and story building** with an AI character named **S.andy**.

You type a line (narration, dialogue, or a sudden turn).
S.andy replies with one short continuation of **what happens next**.
The thread is the story.
S.andy usually continues.
About 30% of turns, the **app** (not the model) rolls chaos: keep every established fact, add one sharp turn so you have to think.

Not two-human improv.
Not a chat bot that essays.
Not voice in v1.

## Who is who

- **You:** no display name.
  In a thread, your avatar is a **person silhouette** in a circle (head and shoulders).
  Blue felt outline (Looseleaf "you" pen).
- **S.andy:** the scene partner.
  Avatar is the letter **S** in a circle.
  Dark felt-tip for their words.
  Name is hardcoded.
  Pun: Sandy / yes-and-y.
- **Chat list avatars:** not you and not S.andy.
  Each story gets a **letter from the first line** (skip leading "A "/"An "/"The " if present).
  Same story always same letter.

Never say "beat" in the UI.
A turn is just a message.

## Screens (v1 is only these three)

### 1. Chats (home)

- List of stories, newest updated first.
- Title = first message you sent, truncated to one line.
- Subtitle = last message in that story, truncated.
- Letter avatar as above.
- Gear in the corner → Settings.
- Stamp-style control: **+ New story**.
- Empty: "No stories yet." Same stamp.

No search.
No unread filter.
No story icons (no trees, no emoji scenes).
No binder clip / printer ornament.

### 2. Thread

- Back.
- Title = same as list title, truncated.
- Paper sheet.
- Your messages: person avatar, blue outline on the bubble.
- S.andy messages: **S** avatar, dark ink.
- Graphite captions: `you` and `S.andy`.
- Bottom: empty composer (no placeholder text) + terracotta **Send**.
- While waiting: a short "…" under the last message.
  Send disabled until the reply lands or errors.
- New story: blank sheet, composer focused.

### 3. Settings (calm density)

- Back.
- OpenAI API key (hidden).
- Model: `gpt-4o-mini` (dropdown allowed; default that).
- One graphite note: "Key stays on this phone."
- No login.
- No rename S.andy.

## Visual language

Source: [Looseleaf principles](https://kevintngo.github.io/looseleaf/#/foundations/principles).
Looseleaf is React.
We do **not** embed their components.
We copy the laws in Compose and vendor **Kevin Sans** (MIT) plus a few static SVGs we export (person silhouette, paper tooth tile).

Pens:

- Dark felt: outlines, body text, S.andy.
- Blue felt: you, composer focus ring, your bubble outline.
- Graphite: captions, dates, hints.
- Terracotta marker: **Send** and **+ New story** only.
- Red pencil: errors, always with a word. Never used as S.andy's voice.

Laws we will actually implement in v1:

- Cream paper with tooth. Light from upper left.
- Short warm umber shadow. No long gray Material blur.
- Sheet may tilt about 1° on Chats and Thread.
  Text stays level.
  Settings has **no** tilt (calm).
- Reduced motion → no tilt, no jitter anywhere.
- Fills sit slightly inside outlines where we draw stamps/buttons.
- Chat = comfortable.
  Settings = calm.

Default Material 3 shapes and purple theme stay out.

## Data (on device)

`Story`: id, title, createdAt, updatedAt, letter (cached from first line).

`Message`: id, storyId, role (`you` | `sandy`), text, createdAt.

Room database.
Kill app, data remains.
No cloud backup.

API key in EncryptedSharedPreferences.
Never log it.
Never put it in git.

## S.andy's brain

Phone → `https://api.openai.com/v1/chat/completions`.
No backend.

### Die roll (true random)

Before each request the app rolls:

- 70% continue
- 30% chaos

Chaos injects a **hidden** system/user instruction:
keep all facts; add one new problem or person or place; still one short reply.

Do not ask the model to "be random".
It isn't.

### Output shape (ADHD + no slop)

Adapted from [ayghri/i-have-adhd](https://github.com/ayghri/i-have-adhd) for a scene partner, not a coding agent.

Each S.andy message:

- 1–3 short lines.
- Hard cap 40 words.
- API `max_tokens`: 80.
- First line **is** the scene.
- No preamble, recap, closer, markdown, lists, headings, or "as an AI".
- No "Yes, and" spoken aloud.
- No slop words: tapestry, delve, vibrant, journey, "in this moment".
- One new fact per turn.
- Match the user's length when they wrote one line.
- Characters may fight.
  S.andy the writer must not erase established facts.

Improv rules baked into the system prompt (distilled, not quoted from books):

- Accept the shared world, then add.
- Be changed by what just happened (Johnstone).
- If this is true, what else is true (Close / UCB), but only one step.
- Bring a brick, not a cathedral.

v1 is **not** streamed.
Show "…", then the full reply.

Context: system prompt + messages in this story.
If the thread is huge, drop oldest messages after the first (keep the opening line so the world does not vanish).

## Errors

Red pencil + a short word.
Your already-sent line stays saved.

| Case | What you see |
| --- | --- |
| No key | "add key in Settings" under the composer |
| 401 / bad key | "key refused" |
| Offline | "offline" |
| Timeout / 5xx | "S.andy is stuck" |
| Empty Send | nothing. ignore |

No retry button in v1.
Send again by typing nothing extra: user taps Send on a new line, or we allow resend of empty wait state by tapping Send again only if the last you-message has no sandy reply yet.
**Pick one:** if the last message is yours and S.andy failed, Send with an empty box retries that turn.
That is the only time empty Send does something.

## Out of scope (v1)

Voice.
Accounts, backend, Play Store.
Search, folders, unread.
On-device model.
Renaming S.andy.
Attachments, code blocks, stickers, mascot.
Two-human mode.

Later doors (not this spec): voice, on-device model, friends via a backend.

## Testing (smallest checks)

1. New story, one line, S.andy replies ≤ 3 lines.
2. Kill app, story still in the list, thread intact.
3. Key in Settings survives kill app.
4. Chaos: force the die to chaos in debug; reply must not deny the previous fact.
5. Airplane mode after a send: your line remains; "offline" shows.

## Success

You can sit down, type a scene, get a short yes-and, yank or follow, and later open the same story.
It looks like a notebook on a desk, not ChatGPT.
S.andy sometimes surprises you.
S.andy never writes an essay.
