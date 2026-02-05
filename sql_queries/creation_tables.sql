-- Drop tables in reverse dependency order to avoid FK violations
DROP TABLE IF EXISTS InquiryStatusLookup CASCADE;

DROP TABLE IF EXISTS ClarificationStatusLookup CASCADE;
DROP TABLE IF EXISTS InterviewStatusLookup CASCADE;
DROP TABLE IF EXISTS EvaluationStatusLookup CASCADE;
DROP TABLE IF EXISTS ApplicationStatusLookup CASCADE;

DROP TABLE IF EXISTS UserRoleLookup CASCADE;
DROP TABLE IF EXISTS Notification CASCADE;
DROP TABLE IF EXISTS Report CASCADE;
DROP TABLE IF EXISTS ClarificationRequest CASCADE;
DROP TABLE IF EXISTS Interview CASCADE;
DROP TABLE IF EXISTS Evaluation CASCADE;
DROP TABLE IF EXISTS Document CASCADE;
DROP TABLE IF EXISTS Application CASCADE;
DROP TABLE IF EXISTS Inquiry CASCADE;
DROP TABLE IF EXISTS Criteria CASCADE;
DROP TABLE IF EXISTS Scholarship CASCADE;
DROP TABLE IF EXISTS Admin CASCADE;
DROP TABLE IF EXISTS CommitteeMember CASCADE;
DROP TABLE IF EXISTS Reviewer CASCADE;
DROP TABLE IF EXISTS Student CASCADE;
DROP TABLE IF EXISTS "User" CASCADE;
DROP TABLE IF EXISTS EvaluationScore CASCADE;
DROP TABLE IF EXISTS AuditLog CASCADE;

-- Create Tables

-- Lookups
CREATE TABLE UserRoleLookup ( roleValue VARCHAR(20) PRIMARY KEY );
CREATE TABLE ApplicationStatusLookup ( statusValue VARCHAR(20) PRIMARY KEY );
CREATE TABLE EvaluationStatusLookup ( statusValue VARCHAR(20) PRIMARY KEY );
CREATE TABLE InterviewStatusLookup ( statusValue VARCHAR(20) PRIMARY KEY );
CREATE TABLE ClarificationStatusLookup ( statusValue VARCHAR(20) PRIMARY KEY );
CREATE TABLE InquiryStatusLookup ( statusValue VARCHAR(20) PRIMARY KEY );


-- Main Tables

CREATE TABLE "User" (
    userID SERIAL PRIMARY KEY,
    fullName VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    isActive BOOLEAN DEFAULT TRUE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Student (
    studentID VARCHAR(20) PRIMARY KEY,
    cgpa DECIMAL(3, 2),
    major VARCHAR(100),
    yearOfStudy VARCHAR(10),
    qualification VARCHAR(50),
    expectedGraduation DATE,
    familyIncome DECIMAL(10, 2)
) INHERITS ("User");



CREATE TABLE Reviewer (
    reviewerID VARCHAR(20) PRIMARY KEY,
    department VARCHAR(100),
    UNIQUE (email)
) INHERITS ("User");

CREATE TABLE CommitteeMember (
    committeeID VARCHAR(20) PRIMARY KEY,
    position VARCHAR(50),
    UNIQUE (email)
) INHERITS ("User");

CREATE TABLE Admin (
    adminID VARCHAR(20) PRIMARY KEY,
    adminLevel VARCHAR(20),
    UNIQUE (email)
) INHERITS ("User");

CREATE TABLE Scholarship (
    scholarshipID SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(200), -- Increased length slightly for better descriptions
    amount VARCHAR(50), -- Changed to 50 to accommodate "RM 10,000" etc comfortably
    forQualification VARCHAR(20),
    deadline DATE,
    minCGPA DECIMAL(3,2) DEFAULT 0.0,
    maxFamilyIncome DECIMAL(12,2) DEFAULT 0.0,
    requiresInterview BOOLEAN DEFAULT FALSE,
    isActive BOOLEAN DEFAULT TRUE
);

CREATE TABLE Application (
    appID SERIAL PRIMARY KEY,
    studentID VARCHAR(20) NOT NULL,
    scholarshipID INTEGER NOT NULL,
    submissionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'Pending',
    personalStatement TEXT,
    otherScholarships TEXT,
    decisionComments TEXT,
    CONSTRAINT FK_Application_Student FOREIGN KEY (studentID) REFERENCES Student(studentID) ON DELETE CASCADE,
    CONSTRAINT FK_Application_Scholarship FOREIGN KEY (scholarshipID) REFERENCES Scholarship(scholarshipID) ON DELETE CASCADE,
    CONSTRAINT FK_Application_Status FOREIGN KEY (status) REFERENCES ApplicationStatusLookup(statusValue) ON UPDATE CASCADE
);


-- Dependent Tables (Updated keys where necessary)

CREATE TABLE Criteria (
    criteriaID SERIAL PRIMARY KEY,
    scholarshipID INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    weightage INTEGER,
    maxScore DECIMAL(10,2),
    mappedField VARCHAR(50) DEFAULT 'none',
    CONSTRAINT FK_Criteria_Scholarship FOREIGN KEY (scholarshipID) REFERENCES Scholarship(scholarshipID) ON DELETE CASCADE
);

CREATE TABLE Inquiry (
    inquiryID SERIAL PRIMARY KEY,
    studentID VARCHAR(20) NOT NULL, 
    message TEXT,
    answer TEXT,
    status VARCHAR(20) DEFAULT 'Pending',
    submittedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    answeredAt TIMESTAMP,
    CONSTRAINT FK_Inquiry_Student FOREIGN KEY (studentID) REFERENCES Student(studentID) ON DELETE CASCADE,
    CONSTRAINT FK_Inquiry_Status FOREIGN KEY (status) REFERENCES InquiryStatusLookup(statusValue) ON UPDATE CASCADE
);



CREATE TABLE Document (
    docID SERIAL PRIMARY KEY,
    appID INTEGER NOT NULL,
    fileName VARCHAR(255) NOT NULL,
    fileType VARCHAR(50),
    fileContent TEXT, -- Added for base64 content
    uploadDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Document_Application FOREIGN KEY (appID) REFERENCES Application(appID) ON DELETE CASCADE
);

CREATE TABLE Evaluation (
    evalID SERIAL PRIMARY KEY,
    appID INTEGER NOT NULL,
    reviewerID VARCHAR(20) NOT NULL, -- Renamed from reviewerStaffID and matched type
    scholarshipComments TEXT,
    interviewScore DECIMAL(10,2),
    interviewComments TEXT,
    status VARCHAR(20) DEFAULT 'Pending',
    evaluatedDate TIMESTAMP,
    CONSTRAINT FK_Evaluation_Application FOREIGN KEY (appID) REFERENCES Application(appID) ON DELETE CASCADE,
    CONSTRAINT FK_Evaluation_Reviewer FOREIGN KEY (reviewerID) REFERENCES Reviewer(reviewerID) ON DELETE CASCADE
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

CREATE TABLE EvaluationScore (
    scoreID SERIAL PRIMARY KEY,
    evalID INTEGER NOT NULL,
    criteriaID INTEGER NOT NULL,
    score DECIMAL(10,2),
    CONSTRAINT FK_EvaluationScore_Evaluation FOREIGN KEY (evalID) REFERENCES Evaluation(evalID) ON DELETE CASCADE,
    CONSTRAINT FK_EvaluationScore_Criteria FOREIGN KEY (criteriaID) REFERENCES Criteria(criteriaID) ON DELETE CASCADE
);

CREATE TABLE Report (
    reportID SERIAL PRIMARY KEY,
    adminID VARCHAR(20) NOT NULL, -- Matched Admin.adminID type
    type VARCHAR(50) NOT NULL,
    generatedFile VARCHAR(255),
    generatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Report_Admin FOREIGN KEY (adminID) REFERENCES Admin(adminID) ON DELETE CASCADE
);

CREATE TABLE Notification (
    notifID SERIAL PRIMARY KEY,
    userID INTEGER NOT NULL, -- Note: FK constraint removed due to PostgreSQL inheritance limitations
    message TEXT NOT NULL,
    sentAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    isRead BOOLEAN DEFAULT FALSE
);

-- Audit Log Table for tracking all system changes
CREATE TABLE AuditLog (
    logID SERIAL PRIMARY KEY,
    userID INTEGER,
    userEmail VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    entityType VARCHAR(50),
    entityID VARCHAR(50),
    details TEXT,
    ipAddress VARCHAR(45),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

