# SEF Scholarship System

A web-based scholarship management system built with **Pure Java** (Standard JDK HTTP Server) and a vanilla **HTML/CSS/JS** frontend. No heavyweight frameworks required.

![System Preview](/sample_screen/scholarship-system-1.png)

---

## ✨ Features

- **Student Registration & Login**: Full authentication system.
- **Browse & Apply**: Students can search for and apply to various scholarships.
- **Document Submission**: Complete application tracking with document uploads.
- **Review System**: Reviewers can evaluate and score applications.
- **Committee Management**: High-level oversight and final decision making.
- **Interview Scheduling**: Integrated tool for scheduling and tracking interviews.
- **Clarification System**: Inquiries and clarification requests between students and reviewers.
- **Admin Dashboard**: Comprehensive statistics and system oversight.
- **Automated Notifications**: Deadline reminders and status updates.
- **Password Reset**: Secure reset via email.

---

## 🚀 Live Deployment (Render)

This project is configured for one-click deployment on **Render** using Docker.

### 1. Database Setup
1. Create a **PostgreSQL** database on Render.
2. Copy the **Internal Database URL**.

### 2. Web Service Setup
1. Create a new **Web Service** on Render and connect this repository.
2. Select **Docker** as the runtime.
3. Add the following **Environment Variables**:

| Key | Value | Note |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://user:pass@host/db` | Convert `postgres://` to `jdbc:postgresql://` |
| `DB_USER` | `your_db_user` | Provided by Render |
| `DB_PASSWORD` | `your_db_password` | Provided by Render |
| `AUTO_SEED` | `true` | **Set to 'true' only for the first run** to create tables |

---

## 🔑 Sample Credentials
Use these to test the various dashboards:

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@mmu.edu.my` | `Admin@123` |
| **Reviewer** | `reviewer1@mmu.edu.my` | `Review@123` |
| **Committee** | `committee1@mmu.edu.my` | `Committee@123` |
| **Student** | `ahmad.ismail@student.mmu.edu.my` | `Student@123` |

---

## 💻 Local Setup

### Prerequisites
- **Java JDK 11+**
- **PostgreSQL** installed locally

### Quick Start
1. Create a `db.properties` file in the root:
   ```properties
   db.url=jdbc:postgresql://localhost:5432/your_db
   db.user=postgres
   db.password=your_password
   ```
2. Run `setup.bat` to initialize the database.
3. Run `run.bat` to start the server at `http://localhost:8080`.

## Running Tests

```bat
test.bat
```

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

## Notes

- `db.properties` is excluded from version control — do not commit credentials.
- The server runs on port `8080` by default.
- To stop the server, run `kill.bat`.

---

## 🤝 Contributions

- Lee Xiu Wei
- Lee Chee Xuan
- Teng Ming Hein
- Lai Seng Kung

Primary development by **Lee Xiu Wei**, with collaborative design and documentation from Lee Chee Xuan, Teng Ming Hein, and Lai Seng Kung.

Project deployment is also some extra work done after the submission of this academic project. 
