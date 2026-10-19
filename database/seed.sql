WITH new_answer AS (
  INSERT INTO answers (slug, topic, question, short_answer, explanation, last_verified, reviewed_by)
  VALUES (
    'visitation-support-unpaid',
    'visitation',
    'Do I have to let my children see their father if he isn''t paying child support?',
    'Follow the custody order even if support is unpaid.',
    'Visitation and child support are handled separately. Support is enforced through the court.',
    NULL,
    'DEMO DATA - not reviewed'
  )
  RETURNING id
),
new_sources AS (
  INSERT INTO sources (title, url)
  VALUES
    ('DEMO - California Family Code § 3020', 'https://leginfo.legislature.ca.gov/'),
    ('DEMO - California Family Code § 3556', 'https://leginfo.legislature.ca.gov/')
  RETURNING id
)
INSERT INTO answer_sources (answer_id, source_id)
SELECT new_answer.id, new_sources.id
FROM new_answer
CROSS JOIN new_sources;