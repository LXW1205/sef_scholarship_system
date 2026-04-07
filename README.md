# SEF Scholarship System

A web-based scholarship management system built with plain Java and vanilla HTML/CSS/JavaScript — no heavyweight frameworks required.

![Alt text](/sample_screen/scholarship-system-1.png)

---

## Features

- Student registration and login
- Browse and apply for scholarships
- Document submission and application tracking
- Reviewer evaluation and scoring system
- Committee member management
- Interview scheduling
- Clarification requests and inquiry system
- Admin dashboard with statistics
- Automated deadline notifications
- Password reset via email

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Pure Java (JDK built-in HTTP server) |
| Database | PostgreSQL |
| DB Driver | PostgreSQL JDBC (`postgresql-42.7.8.jar`) |
| Frontend | HTML + CSS + Vanilla JavaScript |
| Testing | JUnit 5 (`junit-platform-console-standalone-1.10.1.jar`) |

---

## Prerequisites

- **Java JDK 11+** installed and added to `PATH`
- **PostgreSQL** installed and running
- A PostgreSQL database created for the system

---

## Setup

### 1. Configure the Database Connection

Create a `db.properties` file in the project root directory:

```properties
db.url=jdbc:postgresql://localhost:5432/your_database_name
db.user=your_postgres_username
db.password=your_postgres_password
```

### 2. Initialize the Database Schema

Run the setup script to create all required tables:

```bat
setup.bat
```

### 3. Run the Application

```bat
run.bat
```

The server will start on **http://localhost:8080**.

---

## Project Structure

```
sef_scholarship_system/
├── src/
│   ├── Main.java             # Entry point, HTTP server setup
│   ├── server/               # REST API handlers
│   ├── dao/                  # Data access objects (DB queries)
│   ├── model/                # Domain models (Student, Application, etc.)
│   ├── db/                   # Database connection
│   └── utils/                # Utilities (DatabaseSetup, etc.)
├── www/                      # Frontend (HTML, CSS, JS)
├── lib/                      # External JAR dependencies
├── tests/                    # JUnit test files
├── sql_queries/              # SQL scripts
├── db.properties             # Database config (not committed)
├── run.bat                   # Compile and run
├── setup.bat                 # Initialize DB schema
├── kill.bat                  # Stop the server
└── test.bat                  # Run tests
```

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/register` | Register |
| POST | `/api/auth/forgot-password` | Request password reset |
| POST | `/api/auth/reset-password` | Reset password |
| GET/POST | `/api/scholarships` | Manage scholarships |
| GET/POST | `/api/applications` | Manage applications |
| GET/PUT | `/api/users` | User management |
| GET | `/api/admin/stats` | Admin dashboard stats |
| GET/POST | `/api/notifications` | Notifications |
| GET/POST | `/api/inquiries` | Student inquiries |
| GET/POST | `/api/clarifications` | Clarification requests |
| GET/POST | `/api/interviews` | Interview scheduling |

---

## Running Tests

```bat
test.bat
```

---

## Notes

- `db.properties` is excluded from version control — do not commit credentials.
- The server runs on port `8080` by default.
- To stop the server, run `kill.bat`.

---

## Contributions

- Lee Xiu Wei
- Lee Chee Xuan
- Teng Ming Hein
- Lai Seng Kung

While this was a collaborative project, the system implementation and technical development were primarily carried out by Xiu Wei.

While the other teammates contributed more on the design side like UML diagrams and documentation.
