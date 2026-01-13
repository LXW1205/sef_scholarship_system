INSERT INTO "User" (username, password, email, role, isActive)
VALUES ('admin001', 'Admin@123', 'admin@mmu.edu.my', 'Admin', TRUE);

INSERT INTO Admin (userID, adminLevel)
VALUES (1, 'Super Admin');

INSERT INTO "User" (username, password, email, role, isActive)
VALUES ('reviewer001', 'Review@123', 'reviewer1@mmu.edu.my', 'Reviewer', TRUE);

INSERT INTO Reviewer (staffID, userID, department)
VALUES ('R001', 2, 'Faculty of Engineering');

INSERT INTO "User" (username, password, email, role, isActive)
VALUES ('reviewer002', 'Review@123', 'reviewer2@mmu.edu.my', 'Reviewer', TRUE);

INSERT INTO Reviewer (staffID, userID, department)
VALUES ('R002', 3, 'Faculty of Computing and Informatics');

INSERT INTO "User" (username, password, email, role, isActive)
VALUES ('reviewer003', 'Review@123', 'reviewer3@mmu.edu.my', 'Reviewer', TRUE);

INSERT INTO Reviewer (staffID, userID, department)
VALUES ('R003', 4, 'Faculty of Management');

INSERT INTO "User" (username, password, email, role, isActive)
VALUES ('student001', 'Student@123', 'ahmad.ismail@student.mmu.edu.my', 'Student', TRUE);

INSERT INTO Student (studentID, userID, fullName, cgpa)
VALUES ('S2024001', 5, 'Ahmad bin Ismail', 3.85);

INSERT INTO "User" (username, password, email, role, isActive)
VALUES ('student002', 'Student@123', 'siti.rahman@student.mmu.edu.my', 'Student', TRUE);

INSERT INTO Student (studentID, userID, fullName, cgpa)
VALUES ('S2024002', 6, 'Siti binti Rahman', 3.92);

INSERT INTO "User" (username, password, email, role, isActive)
VALUES ('student003', 'Student@123', 'chong.wei@student.mmu.edu.my', 'Student', TRUE);

INSERT INTO Student (studentID, userID, fullName, cgpa)
VALUES ('S2024003', 7, 'Chong Wei Lun', 3.67);

INSERT INTO "User" (username, password, email, role, isActive)
VALUES ('student004', 'Student@123', 'kumar.raj@student.mmu.edu.my', 'Student', TRUE);

INSERT INTO Student (studentID, userID, fullName, cgpa)
VALUES ('S2024004', 8, 'Kumar a/l Rajendran', 3.45);

INSERT INTO "User" (username, password, email, role, isActive)
VALUES ('student005', 'Student@123', 'nurul.aina@student.mmu.edu.my', 'Student', TRUE);

INSERT INTO Student (studentID, userID, fullName, cgpa)
VALUES ('S2024005', 9, 'Nurul Aina binti Hassan', 3.78);

INSERT INTO "User" (username, password, email, role, isActive)
VALUES ('committee001', 'Committee@123', 'committee1@mmu.edu.my', 'Committee', TRUE);

INSERT INTO CommitteeMember (userID, position)
VALUES (10, 'Chairperson');

INSERT INTO "User" (username, password, email, role, isActive)
VALUES ('committee002', 'Committee@123', 'committee2@mmu.edu.my', 'Committee', TRUE);

INSERT INTO CommitteeMember (userID, position)
VALUES (11, 'Secretary');

INSERT INTO "User" (username, password, email, role, isActive)
VALUES ('committee003', 'Committee@123', 'committee3@mmu.edu.my', 'Committee', TRUE);

INSERT INTO CommitteeMember (userID, position)
VALUES (12, 'Member');

INSERT INTO Scholarship (title, deadline, isActive)
VALUES ('MMU Chancellor Scholarship - Bachelor Degree', '2025-08-31', TRUE);

INSERT INTO Scholarship (title, deadline, isActive)
VALUES ('MMU Chancellor Scholarship - Foundation', '2025-08-31', TRUE);

INSERT INTO Scholarship (title, deadline, isActive)
VALUES ('MMU President Scholarship - Bachelor Degree', '2025-08-31', TRUE);

INSERT INTO Scholarship (title, deadline, isActive)
VALUES ('MMU President Scholarship - Foundation', '2025-08-31', TRUE);

INSERT INTO Scholarship (title, deadline, isActive)
VALUES ('MMU Merit Scholarship', '2025-08-31', TRUE);

INSERT INTO Scholarship (title, deadline, isActive)
VALUES ('MMU Talent Scholarship - Sports (National Level)', '2025-08-31', TRUE);

INSERT INTO Scholarship (title, deadline, isActive)
VALUES ('MMU Talent Scholarship - Sports (State Level)', '2025-08-31', TRUE);

INSERT INTO Scholarship (title, deadline, isActive)
VALUES ('Sin Chew Daily Education Fund - Engineering (Bachelor)', '2025-07-31', TRUE);

INSERT INTO Scholarship (title, deadline, isActive)
VALUES ('Star Education Fund - Engineering (Bachelor)', '2025-07-31', TRUE);

INSERT INTO Scholarship (title, deadline, isActive)
VALUES ('Ibnu Haitham Scholarship', '2025-08-31', TRUE);

INSERT INTO Scholarship (title, deadline, isActive)
VALUES ('JAWHAR Scholarship', '2025-08-31', TRUE);

INSERT INTO Criteria (scholarshipID, name, weightage, maxScore)
VALUES (1, 'Academic Excellence', 40, 100);

INSERT INTO Criteria (scholarshipID, name, weightage, maxScore)
VALUES (1, 'Leadership and Co-curricular Activities', 30, 100);

INSERT INTO Criteria (scholarshipID, name, weightage, maxScore)
VALUES (1, 'Financial Need (B40 Category)', 20, 100);

INSERT INTO Criteria (scholarshipID, name, weightage, maxScore)
VALUES (1, 'Interview Performance', 10, 100);

INSERT INTO Criteria (scholarshipID, name, weightage, maxScore)
VALUES (5, 'Academic Performance (CGPA 3.5-3.74)', 50, 100);

INSERT INTO Criteria (scholarshipID, name, weightage, maxScore)
VALUES (5, 'Consistency in Academic Results', 30, 100);

INSERT INTO Criteria (scholarshipID, name, weightage, maxScore)
VALUES (5, 'Financial Need', 20, 100);

-- Insert Criteria for MMU Talent Scholarship
INSERT INTO Criteria (scholarshipID, name, weightage, maxScore)
VALUES (6, 'National Level Achievement', 50, 100);

INSERT INTO Criteria (scholarshipID, name, weightage, maxScore)
VALUES (6, 'Verification from National Sports Association', 30, 100);

INSERT INTO Criteria (scholarshipID, name, weightage, maxScore)
VALUES (6, 'Commitment to Represent MMU', 20, 100);