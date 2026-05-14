package com.gestnotes.controller;

import com.gestnotes.dao.EtudiantDAO;
import com.gestnotes.dao.NoteDAO;
import com.gestnotes.model.Etudiant;
import com.gestnotes.model.EtudiantResultat;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

public class StatistiquesController {

    @FXML private Label totalEtudiantsLabel;
    @FXML private Label moyenneClasseLabel;
    @FXML private Label meilleureMoyenneLabel;
    @FXML private Label admisLabel;
    @FXML private Label ajournesLabel;
    @FXML private TableView<EtudiantResultat> resultatsTable;
    @FXML private TableColumn<EtudiantResultat, String> colNom, colPrenom, colCin, colFiliere, colMoyenne, colResultat;

    private final EtudiantDAO etudiantDAO = new EtudiantDAO();
    private final NoteDAO noteDAO = new NoteDAO();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCin.setCellValueFactory(new PropertyValueFactory<>("cin"));
        colFiliere.setCellValueFactory(new PropertyValueFactory<>("filiere"));
        colMoyenne.setCellValueFactory(new PropertyValueFactory<>("moyenne"));
        colResultat.setCellValueFactory(new PropertyValueFactory<>("resultat"));

        colResultat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if ("Admis".equals(item))
                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                else if ("Ajourné".equals(item))
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                else
                    setStyle("-fx-text-fill: #95a5a6;");
            }
        });

        loadStatistiques();
    }

    @FXML
    private void handleRefresh() {
        loadStatistiques();
    }

    private void loadStatistiques() {
        List<Etudiant> etudiants = etudiantDAO.findAll();
        totalEtudiantsLabel.setText(String.valueOf(etudiants.size()));

        List<EtudiantResultat> resultats = new ArrayList<>();
        double somme = 0;
        double meilleure = -1;
        int admis = 0, ajournes = 0, countWithNotes = 0;

        for (Etudiant e : etudiants) {
            if (noteDAO.hasNotes(e.getId())) {
                double mg = noteDAO.getMoyenneGenerale(e.getId());
                String resultat = mg >= 10 ? "Admis" : "Ajourné";
                resultats.add(new EtudiantResultat(
                    e.getNom(), e.getPrenom(), e.getCin(), e.getFiliere(),
                    String.format("%.2f", mg), resultat));
                somme += mg;
                countWithNotes++;
                if (mg > meilleure) meilleure = mg;
                if (mg >= 10) admis++; else ajournes++;
            } else {
                resultats.add(new EtudiantResultat(
                    e.getNom(), e.getPrenom(), e.getCin(), e.getFiliere(), "—", "—"));
            }
        }

        moyenneClasseLabel.setText(countWithNotes > 0 ? String.format("%.2f", somme / countWithNotes) : "—");
        meilleureMoyenneLabel.setText(meilleure >= 0 ? String.format("%.2f", meilleure) : "—");
        admisLabel.setText(String.valueOf(admis));
        ajournesLabel.setText(String.valueOf(ajournes));
        resultatsTable.setItems(FXCollections.observableArrayList(resultats));
    }
}
