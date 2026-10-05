package com.visa.app.dao;

import com.visa.app.model.Child;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for `children` table.
 */
public class ChildDAO {

    private static final Logger LOGGER = Logger.getLogger(ChildDAO.class.getName());

    public boolean insertChild(Child child, Connection conn) throws SQLException {
        String sql = "INSERT INTO children (applicant_id, child_name, child_age) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, child.getApplicantId());
            ps.setString(2, child.getName());
            ps.setInt(3, child.getAge());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    child.setChildId(keys.getInt(1));
                }
            }
            return true;
        }
    }

    public List<Child> getChildrenByApplicantId(int applicantId) {
        String sql = "SELECT child_id, applicant_id, child_name, child_age FROM children WHERE applicant_id = ?";
        List<Child> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, applicantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new Child(
                                rs.getInt("child_id"),
                                rs.getInt("applicant_id"),
                                rs.getString("child_name"),
                                rs.getInt("child_age")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching children for applicant_id " + applicantId, e);
        }
        return list;
    }

    public void deleteChildrenByApplicantId(int applicantId, Connection conn) throws SQLException {
        String sql = "DELETE FROM children WHERE applicant_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicantId);
            ps.executeUpdate();
        }
    }
}
