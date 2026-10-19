require('dotenv').config();
const { Pool } = require('pg');
const pool = new Pool({ connectionString: process.env.DATABASE_URL });

(async () => {
  try {
    const r = await pool.query('SELECT current_user');
    console.log('Connected as:', r.rows[0].current_user);
    const c = await pool.query('SELECT count(*) FROM answers');
    console.log('answers rows:', c.rows[0].count);
  } catch (err) {
    console.error('Connection failed:', err.code, err.message);
  } finally {
    await pool.end();
  }
})();