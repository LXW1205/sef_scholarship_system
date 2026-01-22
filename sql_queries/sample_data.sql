INSERT INTO UserRoleLookup (roleValue) VALUES 
('Student'),
('Reviewer'),
('Committee'),
('Admin');

INSERT INTO ApplicationStatusLookup (statusValue) VALUES 
('Pending'),
('Reviewing'),
('Reviewed'),
('Approved'),
('Rejected'),
('Withdrawn');

INSERT INTO EvaluationStatusLookup (statusValue) VALUES 
('Pending'),
('In Progress'),
('Completed');

INSERT INTO InterviewStatusLookup (statusValue) VALUES 
('Scheduled'),
('Completed'),
('Cancelled'),
('Rescheduled');

INSERT INTO ClarificationStatusLookup (statusValue) VALUES 
('Pending'),
('Answered'),
('Resolved');

-- Admin
INSERT INTO Admin (fullName, password, email, role, isActive, adminID, adminLevel) 
VALUES ('Admin One', 'Admin@123', 'admin@mmu.edu.my', 'Admin', TRUE, 'A1001', 'Super Admin');

-- Reviewers
INSERT INTO Reviewer (fullName, password, email, role, isActive, reviewerID, department) 
VALUES ('John Tan', 'Review@123', 'reviewer1@mmu.edu.my', 'Reviewer', TRUE, 'R001', 'Faculty of Engineering');
INSERT INTO Reviewer (fullName, password, email, role, isActive, reviewerID, department) 
VALUES ('Omar bin Saad', 'Review@123', 'reviewer2@mmu.edu.my', 'Reviewer', TRUE, 'R002', 'Faculty of Computing');
INSERT INTO Reviewer (fullName, password, email, role, isActive, reviewerID, department) 
VALUES ('Amirul Sulaiman', 'Review@123', 'reviewer3@mmu.edu.my', 'Reviewer', TRUE, 'R003', 'Faculty of Management');

-- Students
INSERT INTO Student (fullName, password, email, role, isActive, studentID, cgpa, major, qualification, yearOfStudy, expectedGraduation, familyIncome)
VALUES ('Ahmad bin Ismail', 'Student@123', 'ahmad.ismail@student.mmu.edu.my', 'Student', TRUE, 'S2024001', 3.85, 'Software Engineering', 'Degree', '2', '2026-06-01', 3500.00);

INSERT INTO Student (fullName, password, email, role, isActive, studentID, cgpa, major, qualification, yearOfStudy, expectedGraduation, familyIncome)
VALUES ('Siti binti Rahman', 'Student@123', 'siti.rahman@student.mmu.edu.my', 'Student', TRUE, 'S2024002', 3.92, 'Data Science', 'Degree', '3', '2025-11-01', 4200.00);

INSERT INTO Student (fullName, password, email, role, isActive, studentID, cgpa, major, qualification, yearOfStudy, expectedGraduation, familyIncome)
VALUES ('Chong Wei Lun', 'Student@123', 'chong.wei@student.mmu.edu.my', 'Student', TRUE, 'S2024003', 3.67, 'Computer Science', 'Degree', '1', '2027-05-01', 8000.00);

INSERT INTO Student (fullName, password, email, role, isActive, studentID, cgpa, major, qualification, yearOfStudy, expectedGraduation, familyIncome)
VALUES ('Kumar a/l Rajendran', 'Student@123', 'kumar.raj@student.mmu.edu.my', 'Student', TRUE, 'S2024004', 3.45, 'Mechanical Engineering', 'Degree', '2', '2026-12-01', 2500.00);

INSERT INTO Student (fullName, password, email, role, isActive, studentID, cgpa, major, qualification, yearOfStudy, expectedGraduation, familyIncome)
VALUES ('Nurul Aina', 'Student@123', 'nurul.aina@student.mmu.edu.my', 'Student', TRUE, 'S2024005', 3.78, 'Foundation in IT', 'Foundation', '1', '2025-01-01', 12000.00);

-- Committee
INSERT INTO CommitteeMember (fullName, password, email, role, isActive, committeeID, position) 
VALUES ('Zulkifli bin Hussain', 'Committee@123', 'committee1@mmu.edu.my', 'Committee', TRUE, 'C1001', 'Chairperson');
INSERT INTO CommitteeMember (fullName, password, email, role, isActive, committeeID, position) 
VALUES ('Azlan Chu', 'Committee@123', 'committee2@mmu.edu.my', 'Committee', TRUE, 'C1002', 'Secretary');
INSERT INTO CommitteeMember (fullName, password, email, role, isActive, committeeID, position) 
VALUES ('Lim Chong Wei', 'Committee@123', 'committee3@mmu.edu.my', 'Committee', TRUE, 'C1003', 'Member');

-- Insert Scholarships (New Fields: description, amount, forQualification)
INSERT INTO Scholarship (title, description, amount, forQualification, deadline, minCGPA, maxFamilyIncome, isActive)
VALUES ('MMU Chancellor Scholarship', 'Full scholarship for high achievers.', 'RM 50,000', 'Degree', '2025-08-31', 3.75, 0.00, TRUE);

INSERT INTO Scholarship (title, description, amount, forQualification, deadline, minCGPA, maxFamilyIncome, isActive)
VALUES ('MMU President Scholarship', 'Partial scholarship for Foundation students.', 'RM 15,000', 'Foundation', '2025-08-31', 3.50, 5000.00, TRUE);

INSERT INTO Scholarship (title, description, amount, forQualification, deadline, minCGPA, maxFamilyIncome, isActive)
VALUES ('MMU Merit Scholarship', 'Merit-based award for outstanding academic results.', 'RM 20,000', 'All', '2025-08-31', 3.00, 0.00, TRUE);

INSERT INTO Scholarship (title, description, amount, forQualification, deadline, minCGPA, maxFamilyIncome, isActive)
VALUES ('MMU Sports Talent', 'For state/national players.', 'RM 10,000', 'All', '2025-08-31', 2.50, 0.00, TRUE);

INSERT INTO Scholarship (title, description, amount, forQualification, deadline, minCGPA, maxFamilyIncome, isActive)
VALUES ('Star Education Fund', 'External scholarship for Engineering.', 'RM 40,000', 'Degree', '2025-07-31', 3.80, 0.00, TRUE);

-- Scholarship 1: MMU Chancellor Scholarship
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (1, 'Academic Excellence', 50, 100);
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (1, 'Leadership Roles', 20, 100);
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (1, 'Community Service', 10, 100);
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (1, 'Personal Statement', 20, 100);

-- Scholarship 2: MMU President Scholarship
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (2, 'SPM/Foundation Results', 70, 100);
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (2, 'Extracurricular Involvement', 30, 100);

-- Scholarship 3: MMU Merit Scholarship
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (3, 'CGPA Performance', 80, 100);
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (3, 'Technical Portfolio', 20, 100);

-- Scholarship 4: MMU Sports Talent
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (4, 'State/National Achievement', 60, 100);
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (4, 'Physical Fitness Test', 20, 100);
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (4, 'Sportsmanship Track Record', 20, 100);

-- Scholarship 5: Star Education Fund
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (5, 'Financial Need Analysis', 50, 100);
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (5, 'Future Aspirations Statement', 30, 100);
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore) VALUES (5, 'English Proficiency (MUET/IELTS)', 20, 100);

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