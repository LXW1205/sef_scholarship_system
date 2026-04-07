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

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Pure Java (JDK built-in HTTP server) |
| **Database** | PostgreSQL |
| **DB Driver** | PostgreSQL JDBC (`postgresql-42.7.8.jar`) |
| **Frontend** | HTML5 + CSS3 + Vanilla JavaScript |
| **Testing** | JUnit 5 (`junit-platform-console-standalone-1.10.1.jar`) |
| **Deployment** | Docker + Render |

---

## 📂 Project Structure
- `src/`: Java source code (Handler-based API).
- `www/`: Static frontend assets (HTML, CSS, JS).
- `sql_queries/`: Database schema and sample data.
- `lib/`: Third-party JARs (JDBC Driver, JUnit).
- `Dockerfile`: Multi-stage build for cloud deployment.
- `*.bat`: Windows scripts for compilation and local execution.

---

## 🤝 Contributions
Primary development by **Lee Xiu Wei**, with collaborative design and documentation from Lee Chee Xuan, Teng Ming Hein, and Lai Seng Kung.
