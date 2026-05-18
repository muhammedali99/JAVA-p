# Rapport de Projet — GestNotes
### Application de Gestion des Étudiants, Modules et Notes
**Technologie :** Java 21 · JavaFX 21 · SQLite · Maven · Docker

---

## 1. Introduction

GestNotes est une application desktop développée en Java avec JavaFX. Elle permet à un établissement universitaire de centraliser la gestion des étudiants, des modules pédagogiques et des notes. L'utilisateur peut enregistrer les informations personnelles de chaque étudiant, créer les modules avec leurs coefficients, saisir les notes de contrôle continu et d'examen, puis consulter automatiquement les moyennes et les mentions obtenues.

L'application repose sur une base de données SQLite locale, une architecture MVC claire et un système d'authentification à deux profils (Administrateur et Enseignant). Elle a été containerisée avec Docker pour garantir un environnement d'exécution reproductible indépendamment du système hôte.

---

## 2. Objectifs

- Maîtriser la **programmation orientée objet** en Java (classes, encapsulation, héritage).
- Utiliser **JavaFX** pour concevoir une interface graphique moderne et ergonomique.
- Manipuler le composant **TableView** pour afficher et interagir avec des données tabulaires.
- Gérer les **événements des boutons** et les interactions utilisateur.
- Appliquer des **contrôles de saisie** rigoureux avec des messages d'erreur via `Alert`.
- Organiser le code selon une **architecture MVC** (Modèle – Vue – Contrôleur).
- Gérer les données avec une **base de données SQLite** via JDBC.
- Mettre en place un système d'**authentification** avec gestion des rôles.
- Calculer automatiquement les **moyennes pondérées** et les **mentions** selon des règles précises.
- Présenter des **statistiques de classe** (total, meilleure moyenne, admis/ajournés).

---

## 3. Architecture

Le projet suit strictement le patron **MVC (Modèle – Vue – Contrôleur)** :

```
src/main/
├── java/com/gestnotes/
│   ├── Main.java                   ← Point d'entrée JavaFX
│   ├── Session.java                ← Utilisateur connecté (état global)
│   ├── model/                      ← Modèle : entités métier
│   ├── dao/                        ← Accès aux données (JDBC/SQLite)
│   └── controller/                 ← Contrôleurs FXML
└── resources/com/gestnotes/
    ├── *.fxml                      ← Vues (interfaces graphiques)
    └── styles.css                  ← Feuille de style globale
```

### Classes et leurs rôles

| Couche | Classe | Rôle | Méthodes clés |
|--------|--------|------|---------------|
| **Modèle** | `Etudiant` | Représente un étudiant | Getters/setters pour id, nom, prénom, CIN, email, téléphone, dateNaissance, niveau, filière, groupe |
| **Modèle** | `Module` | Représente un module pédagogique | Getters/setters pour id, code, nom, coefficient, enseignant |
| **Modèle** | `Note` | Note d'un étudiant pour un module | `getMention()` — calcule la mention à partir de la moyenne |
| **Modèle** | `Utilisateur` | Compte utilisateur | Getters/setters pour id, username, password, role |
| **Modèle** | `EtudiantResultat` | DTO pour la vue statistiques | getNom(), getMoyenne(), getResultat() |
| **DAO** | `DatabaseConnection` | Connexion SQLite singleton + initialisation des tables | `getInstance()`, `initDB()` |
| **DAO** | `EtudiantDAO` | CRUD étudiants | `findAll()`, `add()`, `update()`, `delete()`, `search()`, `cinExists()`, `cinExistsExcept()` |
| **DAO** | `ModuleDAO` | CRUD modules | `findAll()`, `add()`, `update()`, `delete()` |
| **DAO** | `NoteDAO` | Gestion des notes | `findByEtudiant()`, `saveOrUpdate()`, `getMoyenneGenerale()`, `hasNotes()` |
| **DAO** | `UtilisateurDAO` | Authentification | `findByUsernameAndPassword()` |
| **Contrôleur** | `ConnexionController` | Gère la fenêtre de login | `handleLogin()` |
| **Contrôleur** | `MainController` | Navigation principale | `showEtudiants()`, `showModules()`, `showNotes()`, `showStatistiques()`, `handleDeconnexion()`, `handleQuitter()` |
| **Contrôleur** | `EtudiantController` | CRUD + recherche étudiants | `handleAjouter()`, `handleModifier()`, `handleSupprimer()`, `handleRecherche()`, `validateForm()` |
| **Contrôleur** | `ModuleController` | CRUD modules | `handleAjouter()`, `handleModifier()`, `handleSupprimer()` |
| **Contrôleur** | `NoteController` | Saisie et calcul des notes | `handleCalculer()`, `handleSauvegarder()`, `handleEtudiantChange()`, `handleModuleChange()` |
| **Contrôleur** | `StatistiquesController` | Statistiques de classe | `loadStatistiques()`, `handleRefresh()` |
| **Utilitaire** | `Session` | Stockage de l'utilisateur connecté | Champ statique `currentUser` |
| **Entrée** | `Main` | Lance l'application JavaFX | `start()`, `main()` |

---

## 4. Fonctionnalités

### 4.1 Authentification

La fenêtre de connexion demande un nom d'utilisateur et un mot de passe. Le contrôleur interroge la base de données et redirige vers la fenêtre principale si les identifiants sont corrects.

```java
// ConnexionController.java
Utilisateur user = utilisateurDAO.findByUsernameAndPassword(username, password);
if (user == null) {
    errorLabel.setText("Identifiants incorrects");
    return;
}
Session.currentUser = user;
FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestnotes/main.fxml"));
Scene scene = new Scene(loader.load(), 1000, 650);
Stage stage = new Stage();
stage.setTitle("GestNotes");
stage.setScene(scene);
stage.show();
```

La requête SQL utilise des paramètres préparés pour éviter les injections SQL :

```java
// UtilisateurDAO.java
String sql = "SELECT * FROM utilisateur WHERE username=? AND password=?";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, username);
    ps.setString(2, password);
    ...
}
```

---

### 4.2 Gestion des étudiants

Le formulaire permet d'ajouter, modifier et supprimer des étudiants. La recherche dynamique filtre la liste en temps réel à chaque frappe clavier.

```java
// EtudiantController.java — recherche dynamique (onKeyReleased dans le FXML)
private void handleRecherche() {
    String keyword = rechercheField.getText().trim();
    if (keyword.isEmpty()) {
        refreshTable(etudiantDAO.findAll());
    } else {
        refreshTable(etudiantDAO.search(keyword));
    }
}
```

La requête de recherche couvre plusieurs champs simultanément :

```java
// EtudiantDAO.java
String sql = "SELECT * FROM etudiant WHERE nom LIKE ? OR prenom LIKE ? "
           + "OR cin LIKE ? OR filiere LIKE ? ORDER BY nom, prenom";
String k = "%" + keyword + "%";
```

La suppression affiche une confirmation avant d'agir :

```java
Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
confirm.setContentText("Êtes-vous sûr de vouloir supprimer "
    + selectedEtudiant.getPrenom() + " " + selectedEtudiant.getNom() + " ?");
Optional<ButtonType> result = confirm.showAndWait();
if (result.isPresent() && result.get() == ButtonType.OK) {
    etudiantDAO.delete(selectedEtudiant.getId());
}
```

---

### 4.3 Gestion des modules

CRUD complet sur les modules. Le code du module est unique (contrainte UNIQUE en base). En cas de doublon, une `Alert` d'erreur s'affiche au lieu d'un crash :

```java
// ModuleController.java
try {
    moduleDAO.add(buildFromForm());
    refreshTable();
    clearForm();
} catch (RuntimeException e) {
    if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
        showError("Ce code de module existe déjà. Choisissez un code différent.");
    } else {
        showError("Erreur lors de l'ajout : " + e.getMessage());
    }
}
```

---

### 4.4 Gestion des notes

L'utilisateur sélectionne un étudiant et un module, saisit les notes CC et examen, puis calcule et sauvegarde. Si une note existe déjà pour ce couple étudiant/module, les champs sont pré-remplis.

```java
// NoteController.java — pré-remplissage si note existante
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
```

La sauvegarde utilise un `INSERT OR UPDATE` pour gérer création et mise à jour en une seule requête :

```java
// NoteDAO.java
String sql = """
    INSERT INTO note(etudiant_id, module_id, note_cc, note_examen, moyenne_module)
    VALUES(?,?,?,?,?)
    ON CONFLICT(etudiant_id, module_id) DO UPDATE SET
      note_cc=excluded.note_cc,
      note_examen=excluded.note_examen,
      moyenne_module=excluded.moyenne_module
    """;
```

---

### 4.5 Statistiques

La vue statistiques calcule dynamiquement les indicateurs de la classe et affiche les résultats par étudiant avec un code couleur (vert = Admis, rouge = Ajourné).

```java
// StatistiquesController.java
for (Etudiant e : etudiants) {
    if (noteDAO.hasNotes(e.getId())) {
        double mg = noteDAO.getMoyenneGenerale(e.getId());
        String resultat = mg >= 10 ? "Admis" : "Ajourné";
        resultats.add(new EtudiantResultat(..., String.format("%.2f", mg), resultat));
        somme += mg;
        countWithNotes++;
        if (mg > meilleure) meilleure = mg;
        if (mg >= 10) admis++; else ajournes++;
    }
}
moyenneClasseLabel.setText(countWithNotes > 0
    ? String.format("%.2f", somme / countWithNotes) : "—");
```

---

## 5. Formules de calcul

### Moyenne du module

```
Moyenne module = Note CC × 0,4 + Note Examen × 0,6
```

**Code (NoteController.java) :**
```java
double moyenne = cc * 0.4 + examen * 0.6;
```

### Moyenne générale pondérée

La moyenne générale tient compte du coefficient de chaque module :

```
Moyenne générale = Σ (moyenne_module × coefficient) / Σ (coefficient)
```

**Code (NoteDAO.java) :**
```java
String sql = """
    SELECT SUM(n.moyenne_module * m.coefficient) / SUM(m.coefficient)
    FROM note n
    JOIN module m ON n.module_id = m.id
    WHERE n.etudiant_id = ?
    """;
```

### Mentions

| Intervalle | Mention |
|-----------|---------|
| Moyenne < 10 | Ajourné |
| 10 ≤ Moyenne < 12 | Passable |
| 12 ≤ Moyenne < 14 | Assez bien |
| 14 ≤ Moyenne < 16 | Bien |
| Moyenne ≥ 16 | Très bien |

**Code (Note.java) :**
```java
public String getMention() {
    if (moyenneModule < 10) return "Ajourné";
    if (moyenneModule < 12) return "Passable";
    if (moyenneModule < 14) return "Assez bien";
    if (moyenneModule < 16) return "Bien";
    return "Très bien";
}
```

---

## 6. Contrôles de saisie

Toutes les validations utilisent `Alert(Alert.AlertType.ERROR)` pour afficher le message d'erreur.

| Règle | Champ | Code |
|-------|-------|------|
| Champs obligatoires | Tous | `if (nom.isEmpty() \|\| prenom.isEmpty() \|\| ...)` |
| Email valide | Email | `if (!email.contains("@"))` |
| Téléphone numérique | Téléphone | `if (!tel.matches("[0-9]+"))` |
| CIN unique (ajout) | CIN | `if (etudiantDAO.cinExists(cin))` |
| CIN unique (modif) | CIN | `if (etudiantDAO.cinExistsExcept(cin, id))` |
| Note entre 0 et 20 | Note CC / Examen | `if (note < 0 \|\| note > 20)` |
| Coefficient > 0 | Coefficient module | `if (coeff <= 0)` |
| Coefficient numérique | Coefficient module | `catch (NumberFormatException e)` |
| Code module unique | Code module | `catch (RuntimeException e) { if (e.getMessage().contains("UNIQUE"))` |

**Exemple complet — validation étudiant :**
```java
// EtudiantController.java
private boolean validateForm(boolean isNew) {
    if (nom.isEmpty() || prenom.isEmpty() || cin.isEmpty()
            || email.isEmpty() || tel.isEmpty()
            || niveauCombo.getValue() == null
            || filiereCombo.getValue() == null
            || groupeCombo.getValue() == null) {
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
    if (!isNew && etudiantDAO.cinExistsExcept(cin, selectedEtudiant.getId())) {
        showError("Ce CIN est déjà utilisé par un autre étudiant.");
        return false;
    }
    return true;
}
```

---

## 7. Base de données

La base de données SQLite (`gestnotes.db`) est créée automatiquement au premier lancement via `DatabaseConnection.initDB()`. Le patron **Singleton** garantit qu'une seule connexion est ouverte pendant toute la durée de vie de l'application.

```java
// DatabaseConnection.java — Singleton
public static Connection getInstance() {
    if (instance == null) {
        instance = DriverManager.getConnection(URL);
        initDB(instance);
    }
    return instance;
}
```

### Schéma SQL complet

```sql
CREATE TABLE IF NOT EXISTS etudiant (
  id             INTEGER PRIMARY KEY AUTOINCREMENT,
  nom            TEXT NOT NULL,
  prenom         TEXT NOT NULL,
  cin            TEXT UNIQUE NOT NULL,   -- CIN unique pour chaque étudiant
  email          TEXT,
  telephone      TEXT,
  date_naissance TEXT,
  niveau         TEXT,                   -- L1, L2, L3, M1, M2
  filiere        TEXT,                   -- INFO, MATH, PHYS, ELEC
  groupe         TEXT                    -- A, B, C
);

CREATE TABLE IF NOT EXISTS module (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  code        TEXT UNIQUE NOT NULL,      -- Identifiant pédagogique unique
  nom         TEXT NOT NULL,
  coefficient REAL NOT NULL,             -- Utilisé dans le calcul de la moyenne générale
  enseignant  TEXT
);

CREATE TABLE IF NOT EXISTS note (
  id             INTEGER PRIMARY KEY AUTOINCREMENT,
  etudiant_id    INTEGER NOT NULL,
  module_id      INTEGER NOT NULL,
  note_cc        REAL,                   -- Note contrôle continu (0–20)
  note_examen    REAL,                   -- Note examen (0–20)
  moyenne_module REAL,                   -- CC × 0,4 + Examen × 0,6
  FOREIGN KEY (etudiant_id) REFERENCES etudiant(id),
  FOREIGN KEY (module_id)   REFERENCES module(id),
  UNIQUE(etudiant_id, module_id)         -- Une seule note par étudiant par module
);

CREATE TABLE IF NOT EXISTS utilisateur (
  id       INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT UNIQUE NOT NULL,
  password TEXT NOT NULL,
  role     TEXT NOT NULL                 -- 'ADMIN' ou 'ENSEIGNANT'
);
```

### Description des tables

| Table | Rôle |
|-------|------|
| `etudiant` | Stocke les informations personnelles et académiques de chaque étudiant |
| `module` | Stocke les modules avec leurs coefficients pédagogiques |
| `note` | Associe un étudiant à un module avec ses notes ; contrainte UNIQUE sur le couple (etudiant_id, module_id) |
| `utilisateur` | Comptes de connexion avec rôle ADMIN ou ENSEIGNANT ; deux comptes créés automatiquement au premier lancement |

---

## 8. Rôles utilisateurs

L'application distingue deux profils :

| Profil | Droits |
|--------|--------|
| **ADMIN** | Accès complet : étudiants (CRUD), modules (CRUD), notes (saisie), statistiques |
| **ENSEIGNANT** | Étudiants en lecture seule, modules inaccessibles, notes (saisie/modification), statistiques |

### Implémentation

L'utilisateur connecté est stocké dans la classe `Session` :

```java
// Session.java
public class Session {
    public static Utilisateur currentUser;
}
```

**Restriction dans la navigation principale :**
```java
// MainController.java — désactivation du bouton Modules pour l'enseignant
if ("ENSEIGNANT".equals(Session.currentUser.getRole())) {
    modulesBtn.setDisable(true);
}
```

**Restriction dans la gestion des étudiants :**
```java
// EtudiantController.java — lecture seule pour l'enseignant
if (Session.currentUser != null
        && "ENSEIGNANT".equals(Session.currentUser.getRole())) {
    ajouterBtn.setDisable(true);
    modifierBtn.setDisable(true);
    supprimerBtn.setDisable(true);
}
```

Le profil de l'utilisateur connecté est affiché en permanence dans l'en-tête de la fenêtre principale :

```java
// MainController.java
userInfoLabel.setText(
    Session.currentUser.getUsername() + " (" + Session.currentUser.getRole() + ")"
);
```

---

## 9. Difficultés rencontrées

### 9.1 Ambiguïté de la méthode `setAll()` dans JavaFX

**Problème :** Le compilateur Java 21 refusait l'appel `contentArea.getChildren().setAll(loader.load())` avec une erreur d'ambiguïté entre la version varargs et la version `Collection`.

**Solution :** Typage explicite du résultat du `FXMLLoader` :
```java
javafx.scene.Node view = loader.load();
contentArea.getChildren().setAll(view);
```

---

### 9.2 Tag `<URL>` non reconnu dans les fichiers FXML

**Problème :** Toutes les vues FXML utilisaient `<URL value="@styles.css"/>` pour charger la feuille de style, mais le FXMLLoader levait une `LoadException : URL is not a valid type`.

**Solution :** Ajout de la déclaration d'import manquante en tête de chaque fichier FXML :
```xml
<?import java.net.URL?>
```

---

### 9.3 `<Insets>` placé directement comme enfant d'un VBox

**Problème :** Le FXML plaçait `<Insets top="20"/>` directement dans le `VBox` de la barre latérale, ce qui provoquait `Unable to coerce Insets to class javafx.scene.Node`.

**Solution :** Encapsulation dans l'élément de propriété `<padding>` :
```xml
<VBox>
    <padding><Insets top="20"/></padding>
    ...
</VBox>
```

---

### 9.4 Format incorrect de `mainClass` dans `pom.xml`

**Problème :** La configuration initiale `<mainClass>com.gestnotes/com.gestnotes.Main</mainClass>` (format module JPMS) produisait `ClassNotFoundException: com.gestnotes.com.gestnotes.Main`.

**Solution :** Le projet ne déclare pas de `module-info.java`, donc le format simple suffit :
```xml
<mainClass>com.gestnotes.Main</mainClass>
```

---

### 9.5 Répertoire `target/` appartenant à root après exécution Docker

**Problème :** Docker s'exécutant en root crée les fichiers de `target/` avec les droits root. Les compilations suivantes depuis le système hôte ou l'IDE échouaient avec `FileSystemException: Operation not permitted`.

**Solution :** Supprimer `target/` avec `sudo` pour permettre à Docker de le recréer :
```bash
sudo rm -rf target/
docker compose run --rm app mvn javafx:run
```

---

### 9.6 Autorisation X11 refusée pour le conteneur Docker

**Problème :** Le conteneur Docker ne pouvait pas ouvrir l'affichage graphique : `Authorization required, but no authorization protocol specified` — `Unable to open DISPLAY`.

**Solution :** Autoriser explicitement les connexions locales au serveur X11 avant de lancer le conteneur :
```bash
xhost +local:docker
docker compose run --rm app mvn javafx:run
```

---

### 9.7 Gestion des doublons de code module sans crash

**Problème :** La contrainte `UNIQUE` sur le code du module en base SQLite levait une `RuntimeException` non gérée, ce qui faisait planter l'application silencieusement.

**Solution :** Interception de l'exception et affichage d'un message utilisateur clair :
```java
} catch (RuntimeException e) {
    if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
        showError("Ce code de module existe déjà.");
    }
}
```

---

## 10. Conclusion

Le projet GestNotes répond à l'ensemble des exigences du cahier des charges :

- Interface graphique complète avec JavaFX 21 (TableView, DatePicker, ComboBox, Alert, etc.)
- Architecture MVC rigoureuse avec séparation totale des couches
- Persistance des données via SQLite avec requêtes préparées
- Authentification avec deux profils distincts et restrictions d'accès appliquées
- Calcul automatique des moyennes pondérées et des mentions
- Tableau de bord statistique de la classe
- Validations complètes de toutes les saisies utilisateur
- Déploiement reproductible via Docker

### Améliorations possibles

| Amélioration | Description |
|---|---|
| Exportation PDF | Générer le relevé de notes d'un étudiant en PDF avec iText ou Apache PDFBox |
| Filtrage avancé | Ajouter des filtres par filière, niveau et groupe dans la liste des étudiants |
| Hachage des mots de passe | Remplacer le stockage en clair par BCrypt ou SHA-256 |
| Graphiques | Utiliser les charts JavaFX pour visualiser la distribution des notes |
| Import CSV | Permettre l'import en masse d'étudiants depuis un fichier CSV |
| Thème sombre | Ajouter un thème sombre commutable via la feuille de style CSS |
