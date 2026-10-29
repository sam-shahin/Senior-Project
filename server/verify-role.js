// verify-role.js
// Proves the Express connection is a least-privilege role and cannot write.
// Run from the server/ folder:  node verify-role.js
// Reads DATABASE_URL from .env. Never prints the connection string.
//
// Rules:
//  - PASS only when a write fails with Postgres error 42501 (insufficient_privilege).
//  - A write that executes without error is a FAIL, even if it touched 0 rows.
//  - Any other error is a FAIL (inconclusive), never a PASS.
//  - Every write test runs inside a transaction that is always rolled back.
//  - This file must never contain a COMMIT statement.

require('dotenv').config();
const { Pool } = require('pg');

const pool = new Pool({ connectionString: process.env.DATABASE_URL });
const TABLES = ['answers', 'sources', 'answer_sources'];

let failures = 0;
function pass(msg) { console.log('PASS  ' + msg); }
function fail(msg) { console.log('FAIL  ' + msg); failures++; }

async function expectPrivilegeDenied(label, sql) {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    try {
      const r = await client.query(sql);
      fail(`${label}: executed without error (rowCount=${r.rowCount}) - role HAS this privilege`);
    } catch (err) {
      if (err.code === '42501') pass(`${label}: denied (42501)`);
      else fail(`${label}: unexpected error ${err.code} - inconclusive, investigate`);
    }
  } finally {
    await client.query('ROLLBACK').catch(() => {});
    client.release();
  }
}

async function counts() {
  const out = {};
  for (const t of TABLES) {
    const r = await pool.query(`SELECT count(*)::int AS n FROM ${t}`);
    out[t] = r.rows[0].n;
  }
  return out;
}

(async () => {
  try {
    // 1. Identity
    const who = await pool.query(
      `SELECT current_user AS u,
              (SELECT rolsuper FROM pg_roles WHERE rolname = current_user) AS is_super,
              (SELECT rolbypassrls FROM pg_roles WHERE rolname = current_user) AS bypass`
    );
    const { u, is_super, bypass } = who.rows[0];
    console.log(`Connected as: ${u}  (superuser=${is_super}, bypassrls=${bypass})`);
    if (is_super || bypass) fail('role is superuser or bypasses RLS');
    else pass('role is not superuser and does not bypass RLS');

    // 2. Snapshot before
    const before = await counts();
    console.log('Row counts before:', JSON.stringify(before));

    // 3. Privilege gate (writes nothing), using current_user, not a hardcoded name
    let gateClean = true;
    for (const t of TABLES) {
      const p = await pool.query(
        `SELECT has_table_privilege(current_user, $1, 'SELECT') AS s,
                has_table_privilege(current_user, $1, 'INSERT') AS i,
                has_table_privilege(current_user, $1, 'UPDATE') AS u,
                has_table_privilege(current_user, $1, 'DELETE') AS d`,
        [t]
      );
      const { s, i, u: up, d } = p.rows[0];
      console.log(`Privileges on ${t}: select=${s} insert=${i} update=${up} delete=${d}`);
      if (!s) { fail(`${t}: role cannot SELECT`); gateClean = false; }
      if (i || up || d) { fail(`${t}: role has a write privilege`); gateClean = false; }
    }

    // 4. Write attempts only if the gate is clean. All use impossible conditions.
    if (!gateClean) {
      console.log('Privilege gate FAILED: skipping write attempts.');
    } else {
      await expectPrivilegeDenied('INSERT answers',
        "INSERT INTO answers (slug, topic, question, short_answer, explanation) SELECT 'zz','t','q','a','e' WHERE false");
      await expectPrivilegeDenied('UPDATE answers', "UPDATE answers SET topic = 'x' WHERE id = -1");
      await expectPrivilegeDenied('DELETE answers', 'DELETE FROM answers WHERE id = -1');
      await expectPrivilegeDenied('INSERT sources',
        "INSERT INTO sources (title, url) SELECT 't','u' WHERE false");
      await expectPrivilegeDenied('UPDATE sources', "UPDATE sources SET title = 'x' WHERE id = -1");
      await expectPrivilegeDenied('DELETE sources', 'DELETE FROM sources WHERE id = -1');
      await expectPrivilegeDenied('INSERT answer_sources',
        'INSERT INTO answer_sources (answer_id, source_id) SELECT 1,1 WHERE false');
      await expectPrivilegeDenied('UPDATE answer_sources', 'UPDATE answer_sources SET answer_id = answer_id WHERE answer_id = -1');
      await expectPrivilegeDenied('DELETE answer_sources', 'DELETE FROM answer_sources WHERE answer_id = -1');
    }

    // 5. Snapshot after
    const after = await counts();
    console.log('Row counts after: ', JSON.stringify(after));
    if (JSON.stringify(before) !== JSON.stringify(after)) fail('row counts changed');
    else pass('row counts unchanged');

    console.log(failures === 0 ? '\nRESULT: ALL PASS' : `\nRESULT: ${failures} FAILURE(S)`);
  } catch (err) {
    console.error('Script error:', err.code, err.message);
    failures++;
  } finally {
    await pool.end();
    process.exit(failures === 0 ? 0 : 1);
  }
})();