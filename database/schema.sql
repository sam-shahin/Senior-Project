CREATE TABLE answers (
  id SERIAL PRIMARY KEY,
  slug TEXT NOT NULL UNIQUE,
  topic TEXT NOT NULL,
  question TEXT NOT NULL,
  short_answer TEXT NOT NULL,
  explanation TEXT NOT NULL,
  last_verified DATE,
  reviewed_by TEXT
);

CREATE TABLE sources (
  id SERIAL PRIMARY KEY,
  title TEXT NOT NULL,
  url TEXT NOT NULL
);

CREATE TABLE answer_sources (
  answer_id INT REFERENCES answers(id) ON DELETE CASCADE,
  source_id INT REFERENCES sources(id) ON DELETE CASCADE,
  PRIMARY KEY (answer_id, source_id)
);

-- Read-only public access (Row Level Security)
ALTER TABLE answers ENABLE ROW LEVEL SECURITY;
ALTER TABLE sources ENABLE ROW LEVEL SECURITY;
ALTER TABLE answer_sources ENABLE ROW LEVEL SECURITY;

CREATE POLICY "public read" ON answers FOR SELECT USING (true);
CREATE POLICY "public read" ON sources FOR SELECT USING (true);
CREATE POLICY "public read" ON answer_sources FOR SELECT USING (true);