-- Read-only role for the Express server.
-- Run in the Supabase SQL editor. Replace the placeholder with a generated
-- password TYPED INTO THE EDITOR ONLY. Never save or commit the real one.
CREATE ROLE app_readonly LOGIN PASSWORD 'REPLACE_ME_IN_EDITOR_ONLY';
GRANT USAGE ON SCHEMA public TO app_readonly;
GRANT SELECT ON answers, sources, answer_sources TO app_readonly;