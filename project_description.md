# MediLink: Unified Healthcare & Patient Record Platform

**MediLink** is a next-generation, high-performance web application designed to eliminate healthcare records fragmentation. It connects patients, doctors, and hospitals under a secure, unified digital portal. Featuring a strict organizational hierarchy, real-time clinical registration, automated unique medical identifiers, and a glassmorphic dashboard UI, MediLink represents a modern, production-grade clinical information system.

---

## 🌟 Key Product Features

### 1. Multi-Tier Healthcare Hierarchy
MediLink operates under a strict, real-world hierarchical permission tree to prevent unauthorized accounts and secure patient data:
```mermaid
graph TD
    A[System Admin] -->|Provisions & Audits| B[Hospital Branches]
    B -->|Registers & Manages| C[Clinicians / Doctors]
    C -->|Onboards & Generates UHID| D[Patients]
    C -->|Saves Unified Diagnosis| E[Medical Records]
```
*   **System Admin**: Pre-seeded on application startup. Holds master privileges to register active Hospital organizations onto the network.
*   **Hospital Branches**: Onboarded by the Admin. Log in to register and manage Doctors working at their specific medical facility.
*   **Clinicians (Doctors)**: Registered by their parent Hospital. Log in to register walk-in Patients and record diagnostic histories.
*   **Patients**: Onboarded exclusively by active Doctors. Access a dedicated portal to view their lifelong medical records.

### 2. Auto-Generated Unique Health Identifiers (UHID)
*   When a doctor registers a patient, the system automatically generates a permanent, cryptographically sound **UHID** (e.g., `ML-A1B2C3D4`).
*   This UHID serves as the single source of truth across all hospitals on the MediLink network, allowing instantaneous profile lookups and records synchronization.

### 3. Dynamic Clinical Records Resolution
*   When a doctor logs a medical record (Diagnosis & Prescription), the backend automatically resolves the doctor's linked Hospital name from the database.
*   This avoids hardcoding or manual entry errors, ensuring the patient's timeline shows exactly which clinician treated them and at which facility.

### 4. High-End Glassmorphic User Interface
*   **Vibrant Mesh Backdrops**: Employs animated backdrop layers that slowly breathe (`scale` and `translate` keyframe loops) to keep the portal engaging.
*   **Glassmorphism Theme**: Translucent cards utilizing extensive CSS blur filters (`backdrop-filter: blur(24px)`) and subtle border highlights.
*   **Shimmer Interactions**: Interactive buttons feature sweeping reflective shine effects on hover, alongside 3D translation lifts for lists and timelines.
*   **Holographic UHID Badges**: Neon-glowing digital containers styled with monospace lettering to look premium.
*   **Custom Toast System**: Native browser alerts are completely deleted. Handled via a sleek, custom-designed sliding glass Toast Notification manager.

---

## 🛠️ Technology Stack & Architecture

### **Frontend (Client-Side)**
*   **Core**: HTML5 (Semantic Structure), CSS3 (Custom Properties / CSS Variables, Advanced Grid & Flexbox, Custom Keyframes).
*   **Logic & Routing**: Vanilla JavaScript (Modular ES6 Architecture). Single-Page Application (SPA) reactive routing handles view switches without page reloads.
*   **UI Assets**: Custom-designed, glowing inline SVG vector graphics (perfectly transparent, zero load-latency, infinite sharpness).

### **Backend (Server-Side)**
*   **Framework**: Spring Boot 3.2.3 (Java 17).
*   **API Layer**: Spring Web (RESTful REST Controllers with structured DTO serialization).
*   **Persistence Layer**: Spring Data JPA (Entity relationship mapping, automated database migrations).
*   **ORM**: Hibernate.

### **Database (Cloud Infrastructure)**
*   **Engine**: PostgreSQL.
*   **Hosting**: Supabase Cloud Database Cluster.
*   **Pooling**: HikariCP (High-efficiency live database connection pooling).

---

## 📊 Database Schema & Relationships

```mermaid
erDiagram
    APP_USERS ||--|| PATIENTS : "One-to-One mapping"
    APP_USERS ||--|| HOSPITALS : "One-to-One mapping"
    APP_USERS ||--|| DOCTORS : "One-to-One mapping"
    HOSPITALS ||--o{ DOCTORS : "One-to-Many hierarchy"
    PATIENTS ||--o{ MEDICAL_RECORDS : "One-to-Many diagnostic timeline"
    DOCTORS ||--o{ MEDICAL_RECORDS : "One-to-Many consulting entries"
```

1.  **`AppUser`**: Stores user authentication credentials (`username`, `password`, and role: `ADMIN`, `HOSPITAL`, `DOCTOR`, `PATIENT`).
2.  **`Patient`**: Maps to an `AppUser`. Stores demographic details (`name`, `age`, `bloodGroup`) and holds the unique `uhid`.
3.  **`Hospital`**: Maps to an `AppUser`. Stores institutional branch details (`name`, `address`).
4.  **`Doctor`**: Maps to an `AppUser`. Stores clinical details (`name`, `specialization`) and maintains a `@ManyToOne` join relationship to the parent `Hospital`.
5.  **`MedicalRecord`**: Represents individual clinical logs. Links directly to a `Patient` and a `Doctor`, dynamically recording diagnosis dates and treatment prescriptions.

---

