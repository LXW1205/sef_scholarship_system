INSERT INTO UserRoleLookup (roleValue) VALUES 
('Student'),
('Reviewer'),
('Committee'),
('Admin');

INSERT INTO ApplicationStatusLookup (statusValue) VALUES 
('Pending'),
('Under Review'),
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