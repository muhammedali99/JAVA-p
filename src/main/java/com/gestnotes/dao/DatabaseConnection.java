package com.gestnotes.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:gestnotes.db";
    private static Connection instance;

    private DatabaseConnection() {}

    public static Connection getInstance() {
        if (instance == null) {
            try {
                instance = DriverManager.getConnection(URL);
                initDB(instance);
            } catch (SQLException e) {
                throw new RuntimeException("Cannot connect to database", e);
            }
        }
        return instance;
    }

    private static void initDB(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS etudiant (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  nom TEXT NOT NULL,
                  prenom TEXT NOT NULL,
                  cin TEXT UNIQUE NOT NULL,
                  email TEXT,
                  telephone TEXT,
                  date_naissance TEXT,
                  niveau TEXT,
                  filiere TEXT,
                  groupe TEXT
                )
                """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS module (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  code TEXT UNIQUE NOT NULL,
                  nom TEXT NOT NULL,
                  coefficient REAL NOT NULL,
                  enseignant TEXT
                )
                """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS note (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  etudiant_id INTEGER NOT NULL,
                  module_id INTEGER NOT NULL,
                  note_cc REAL,
                  note_examen REAL,
                  moyenne_module REAL,
                  FOREIGN KEY (etudiant_id) REFERENCES etudiant(id),
                  FOREIGN KEY (module_id) REFERENCES module(id),
                  UNIQUE(etudiant_id, module_id)
                )
                """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS utilisateur (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  username TEXT UNIQUE NOT NULL,
                  password TEXT NOT NULL,
                  role TEXT NOT NULL
                )
                """);

            var rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM utilisateur");
            if (rs.next() && rs.getInt(1) == 0) {
                st.executeUpdate("INSERT INTO utilisateur(username,password,role) VALUES('admin','admin123','ADMIN')");
                st.executeUpdate("INSERT INTO utilisateur(username,password,role) VALUES('enseignant','ens123','ENSEIGNANT')");
            }
        }
        DataSeeder.seed(conn);
    }
}
