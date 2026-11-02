# Let's Talk – Answers API: What the Android App Receives

_As of 2026-11-02_

The Android app gets one thing from the backend today: a single legal-info answer, looked up by its slug, with its sources attached.

**Status: DRAFT contract.** Everything served is **DEMO DATA – not reviewed**. Nothing here is verified legal information, and statute wording and URLs still need advisor review. Items marked **[CONFIRM]** depend on server code or database checks that are not finished yet. The backend owner ([EDIT: name]) should review this doc before anyone builds against it.

## How the app gets data

The app sends an HTTPS request to the Express server and gets JSON back. Express reads the database and returns the result. The app never talks to the database and never holds database credentials.

1. The Android app calls `GET /api/answers/:slug`.
2. Express validates the slug and runs one read-only query as the `app_readonly` database role.
3. Express returns JSON: either one answer plus its list of sources, or an error.

This endpoint is read-only. The app cannot create, change, or delete answers through it.

**Base URL: [CONFIRM]**. The endpoint is not hosted yet. Hosting and HTTPS belong to the backend owner. The CSUB server is approved and requires a VPN, and public access for the pilot is still an open question. Until that is settled, the endpoint only runs on a developer's machine.

## The request

```
GET /api/answers/{slug}
```

There is no request body, no query parameters, and no login. The slug is the answer's short identifier, for example `[EDIT: demo slug from seed.sql]`.

The server checks these slug rules before it touches the database:

- Lowercase letters `a-z` and digits `0-9` only
- Hyphens are allowed only singly and only between characters (no leading, trailing, or double hyphens)
- 1 to 100 characters

A slug that breaks these rules gets a 400 response. The app should check the same rules before sending, so it never builds a bad URL.

The app must already know the slug. Typing a free-text question and being matched to an answer is a later feature and is not part of this endpoint.

## Success: 200 OK

A 200 response returns one answer object with its sources as a list. No database `id` fields are included.

**[CONFIRM]** the exact key names and casing against the output of the sourced query in `database/queries.sql` and the server code. The names below assume the database column names are passed through unchanged.

Example shape (placeholder values, not real legal content):

```json
{
  "slug": "example-slug",
  "topic": "PLACEHOLDER topic",
  "question": "PLACEHOLDER question text",
  "short_answer": "PLACEHOLDER short answer",
  "explanation": "PLACEHOLDER parent-friendly explanation",
  "last_verified": null,
  "reviewed_by": "[CONFIRM: value or null]",
  "sources": [
    { "title": "DEMO - PLACEHOLDER source title", "url": "https://legislature.example/" }
  ]
}
```

| Field | Type | What it is | Demo-data note |
| --- | --- | --- | --- |
| `slug` | string | The identifier the app asked for | – |
| `topic` | string | Topic label for grouping | – |
| `question` | string | The question in plain words | – |
| `short_answer` | string | One-line answer | Demo text, not reviewed |
| `explanation` | string | Longer parent-friendly explanation | Demo text, not reviewed |
| `last_verified` | date or null | When a reviewer last checked it | **null** on all demo answers; format [CONFIRM] |
| `reviewed_by` | string or null | Who reviewed it | [CONFIRM] |
| `sources` | array of objects | Sources for this answer, each with `title` and `url` | Titles start with `DEMO - `; URLs point to the legislature home page, not to specific statutes |
| `sources[].title` | string | Display name of the source | – |
| `sources[].url` | string | Link to open | – |

**Display rule for the app:** if `last_verified` is null, show the answer as unverified, for example with a "Demo data – not reviewed" label. Never present it as checked legal information.

## Errors and what the app should show

The endpoint uses four status codes, and all four were tested locally on Oct 19. The error body shape is **[CONFIRM]** with the backend owner, so the app should decide what to show from the status code, not from the body text.

| Status | When it happens | Suggested app state |
| --- | --- | --- |
| 200 | Answer found | Show the answer and its sources |
| 400 | Slug breaks the slug rules | "Something went wrong" (this means an app bug; log it) |
| 404 | Valid slug, but no answer with that slug | "We don't have an answer for this yet" |
| 500 | Server or database problem | "Try again later" with a retry button |
| No response | No network, a timeout, or the server is unreachable | "Check your connection" with a retry button |

The app-state column is only a suggestion. The group has not yet decided who owns the final status-code-to-app-state table.

## Nulls and Kotlin data classes

Rule: make a Kotlin field non-nullable only if the schema says NOT NULL **and** the endpoint never outputs null for it. Until a field is confirmed, keep it nullable.

| Field | Kotlin type for now | Why |
| --- | --- | --- |
| `slug` | `String` | Unique key; NOT NULL still [CONFIRM] |
| `topic` | `String?` | NULL status not checked yet |
| `question` | `String?` | NULL status not checked yet |
| `short_answer` | `String?` | NULL status not checked yet |
| `explanation` | `String?` | NULL status not checked yet |
| `last_verified` | `String?` | Known to be null on demo data; parse it into a date only after the format is confirmed |
| `reviewed_by` | `String?` | NULL status not checked yet |
| `sources` | `List<Source>?` | [CONFIRM] whether an answer with no sources returns `[]` or `null` |
| `Source.title` / `Source.url` | `String?` | NULL status not checked yet |

This is a sketch only, since the owner of the Android data classes is not decided:

```kotlin
data class Answer(
    val slug: String,
    val topic: String?,
    val question: String?,
    val short_answer: String?,
    val explanation: String?,
    val last_verified: String?,
    val reviewed_by: String?,
    val sources: List<Source>?
)

data class Source(
    val title: String?,
    val url: String?
)
```

The database owner will supply three things: the confirmed NULL / NOT NULL list, what an answer with no sources returns, and the exact `last_verified` format. This table gets updated then.

## Not in this API yet, and open questions

- There is no hosted URL, no HTTPS, and no Android connection yet. The endpoint is built and was curl-tested locally, but it is not merged.
- There is no free-text question search; the app must already know the slug.
- There are no user accounts, co-parent messages, or AI phrasing suggestions. Each of those needs its own secure design later.
- There is no verified legal content. All answers stay demo data until an advisor reviews the wording, citations, and statute-specific URLs.
- Any change to the schema, the query output, the database role, or RLS can change this response. Each change gets its own ticket and a heads-up to the backend owner before it merges.

**Open questions**

- [ ] Exact JSON key names and casing (backend owner)
- [ ] Error body shape for 400 / 404 / 500 (backend owner)
- [ ] Base URL once hosted (backend owner)
- [ ] NULL / NOT NULL for every field, the empty-sources result, and the `last_verified` format (database owner)
- [ ] Should the response ever expose `id` fields? (group decision; currently no)
- [ ] Who owns the Android data classes and the status-to-app-state table? (group decision)
