# Senior-Project
Lets Talk web application


## Database Spike

**Spike question:** Can legal information be stored and delivered in a sourced, reliable, organized way?

**Status:** Layer 1 complete (schema, placeholder data, JSON query). Express endpoint, advisor review of legal content, and user/message data design are next steps.

**Decisions**
- **SQL (PostgreSQL) over NoSQL:** legal answers are structured and have relationships (one answer, many sources).
- **Supabase for hosting:** hosted Postgres, so the same schema runs locally or hosted.
- **JSON over XML:** native to AJAX and Android libraries.

**Files in `database/`**
| File | Purpose |
|---|---|
| `schema.sql` | Creates the `answers`, `sources`, and `answer_sources` tables, with read-only Row Level Security |
| `seed.sql` | Inserts placeholder (fake) data, labeled `DEMO DATA` |
| `queries.sql` | Returns one answer with its sources as JSON |

**How to rebuild**
1. Create a Supabase project.
2. In the SQL Editor, run `schema.sql`.
3. Run `seed.sql` (once only, or you will create duplicates).
4. Run `queries.sql` and confirm one row comes back with a JSON `sources` column.

**Notes**
- All data is placeholder content and has not been reviewed by legal advisors.
- Never commit database passwords or connection strings.