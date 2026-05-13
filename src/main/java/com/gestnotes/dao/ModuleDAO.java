package com.gestnotes.dao;

import com.gestnotes.model.Module;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleDAO {
    private final Connection conn;

    public ModuleDAO() {
        this.conn = DatabaseConnection.getInstance();
    }

    public List<Module> findAll() {
        List<Module> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM module ORDER BY code")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void add(Module m) {
        String sql = "INSERT INTO module(code,nom,coefficient,enseignant) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getCode());
            ps.setString(2, m.getNom());
            ps.setDouble(3, m.getCoefficient());
            ps.setString(4, m.getEnseignant());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(Module m) {
        String sql = "UPDATE module SET code=?,nom=?,coefficient=?,enseignant=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getCode());
            ps.setString(2, m.getNom());
            ps.setDouble(3, m.getCoefficient());
            ps.setString(4, m.getEnseignant());
            ps.setInt(5, m.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM module WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Module map(ResultSet rs) throws SQLException {
        return new Module(
            rs.getInt("id"), rs.getString("code"), rs.getString("nom"),
            rs.getDouble("coefficient"), rs.getString("enseignant")
        );
    }
}
