package com.gestnotes.dao;

import com.gestnotes.model.Utilisateur;
import java.sql.*;

public class UtilisateurDAO {
    private final Connection conn;

    public UtilisateurDAO() {
        this.conn = DatabaseConnection.getInstance();
    }

    public Utilisateur findByUsernameAndPassword(String username, String password) {
        String sql = "SELECT * FROM utilisateur WHERE username=? AND password=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Utilisateur(
                        rs.getInt("id"), rs.getString("username"),
                        rs.getString("password"), rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
