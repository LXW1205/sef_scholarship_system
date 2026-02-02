package dao;

import db.DatabaseConnection;
import model.Document;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {

    public boolean save(Document doc) {
        String sql = "INSERT INTO Document (appID, fileName, fileType) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doc.getAppID());
            pstmt.setString(2, doc.getFileName());
            pstmt.setString(3, doc.getFileType());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Document> findByAppId(int appId) {
        List<Document> docs = new ArrayList<>();
        String sql = "SELECT * FROM Document WHERE appID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    docs.add(new Document(
                            rs.getInt("docID"),
                            rs.getInt("appID"),
                            rs.getString("fileName"),
                            rs.getString("fileType"),
                            rs.getString("fileContent"),
                            rs.getTimestamp("uploadDate")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return docs;
    }
}
