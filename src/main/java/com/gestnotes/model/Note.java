package com.gestnotes.model;

public class Note {
    private int id;
    private int etudiantId;
    private int moduleId;
    private double noteCC;
    private double noteExamen;
    private double moyenneModule;

    private String moduleCode;
    private String moduleNom;

    public Note() {}

    public Note(int id, int etudiantId, int moduleId, double noteCC, double noteExamen, double moyenneModule) {
        this.id = id;
        this.etudiantId = etudiantId;
        this.moduleId = moduleId;
        this.noteCC = noteCC;
        this.noteExamen = noteExamen;
        this.moyenneModule = moyenneModule;
    }

    public String getMention() {
        if (moyenneModule < 10) return "Ajourné";
        if (moyenneModule < 12) return "Passable";
        if (moyenneModule < 14) return "Assez bien";
        if (moyenneModule < 16) return "Bien";
        return "Très bien";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEtudiantId() { return etudiantId; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }

    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }

    public double getNoteCC() { return noteCC; }
    public void setNoteCC(double noteCC) { this.noteCC = noteCC; }

    public double getNoteExamen() { return noteExamen; }
    public void setNoteExamen(double noteExamen) { this.noteExamen = noteExamen; }

    public double getMoyenneModule() { return moyenneModule; }
    public void setMoyenneModule(double moyenneModule) { this.moyenneModule = moyenneModule; }

    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }

    public String getModuleNom() { return moduleNom; }
    public void setModuleNom(String moduleNom) { this.moduleNom = moduleNom; }
}
