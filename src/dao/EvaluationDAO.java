package dao;

import db.DatabaseConnection;
import model.Evaluation;
import java.sql.*;

public class EvaluationDAO {

    public EvaluationDAO() {
        createScoreTableIfNotExists();
    }

    private void createScoreTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS EvaluationScore (" +
                "scoreID SERIAL PRIMARY KEY, " +
                "evalID INTEGER NOT NULL, " +
                "criteriaID INTEGER NOT NULL, " +
                "score DECIMAL(10,2), " +
                "CONSTRAINT FK_EvaluationScore_Evaluation FOREIGN KEY (evalID) REFERENCES Evaluation(evalID) ON DELETE CASCADE, "
                +
                "CONSTRAINT FK_EvaluationScore_Criteria FOREIGN KEY (criteriaID) REFERENCES Criteria(criteriaID) ON DELETE CASCADE)";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            // Also try to alter existing tables for higher precision
            try {
                stmt.execute("ALTER TABLE EvaluationScore ALTER COLUMN score TYPE DECIMAL(10,2)");
                stmt.execute("ALTER TABLE Evaluation ALTER COLUMN interviewScore TYPE DECIMAL(10,2)");
                stmt.execute("ALTER TABLE Criteria ALTER COLUMN maxScore TYPE DECIMAL(10,2)");
            } catch (SQLException e) {
                // Ignore if migration fails (e.g. columns don't exist yet or already altered)
            }
        } catch (SQLException e) {
            System.err.println("[WARN] Failed to check/create EvaluationScore table: " + e.getMessage());
        }
    }

    public boolean save(Evaluation eval) {
        String sql = "INSERT INTO Evaluation (appID, reviewerID, scholarshipComments, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eval.getAppID());
            pstmt.setString(2, eval.getReviewerID());
            pstmt.setString(3, eval.getScholarshipComments());
            pstmt.setString(4, eval.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Evaluation eval) {
        String sql = "UPDATE Evaluation SET scholarshipComments = ?, interviewScore = ?, interviewComments = ?, status = ?, evaluatedDate = CURRENT_TIMESTAMP WHERE appID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, eval.getScholarshipComments());
            pstmt.setFloat(2, eval.getInterviewScore());
            pstmt.setString(3, eval.getInterviewComments());
            pstmt.setString(4, eval.getStatus());
            pstmt.setInt(5, eval.getAppID());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void saveScores(int evalId, java.util.List<model.EvaluationScore> scores) {
        String delSql = "DELETE FROM EvaluationScore WHERE evalID = ?";
        String insSql = "INSERT INTO EvaluationScore (evalID, criteriaID, score) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Delete old scores for this eval first (replace strategy)
            try (PreparedStatement delStmt = conn.prepareStatement(delSql)) {
                delStmt.setInt(1, evalId);
                delStmt.executeUpdate();
            }

            // Insert new scores
            try (PreparedStatement insStmt = conn.prepareStatement(insSql)) {
                for (model.EvaluationScore s : scores) {
                    insStmt.setInt(1, evalId);
                    insStmt.setInt(2, s.getCriteriaID());
                    insStmt.setDouble(3, s.getScore());
                    insStmt.addBatch();
                }
                insStmt.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public java.util.List<model.EvaluationScore> findScoresByEvalId(int evalId) {
        java.util.List<model.EvaluationScore> list = new java.util.ArrayList<>();
        String sql = "SELECT es.*, c.name as criteriaName FROM EvaluationScore es " +
                "JOIN Criteria c ON es.criteriaID = c.criteriaID " +
                "WHERE es.evalID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, evalId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    model.EvaluationScore s = new model.EvaluationScore(
                            rs.getInt("scoreID"),
                            rs.getInt("evalID"),
                            rs.getInt("criteriaID"),
                            rs.getDouble("score"));
                    s.setCriteriaName(rs.getString("criteriaName"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Evaluation findByAppId(int appId) {
        String sql = "SELECT * FROM Evaluation WHERE appID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Evaluation(
                            rs.getInt("evalID"),
                            rs.getInt("appID"),
                            rs.getString("reviewerID"),
                            rs.getString("scholarshipComments"),
                            rs.getFloat("interviewScore"),
                            rs.getString("interviewComments"),
                            rs.getString("status"),
                            rs.getTimestamp("evaluatedDate"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
