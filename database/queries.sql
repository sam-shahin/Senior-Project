SELECT a.short_answer, a.explanation, a.last_verified,
       json_agg(json_build_object('title', s.title, 'url', s.url)) AS sources
FROM answers a
JOIN answer_sources x ON x.answer_id = a.id
JOIN sources s ON s.id = x.source_id
WHERE a.id = 1
GROUP BY a.id;