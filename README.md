# EventFlow — Full-Stack Online Event Management System

**EventFlow** is an enterprise-grade, modern, full-stack Online Event Management Platform built with **Java 21**, **Spring Boot 3.3.4**, **Spring Security 6**, **Spring Data JPA & Hibernate**, **MySQL 8 / MariaDB / H2**, **Flyway**, **Thymeleaf**, **Bootstrap 5**, **Bootstrap Icons**, **Chart.js**, and **ZXing QR Code Engine**.

---

## 🌟 Key Features & Role-Based Workflows

### 1. Public Landing & Discovery
- **Hero & Curated Collections**: "Discover Events. Create Memories." with search by keyword, category, city, and dates.
- **Event Categories**: Technology, Music, Business, Education, Sports, Arts & Culture.
- **Rich Event Details**: Venue/virtual meeting credentials, ticket tier breakdowns, organizer cards, real-time seat countdowns.
- **Responsive Layout**: Designed with a custom SaaS design system, soft gradients, modern cards, and micro-animations.

### 2. Multi-Role Authentication & Security
- **Secure Password Hashing**: BCrypt encryption for all user credentials.
- **Role-Based Access Control**:
  - `/admin/**` &rarr; Strict `ADMIN` role.
  - `/organizer/**` &rarr; Strict `ORGANIZER` role.
  - `/attendee/**` &rarr; Strict `ATTENDEE` role.
- **Smart Redirection**: Automatic redirection to appropriate dashboards upon login.
- **Data Isolation**: Organizers can only manage their own events and attendees.
- **Account Safeguards**: Deactivation toggles, duplicate prevention, and CSRF protection.

### 3. Administrator Console (`/admin/dashboard`)
- **System Telemetry**: Total users, total events, pending approvals, registrations, and revenue.
- **Interactive Chart.js Visualizations**: Daily registration & revenue trends, event category distribution doughnut.
- **User Governance (`/admin/users`)**: Search, filter by role/status, toggle active status, edit permissions, delete accounts.
- **Event Approvals (`/admin/events/pending`)**: Review organizer submissions, approve for publication, or reject with mandatory feedback.
- **Global Settings (`/admin/settings`)**: Currency, public registration toggle, contact emails, and approval policies.
- **Activity Audit Log (`/admin/activity`)**: Real-time audit trail of logins, bookings, approvals, and security updates.

### 4. Event Organizer Hub (`/organizer/dashboard`)
- **Performance Metrics**: Tickets sold, upcoming events, gross revenue, daily attendee signup graphs.
- **Event Management (`/organizer/events`)**: Complete event CRUD, venue logistics, online meeting links, and capacity limits.
- **Approval Workflow**: State machine transitions (`DRAFT` &rarr; `PENDING_APPROVAL` &rarr; `APPROVED` &rarr; `PUBLISHED` / `REJECTED`).
- **Ticket Pricing & Inventory (`/organizer/tickets`)**: Free, General Admission, VIP, and Early Bird tiers with max-per-order caps.
- **Pessimistic Concurrency Locking**: Prevents overselling during high-volume checkout rushes.
- **Attendee Roster & Export (`/organizer/attendees`)**: Search, filter, and export full attendee list to CSV.
- **Door Check-in Scanner (`/organizer/checkin`)**: Scan or enter ticket codes to validate entrance and prevent duplicate check-ins.
- **Broadcast Announcements (`/organizer/announcements`)**: Broadcast urgent messages directly to registered attendees.

### 5. Attendee Experience (`/attendee/dashboard`)
- **Ticket Checkout**: Multi-step booking with live price calculations and server-side payment processing.
- **Digital Tickets with QR Codes**: Unique ticket codes (`TKT-XXXXXXXXXX`) and scannable Base64 QR codes.
- **Printable Pass (`/attendee/tickets/{code}`)**: Clean printable pass layout for mobile and paper entry.
- **Registration History (`/attendee/registrations`)**: View past orders and access digital tickets anytime.
- **Notification Center (`/attendee/notifications`)**: Read organizer broadcasts, booking confirmations, and reminders.
- **Profile Management (`/attendee/profile`)**: Manage personal contact details and password.

---

## 💻 Tech Stack & Architecture

- **Backend Framework**: Spring Boot 3.3.4 (Java 21)
- **Security**: Spring Security 6 with Form Login, BCrypt, and Role Authorization
- **Persistence**: Spring Data JPA & Hibernate 6
- **Database Migrations**: Flyway Core & Flyway MySQL
- **Database Support**: MySQL 8 / MariaDB (Production/Dev), H2 In-Memory (Automated Testing & Fast Local Dev)
- **View Engine**: Thymeleaf + Thymeleaf Extras Spring Security 6
- **Frontend**: Bootstrap 5.3, Bootstrap Icons 1.11, Chart.js 4.4, Custom Vanilla CSS
- **Utilities**:
  - `com.google.zxing:core` & `javase` (QR Code Engine)
  - `com.opencsv:opencsv` (CSV Export)
- **Testing**: JUnit 5, Mockito, Spring Boot Test, Spring Security Test, MockMvc

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java 21 JDK** (configured in `C:\Users\ASUS\tools\jdk-21.0.6+7`)
- **Maven 3.9+** (configured in `C:\Users\ASUS\tools\apache-maven-3.9.9` and via `./mvnw`)

### Default Demo Accounts

All default credentials are automatically populated on first run:

| Role | Email | Password | Dashboard URL |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@eventflow.com` | `Admin@123` | [http://localhost:8080/admin/dashboard](http://localhost:8080/admin/dashboard) |
| **Event Organizer**| `organizer@eventflow.com` | `Organizer@123` | [http://localhost:8080/organizer/dashboard](http://localhost:8080/organizer/dashboard) |
| **Attendee** | `attendee@eventflow.com` | `Attendee@123` | [http://localhost:8080/attendee/dashboard](http://localhost:8080/attendee/dashboard) |

---

### Running the Application

#### Option 1: One-Click Launch (Windows)
Double-click `run-app.bat` or run in PowerShell:
```powershell
.\run-app.ps1
```

#### Option 2: Running with Maven
```powershell
$env:JAVA_HOME = "C:\Users\ASUS\tools\jdk-21.0.6+7"
$env:Path = "$env:JAVA_HOME\bin;C:\Users\ASUS\tools\apache-maven-3.9.9\bin;" + $env:Path

# Fast zero-setup mode (in-memory H2 with Flyway migrations):
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"

# Or with MySQL:
mvn spring-boot:run
```

Once started, open your browser to **[http://localhost:8080/](http://localhost:8080/)**.

---

### Database Configuration (MySQL / MariaDB)

To connect to your local MySQL / MariaDB instance:
1. Ensure MySQL / MariaDB service is running on port `3306`.
2. Configure credentials in `.env` or set environment variables:
   ```env
   DB_URL=jdbc:mysql://localhost:3306/event_management_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   DB_USERNAME=root
   DB_PASSWORD=your_password
   ```
3. Flyway migrations (`V1__init_schema.sql` and `V2__seed_initial_data.sql`) will automatically create all tables, indexes, and initial data on launch.

---

### Running Automated Tests

Run the test suite via Maven:
```powershell
$env:JAVA_HOME = "C:\Users\ASUS\tools\jdk-21.0.6+7"
$env:Path = "$env:JAVA_HOME\bin;C:\Users\ASUS\tools\apache-maven-3.9.9\bin;" + $env:Path
mvn test
```

Test Results:
- **15 tests passing, 0 failures, 0 errors**:
  - `EventManagementApplicationTests`: Context loading & Flyway migrations
  - `SecurityAndAccessTest`: Public routes, unauthenticated redirects, attendee access restriction, admin authorization
  - `UserServiceTest`: Registration validation, duplicate email prevention, role assignment, account status toggling
  - `EventServiceTest`: Event creation, organizer data isolation, admin review & notification triggers
  - `RegistrationServiceTest`: Pessimistic locking inventory deduction, duplicate registration prevention, capacity enforcement, QR generation

---

## 📂 Project Structure

```
guvi project2/
├── pom.xml
├── run-app.bat
├── run-app.ps1
├── .env.example
├── README.md
└── src/
    ├── main/
    │   ├── java/com/eventflow/
    │   │   ├── EventManagementApplication.java
    │   │   ├── config/
    │   │   │   └── DatabaseInitializer.java
    │   │   ├── controller/
    │   │   │   ├── AdminController.java
    │   │   │   ├── AttendeeController.java
    │   │   │   ├── AuthController.java
    │   │   │   ├── CheckoutController.java
    │   │   │   ├── HomeController.java
    │   │   │   └── OrganizerController.java
    │   │   ├── dto/
    │   │   ├── entity/
    │   │   │   └── enums/
    │   │   ├── exception/
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── repository/
    │   │   ├── security/
    │   │   │   ├── CustomAuthenticationSuccessHandler.java
    │   │   │   ├── CustomUserDetails.java
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   └── SecurityConfig.java
    │   │   ├── service/
    │   │   └── util/
    │   │       ├── CsvExporter.java
    │   │       ├── QrCodeGenerator.java
    │   │       └── SlugUtils.java
    │   └── resources/
    │       ├── application.properties
    │       ├── application-h2.properties
    │       ├── db/migration/
    │       │   ├── V1__init_schema.sql
    │       │   └── V2__seed_initial_data.sql
    │       ├── static/
    │       │   ├── css/styles.css
    │       │   └── js/app.js
    │       └── templates/
    │           ├── admin/
    │           ├── attendee/
    │           ├── auth/
    │           ├── checkout/
    │           ├── error/
    │           ├── events/
    │           ├── fragments/
    │           └── organizer/
    └── test/
        ├── java/com/eventflow/
        └── resources/application-test.properties
```
