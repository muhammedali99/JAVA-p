package com.gestnotes.controller;

import com.gestnotes.Session;
import com.gestnotes.dao.UtilisateurDAO;
import com.gestnotes.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConnexionController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez saisir le nom d'utilisateur et le mot de passe.");
            return;
        }

        Utilisateur user = utilisateurDAO.findByUsernameAndPassword(username, password);
        if (user == null) {
            errorLabel.setText("Identifiants incorrects");
            return;
        }

        Session.currentUser = user;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestnotes/main.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 650);
            Stage stage = new Stage();
            stage.setTitle("GestNotes");
            stage.setScene(scene);
            stage.show();

            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();
        } catch (Exception e) {
            errorLabel.setText("Erreur lors du chargement de l'application.");
            e.printStackTrace();
        }
    }
}
