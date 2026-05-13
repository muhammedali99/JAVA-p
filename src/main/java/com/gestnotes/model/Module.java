package com.gestnotes.model;

public class Module {
    private int id;
    private String code;
    private String nom;
    private double coefficient;
    private String enseignant;

    public Module() {}

    public Module(int id, String code, String nom, double coefficient, String enseignant) {
        this.id = id;
        this.code = code;
        this.nom = nom;
        this.coefficient = coefficient;
        this.enseignant = enseignant;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getCoefficient() { return coefficient; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }

    public String getEnseignant() { return enseignant; }
    public void setEnseignant(String enseignant) { this.enseignant = enseignant; }

    @Override
    public String toString() {
        return code + " - " + nom;
    }
}
