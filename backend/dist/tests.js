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
async function runBackendTests() {
    console.log('Running MediPulse Backend Automated Tests...');
    const client = await pool.connect();
    try {
        // 1. Database connectivity test
        const dbRes = await client.query('SELECT 1 as connected');
        if (dbRes.rows[0].connected === 1) {
            console.log('✓ Database connectivity test passed.');
        }
        else {
            throw new Error('Database connectivity test failed.');
        }
        // 2. Check admin user exists
        const adminRes = await client.query("SELECT id, email, role FROM users WHERE role = 'ADMIN' LIMIT 1");
        if (adminRes.rows.length > 0) {
            console.log(`✓ Admin user verification passed (${adminRes.rows[0].email}).`);
        }
        else {
            throw new Error('No admin user found in database.');
        }
        // 3. Check audit logs table
        const auditRes = await client.query('SELECT COUNT(*) as count FROM audit_logs');
        console.log(`✓ Audit logs table accessible (Total logs: ${auditRes.rows[0].count}).`);
        // 4. Check active admins count (last-admin protection verification)
        const adminCountRes = await client.query("SELECT COUNT(*) as count FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE'");
        const activeAdmins = parseInt(adminCountRes.rows[0].count, 10);
        console.log(`✓ Last-admin protection check: Active admins = ${activeAdmins} (Must be >= 1).`);
        if (activeAdmins < 1) {
            throw new Error('Violation: No active administrators found.');
        }
        console.log('All backend automated checks completed successfully!');
    }
    catch (err) {
        console.error('Backend automated test failed:', err.message);
        process.exit(1);
    }
    finally {
        client.release();
        await pool.end();
    }
}
runBackendTests();
