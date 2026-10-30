"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const pg_1 = require("pg");
const argon2_1 = __importDefault(require("argon2"));
const jsonwebtoken_1 = __importDefault(require("jsonwebtoken"));
const helmet_1 = __importDefault(require("helmet"));
const cors_1 = __importDefault(require("cors"));
const express_rate_limit_1 = __importDefault(require("express-rate-limit"));
const zod_1 = require("zod");
const dotenv_1 = __importDefault(require("dotenv"));
dotenv_1.default.config();
const app = (0, express_1.default)();
const port = process.env.PORT || 5000;
const JWT_SECRET = process.env.JWT_SECRET || 'medipulse_jwt_secret_key_2026';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'medipulse_refresh_secret_key_2026';
const pool = new pg_1.Pool({
    connectionString: process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/medipulse'
});
app.use((0, helmet_1.default)());
app.use((0, cors_1.default)());
app.use(express_1.default.json());
const authLimiter = (0, express_rate_limit_1.default)({
    windowMs: 15 * 60 * 1000,
    max: 50,
    message: { error: 'Too many attempts, please try again later.' }
});
app.use('/api/v1/auth/', authLimiter);
// Health check
app.get('/api/health', async (req, res) => {
    try {
        await pool.query('SELECT 1');
        res.json({
            status: 'healthy',
            database: 'connected',
            timestamp: new Date().toISOString()
        });
    }
    catch (err) {
        res.status(500).json({ status: 'unhealthy', error: 'Database unreachable' });
    }
});
// Zod schemas
const registerSchema = zod_1.z.object({
    email: zod_1.z.string().email().max(254),
    phone: zod_1.z.string().regex(/^\+\d{10,15}$/),
    password: zod_1.z.string().min(8),
    firstName: zod_1.z.string().min(2).max(60),
    lastName: zod_1.z.string().min(2).max(60),
    role: zod_1.z.enum(['MANAGER', 'PHARMACIST', 'PATIENT']),
    gender: zod_1.z.string().optional(),
    dob: zod_1.z.string().optional(),
    addressLine1: zod_1.z.string().optional(),
    city: zod_1.z.string().optional(),
    state: zod_1.z.string().optional(),
    postalCode: zod_1.z.string().optional(),
    licenceNumber: zod_1.z.string().optional()
});
const loginSchema = zod_1.z.object({
    identifier: zod_1.z.string(),
    password: zod_1.z.string()
});
function authenticateToken(req, res, next) {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];
    if (!token)
        return res.status(401).json({ error: 'Access token required' });
    jsonwebtoken_1.default.verify(token, JWT_SECRET, (err, user) => {
        if (err)
            return res.status(403).json({ error: 'Invalid or expired token' });
        req.user = user;
        next();
    });
}
function requireRole(roles) {
    return (req, res, next) => {
        if (!req.user || !roles.includes(req.user.role)) {
            return res.status(403).json({ error: 'Insufficient permissions' });
        }
        next();
    };
}
// Auth Routes
app.post('/api/v1/auth/register', async (req, res) => {
    try {
        const data = registerSchema.parse(req.body);
        const client = await pool.connect();
        try {
            const existing = await client.query('SELECT id FROM users WHERE email = $1 OR phone = $2', [data.email, data.phone]);
            if (existing.rows.length > 0) {
                return res.status(422).json({ error: 'Email or phone already registered' });
            }
            const passwordHash = await argon2_1.default.hash(data.password);
            const initialStatus = data.role === 'PATIENT' ? 'ACTIVE' : 'PENDING';
            const isVerified = data.role === 'PATIENT';
            const result = await client.query(`INSERT INTO users (email, phone, password_hash, first_name, last_name, role, status, gender, dob, address_line1, city, state, postal_code, licence_number, is_verified)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15) RETURNING id, email, role, status`, [data.email, data.phone, passwordHash, data.firstName, data.lastName, data.role, initialStatus, data.gender, data.dob, data.addressLine1, data.city, data.state, data.postalCode, data.licenceNumber, isVerified]);
            res.status(201).json({ message: 'Registration successful', user: result.rows[0] });
        }
        finally {
            client.release();
        }
    }
    catch (err) {
        if (err instanceof zod_1.z.ZodError) {
            return res.status(422).json({ error: 'Validation failed', details: err.errors });
        }
        res.status(500).json({ error: err.message });
    }
});
app.post('/api/v1/auth/login', async (req, res) => {
    try {
        const { identifier, password } = loginSchema.parse(req.body);
        const client = await pool.connect();
        try {
            const result = await client.query('SELECT * FROM users WHERE email = $1 OR phone = $1', [identifier]);
            if (result.rows.length === 0)
                return res.status(401).json({ error: 'Invalid credentials' });
            const user = result.rows[0];
            if (user.status === 'DEACTIVATED')
                return res.status(403).json({ error: 'Account is deactivated' });
            if (user.status === 'PENDING')
                return res.status(403).json({ error: 'Account pending admin approval' });
            const validPassword = await argon2_1.default.verify(user.password_hash, password);
            if (!validPassword)
                return res.status(401).json({ error: 'Invalid credentials' });
            const accessToken = jsonwebtoken_1.default.sign({ id: user.id, role: user.role, email: user.email }, JWT_SECRET, { expiresIn: '15m' });
            const refreshToken = jsonwebtoken_1.default.sign({ id: user.id }, JWT_REFRESH_SECRET, { expiresIn: '7d' });
            const refreshTokenHash = await argon2_1.default.hash(refreshToken);
            await client.query('INSERT INTO sessions (user_id, refresh_token_hash, expires_at) VALUES ($1, $2, NOW() + INTERVAL \'7 days\')', [user.id, refreshTokenHash]);
            res.json({
                accessToken,
                refreshToken,
                user: {
                    id: user.id,
                    email: user.email,
                    firstName: user.first_name,
                    lastName: user.last_name,
                    role: user.role,
                    status: user.status
                }
            });
        }
        finally {
            client.release();
        }
    }
    catch (err) {
        res.status(400).json({ error: err.message });
    }
});
app.get('/api/v1/me', authenticateToken, async (req, res) => {
    try {
        const client = await pool.connect();
        try {
            const result = await client.query('SELECT id, email, phone, first_name, last_name, role, status, gender, dob, address_line1, city, state, postal_code, country, avatar_url, licence_number, department, is_verified, created_at FROM users WHERE id = $1', [req.user?.id]);
            if (result.rows.length === 0)
                return res.status(404).json({ error: 'User not found' });
            res.json(result.rows[0]);
        }
        finally {
            client.release();
        }
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
// Admin endpoints
app.get('/api/v1/admin/users', authenticateToken, requireRole(['ADMIN']), async (req, res) => {
    try {
        const client = await pool.connect();
        try {
            const result = await client.query('SELECT id, email, phone, first_name, last_name, role, status, created_at FROM users ORDER BY created_at DESC');
            res.json(result.rows);
        }
        finally {
            client.release();
        }
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
app.put('/api/v1/admin/users/:id/status', authenticateToken, requireRole(['ADMIN']), async (req, res) => {
    try {
        const { status } = req.body;
        const userId = req.params.id;
        const client = await pool.connect();
        try {
            const result = await client.query('UPDATE users SET status = $1, updated_at = NOW() WHERE id = $2 RETURNING id, email, status', [status, userId]);
            if (result.rows.length === 0)
                return res.status(404).json({ error: 'User not found' });
            res.json({ message: 'User status updated', user: result.rows[0] });
        }
        finally {
            client.release();
        }
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
app.get('/api/v1/admin/audit-logs', authenticateToken, requireRole(['ADMIN']), async (req, res) => {
    try {
        const client = await pool.connect();
        try {
            const result = await client.query('SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT 100');
            res.json(result.rows);
        }
        finally {
            client.release();
        }
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
app.get('/api/v1/admin/health', authenticateToken, requireRole(['ADMIN']), async (req, res) => {
    try {
        await pool.query('SELECT 1');
        res.json({ status: 'healthy', database: 'connected', timestamp: new Date().toISOString() });
    }
    catch (err) {
        res.status(500).json({ status: 'unhealthy', error: err.message });
    }
});
// Catalog / Medicines
app.get('/api/v1/medicines', authenticateToken, async (req, res) => {
    try {
        const client = await pool.connect();
        try {
            const result = await client.query('SELECT * FROM medicines ORDER BY name ASC');
            res.json(result.rows);
        }
        finally {
            client.release();
        }
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
// Inventory Batches (FEFO)
app.get('/api/v1/inventory', authenticateToken, async (req, res) => {
    try {
        const client = await pool.connect();
        try {
            const result = await client.query(`
        SELECT ib.*, m.name as medicine_name, m.dosage_form, s.name as supplier_name
        FROM inventory_batches ib
        JOIN medicines m ON ib.medicine_id = m.id
        LEFT JOIN suppliers s ON ib.supplier_id = s.id
        ORDER BY ib.expiry_date ASC
      `);
            res.json(result.rows);
        }
        finally {
            client.release();
        }
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
// Suppliers
app.get('/api/v1/suppliers', authenticateToken, async (req, res) => {
    try {
        const client = await pool.connect();
        try {
            const result = await client.query('SELECT * FROM suppliers ORDER BY name ASC');
            res.json(result.rows);
        }
        finally {
            client.release();
        }
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
// POS / Sales
app.post('/api/v1/sales', authenticateToken, requireRole(['PHARMACIST', 'MANAGER', 'ADMIN']), async (req, res) => {
    const { items, customerName, discountAmount } = req.body;
    const client = await pool.connect();
    try {
        await client.query('BEGIN');
        let subtotal = 0;
        let taxTotal = 0;
        const saleItemsToInsert = [];
        for (const item of items) {
            const medRes = await client.query('SELECT price, tax_pct FROM medicines WHERE id = $1', [item.medicineId]);
            if (medRes.rows.length === 0)
                throw new Error(`Medicine ID ${item.medicineId} not found`);
            const med = medRes.rows[0];
            const unitPrice = parseFloat(med.price);
            const taxPct = parseFloat(med.tax_pct);
            const batchRes = await client.query('SELECT id, quantity FROM inventory_batches WHERE medicine_id = $1 AND quantity > 0 ORDER BY expiry_date ASC FOR UPDATE', [item.medicineId]);
            let remainingQty = item.quantity;
            for (const batch of batchRes.rows) {
                if (remainingQty <= 0)
                    break;
                const deduct = Math.min(remainingQty, batch.quantity);
                saleItemsToInsert.push({
                    medicineId: item.medicineId,
                    batchId: batch.id,
                    quantity: deduct,
                    unitPrice
                });
                await client.query('UPDATE inventory_batches SET quantity = quantity - $1 WHERE id = $2', [deduct, batch.id]);
                remainingQty -= deduct;
            }
            if (remainingQty > 0)
                throw new Error(`Insufficient stock for medicine ID ${item.medicineId}`);
            const itemTotal = unitPrice * item.quantity;
            subtotal += itemTotal;
            taxTotal += itemTotal * (taxPct / 100);
        }
        const discount = discountAmount ? parseFloat(discountAmount) : 0;
        const totalAmount = subtotal + taxTotal - discount;
        const saleRes = await client.query(`INSERT INTO sales (cashier_id, customer_name, total_amount, discount_amount, tax_amount)
       VALUES ($1, $2, $3, $4, $5) RETURNING id`, [req.user?.id, customerName || 'Walk-in', totalAmount, discount, taxTotal]);
        const saleId = saleRes.rows[0].id;
        for (const si of saleItemsToInsert) {
            await client.query(`INSERT INTO sale_items (sale_id, medicine_id, batch_id, quantity, unit_price)
         VALUES ($1, $2, $3, $4, $5)`, [saleId, si.medicineId, si.batchId, si.quantity, si.unitPrice]);
        }
        await client.query('COMMIT');
        res.status(201).json({ message: 'Sale completed successfully', saleId, totalAmount });
    }
    catch (err) {
        await client.query('ROLLBACK');
        res.status(400).json({ error: err.message });
    }
    finally {
        client.release();
    }
});
// Prescriptions
app.get('/api/v1/prescriptions', authenticateToken, async (req, res) => {
    try {
        const client = await pool.connect();
        try {
            let query = `
        SELECT p.*, u.first_name || ' ' || u.last_name as patient_name
        FROM prescriptions p
        JOIN users u ON p.patient_id = u.id
      `;
            const params = [];
            if (req.user?.role === 'PATIENT') {
                query += ' WHERE p.patient_id = $1';
                params.push(req.user.id);
            }
            query += ' ORDER BY p.created_at DESC';
            const result = await client.query(query, params);
            res.json(result.rows);
        }
        finally {
            client.release();
        }
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
// Forecasting & Analytics
app.get('/api/v1/forecasts', authenticateToken, requireRole(['ADMIN', 'MANAGER', 'PHARMACIST']), async (req, res) => {
    try {
        const client = await pool.connect();
        try {
            const result = await client.query(`
        SELECT m.name, m.category, SUM(ib.quantity) as total_stock,
               CASE WHEN SUM(ib.quantity) < 50 THEN 'CRITICAL' WHEN SUM(ib.quantity) < 100 THEN 'MODERATE' ELSE 'LOW' END as shortage_class
        FROM medicines m
        LEFT JOIN inventory_batches ib ON m.id = ib.medicine_id
        GROUP BY m.id, m.name, m.category
      `);
            res.json(result.rows);
        }
        finally {
            client.release();
        }
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
app.listen(port, () => {
    console.log(`MediPulse backend running on port ${port}`);
});
