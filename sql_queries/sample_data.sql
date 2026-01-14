-- Insert Users (Base Table)
-- Admin
INSERT INTO "User" (fullName, password, email, role, isActive) VALUES ('Admin One', 'Admin@123', 'admin@mmu.edu.my', 'Admin', TRUE); -- userID 1

-- Reviewers
INSERT INTO "User" (fullName, password, email, role, isActive) VALUES ('Dr. Reviewer One', 'Review@123', 'reviewer1@mmu.edu.my', 'Reviewer', TRUE); -- 2
INSERT INTO "User" (fullName, password, email, role, isActive) VALUES ('Prof. Reviewer Two', 'Review@123', 'reviewer2@mmu.edu.my', 'Reviewer', TRUE); -- 3
INSERT INTO "User" (fullName, password, email, role, isActive) VALUES ('Dr. Reviewer Three', 'Review@123', 'reviewer3@mmu.edu.my', 'Reviewer', TRUE); -- 4

-- Students
INSERT INTO "User" (fullName, password, email, role, isActive) VALUES ('Ahmad bin Ismail', 'Student@123', 'ahmad.ismail@student.mmu.edu.my', 'Student', TRUE); -- 5
INSERT INTO "User" (fullName, password, email, role, isActive) VALUES ('Siti binti Rahman', 'Student@123', 'siti.rahman@student.mmu.edu.my', 'Student', TRUE); -- 6
INSERT INTO "User" (fullName, password, email, role, isActive) VALUES ('Chong Wei Lun', 'Student@123', 'chong.wei@student.mmu.edu.my', 'Student', TRUE); -- 7
INSERT INTO "User" (fullName, password, email, role, isActive) VALUES ('Kumar a/l Rajendran', 'Student@123', 'kumar.raj@student.mmu.edu.my', 'Student', TRUE); -- 8
INSERT INTO "User" (fullName, password, email, role, isActive) VALUES ('Nurul Aina', 'Student@123', 'nurul.aina@student.mmu.edu.my', 'Student', TRUE); -- 9

-- Committee
INSERT INTO "User" (fullName, password, email, role, isActive) VALUES ('Committee Chair', 'Committee@123', 'committee1@mmu.edu.my', 'Committee', TRUE); -- 10
INSERT INTO "User" (fullName, password, email, role, isActive) VALUES ('Committee Sec', 'Committee@123', 'committee2@mmu.edu.my', 'Committee', TRUE); -- 11
INSERT INTO "User" (fullName, password, email, role, isActive) VALUES ('Committee Member', 'Committee@123', 'committee3@mmu.edu.my', 'Committee', TRUE); -- 12

-- Insert Role Specific Data (New String IDs and Extra Fields)
INSERT INTO Admin (adminID, userID, adminLevel) VALUES ('A1001', 1, 'Super Admin');

INSERT INTO Reviewer (reviewerID, userID, department) VALUES ('R001', 2, 'Faculty of Engineering');
INSERT INTO Reviewer (reviewerID, userID, department) VALUES ('R002', 3, 'Faculty of Computing');
INSERT INTO Reviewer (reviewerID, userID, department) VALUES ('R003', 4, 'Faculty of Management');

INSERT INTO Student (studentID, userID, cgpa, major, qualification, yearOfStudy, expectedGraduation, familyIncome)
VALUES ('S2024001', 5, 3.85, 'Software Engineering', 'Bachelor', 'Year 2', '2026', 3500.00);

INSERT INTO Student (studentID, userID, cgpa, major, qualification, yearOfStudy, expectedGraduation, familyIncome)
VALUES ('S2024002', 6, 3.92, 'Data Science', 'Bachelor', 'Year 3', '2025', 4200.00);

INSERT INTO Student (studentID, userID, cgpa, major, qualification, yearOfStudy, expectedGraduation, familyIncome)
VALUES ('S2024003', 7, 3.67, 'Computer Science', 'Bachelor', 'Year 1', '2027', 8000.00);

INSERT INTO Student (studentID, userID, cgpa, major, qualification, yearOfStudy, expectedGraduation, familyIncome)
VALUES ('S2024004', 8, 3.45, 'Mechanical Engineering', 'Bachelor', 'Year 2', '2026', 2500.00);

INSERT INTO Student (studentID, userID, cgpa, major, qualification, yearOfStudy, expectedGraduation, familyIncome)
VALUES ('S2024005', 9, 3.78, 'Foundation in IT', 'Foundation', 'Year 1', '2025', 12000.00);

INSERT INTO CommitteeMember (committeeID, userID, position) VALUES ('C1001', 10, 'Chairperson');
INSERT INTO CommitteeMember (committeeID, userID, position) VALUES ('C1002', 11, 'Secretary');
INSERT INTO CommitteeMember (committeeID, userID, position) VALUES ('C1003', 12, 'Member');

-- Insert Scholarships (New Fields: description, amount, forQualification)
INSERT INTO Scholarship (title, description, amount, forQualification, deadline, isActive)
VALUES ('MMU Chancellor Scholarship', 'Full scholarship for high achievers.', 'RM 50,000', 'Bachelor', '2025-08-31', TRUE);

INSERT INTO Scholarship (title, description, amount, forQualification, deadline, isActive)
VALUES ('MMU President Scholarship', 'Partial scholarship for Foundation students.', 'RM 15,000', 'Foundation', '2025-08-31', TRUE);

INSERT INTO Scholarship (title, description, amount, forQualification, deadline, isActive)
VALUES ('MMU Merit Scholarship', 'Merit-based award for outstanding academic results.', 'RM 20,000', 'All', '2025-08-31', TRUE);

INSERT INTO Scholarship (title, description, amount, forQualification, deadline, isActive)
VALUES ('MMU Sports Talent', 'For state/national players.', 'RM 10,000', 'All', '2025-08-31', TRUE);

INSERT INTO Scholarship (title, description, amount, forQualification, deadline, isActive)
VALUES ('Star Education Fund', 'External scholarship for Engineering.', 'RM 40,000', 'Bachelor', '2025-07-31', TRUE);

-- Insert Criteria (Linked to IDs 1-5 which are Serial, so we assume order)
-- Scholarship 1
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (1, 'Academic Excellence', 60, 100);
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (1, 'Interview', 40, 100);

-- Scholarship 2
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (2, 'SPM Results', 100, 100);

-- Insert Applications (Minimal sample)
-- Student 1 (S2024001) applying for Scholarship 1
INSERT INTO Application (studentID, scholarshipID, submissionDate, status, personalStatement, otherScholarships)
VALUES ('S2024001', 1, CURRENT_TIMESTAMP, 'Pending', 'I am a very good student...', 'None');

-- Student 2 (S2024002) applying for Scholarship 1
INSERT INTO Application (studentID, scholarshipID, submissionDate, status, personalStatement, otherScholarships)
VALUES ('S2024002', 1, CURRENT_TIMESTAMP, 'Pending', 'Passionate about Data Science.', 'JPA Scholarship');

-- Student 5 (S2024005) joining Foundation, applying for Scholarship 2
INSERT INTO Application (studentID, scholarshipID, submissionDate, status, personalStatement, otherScholarships)
VALUES ('S2024005', 2, CURRENT_TIMESTAMP, 'Approved', 'Top SPM scorer.', 'None');