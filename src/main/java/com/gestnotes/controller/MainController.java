package com.gestnotes.controller;

import com.gestnotes.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private Label userInfoLabel;
    @FXML private StackPane contentArea;
    @FXML private Button etudiantsBtn;
    @FXML private Button modulesBtn;
    @FXML private Button notesBtn;
    @FXML private Button statsBtn;

    @FXML
    public void initialize() {
        if (Session.currentUser != null) {
            userInfoLabel.setText(Session.currentUser.getUsername() + " (" + Session.currentUser.getRole() + ")");
            if ("ENSEIGNANT".equals(Session.currentUser.getRole())) {
                modulesBtn.setDisable(true);
            }
        }
        showEtudiants();
    }

    @FXML
    private void showEtudiants() {
        loadView("/com/gestnotes/etudiant.fxml");
    }

    @FXML
    private void showModules() {
        loadView("/com/gestnotes/module.fxml");
    }

    @FXML
    private void showNotes() {
        loadView("/com/gestnotes/note.fxml");
    }

    @FXML
    private void showStatistiques() {
        loadView("/com/gestnotes/statistiques.fxml");
    }

    @FXML
    private void handleQuitter() {
        Platform.exit();
    }

    @FXML
    private void handleDeconnexion() {
        Session.currentUser = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestnotes/connexion.fxml"));
            Scene scene = new Scene(loader.load(), 500, 350);
            Stage stage = new Stage();
            stage.setTitle("GestNotes — Connexion");
            stage.setScene(scene);
            stage.show();

            Stage mainStage = (Stage) contentArea.getScene().getWindow();
            mainStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
