INSERT INTO answers (topic, question, short_answer, explanation, last_verified, reviewed_by)
VALUES ('visitation',
  'Do I have to let my children see their father if he isn''t paying child support?',
  'Follow the custody order even if support is unpaid.',
  'Visitation and child support are handled separately. Support is enforced through the court.',
  '2026-10-02', 'DEMO DATA - not reviewed');

INSERT INTO sources (title, url) VALUES
  ('California Family Code § 3020', 'https://leginfo.legislature.ca.gov/'),
  ('California Family Code § 3556', 'https://leginfo.legislature.ca.gov/');

INSERT INTO answer_sources VALUES (1, 1), (1, 2);