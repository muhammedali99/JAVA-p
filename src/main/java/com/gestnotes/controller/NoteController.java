package com.gestnotes.controller;

import com.gestnotes.dao.EtudiantDAO;
import com.gestnotes.dao.ModuleDAO;
import com.gestnotes.dao.NoteDAO;
import com.gestnotes.model.Etudiant;
import com.gestnotes.model.Module;
import com.gestnotes.model.Note;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class NoteController {

    @FXML private ComboBox<Etudiant> etudiantCombo;
    @FXML private ComboBox<Module> moduleCombo;
    @FXML private TextField noteCCField, noteExamenField;
    @FXML private Button calculerBtn, sauvegarderBtn;
    @FXML private Label moyenneLabel, mentionLabel, moyenneGeneraleLabel;
    @FXML private TableView<Note> notesTable;
    @FXML private TableColumn<Note, String> colModule, colMention;
    @FXML private TableColumn<Note, Double> colNoteCC, colNoteExamen, colMoyenne;

    private final EtudiantDAO etudiantDAO = new EtudiantDAO();
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final NoteDAO noteDAO = new NoteDAO();

    @FXML
    public void initialize() {
        colModule.setCellValueFactory(data -> {
            Note n = data.getValue();
            String label = n.getModuleCode() != null ? n.getModuleCode() + " - " + n.getModuleNom() : "";
            return new javafx.beans.property.SimpleStringProperty(label);
        });
        colNoteCC.setCellValueFactory(new PropertyValueFactory<>("noteCC"));
        colNoteExamen.setCellValueFactory(new PropertyValueFactory<>("noteExamen"));
        colMoyenne.setCellValueFactory(new PropertyValueFactory<>("moyenneModule"));
        colMention.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getMention()));

        etudiantCombo.setItems(FXCollections.observableArrayList(etudiantDAO.findAll()));
        moduleCombo.setItems(FXCollections.observableArrayList(moduleDAO.findAll()));
    }

    @FXML
    private void handleEtudiantChange() {
        Etudiant etudiant = etudiantCombo.getValue();
        if (etudiant == null) return;
        refreshNotesTable(etudiant.getId());
        updateMoyenneGenerale(etudiant.getId());
        noteCCField.clear();
        noteExamenField.clear();
        moyenneLabel.setText("—");
        mentionLabel.setText("—");
    }

    @FXML
    private void handleModuleChange() {
        Etudiant etudiant = etudiantCombo.getValue();
        Module module = moduleCombo.getValue();
        if (etudiant == null || module == null) return;

        List<Note> notes = noteDAO.findByEtudiant(etudiant.getId());
        for (Note n : notes) {
            if (n.getModuleId() == module.getId()) {
                noteCCField.setText(String.valueOf(n.getNoteCC()));
                noteExamenField.setText(String.valueOf(n.getNoteExamen()));
                moyenneLabel.setText(String.format("%.2f", n.getMoyenneModule()));
                mentionLabel.setText(n.getMention());
                return;
            }
        }
        noteCCField.clear();
        noteExamenField.clear();
        moyenneLabel.setText("—");
        mentionLabel.setText("—");
    }

    @FXML
    private void handleCalculer() {
        double cc, examen;
        try {
            cc = Double.parseDouble(noteCCField.getText().trim());
            examen = Double.parseDouble(noteExamenField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Veuillez entrer des notes numériques valides.");
            return;
        }
        if (!isValidNote(cc) || !isValidNote(examen)) {
            showError("Les notes doivent être comprises entre 0 et 20.");
            return;
        }
        double moyenne = cc * 0.4 + examen * 0.6;
        Note temp = new Note();
        temp.setMoyenneModule(moyenne);
        moyenneLabel.setText(String.format("%.2f", moyenne));
        mentionLabel.setText(temp.getMention());
    }

    @FXML
    private void handleSauvegarder() {
        Etudiant etudiant = etudiantCombo.getValue();
        Module module = moduleCombo.getValue();
        if (etudiant == null || module == null) {
            showError("Veuillez sélectionner un étudiant et un module.");
            return;
        }
        double cc, examen;
        try {
            cc = Double.parseDouble(noteCCField.getText().trim());
            examen = Double.parseDouble(noteExamenField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Veuillez entrer des notes numériques valides.");
            return;
        }
        if (!isValidNote(cc) || !isValidNote(examen)) {
            showError("Les notes doivent être comprises entre 0 et 20.");
            return;
        }
        double moyenne = cc * 0.4 + examen * 0.6;
        Note note = new Note();
        note.setEtudiantId(etudiant.getId());
        note.setModuleId(module.getId());
        note.setNoteCC(cc);
        note.setNoteExamen(examen);
        note.setMoyenneModule(moyenne);
        noteDAO.saveOrUpdate(note);

        refreshNotesTable(etudiant.getId());
        updateMoyenneGenerale(etudiant.getId());
    }

    private void refreshNotesTable(int etudiantId) {
        notesTable.setItems(FXCollections.observableArrayList(noteDAO.findByEtudiant(etudiantId)));
    }

    private void updateMoyenneGenerale(int etudiantId) {
        double mg = noteDAO.getMoyenneGenerale(etudiantId);
        moyenneGeneraleLabel.setText(String.format("%.2f", mg));
    }

    private boolean isValidNote(double note) {
        return note >= 0 && note <= 20;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
