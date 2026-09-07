# Smart Parking Management System

Enterprise Desktop Smart Parking Management System built with **Java 17/21**, **JavaFX 21**, **FXML**, **CSS**, **Maven**, and **MySQL 8.x**.

---

## 1. Project Overview

The **Smart Parking Management System** is an offline-capable, high-performance desktop application designed for campus environments and modern parking facilities. It provides:
- Real-time parking slot availability and reservation management.
- Multi-floor and multi-facility support (including standard, compact, EV charging, and accessible bays).
- Digital QR code pass validation at entry/exit gate barriers (powered by ZXing).
- Interactive campus locator mapping (via JavaFX WebView & Google Maps Platform).
- Automated duration-based parking tariff calculation and billing audits.
- Role-Based Access Control (RBAC) separating administrative facility supervisors from drivers/customers.

> **Architecture Note**: The earlier web prototype in this repository is preserved purely as a visual UI reference. The production application is implemented in pure JavaFX (MVC + layered service/DAO architecture) backed by MySQL.

---

## 2. Technology Stack

- **Platform & Language**: Java 17 or Java 21 (LTS)
- **GUI Framework**: JavaFX 21 (`javafx-controls`, `javafx-fxml`, `javafx-web`)
- **Build & Dependency Management**: Apache Maven 3.8+
- **Database**: MySQL 8.x (InnoDB engine, `utf8mb4_unicode_ci`)
- **Persistence & Connectivity**: JDBC with `PreparedStatements` and **HikariCP 5.1.0** connection pooling
- **Security & Cryptography**: **jBCrypt 0.4** (salted Blowfish password hashing)
- **Barcode & Matrix Encoding**: **ZXing 3.5.3** (Zebra Crossing)
- **Logging**: SLF4J 2.0 with SimpleLogger
- **Testing**: JUnit 5 Jupiter 5.10.2

---

## 3. Architecture & Project Structure

The project strictly follows a **layered MVC architecture**:

```
Presentation Layer  (JavaFX + FXML + CSS)
         │
Controller Layer    (JavaFX FXML Controllers)
         │
Business Layer      (Services & Domain Validations)
         │
Data Access Layer   (DAO Interfaces & JDBC Implementations with HikariCP)
         │
Database Layer      (MySQL 8.x Relational Schema)
```

### Directory Structure

```
smart-parking/
├── pom.xml                               # Maven Project Descriptor
├── README.md                             # Project Documentation
├── db.properties.example                 # Database Configuration Template
├── .env.example                          # Environment Variables Template
│
├── database/
│   ├── schema.sql                        # DDL: 5 Core Tables + Constraints & Indexes
│   └── seed.sql                          # DML: Realistic Chennai Campus Seed Data
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── smartparking/
    │   │           ├── MainApp.java      # JavaFX Application Entry Point
    │   │           ├── config/
    │   │           │   └── DatabaseConfig.java   # HikariCP Connection Pool Manager
    │   │           ├── model/
    │   │           │   ├── User.java             # User Account Entity
    │   │           │   ├── ParkingLocation.java  # Facility Entity
    │   │           │   ├── ParkingSlot.java      # Bay Entity
    │   │           │   ├── Reservation.java      # Booking Record Entity
    │   │           │   ├── Payment.java          # Billing Entity
    │   │           │   └── enums/
    │   │           │       ├── Role.java
    │   │           │       ├── SlotType.java
    │   │           │       ├── SlotStatus.java
    │   │           │       ├── ReservationStatus.java
    │   │           │       ├── PaymentStatus.java
    │   │           │       └── PaymentMethod.java
    │   │           ├── dao/              # JDBC DAO Interfaces & Impls (Phase 2 & 3)
    │   │           ├── service/          # Domain Services (Phase 2 & 3)
    │   │           ├── controller/       # JavaFX Controllers (Phase 2 & 3)
    │   │           └── util/
    │   │               └── AppSession.java       # User Session State Singleton
    │   │
    │   └── resources/
    │       ├── fxml/
    │       │   └── main.fxml             # Phase 1 JavaFX Viewport
    │       ├── css/
    │       │   └── style.css             # JavaFX Modern Stylesheet
    │       ├── images/                   # UI Icons and Branding Assets
    │       └── map/                      # Google Maps WebView Templates
    │
    └── test/
        └── java/
            └── com/
                └── smartparking/
                    └── ModelAndConfigTest.java   # Unit Tests for Models & Config
```

---

## 4. Prerequisites

Before setting up and running the application, ensure the following are installed:
1. **Java Development Kit (JDK)**: OpenJDK 17 or OpenJDK 21.
   - Verify with: `java -version` and `javac -version`
2. **Apache Maven**: Version 3.8.0 or higher.
   - Verify with: `mvn -version`
3. **MySQL Server**: Version 8.0 or 8.3+.
   - Verify with: `mysql --version`

---

## 5. Database Setup (MySQL 8.x)

### Step 5.1 — Access MySQL CLI or Workbench
Log in to your local MySQL server:
```bash
mysql -u root -p
```

### Step 5.2 — Execute `schema.sql`
Run the schema creation script to instantiate the `smart_parking` database and tables:
```bash
mysql -u root -p < database/schema.sql
```
*Or execute `database/schema.sql` directly inside MySQL Workbench.*

### Step 5.3 — Execute `seed.sql`
Populate the database with realistic sample campus data (Chennai CEG & IITM Research Park):
```bash
mysql -u root -p < database/seed.sql
```

### Database Tables Summary

| Table | Primary Key | Description |
| :--- | :--- | :--- |
| `users` | `user_id` | Stores user credentials, hashed passwords, vehicle numbers, and roles (`CUSTOMER`, `ADMIN`). |
| `parking_locations` | `location_id` | Campus facilities, GPS coordinates (latitude/longitude), and hourly rates. |
| `parking_slots` | `slot_id` | Individual bays with floor levels, classifications (`STANDARD`, `COMPACT`, `EV_CHARGING`, `HANDICAPPED`), and real-time status. |
| `reservations` | `reservation_id` | Unique booking tokens (`reservation_code`), user IDs, slot IDs, and check-in/out timestamps. |
| `payments` | `payment_id` | Financial settlement audits, calculated duration, total fees, payment status (`PAID`, `PENDING`), and methods. |

### Pre-Configured Seed User Credentials

| Full Name | Email | Password | Role | Registered Vehicle |
| :--- | :--- | :--- | :--- | :--- |
| Dr. Sarah Jenkins | `admin@campus.edu` | `Admin@123` | `ADMIN` | TN 07 CE 0001 |
| Arun Kumar | `arun.kumar@gmail.com` | `Customer@123` | `CUSTOMER` | TN 09 BX 4512 |
| Priya Sundaram | `priya.sundaram@gmail.com` | `Student@123` | `CUSTOMER` | TN 07 CA 8924 |
| Prof. Karthik Rajan | `karthik.rajan@annauniv.edu` | `Staff@123` | `CUSTOMER` | TN 22 DQ 3110 |

*All seed passwords are encrypted using BCrypt ($2a$10$...) and match their plain-text representations.*

---

## 6. Database Configuration

The application **never** stores hardcoded passwords in Java source code. Configuration is resolved via:
1. **Environment Variables** (Highest Priority)
2. **`db.properties` File** (Secondary Priority)
3. **Internal Safe Fallbacks**

### Option A — Using Environment Variables (Recommended for CI/CD)
Set the following environment variables in your terminal or OS environment:
```bash
export DB_HOST="localhost"
export DB_PORT="3306"
export DB_NAME="smart_parking"
export DB_USER="root"
export DB_PASSWORD="your_mysql_password"
```

On Windows (PowerShell):
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="smart_parking"
$env:DB_USER="root"
$env:DB_PASSWORD="your_mysql_password"
```

### Option B — Using `db.properties`
1. Copy the provided template:
   ```bash
   cp db.properties.example src/main/resources/db.properties
   ```
2. Edit `src/main/resources/db.properties` with your credentials:
   ```properties
   db.host=localhost
   db.port=3306
   db.name=smart_parking
   db.user=root
   db.password=your_mysql_password
   ```

---

## 7. How to Build & Run the JavaFX Application

### Step 7.1 — Compile the Project & Run Unit Tests
```bash
mvn clean test
```

### Step 7.2 — Launch the JavaFX Application
Launch directly using the OpenJFX Maven Plugin:
```bash
mvn javafx:run
```

### Step 7.3 — Package as a Standalone Executable JAR
```bash
mvn clean package
```
The compiled JAR will be output to `target/smart-parking-system-1.0.0-SNAPSHOT.jar`.

---

## 8. Current Implementation Status (Phase 1)

- [x] **Maven JavaFX 21 Project Setup**: Clean POM with JavaFX, MySQL Connector, HikariCP, ZXing, jBCrypt, SLF4J, and JUnit 5.
- [x] **Database Schema (`database/schema.sql`)**: 5 relational tables with foreign keys, cascading rules, and performance indexes.
- [x] **Realistic Seed Data (`database/seed.sql`)**: 3 facilities in Chennai, 28 multi-floor slots across 4 types, sample reservations, payments, and BCrypt-hashed credentials.
- [x] **Domain Models & Enums**: Entity POJOs (`User`, `ParkingLocation`, `ParkingSlot`, `Reservation`, `Payment`) and type-safe enums (`Role`, `SlotType`, `SlotStatus`, `ReservationStatus`, `PaymentStatus`, `PaymentMethod`).
- [x] **Connection Pool (`DatabaseConfig.java`)**: Thread-safe HikariCP initialization with automatic environment variable fallbacks.
- [x] **Session State Manager (`AppSession.java`)**: Thread-safe user session singleton.
- [x] **JavaFX Entry Point & FXML (`MainApp.java`, `main.fxml`, `style.css`)**: Bootstrap stage and styling.
- [x] **Unit Testing (`ModelAndConfigTest.java`)**: Automated POJO and session test suite.
- [x] **Prototype Separation**: Clear disclaimer banner added to the React UI prototype.

---

## 9. Upcoming Development Roadmap

- **Phase 2 — Authentication & User Module**:
  - `UserDAO` implementation (JDBC queries with `PreparedStatements`).
  - `AuthService` with BCrypt password hashing & validation.
  - Login & Registration FXML screens and controllers.
  - Role-based navigation routing (Customer vs. Admin).
- **Phase 3 — Slot Management & Real-Time Booking**:
  - `ParkingSlotDAO`, `LocationDAO`, and `ReservationDAO`.
  - Floor-level visual bay selector with live occupancy color-coding.
  - Slot reservation workflow with time-slot locking and conflict prevention.
- **Phase 4 — Google Maps & Spatial Discovery**:
  - JavaFX WebView Leaflet/Google Maps Platform bridge.
  - Interactive map pins showing capacity, rates, and distance.
- **Phase 5 — ZXing QR Code Gate Simulator & Automated Billing**:
  - Dynamic QR code generation for confirmed reservations.
  - Gate check-in and check-out scanning with duration billing and simulated receipts.
- **Phase 6 — Analytics Dashboard & Reporting**:
  - Occupancy rate charts, peak hour graphs, and revenue CSV exports.
