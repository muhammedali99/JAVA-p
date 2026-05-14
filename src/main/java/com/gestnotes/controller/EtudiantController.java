package com.gestnotes.controller;

import com.gestnotes.Session;
import com.gestnotes.dao.EtudiantDAO;
import com.gestnotes.model.Etudiant;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EtudiantController {

    @FXML private TextField nomField, prenomField, cinField, emailField, telephoneField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private ComboBox<String> niveauCombo, filiereCombo, groupeCombo;
    @FXML private Button ajouterBtn, modifierBtn, supprimerBtn, reinitialiserBtn;
    @FXML private TextField rechercheField;
    @FXML private TableView<Etudiant> etudiantTable;
    @FXML private TableColumn<Etudiant, Integer> colId;
    @FXML private TableColumn<Etudiant, String> colNom, colPrenom, colCin, colEmail, colTelephone, colDateNaissance, colNiveau, colFiliere, colGroupe;

    private final EtudiantDAO etudiantDAO = new EtudiantDAO();
    private Etudiant selectedEtudiant;

    @FXML
    public void initialize() {
        niveauCombo.setItems(FXCollections.observableArrayList("L1", "L2", "L3", "M1", "M2"));
        filiereCombo.setItems(FXCollections.observableArrayList("INFO", "MATH", "PHYS", "ELEC"));
        groupeCombo.setItems(FXCollections.observableArrayList("A", "B", "C"));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colCin.setCellValueFactory(new PropertyValueFactory<>("cin"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colDateNaissance.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colFiliere.setCellValueFactory(new PropertyValueFactory<>("filiere"));
        colGroupe.setCellValueFactory(new PropertyValueFactory<>("groupe"));

        etudiantTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) populateForm(newVal);
        });

        if (Session.currentUser != null && "ENSEIGNANT".equals(Session.currentUser.getRole())) {
            ajouterBtn.setDisable(true);
            modifierBtn.setDisable(true);
            supprimerBtn.setDisable(true);
        }

        refreshTable(etudiantDAO.findAll());
    }

    @FXML
    private void handleAjouter() {
        if (!validateForm(true)) return;
        Etudiant e = buildFromForm();
        etudiantDAO.add(e);
        refreshTable(etudiantDAO.findAll());
        clearForm();
    }

    @FXML
    private void handleModifier() {
        if (selectedEtudiant == null) {
            showError("Veuillez sélectionner un étudiant à modifier.");
            return;
        }
        if (!validateForm(false)) return;
        Etudiant e = buildFromForm();
        e.setId(selectedEtudiant.getId());
        etudiantDAO.update(e);
        refreshTable(etudiantDAO.findAll());
        clearForm();
    }

    @FXML
    private void handleSupprimer() {
        if (selectedEtudiant == null) {
            showError("Veuillez sélectionner un étudiant à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'étudiant");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " + selectedEtudiant.getPrenom() + " " + selectedEtudiant.getNom() + " ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            etudiantDAO.delete(selectedEtudiant.getId());
            refreshTable(etudiantDAO.findAll());
            clearForm();
        }
    }

    @FXML
    private void handleReinitialiser() {
        clearForm();
    }

    @FXML
    private void handleRecherche() {
        String keyword = rechercheField.getText().trim();
        if (keyword.isEmpty()) {
            refreshTable(etudiantDAO.findAll());
        } else {
            refreshTable(etudiantDAO.search(keyword));
        }
    }

    private boolean validateForm(boolean isNew) {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String cin = cinField.getText().trim();
        String email = emailField.getText().trim();
        String tel = telephoneField.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || cin.isEmpty() || email.isEmpty() || tel.isEmpty()
                || niveauCombo.getValue() == null || filiereCombo.getValue() == null || groupeCombo.getValue() == null) {
            showError("Tous les champs sont obligatoires.");
            return false;
        }
        if (!email.contains("@")) {
            showError("L'adresse email doit contenir '@'.");
            return false;
        }
        if (!tel.matches("[0-9]+")) {
            showError("Le téléphone doit contenir uniquement des chiffres.");
            return false;
        }
        if (isNew && etudiantDAO.cinExists(cin)) {
            showError("Ce CIN existe déjà.");
            return false;
        }
        if (!isNew && selectedEtudiant != null && etudiantDAO.cinExistsExcept(cin, selectedEtudiant.getId())) {
            showError("Ce CIN est déjà utilisé par un autre étudiant.");
            return false;
        }
        return true;
    }

    private Etudiant buildFromForm() {
        Etudiant e = new Etudiant();
        e.setNom(nomField.getText().trim());
        e.setPrenom(prenomField.getText().trim());
        e.setCin(cinField.getText().trim());
        e.setEmail(emailField.getText().trim());
        e.setTelephone(telephoneField.getText().trim());
        e.setDateNaissance(dateNaissancePicker.getValue() != null ? dateNaissancePicker.getValue().toString() : "");
        e.setNiveau(niveauCombo.getValue());
        e.setFiliere(filiereCombo.getValue());
        e.setGroupe(groupeCombo.getValue());
        return e;
    }

    private void populateForm(Etudiant e) {
        selectedEtudiant = e;
        nomField.setText(e.getNom());
        prenomField.setText(e.getPrenom());
        cinField.setText(e.getCin());
        emailField.setText(e.getEmail());
        telephoneField.setText(e.getTelephone());
        try {
            dateNaissancePicker.setValue(
                e.getDateNaissance() != null && !e.getDateNaissance().isEmpty()
                    ? LocalDate.parse(e.getDateNaissance()) : null);
        } catch (Exception ex) {
            dateNaissancePicker.setValue(null);
        }
        niveauCombo.setValue(e.getNiveau());
        filiereCombo.setValue(e.getFiliere());
        groupeCombo.setValue(e.getGroupe());
    }

    private void clearForm() {
        selectedEtudiant = null;
        nomField.clear(); prenomField.clear(); cinField.clear();
        emailField.clear(); telephoneField.clear(); dateNaissancePicker.setValue(null);
        niveauCombo.setValue(null); filiereCombo.setValue(null); groupeCombo.setValue(null);
        etudiantTable.getSelectionModel().clearSelection();
    }

    private void refreshTable(List<Etudiant> list) {
        etudiantTable.setItems(FXCollections.observableArrayList(list));
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
