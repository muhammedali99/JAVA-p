package com.gestnotes.model;

public class EtudiantResultat {
    private String nom;
    private String prenom;
    private String cin;
    private String filiere;
    private String moyenne;
    private String resultat;

    public EtudiantResultat(String nom, String prenom, String cin, String filiere,
                             String moyenne, String resultat) {
        this.nom = nom;
        this.prenom = prenom;
        this.cin = cin;
        this.filiere = filiere;
        this.moyenne = moyenne;
        this.resultat = resultat;
    }

    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getCin() { return cin; }
    public String getFiliere() { return filiere; }
    public String getMoyenne() { return moyenne; }
    public String getResultat() { return resultat; }
}
