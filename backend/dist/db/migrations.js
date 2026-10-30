"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const pg_1 = require("pg");
const dotenv_1 = __importDefault(require("dotenv"));
dotenv_1.default.config();
const pool = new pg_1.Pool({
    connectionString: process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/medipulse'
});
async function runMigrations() {
    const client = await pool.connect();
    try {
        await client.query('BEGIN');
        await client.query(`
      CREATE TABLE IF NOT EXISTS users (
        id SERIAL PRIMARY KEY,
        email VARCHAR(254) UNIQUE NOT NULL,
        phone VARCHAR(20) UNIQUE NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        first_name VARCHAR(60) NOT NULL,
        last_name VARCHAR(60) NOT NULL,
        role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'PHARMACIST', 'PATIENT')),
        status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'DEACTIVATED', 'REJECTED')),
        gender VARCHAR(20),
        dob DATE,
        address_line1 VARCHAR(120),
        address_line2 VARCHAR(120),
        city VARCHAR(60),
        state VARCHAR(60),
        postal_code VARCHAR(10),
        country VARCHAR(60) DEFAULT 'India',
        avatar_url TEXT,
        licence_number VARCHAR(60),
        department VARCHAR(60),
        is_verified BOOLEAN DEFAULT FALSE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
        await client.query(`
      CREATE TABLE IF NOT EXISTS sessions (
        id SERIAL PRIMARY KEY,
        user_id INT REFERENCES users(id) ON DELETE CASCADE,
        refresh_token_hash VARCHAR(255) NOT NULL,
        device_info TEXT,
        expires_at TIMESTAMP NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
        await client.query(`
      CREATE TABLE IF NOT EXISTS suppliers (
        id SERIAL PRIMARY KEY,
        name VARCHAR(120) NOT NULL,
        contact_person VARCHAR(120),
        phone VARCHAR(20),
        email VARCHAR(254),
        lead_time_days INT DEFAULT 3,
        reliability_pct DECIMAL(5,2) DEFAULT 95.00,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
        await client.query(`
      CREATE TABLE IF NOT EXISTS medicines (
        id SERIAL PRIMARY KEY,
        name VARCHAR(120) NOT NULL,
        generic_name VARCHAR(120),
        brand VARCHAR(120),
        manufacturer VARCHAR(120),
        category VARCHAR(60),
        strength VARCHAR(60),
        dosage_form VARCHAR(60),
        price DECIMAL(10,2) NOT NULL,
        tax_pct DECIMAL(5,2) DEFAULT 12.00,
        barcode VARCHAR(64) UNIQUE,
        storage_conditions VARCHAR(120),
        description TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
        await client.query(`
      CREATE TABLE IF NOT EXISTS inventory_batches (
        id SERIAL PRIMARY KEY,
        medicine_id INT REFERENCES medicines(id) ON DELETE CASCADE,
        batch_number VARCHAR(60) NOT NULL,
        quantity INT NOT NULL,
        expiry_date DATE NOT NULL,
        supplier_id INT REFERENCES suppliers(id) ON DELETE SET NULL,
        reorder_threshold INT DEFAULT 20,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
        await client.query(`
      CREATE TABLE IF NOT EXISTS sales (
        id SERIAL PRIMARY KEY,
        cashier_id INT REFERENCES users(id),
        customer_name VARCHAR(120),
        total_amount DECIMAL(10,2) NOT NULL,
        discount_amount DECIMAL(10,2) DEFAULT 0.00,
        tax_amount DECIMAL(10,2) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
        await client.query(`
      CREATE TABLE IF NOT EXISTS sale_items (
        id SERIAL PRIMARY KEY,
        sale_id INT REFERENCES sales(id) ON DELETE CASCADE,
        medicine_id INT REFERENCES medicines(id),
        batch_id INT REFERENCES inventory_batches(id),
        quantity INT NOT NULL,
        unit_price DECIMAL(10,2) NOT NULL
      );
    `);
        await client.query(`
      CREATE TABLE IF NOT EXISTS prescriptions (
        id SERIAL PRIMARY KEY,
        patient_id INT REFERENCES users(id) ON DELETE CASCADE,
        prescriber_name VARCHAR(120) NOT NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'DISPENSED', 'REJECTED')),
        notes TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
        await client.query(`
      CREATE TABLE IF NOT EXISTS prescription_items (
        id SERIAL PRIMARY KEY,
        prescription_id INT REFERENCES prescriptions(id) ON DELETE CASCADE,
        medicine_id INT REFERENCES medicines(id),
        dosage VARCHAR(60) NOT NULL,
        frequency VARCHAR(60) NOT NULL,
        duration VARCHAR(60) NOT NULL
      );
    `);
        await client.query(`
      CREATE TABLE IF NOT EXISTS scheduler_jobs (
        id SERIAL PRIMARY KEY,
        job_name VARCHAR(120) NOT NULL,
        status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED', 'RUNNING', 'PENDING')),
        error_message TEXT,
        executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
        await client.query(`
      CREATE TABLE IF NOT EXISTS audit_logs (
        id SERIAL PRIMARY KEY,
        user_id INT REFERENCES users(id) ON DELETE SET NULL,
        action VARCHAR(120) NOT NULL,
        details TEXT,
        ip_address VARCHAR(45),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
        await client.query('COMMIT');
        console.log('Database migrations for Phase 5 completed successfully.');
    }
    catch (err) {
        await client.query('ROLLBACK');
        console.error('Migration failed:', err);
        process.exit(1);
    }
    finally {
        client.release();
        await pool.end();
    }
}
runMigrations();
