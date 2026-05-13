package com.gestnotes.controller;

import com.gestnotes.dao.ModuleDAO;
import com.gestnotes.model.Module;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class ModuleController {

    @FXML private TextField codeField, nomField, coefficientField, enseignantField;
    @FXML private Button ajouterBtn, modifierBtn, supprimerBtn, reinitialiserBtn;
    @FXML private TableView<Module> moduleTable;
    @FXML private TableColumn<Module, Integer> colId;
    @FXML private TableColumn<Module, String> colCode, colNom, colEnseignant;
    @FXML private TableColumn<Module, Double> colCoefficient;

    private final ModuleDAO moduleDAO = new ModuleDAO();
    private Module selectedModule;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCoefficient.setCellValueFactory(new PropertyValueFactory<>("coefficient"));
        colEnseignant.setCellValueFactory(new PropertyValueFactory<>("enseignant"));

        moduleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) populateForm(newVal);
        });

        refreshTable();
    }

    @FXML
    private void handleAjouter() {
        if (!validateForm()) return;
        Module m = buildFromForm();
        moduleDAO.add(m);
        refreshTable();
        clearForm();
    }

    @FXML
    private void handleModifier() {
        if (selectedModule == null) {
            showError("Veuillez sélectionner un module à modifier.");
            return;
        }
        if (!validateForm()) return;
        Module m = buildFromForm();
        m.setId(selectedModule.getId());
        moduleDAO.update(m);
        refreshTable();
        clearForm();
    }

    @FXML
    private void handleSupprimer() {
        if (selectedModule == null) {
            showError("Veuillez sélectionner un module à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le module");
        confirm.setContentText("Supprimer le module \"" + selectedModule.getNom() + "\" ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            moduleDAO.delete(selectedModule.getId());
            refreshTable();
            clearForm();
        }
    }

    @FXML
    private void handleReinitialiser() {
        clearForm();
    }

    private boolean validateForm() {
        String code = codeField.getText().trim();
        String nom = nomField.getText().trim();
        String coeffStr = coefficientField.getText().trim();
        String enseignant = enseignantField.getText().trim();

        if (code.isEmpty() || nom.isEmpty() || coeffStr.isEmpty() || enseignant.isEmpty()) {
            showError("Tous les champs sont obligatoires.");
            return false;
        }
        try {
            double coeff = Double.parseDouble(coeffStr);
            if (coeff <= 0) {
                showError("Le coefficient doit être supérieur à 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Le coefficient doit être un nombre valide.");
            return false;
        }
        return true;
    }

    private Module buildFromForm() {
        Module m = new Module();
        m.setCode(codeField.getText().trim());
        m.setNom(nomField.getText().trim());
        m.setCoefficient(Double.parseDouble(coefficientField.getText().trim()));
        m.setEnseignant(enseignantField.getText().trim());
        return m;
    }

    private void populateForm(Module m) {
        selectedModule = m;
        codeField.setText(m.getCode());
        nomField.setText(m.getNom());
        coefficientField.setText(String.valueOf(m.getCoefficient()));
        enseignantField.setText(m.getEnseignant());
    }

    private void clearForm() {
        selectedModule = null;
        codeField.clear(); nomField.clear(); coefficientField.clear(); enseignantField.clear();
        moduleTable.getSelectionModel().clearSelection();
    }

    private void refreshTable() {
        moduleTable.setItems(FXCollections.observableArrayList(moduleDAO.findAll()));
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
