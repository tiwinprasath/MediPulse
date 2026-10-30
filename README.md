# MediPulse - Pharmacy Management System & Native Android App

**MediPulse** is a complete, production-quality native Android pharmacy management app built with Jetpack Compose, Material 3, MVVM + Clean Architecture, Hilt, Room, Retrofit, Navigation Compose, and a robust Node.js + Express + TypeScript + PostgreSQL backend.

---

## 🎯 Objectives & Problem Statement
MediPulse simplifies pharmacy operations by digitizing medicine inventory management (FEFO), POS billing, prescription fulfillment, patient health tracking, AI-assisted catalog lookup, supply chain forecasting, and administrative oversight.

---

## 👥 User Roles & Access Control
1. **Admin**: User approvals, role management, audit log viewer, system health monitoring.
2. **Manager**: Inventory monitoring, suppliers, sales analytics, profit tracking, reorder recommendations.
3. **Pharmacist / RX Desk**: Prescriptions, medicine catalog, dispensing, POS billing, barcode scanning.
4. **Patient**: Prescriptions, refill requests, medication reminders, purchase history, pharmacy assistant chat.

---

## 🏗️ Technology Stack & Architecture
- **Android App (`app/`)**:
  - Kotlin, Jetpack Compose, Material 3.
  - MVVM + Clean Architecture (`ui`, `domain`, `data`).
  - Dependency Injection with Dagger Hilt.
  - StateFlow with UiState/UiEvent.
  - Retrofit, OkHttp, Kotlinx Serialization, DataStore.
- **Backend (`backend/`)**:
  - Node.js, Express, TypeScript.
  - PostgreSQL with structured migrations and seed scripts.
  - JWT access tokens (15 min) + rotating refresh tokens (argon2 hashed).
  - Zod validation, helmet security, and rate limiting.

---

## 📁 Project Directory Structure
```text
pharmasenseapp/
├── app/                  # Android Native Module
│   ├── src/main/java/com/medipulse/app/
│   │   ├── data/         # Remote API DTOs, Local DataStore
│   │   ├── di/           # Hilt Network & AppModule
│   │   ├── navigation/   # Role-based Navigation Graphs
│   │   ├── ui/           # Compose Screens & ViewModels (Auth, Catalog, POS, Inventory, Prescription, Profile, Admin, Analytics, Assistant, Scanner)
│   │   └── util/         # Validators & Helpers
│   └── build.gradle.kts
├── backend/              # Node.js + TypeScript Backend
│   ├── src/
│   │   ├── db/           # Migrations & Seed scripts
│   │   └── index.ts      # Express App & REST endpoints
│   └── package.json
└── .github/workflows/    # CI Workflow
```

---

## ⚙️ Setup & Installation

### 1. PostgreSQL & Backend Setup
```bash
cd backend
npm install
# Configure your DATABASE_URL in .env
npm run db:migrate
npm run db:seed
npm run dev
```

### 2. Android Studio Setup
1. Open the project folder in **Android Studio**.
2. Sync project with Gradle files.
3. Run on an Android Emulator (API 26+) or a physical device.

> **API URL Configuration**:
> - **Android Emulator**: Uses `http://10.0.2.2:5000/api/v1/`.
> - **Physical Device**: Update `BASE_URL` in `app/build.gradle.kts` to your PC's local Wi-Fi IPv4 address (e.g., `http://192.168.1.15:5000/api/v1/`), ensuring both PC and phone are on the same Wi-Fi network.

---

## 🔑 Demo Credentials (from Seed Data)
- **Admin**: `admin@medipulse.app` / `Admin@123`
- **Manager**: `manager@medipulse.app` / `Manager@123`
- **Pharmacist**: `pharmacist@medipulse.app` / `Pharma@123`
- **Patient**: `patient1@medipulse.app` / `Patient@123`

---

## 🧪 Running Tests
- **Android Unit Tests**:
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **Backend Build Test**:
  ```bash
  cd backend
  npm run build
  ```

---

## 📦 Building APKs
- **Debug APK**:
  ```bash
  ./gradlew assembleDebug
  ```
- **Release APK (Minified with R8)**:
  ```bash
  ./gradlew assembleRelease
  ```

---

## 🚀 CI/CD & Production Deployment
- **GitHub Actions CI**: Automated checks run on every push and PR (`.github/workflows/ci.yml`).
- **Backend Deployment (Render / Railway)**:
  - Connect repository to Render/Railway.
  - Set environment variables (`DATABASE_URL`, `JWT_SECRET`, `JWT_REFRESH_SECRET`, `PORT`).
  - Build command: `npm run build`
  - Start command: `npm start`
