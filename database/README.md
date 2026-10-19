# Database (Layer 1 spike)

**Status:** Layer 1 complete. All content is placeholder: **DEMO DATA - not reviewed**.
Statute wording and URLs have NOT been reviewed by an advisor.

## Spike question
"Can legal information be stored and delivered in a sourced, reliable, organized way?"
- **Stored:** answered by Layer 1 (this folder).
- **Delivered:** NOT yet answered. The Express endpoint is deferred (see below).

## Files
- `schema.sql`: tables, Row Level Security, and read policies
- `seed.sql`: placeholder rows labeled as demo data
- `queries.sql`: returns one answer with its sources as a JSON array

## Tables and columns
**answers:** id, slug (unique), topic, question, short_answer, explanation, last_verified, reviewed_by
**sources:** id, title, url
**answer_sources (join table):** answer_id, source_id (composite primary key; cascade on delete)

## Demo data labeling
- `answers.reviewed_by` = "DEMO DATA - not reviewed"
- `answers.last_verified` is NULL until an advisor reviews the content
- `sources.title` values are prefixed "DEMO - "
- Source URLs currently point to the generic legislature site
  (https://leginfo.legislature.ca.gov/), not to specific statutes.

## Security
- Row Level Security is enabled on all three tables.
- Each table has a SELECT-only "public read" policy. No INSERT, UPDATE, or DELETE policies exist.
- Tested as the `anon` role: SELECT returns rows; INSERT is rejected (error 42501);
  UPDATE and DELETE change nothing.
- Not yet tested: the role the future Express server will use. A connection as the
  default `postgres` user typically bypasses RLS (verify in Supabase docs).
- No credentials are stored in this repo. `.env` is never committed.

## Deferred
- Express endpoint (`GET /api/answers/:slug`): deferred to a later ticket. Until it is
  built and tested, the app cannot retrieve answers.
- Matching a typed question to an answer (BRD free-text feature): out of scope.
- Advisor review of legal wording, statute citations, and URLs.