import { Pool } from 'pg';
import argon2 from 'argon2';
import dotenv from 'dotenv';

dotenv.config();

const pool = new Pool({
  connectionString: process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/medipulse'
});

async function seedDatabase() {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    await client.query('DELETE FROM sale_items');
    await client.query('DELETE FROM sales');
    await client.query('DELETE FROM inventory_batches');
    await client.query('DELETE FROM medicines');
    await client.query('DELETE FROM suppliers');
    await client.query('DELETE FROM audit_logs');
    await client.query('DELETE FROM sessions');
    await client.query('DELETE FROM otp_codes');
    await client.query('DELETE FROM users');

    const adminPass = await argon2.hash('Admin@123');
    const managerPass = await argon2.hash('Manager@123');
    const pharmaPass = await argon2.hash('Pharma@123');
    const patientPass = await argon2.hash('Patient@123');

    await client.query(`
      INSERT INTO users (email, phone, password_hash, first_name, last_name, role, status, is_verified) VALUES
      ('admin@medipulse.app', '+919876543210', $1, 'Aarav', 'Sharma', 'ADMIN', 'ACTIVE', TRUE),
      ('manager@medipulse.app', '+919876543211', $2, 'Priya', 'Verma', 'MANAGER', 'ACTIVE', TRUE),
      ('pharmacist@medipulse.app', '+919876543212', $3, 'Rohan', 'Gupta', 'PHARMACIST', 'ACTIVE', TRUE),
      ('patient1@medipulse.app', '+919876543213', $4, 'Ananya', 'Iyer', 'PATIENT', 'ACTIVE', TRUE);
    `, [adminPass, managerPass, pharmaPass, patientPass]);

    const supRes = await client.query(`
      INSERT INTO suppliers (name, contact_person, phone, email, lead_time_days, reliability_pct) VALUES
      ('Cipla Pharma Ltd', 'Rajesh Kumar', '+919811122233', 'supply@cipla.com', 2, 98.5),
      ('Sun Pharma Industries', 'Suresh Patel', '+919822233344', 'orders@sunpharma.com', 4, 95.0)
      RETURNING id;
    `);

    const sup1Id = supRes.rows[0].id;
    const sup2Id = supRes.rows[1].id;

    const medRes = await client.query(`
      INSERT INTO medicines (name, generic_name, brand, manufacturer, category, strength, dosage_form, price, tax_pct, barcode, storage_conditions, description) VALUES
      ('Paracetamol 650', 'Paracetamol', 'Calpol', 'Cipla', 'Analgesic', '650mg', 'Tablet', 30.00, 12.00, '8901234567890', 'Store below 25°C', 'Effective fever and pain reliever.'),
      ('Amoxicillin 500', 'Amoxicillin', 'Moxatag', 'Sun Pharma', 'Antibiotic', '500mg', 'Capsule', 120.00, 12.00, '8901234567891', 'Store in a cool dry place', 'Broad-spectrum antibiotic.'),
      ('Cetirizine 10', 'Cetirizine', 'Zyrtec', 'Cipla', 'Antihistamine', '10mg', 'Tablet', 45.00, 12.00, '8901234567892', 'Store at room temperature', 'Allergy relief.')
      RETURNING id;
    `);

    const med1Id = medRes.rows[0].id;
    const med2Id = medRes.rows[1].id;
    const med3Id = medRes.rows[2].id;

    await client.query(`
      INSERT INTO inventory_batches (medicine_id, batch_number, quantity, expiry_date, supplier_id, reorder_threshold) VALUES
      ($1, 'BAT-P650-01', 150, '2027-06-30', $4, 30),
      ($2, 'BAT-AMX-02', 80, '2026-12-15', $5, 20),
      ($3, 'BAT-CET-03', 200, '2028-01-10', $4, 40);
    `, [med1Id, med2Id, med3Id, sup1Id, sup2Id]);

    await client.query('COMMIT');
    console.log('Database seeded successfully with Phase 2 data.');
  } catch (err) {
    await client.query('ROLLBACK');
    console.error('Seeding failed:', err);
    process.exit(1);
  } finally {
    client.release();
    await pool.end();
  }
}

seedDatabase();
