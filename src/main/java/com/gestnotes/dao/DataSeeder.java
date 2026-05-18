package com.gestnotes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Seeds the database with realistic Tunisian demo data.
 * Called once from DatabaseConnection.initDB() when etudiant table is empty.
 */
public class DataSeeder {

    private DataSeeder() {}

    public static void seed(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM etudiant");
            if (rs.next() && rs.getInt(1) > 0) return; // already seeded
        }

        seedModules(conn);
        int[] etudiantIds = seedEtudiants(conn);
        seedNotes(conn, etudiantIds);
    }

    // ------------------------------------------------------------------ modules
    private static void seedModules(Connection conn) throws SQLException {
        String sql = "INSERT OR IGNORE INTO module(code,nom,coefficient,enseignant) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[][] data = {
                {"MAT101", "Mathématiques Fondamentales",    3.0, "M. Ben Salah"},
                {"INFO101","Programmation Java",             4.0, "Mme. Gharbi"},
                {"ANG101", "Anglais Technique",              2.0, "M. Jedidi"},
                {"ALGO101","Algorithmique et Structures",    4.0, "M. Elloumi"},
                {"GEST101","Gestion et Économie",            2.0, "Mme. Bouaziz"},
            };
            for (Object[] row : data) {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setDouble(3, (Double)  row[2]);
                ps.setString(4, (String) row[3]);
                ps.executeUpdate();
            }
        }
    }

    // ---------------------------------------------------------------- etudiants
    private static int[] seedEtudiants(Connection conn) throws SQLException {
        String sql = """
            INSERT OR IGNORE INTO etudiant
              (nom,prenom,cin,email,telephone,date_naissance,niveau,filiere,groupe)
            VALUES(?,?,?,?,?,?,?,?,?)
            """;

        // nom, prenom, cin, email, telephone, date_naissance, niveau, filiere, groupe
        Object[][] data = {
            {"TRABELSI",  "Mohamed",  "12345678", "mohamed.trabelsi@etud.univ-tunis.tn",   "22345678", "2001-03-15", "L1", "INFO", "A"},
            {"BEN SALEM", "Fatima",   "23456789", "fatima.bensalem@etud.univ-tunis.tn",    "95123456", "2002-07-22", "L1", "INFO", "B"},
            {"CHAABANE",  "Amine",    "34567890", "amine.chaabane@etud.univ-tunis.tn",     "55678901", "2001-11-08", "L2", "INFO", "A"},
            {"MAKHLOUF",  "Sana",     "45678901", "sana.makhlouf@etud.univ-tunis.tn",      "47890123", "2000-05-30", "L2", "INFO", "C"},
            {"BOUZID",    "Hamza",    "56789012", "hamza.bouzid@etud.univ-tunis.tn",       "21234567", "2003-02-14", "L1", "MATH", "A"},
            {"HAMDI",     "Ines",     "67890123", "ines.hamdi@etud.univ-tunis.tn",         "93456789", "2001-09-19", "L2", "MATH", "B"},
            {"SAIDI",     "Youssef",  "78901234", "youssef.saidi@etud.univ-tunis.tn",      "56789012", "2002-12-03", "L3", "INFO", "B"},
            {"KHELIFI",   "Mariam",   "89012345", "mariam.khelifi@etud.univ-tunis.tn",     "44567890", "2000-08-25", "L3", "INFO", "A"},
            {"MANSOURI",  "Karim",    "90123456", "karim.mansouri@etud.univ-tunis.tn",     "27890123", "2003-04-11", "L1", "PHYS", "C"},
            {"AGREBI",    "Nour",     "01234567", "nour.agrebi@etud.univ-tunis.tn",        "98765432", "2002-06-17", "L1", "INFO", "C"},
            {"JELASSI",   "Bilel",    "11223344", "bilel.jelassi@etud.univ-tunis.tn",      "52345678", "2001-01-28", "M1", "INFO", "A"},
            {"DRIDI",     "Amira",    "22334455", "amira.dridi@etud.univ-tunis.tn",        "77654321", "2000-10-05", "M1", "INFO", "B"},
            {"FERJANI",   "Skander",  "33445566", "skander.ferjani@etud.univ-tunis.tn",   "23456780", "2003-03-22", "L2", "ELEC", "A"},
            {"BAHRI",     "Rania",    "44556677", "rania.bahri@etud.univ-tunis.tn",        "91234567", "2001-07-09", "L1", "INFO", "A"},
            {"TLILI",     "Mehdi",    "55667788", "mehdi.tlili@etud.univ-tunis.tn",        "46789012", "2002-02-28", "L3", "MATH", "C"},
        };

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++) ps.setString(i + 1, (String) row[i]);
                ps.executeUpdate();
            }
        }

        // Retrieve the inserted IDs in insertion order (by CIN list)
        List<String> cins = List.of(
            "12345678","23456789","34567890","45678901","56789012",
            "67890123","78901234","89012345","90123456","01234567",
            "11223344","22334455","33445566","44556677","55667788"
        );
        int[] ids = new int[cins.size()];
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM etudiant WHERE cin=?")) {
            for (int i = 0; i < cins.size(); i++) {
                ps.setString(1, cins.get(i));
                ResultSet rs = ps.executeQuery();
                ids[i] = rs.next() ? rs.getInt(1) : -1;
            }
        }
        return ids;
    }

    // ------------------------------------------------------------------- notes
    private static void seedNotes(Connection conn, int[] eids) throws SQLException {
        // Retrieve module IDs ordered by code
        int[] mids = new int[5];
        String[] codes = {"ALGO101","ANG101","GEST101","INFO101","MAT101"};
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM module WHERE code=?")) {
            for (int i = 0; i < codes.length; i++) {
                ps.setString(1, codes[i]);
                ResultSet rs = ps.executeQuery();
                mids[i] = rs.next() ? rs.getInt(1) : -1;
            }
        }

        // [etudiant_index][module_index] = {note_cc, note_examen}
        // indices: ALGO101=0, ANG101=1, GEST101=2, INFO101=3, MAT101=4
        double[][][] grades = {
            // TRABELSI Mohamed
            {{14.0,13.5},{12.0,11.0},{15.0,14.5},{16.0,15.5},{13.0,12.0}},
            // BEN SALEM Fatima
            {{18.0,17.5},{15.0,16.0},{14.0,13.0},{17.0,18.0},{16.0,15.0}},
            // CHAABANE Amine
            {{ 9.0, 8.5},{11.0,10.0},{12.0,13.0},{10.0, 9.5},{11.0,10.5}},
            // MAKHLOUF Sana
            {{15.5,16.0},{13.0,14.0},{16.0,15.5},{14.0,15.0},{12.0,13.5}},
            // BOUZID Hamza
            {{ 7.0, 6.5},{ 9.0, 8.0},{10.0, 9.0},{ 8.0, 7.5},{ 6.0, 7.0}},
            // HAMDI Ines
            {{19.0,18.5},{17.0,16.5},{18.0,19.0},{20.0,19.5},{18.5,17.5}},
            // SAIDI Youssef
            {{11.0,12.0},{10.0,11.5},{13.0,12.5},{12.0,11.0},{14.0,13.0}},
            // KHELIFI Mariam
            {{16.5,17.0},{14.0,15.0},{15.0,16.0},{18.0,17.5},{13.0,14.0}},
            // MANSOURI Karim
            {{ 8.0, 9.0},{12.0,11.0},{ 9.0, 8.0},{10.0,11.0},{ 7.5, 8.5}},
            // AGREBI Nour
            {{13.0,14.0},{15.0,14.5},{12.0,13.0},{14.5,15.0},{16.0,15.5}},
            // JELASSI Bilel
            {{17.0,16.5},{13.0,14.0},{15.0,14.5},{18.0,17.0},{14.0,15.0}},
            // DRIDI Amira
            {{12.0,11.5},{10.0,11.0},{13.0,12.0},{11.0,12.5},{10.0, 9.5}},
            // FERJANI Skander
            {{14.5,15.5},{12.0,13.0},{11.0,12.0},{15.0,14.0},{16.0,17.0}},
            // BAHRI Rania
            {{ 9.5, 8.5},{11.0,10.5},{14.0,13.5},{10.0, 9.0},{12.0,11.5}},
            // TLILI Mehdi
            {{15.0,16.0},{14.0,13.5},{13.0,14.0},{16.5,15.5},{17.0,16.0}},
        };

        String sql = """
            INSERT OR IGNORE INTO note(etudiant_id,module_id,note_cc,note_examen,moyenne_module)
            VALUES(?,?,?,?,?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int ei = 0; ei < eids.length; ei++) {
                if (eids[ei] == -1) continue;
                for (int mi = 0; mi < mids.length; mi++) {
                    if (mids[mi] == -1) continue;
                    double cc  = grades[ei][mi][0];
                    double ex  = grades[ei][mi][1];
                    double moy = cc * 0.4 + ex * 0.6;
                    ps.setInt(1, eids[ei]);
                    ps.setInt(2, mids[mi]);
                    ps.setDouble(3, cc);
                    ps.setDouble(4, ex);
                    ps.setDouble(5, moy);
                    ps.executeUpdate();
                }
            }
        }
    }
}
