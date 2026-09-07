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

## 8. Current Implementation Status

### Phase 1 — Project Setup & Database Foundations (Complete)
- [x] **Maven JavaFX 21 Project Setup**: Clean POM with JavaFX, MySQL Connector, HikariCP, ZXing, jBCrypt, SLF4J, and JUnit 5.
- [x] **Database Schema (`database/schema.sql`)**: 5 relational tables with foreign keys, cascading rules, and performance indexes.
- [x] **Realistic Seed Data (`database/seed.sql`)**: 3 facilities in Chennai, 28 multi-floor slots across 4 types, sample reservations, payments, and BCrypt-hashed credentials.
- [x] **Domain Models & Enums**: Entity POJOs (`User`, `ParkingLocation`, `ParkingSlot`, `Reservation`, `Payment`) and type-safe enums (`Role`, `SlotType`, `SlotStatus`, `ReservationStatus`, `PaymentStatus`, `PaymentMethod`).
- [x] **Connection Pool (`DatabaseConfig.java`)**: Thread-safe HikariCP initialization with automatic environment variable fallbacks.
- [x] **Session State Manager (`AppSession.java`)**: Thread-safe user session singleton.
- [x] **JavaFX Entry Point & FXML (`MainApp.java`, `main.fxml`, `style.css`)**: Bootstrap stage and styling.
- [x] **Prototype Separation**: Clear disclaimer banner added to the React UI prototype.

### Phase 2 — Data Access Object (DAO) Layer (Complete)
- [x] **DAO Abstraction Layer**: 5 interfaces (`UserDao`, `ParkingLocationDao`, `ParkingSlotDao`, `ReservationDao`, `PaymentDao`).
- [x] **Concrete JDBC Implementations**: HikariCP-backed DAOs (`JdbcUserDao`, `JdbcParkingLocationDao`, `JdbcParkingSlotDao`, `JdbcReservationDao`, `JdbcPaymentDao`).
- [x] **Exception Encapsulation**: `DaoException` wraps checked `SQLException` instances to prevent database internals from leaking into business or presentation layers.
- [x] **Transaction Orchestration**: `JdbcUtils.executeTransaction` pattern and `ParkingSlotDao.findByIdForUpdate(Connection, int)` for concurrent booking atomicity.
- [x] **Safe Conversions & Precision**: `JdbcUtils` with null-safe Enum parsing, `java.time.LocalDateTime` to `java.sql.Timestamp` mapping, and `BigDecimal` precision for all financial columns.
- [x] **Offline Unit Test Suite**: `JdbcUtilsTest` and `DaoStructureTest` verifying contracts, enum mapping, and reflection signatures.

### Phase 3 — Authentication & User Security Service (Complete)
- [x] **Service Exception Hierarchy**: Unchecked base `ServiceException`, `ValidationException`, and `AuthenticationException` decoupling presentation/controller layers from lower-level database details.
- [x] **Authentication Service (`AuthService.java`)**:
  - Full input validation & normalization (Unicode-compliant names, standard email formatting with lowercase normalization, international phone numbers with optional `+`, raw un-trimmed passwords between 8-72 characters, and vehicle formatting).
  - Duplicate email prevention prior to database insertion.
  - Enforced `Role.CUSTOMER` for public self-registration.
  - Salted BCrypt password hashing (`BCrypt.hashpw`, `BCrypt.gensalt()`) and verification (`BCrypt.checkpw`).
  - Thread-safe `AppSession` integration on login, logout, and role inspection.
  - Generic authentication failure exceptions that resist user enumeration.
  - Pure business logic completely decoupled from JavaFX dependencies.
- [x] **User Management Service (`UserService.java`)**: Dedicated service for non-authentication user operations (profile updates, vehicle updates, session synchronization, and administrative user directories).
- [x] **Comprehensive JUnit 5 Unit Tests (`AuthServiceTest.java`)**: 14 tests covering valid and edge-case registrations, validation boundaries, password hashing, session synchronization, and login flows via an in-memory `FakeUserDao`.

---

## 9. Phase 2 — DAO Layer Architecture & Documentation

### 9.1 DAO Layer Architecture

The persistence tier decouples higher-level domain services from relational SQL syntax:

```
Service Layer (AuthService, ReservationService, BillingService)
                     │
                     ▼
         DAO Interfaces (UserDao, etc.)
                     │
                     ▼
  JDBC Implementations (JdbcUserDao, etc.)
         │                        │
         ▼                        ▼
DatabaseConfig / HikariCP     JdbcUtils (Conversions & Tx)
         │
         ▼
     MySQL 8.x (smart_parking database)
```

### 9.2 DAO Interfaces and Implementations

| Interface | Implementation | Target Table | Primary Responsibilities |
| :--- | :--- | :--- | :--- |
| `UserDao` | `JdbcUserDao` | `users` | CRUD, `findByEmail`, `existsByEmail`, role filtering. *(Passwords handled as raw hashes without business hashing in DAO).* |
| `ParkingLocationDao` | `JdbcParkingLocationDao` | `parking_locations` | CRUD, `findActive`, geo-coordinates (`latitude`/`longitude`), hourly tariffs. |
| `ParkingSlotDao` | `JdbcParkingSlotDao` | `parking_slots` | CRUD, `findByLocationId`, `findByFloor`, status counters, `updateStatus`, and row-locking via `findByIdForUpdate(Connection, int)`. |
| `ReservationDao` | `JdbcReservationDao` | `reservations` | CRUD, `findByCode`, `findActiveByUserId`, user/slot history, status updates, nullable timestamp mapping. |
| `PaymentDao` | `JdbcPaymentDao` | `payments` | CRUD, `findByReservationId`, `findByStatus`, `BigDecimal` financial precision (`duration_hours`, `total_amount`). |

### 9.3 SQL Quality & Security
- **PreparedStatements**: 100% parameter parameterized queries (`?`). User input is never concatenated into SQL strings.
- **Explicit Projection**: No `SELECT *` statements. Every query explicitly names columns matching `database/schema.sql`.
- **Resource Management**: Strict `try-with-resources` blocks ensure all `Connection`, `PreparedStatement`, and `ResultSet` handles close promptly back into the HikariCP pool.
- **Key Generation**: Primary keys generated via `Statement.RETURN_GENERATED_KEYS` are automatically mapped back to entity IDs upon `save()`.

### 9.4 Transaction Ownership & Atomicity
Multi-step business transactions (e.g. locking a slot, verifying status, creating a reservation, and updating slot status) are owned by the **Service Layer** in Phase 3.
- `JdbcUtils.executeTransaction(TransactionCallback<T>)` manages `setAutoCommit(false)`, `commit()`, and `rollback()`.
- `ParkingSlotDao.findByIdForUpdate(Connection connection, int slotId)` accepts the active transactional connection to issue `SELECT ... FOR UPDATE` without opening a secondary connection.

### 9.5 How to Test the DAO Layer

1. **Offline Unit & Structural Tests (No MySQL Required)**:
   ```bash
   mvn clean test
   ```
   Runs `ModelAndConfigTest`, `JdbcUtilsTest`, and `DaoStructureTest`. Verifies mapping logic, null-safe conversions, enum fallbacks, interface compliance, and reflection signatures without needing an active MySQL instance.

2. **Integration Tests (Requires Live MySQL 8.x Instance)**:
   Ensure your local MySQL service is active and seeded with `database/schema.sql` and `database/seed.sql`:
   ```bash
   # Configure environment credentials
   export DB_HOST="localhost"
   export DB_PORT="3306"
   export DB_NAME="smart_parking"
   export DB_USER="root"
   export DB_PASSWORD="your_mysql_password"

   # Run tests or launch JavaFX application
   mvn test
   mvn javafx:run
   ```

---

## 10. Phase 3 — Authentication & User Security Architecture & Documentation

### 10.1 Service Layer Architecture

The service tier contains business validation rules, security policies, and session state orchestration, completely decoupled from JavaFX UI views:

```
Presentation / Controllers (LoginController, RegisterController)
                         │
                         ▼
        Service Layer (AuthService, UserService)
        - Strict Data Validation & Normalization
        - BCrypt 0.4 Password Hashing ($2a$ salt)
        - Session Population (AppSession)
                         │
                         ▼
             DAO Layer (UserDao, etc.)
                         │
                         ▼
                 MySQL 8.x Database
```

### 10.2 Services and Exception Handling

| Class | Type | Package | Primary Responsibilities |
| :--- | :--- | :--- | :--- |
| `AuthService` | Service | `com.smartparking.service` | `register(...)`, `login(...)`, `logout()`, session role queries, BCrypt password hashing & verification. |
| `UserService` | Service | `com.smartparking.service` | Non-auth profile operations: `getUserById`, `updateProfile`, `updateVehicleNumber`, `getAllUsers`, `getUsersByRole`. |
| `ServiceException` | Unchecked Exception | `com.smartparking.service` | Base business exception wrapping internal persistence or runtime faults. |
| `ValidationException` | Unchecked Exception | `com.smartparking.service` | Specific validation failure exception (format, length, duplicate email). |
| `AuthenticationException` | Unchecked Exception | `com.smartparking.service` | Secure credential verification failure exception (prevents account enumeration). |

### 10.3 Business Validation & Normalization Rules

- **Full Name**: Required, trimmed, 2 to 100 characters. Supports letters (including international Unicode `\p{L}`), spaces, hyphens, periods, and apostrophes.
- **Email**: Required, trimmed, converted to lowercase before lookup or persistence, maximum 120 characters. Validated against standard email format (`user@domain.tld`) with whitespace rejected.
- **Duplicate Prevention**: Rejects duplicate email registrations case-insensitively before attempting database persistence.
- **Phone Number**: Required, trimmed, 10 to 15 digits with optional leading `+` symbol (max 20 characters total).
- **Password**: Required, 8 to 72 characters inclusive. Spaces are preserved without trimming or case conversion. Encrypted using `jBCrypt` with randomly generated salt (`BCrypt.gensalt()`).
- **Vehicle Number**: Optional. Trimmed; stored as `null` if empty. Maximum 30 characters; allows alphanumeric characters, spaces, and hyphens.
- **Role Enforcement**: Public registration strictly assigns `Role.CUSTOMER`. Admin role can never be self-assigned.

### 10.4 Offline Unit Testing

The authentication service includes comprehensive JUnit 5 tests using an in-memory `FakeUserDao`:

```bash
mvn clean test
```

Runs:
- `AuthServiceTest`: Tests 14 scenarios including successful registration normalization, BCrypt verification, duplicate email rejection, boundary validations (name, email, phone, password, vehicle), invalid login handling, session synchronization, and logout.
- `ModelAndConfigTest`: Tests Phase 1 entities, enums, and `AppSession`.
- `JdbcUtilsTest`: Tests Phase 2 conversion utilities and enum parsers.
- `DaoStructureTest`: Tests Phase 2 DAO reflection interfaces and signatures.

---

## 11. Upcoming Development Roadmap

- **Phase 4 — JavaFX Authentication UI**:
  - `Login.fxml` and `Register.fxml` views following modern Geometric Balance styling.
  - `LoginController` and `RegisterController` integrating `AuthService`.
  - Role-based dashboard navigation (Customer dashboard vs. Admin dashboard).
- **Phase 5 — Slot Management & Real-Time Booking**:
  - `ReservationService` with atomic multi-step booking transactions (`FOR UPDATE`).
  - Floor-level visual bay selector with live occupancy color-coding.
  - Time-slot locking and conflict prevention.
- **Phase 6 — Google Maps & Spatial Discovery**:
  - JavaFX WebView Leaflet/Google Maps Platform bridge.
  - Interactive map pins showing capacity, rates, and distance.
- **Phase 7 — ZXing QR Code Gate Simulator & Automated Billing**:
  - Dynamic QR code generation for confirmed reservations.
  - Gate check-in and check-out scanning with duration billing and simulated receipts.
- **Phase 8 — Analytics Dashboard & Reporting**:
  - Occupancy rate charts, peak hour graphs, and revenue CSV exports.
