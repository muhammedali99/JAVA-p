package com.gestnotes.dao;

import com.gestnotes.model.Note;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {
    private final Connection conn;

    public NoteDAO() {
        this.conn = DatabaseConnection.getInstance();
    }

    public List<Note> findByEtudiant(int etudiantId) {
        List<Note> list = new ArrayList<>();
        String sql = """
            SELECT n.*, m.code AS module_code, m.nom AS module_nom
            FROM note n
            JOIN module m ON n.module_id = m.id
            WHERE n.etudiant_id = ?
            ORDER BY m.code
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Note note = new Note(
                        rs.getInt("id"), rs.getInt("etudiant_id"), rs.getInt("module_id"),
                        rs.getDouble("note_cc"), rs.getDouble("note_examen"), rs.getDouble("moyenne_module")
                    );
                    note.setModuleCode(rs.getString("module_code"));
                    note.setModuleNom(rs.getString("module_nom"));
                    list.add(note);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void saveOrUpdate(Note n) {
        String sql = """
            INSERT INTO note(etudiant_id, module_id, note_cc, note_examen, moyenne_module)
            VALUES(?,?,?,?,?)
            ON CONFLICT(etudiant_id, module_id) DO UPDATE SET
              note_cc=excluded.note_cc,
              note_examen=excluded.note_examen,
              moyenne_module=excluded.moyenne_module
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, n.getEtudiantId());
            ps.setInt(2, n.getModuleId());
            ps.setDouble(3, n.getNoteCC());
            ps.setDouble(4, n.getNoteExamen());
            ps.setDouble(5, n.getMoyenneModule());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasNotes(int etudiantId) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM note WHERE etudiant_id=?")) {
            ps.setInt(1, etudiantId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public double getMoyenneGenerale(int etudiantId) {
        String sql = """
            SELECT SUM(n.moyenne_module * m.coefficient) / SUM(m.coefficient)
            FROM note n
            JOIN module m ON n.module_id = m.id
            WHERE n.etudiant_id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0.0;
    }
}
