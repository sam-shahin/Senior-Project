require('dotenv').config();
const express = require('express');
const { Pool } = require('pg');

const app = express();
app.disable('x-powered-by');
const pool = new Pool({ connectionString: process.env.DATABASE_URL });

const SLUG_RE = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;

const SQL = `
  SELECT a.slug, a.topic, a.question, a.short_answer, a.explanation,
         a.last_verified, a.reviewed_by,
         COALESCE(
           json_agg(json_build_object('title', s.title, 'url', s.url) ORDER BY s.id)
             FILTER (WHERE s.id IS NOT NULL),
           '[]'::json
         ) AS sources
  FROM answers a
  LEFT JOIN answer_sources x ON x.answer_id = a.id
  LEFT JOIN sources s ON s.id = x.source_id
  WHERE a.slug = $1
  GROUP BY a.id
`;

app.get('/api/answers/:slug', async (req, res) => {
  const slug = req.params.slug;
  if (slug.length > 100 || !SLUG_RE.test(slug)) {
    return res.status(400).json({ error: 'Invalid answer identifier' });
  }
  try {
    const result = await pool.query(SQL, [slug]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Answer not found' });
    }
    return res.status(200).json(result.rows[0]);
  } catch (err) {
    console.error('Query failed:', err.code);
    return res.status(500).json({ error: 'Server error' });
  }
});

app.use((req, res) => res.status(404).json({ error: 'Not found' }));

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Listening on port ${PORT}`));