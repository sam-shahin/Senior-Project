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

//frontend to backend connection testing (Brian)
app.get('/api/test', (req, res) => {
  res.status(200).json({ message: 'Connection successful' });
});

const PORT = process.env.PORT || 3000;
// app.listen(PORT, () => console.log(`Listening on port ${PORT}`));

//frontend to backend connection testing (Brian)
app.use(express.json());
app.use((req, res, next) => {
  console.log("Incoming:", req.method, req.url);
  next();
});

app.post('/api/test-answer', (req, res) => {
  const question = req.body.question;

  if (typeof question !== 'string') {
    return res.status(400).json({ error: 'Question required' });
  }

  let response;

  switch (question) {
    case "Mock general":
      response = {
        type: "general",
        message: "This is a basic example of legal information."
      };
      break;

    case "Mock custody":
      response = {
        type: "follow_up",
        message: "Do you currently have a custody order?"
      };
      break;

    case "Mock more info":
      response = {
        type: "legal",
        message: "Here is a more detailed example of legal information.",
        sourceUrl: "https://courts.ca.gov/forms-rules/rules-court"
      };
      break;

    case "Mock suggestion":
      response = {
        type: "suggestion",
        message: "I would like to discuss our parenting schedule calmly."
      };
      break;

    default:
      response = {
        type: "general",
        message: "That's a good question. This is a mock response."
      };
  }

  return res.status(200).json(response);
});

// NOT part of frontend to backend connection testing
app.use((req, res) => res.status(404).json({ error: 'Not found' }));

//frontend to backend connection testing (Brian)
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on port ${PORT}`)
);