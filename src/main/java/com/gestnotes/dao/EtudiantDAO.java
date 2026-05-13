package com.gestnotes.dao;

import com.gestnotes.model.Etudiant;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EtudiantDAO {
    private final Connection conn;

    public EtudiantDAO() {
        this.conn = DatabaseConnection.getInstance();
    }

    public List<Etudiant> findAll() {
        List<Etudiant> list = new ArrayList<>();
        String sql = "SELECT * FROM etudiant ORDER BY nom, prenom";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void add(Etudiant e) {
        String sql = "INSERT INTO etudiant(nom,prenom,cin,email,telephone,date_naissance,niveau,filiere,groupe) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getPrenom());
            ps.setString(3, e.getCin());
            ps.setString(4, e.getEmail());
            ps.setString(5, e.getTelephone());
            ps.setString(6, e.getDateNaissance());
            ps.setString(7, e.getNiveau());
            ps.setString(8, e.getFiliere());
            ps.setString(9, e.getGroupe());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void update(Etudiant e) {
        String sql = "UPDATE etudiant SET nom=?,prenom=?,cin=?,email=?,telephone=?,date_naissance=?,niveau=?,filiere=?,groupe=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getPrenom());
            ps.setString(3, e.getCin());
            ps.setString(4, e.getEmail());
            ps.setString(5, e.getTelephone());
            ps.setString(6, e.getDateNaissance());
            ps.setString(7, e.getNiveau());
            ps.setString(8, e.getFiliere());
            ps.setString(9, e.getGroupe());
            ps.setInt(10, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM etudiant WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Etudiant> search(String keyword) {
        List<Etudiant> list = new ArrayList<>();
        String sql = "SELECT * FROM etudiant WHERE nom LIKE ? OR prenom LIKE ? OR cin LIKE ? OR filiere LIKE ? ORDER BY nom, prenom";
        String k = "%" + keyword + "%";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k); ps.setString(4, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public boolean cinExists(String cin) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM etudiant WHERE cin=?")) {
            ps.setString(1, cin);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean cinExistsExcept(String cin, int id) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM etudiant WHERE cin=? AND id!=?")) {
            ps.setString(1, cin); ps.setInt(2, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Etudiant map(ResultSet rs) throws SQLException {
        return new Etudiant(
            rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"),
            rs.getString("cin"), rs.getString("email"), rs.getString("telephone"),
            rs.getString("date_naissance"), rs.getString("niveau"),
            rs.getString("filiere"), rs.getString("groupe")
        );
    }
}
