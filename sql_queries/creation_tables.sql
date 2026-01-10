CREATE TABLE "User" (
    userID SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    isActive BOOLEAN DEFAULT TRUE
);

CREATE TABLE Student (
    studentID VARCHAR(20) PRIMARY KEY,
    userID INTEGER NOT NULL,
    fullName VARCHAR(100) NOT NULL,
    cgpa DECIMAL(3,2),
    CONSTRAINT FK_Student_User FOREIGN KEY (userID) REFERENCES "User"(userID) ON DELETE CASCADE
);

CREATE TABLE Reviewer (
    staffID VARCHAR(20) PRIMARY KEY,
    userID INTEGER NOT NULL,
    department VARCHAR(100),
    CONSTRAINT FK_Reviewer_User FOREIGN KEY (userID) REFERENCES "User"(userID) ON DELETE CASCADE
);

CREATE TABLE CommitteeMember (
    memberID SERIAL PRIMARY KEY,
    userID INTEGER NOT NULL,
    position VARCHAR(50),
    CONSTRAINT FK_CommitteeMember_User FOREIGN KEY (userID) REFERENCES "User"(userID) ON DELETE CASCADE
);

CREATE TABLE Admin (
    adminID SERIAL PRIMARY KEY,
    userID INTEGER NOT NULL,
    adminLevel VARCHAR(20),
    CONSTRAINT FK_Admin_User FOREIGN KEY (userID) REFERENCES "User"(userID) ON DELETE CASCADE
);

CREATE TABLE Scholarship (
    scholarshipID SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    deadline DATE,
    isActive BOOLEAN DEFAULT TRUE
);

CREATE TABLE Criteria (
    criteriaID SERIAL PRIMARY KEY,
    scholarshipID INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    weightage INTEGER,
    maxScore DECIMAL(5,2),
    CONSTRAINT FK_Criteria_Scholarship FOREIGN KEY (scholarshipID) REFERENCES Scholarship(scholarshipID) ON DELETE CASCADE
);

CREATE TABLE Inquiry (
    inquiryID SERIAL PRIMARY KEY,
    studentID VARCHAR(20) NOT NULL,
    message TEXT,
    submittedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Inquiry_Student FOREIGN KEY (studentID) REFERENCES Student(studentID) ON DELETE CASCADE
);

CREATE TABLE Application (
    appID SERIAL PRIMARY KEY,
    studentID VARCHAR(20) NOT NULL,
    scholarshipID INTEGER NOT NULL,
    submissionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'Pending',
    CONSTRAINT FK_Application_Student FOREIGN KEY (studentID) REFERENCES Student(studentID) ON DELETE CASCADE,
    CONSTRAINT FK_Application_Scholarship FOREIGN KEY (scholarshipID) REFERENCES Scholarship(scholarshipID) ON DELETE CASCADE
);

CREATE TABLE Document (
    docID SERIAL PRIMARY KEY,
    appID INTEGER NOT NULL,
    fileName VARCHAR(255) NOT NULL,
    fileType VARCHAR(50),
    uploadDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Document_Application FOREIGN KEY (appID) REFERENCES Application(appID) ON DELETE CASCADE
);

CREATE TABLE Evaluation (
    evalID SERIAL PRIMARY KEY,
    appID INTEGER NOT NULL,
    reviewerStaffID VARCHAR(20) NOT NULL,
    scholarshipComments TEXT,
    interviewScore DECIMAL(5,2),
    interviewComments TEXT,
    status VARCHAR(20) DEFAULT 'Pending',
    evaluatedDate TIMESTAMP,
    CONSTRAINT FK_Evaluation_Application FOREIGN KEY (appID) REFERENCES Application(appID) ON DELETE CASCADE,
    CONSTRAINT FK_Evaluation_Reviewer FOREIGN KEY (reviewerStaffID) REFERENCES Reviewer(staffID) ON DELETE CASCADE
);

CREATE TABLE Interview (
    interviewID SERIAL PRIMARY KEY,
    evalID INTEGER NOT NULL,
    dateTime TIMESTAMP,
    venueOrLink VARCHAR(255),
    status VARCHAR(20) DEFAULT 'Scheduled',
    CONSTRAINT FK_Interview_Evaluation FOREIGN KEY (evalID) REFERENCES Evaluation(evalID) ON DELETE CASCADE
);

CREATE TABLE ClarificationRequest (
    reqID SERIAL PRIMARY KEY,
    evalID INTEGER NOT NULL,
    question TEXT NOT NULL,
    answer TEXT,
    status VARCHAR(20) DEFAULT 'Pending',
    requestedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    answeredDate TIMESTAMP,
    CONSTRAINT FK_ClarificationRequest_Evaluation FOREIGN KEY (evalID) REFERENCES Evaluation(evalID) ON DELETE CASCADE
);

CREATE TABLE Report (
    reportID SERIAL PRIMARY KEY,
    adminID INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,
    generatedFile VARCHAR(255),
    generatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Report_Admin FOREIGN KEY (adminID) REFERENCES Admin(adminID) ON DELETE CASCADE
);

CREATE TABLE Notification (
    notifID SERIAL PRIMARY KEY,
    userID INTEGER NOT NULL,
    message TEXT NOT NULL,
    sentAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    isRead BOOLEAN DEFAULT FALSE,
    CONSTRAINT FK_Notification_User FOREIGN KEY (userID) REFERENCES "User"(userID) ON DELETE CASCADE
);

CREATE TABLE UserRoleLookup (
    roleValue VARCHAR(20) PRIMARY KEY
);

CREATE TABLE ApplicationStatusLookup (
    statusValue VARCHAR(20) PRIMARY KEY
);

CREATE TABLE EvaluationStatusLookup (
    statusValue VARCHAR(20) PRIMARY KEY
);

CREATE TABLE InterviewStatusLookup (
    statusValue VARCHAR(20) PRIMARY KEY
);

CREATE TABLE ClarificationStatusLookup (
    statusValue VARCHAR(20) PRIMARY KEY
);