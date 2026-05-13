# CLAUDE.md — GestNotes JavaFX Project Blueprint

## What you must build
A complete JavaFX desktop application called **GestNotes** for managing students, modules and grades.
This is a university assignment. Build the **entire working project** from scratch.
Do NOT ask for confirmation between files — generate everything listed here.

---

## Tech stack
| Item | Value |
|------|-------|
| Language | Java 21 |
| GUI | JavaFX 21 (OpenJFX) via Maven |
| Build tool | Maven |
| Database | SQLite via `sqlite-jdbc` |
| Architecture | MVC (Model / Controller / DAO) |
| FXML | Yes — each view has its own `.fxml` file |

---

## Project structure to generate

```
gestnotes/
├── pom.xml
└── src/
    └── main/
        ├── java/com/gestnotes/
        │   ├── Main.java
        │   ├── model/
        │   │   ├── Etudiant.java
        │   │   ├── Module.java
        │   │   ├── Note.java
        │   │   └── Utilisateur.java
        │   ├── dao/
        │   │   ├── DatabaseConnection.java
        │   │   ├── EtudiantDAO.java
        │   │   ├── ModuleDAO.java
        │   │   ├── NoteDAO.java
        │   │   └── UtilisateurDAO.java
        │   └── controller/
        │       ├── ConnexionController.java
        │       ├── MainController.java
        │       ├── EtudiantController.java
        │       ├── ModuleController.java
        │       └── NoteController.java
        └── resources/com/gestnotes/
            ├── connexion.fxml
            ├── main.fxml
            ├── etudiant.fxml
            ├── module.fxml
            └── note.fxml
```

---

## pom.xml — required exact content

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.gestnotes</groupId>
  <artifactId>gestnotes</artifactId>
  <version>1.0</version>

  <properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <javafx.version>21.0.2</javafx.version>
  </properties>

  <dependencies>
    <dependency><groupId>org.openjfx</groupId><artifactId>javafx-controls</artifactId><version>${javafx.version}</version></dependency>
    <dependency><groupId>org.openjfx</groupId><artifactId>javafx-fxml</artifactId><version>${javafx.version}</version></dependency>
    <dependency><groupId>org.xerial</groupId><artifactId>sqlite-jdbc</artifactId><version>3.45.1.0</version></dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-maven-plugin</artifactId>
        <version>0.0.8</version>
        <configuration>
          <mainClass>com.gestnotes/com.gestnotes.Main</mainClass>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

---

## Models

### Etudiant.java
Fields (all private, with getters/setters):
- `int id`
- `String nom`
- `String prenom`
- `String cin`       — must be unique
- `String email`     — must contain @
- `String telephone` — digits only
- `String dateNaissance`
- `String niveau`    — e.g. L1, L2, L3, M1, M2
- `String filiere`   — e.g. INFO, MATH, PHYS
- `String groupe`    — e.g. A, B, C

### Module.java
Fields:
- `int id`
- `String code`         — unique identifier
- `String nom`
- `double coefficient`  — must be > 0
- `String enseignant`

### Note.java
Fields:
- `int id`
- `int etudiantId`
- `int moduleId`
- `double noteCC`       — contrôle continu, 0–20
- `double noteExamen`   — 0–20
- `double moyenneModule` — calculated: noteCC * 0.4 + noteExamen * 0.6

Calculated fields (not stored):
- `String mention` — derived from moyenneModule

Mention rules:
```
< 10       → Ajourné
10 – 11.99 → Passable
12 – 13.99 → Assez bien
14 – 15.99 → Bien
>= 16      → Très bien
```

### Utilisateur.java
Fields:
- `int id`
- `String username`
- `String password`  — store as plain text for this project (no need to hash)
- `String role`      — "ADMIN" or "ENSEIGNANT"

---

## Database (SQLite)

### DatabaseConnection.java
- Returns a singleton `Connection` to `gestnotes.db` in the working directory.
- On first connection, call `initDB()` which creates all tables if they don't exist.
- Also insert default users in `initDB()` if the users table is empty:
  - admin / admin123 / ADMIN
  - enseignant / ens123 / ENSEIGNANT

### SQL schema

```sql
CREATE TABLE IF NOT EXISTS etudiant (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nom TEXT NOT NULL,
  prenom TEXT NOT NULL,
  cin TEXT UNIQUE NOT NULL,
  email TEXT,
  telephone TEXT,
  date_naissance TEXT,
  niveau TEXT,
  filiere TEXT,
  groupe TEXT
);

CREATE TABLE IF NOT EXISTS module (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  code TEXT UNIQUE NOT NULL,
  nom TEXT NOT NULL,
  coefficient REAL NOT NULL,
  enseignant TEXT
);

CREATE TABLE IF NOT EXISTS note (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  etudiant_id INTEGER NOT NULL,
  module_id INTEGER NOT NULL,
  note_cc REAL,
  note_examen REAL,
  moyenne_module REAL,
  FOREIGN KEY (etudiant_id) REFERENCES etudiant(id),
  FOREIGN KEY (module_id) REFERENCES module(id),
  UNIQUE(etudiant_id, module_id)
);

CREATE TABLE IF NOT EXISTS utilisateur (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT UNIQUE NOT NULL,
  password TEXT NOT NULL,
  role TEXT NOT NULL
);
```

---

## DAO Layer

Each DAO receives the `Connection` from `DatabaseConnection.getInstance()`.

### EtudiantDAO.java
Methods:
- `List<Etudiant> findAll()`
- `void add(Etudiant e)`
- `void update(Etudiant e)`
- `void delete(int id)`
- `List<Etudiant> search(String keyword)` — searches nom, prenom, cin, filiere with LIKE %keyword%
- `boolean cinExists(String cin)` — for uniqueness check
- `boolean cinExistsExcept(String cin, int id)` — for update check

### ModuleDAO.java
Methods:
- `List<Module> findAll()`
- `void add(Module m)`
- `void update(Module m)`
- `void delete(int id)`

### NoteDAO.java
Methods:
- `List<Note> findByEtudiant(int etudiantId)`
- `void saveOrUpdate(Note n)` — INSERT OR REPLACE
- `double getMoyenneGenerale(int etudiantId)` — weighted average using module coefficients

### UtilisateurDAO.java
Methods:
- `Utilisateur findByUsernameAndPassword(String username, String password)`

---

## Controllers

### ConnexionController.java
FXML id bindings:
- `usernameField` (TextField)
- `passwordField` (PasswordField)
- `loginButton` (Button)
- `errorLabel` (Label)

Logic:
- On login button click: call `UtilisateurDAO.findByUsernameAndPassword()`
- If null → show errorLabel "Identifiants incorrects"
- If found → store user in a static `Session.currentUser` field, load `main.fxml` in a new Stage

### Session.java (utility class)
```java
public class Session {
    public static Utilisateur currentUser;
}
```

### MainController.java
FXML: sidebar buttons or menu items for:
- Gestion Étudiants → load `etudiant.fxml` in center pane
- Gestion Modules → load `module.fxml`
- Gestion Notes → load `note.fxml`
- Déconnexion → close window, reopen connexion.fxml

Show the logged-in user's name and role in the header.
If role is ENSEIGNANT, disable "Gestion Modules" button.

### EtudiantController.java
FXML bindings:
- TextFields: `nomField`, `prenomField`, `cinField`, `emailField`, `telephoneField`, `dateNaissanceField`
- ComboBoxes: `niveauCombo` (L1/L2/L3/M1/M2), `filiereCombo` (INFO/MATH/PHYS/ELEC), `groupeCombo` (A/B/C)
- Buttons: `ajouterBtn`, `modifierBtn`, `supprimerBtn`, `reinitialiserBtn`
- TextField: `rechercheField`
- TableView: `etudiantTable` with columns for all fields

Logic:
- Load all students into table on initialize
- Clicking a table row populates form fields
- `ajouterBtn`: validate → add → refresh table → clear form
- `modifierBtn`: validate → update selected → refresh → clear
- `supprimerBtn`: confirm with Alert → delete → refresh
- `rechercheField`: on key release, call search DAO and update table
- Validation: no empty fields, email contains @, phone is digits only, CIN unique

### ModuleController.java
FXML bindings:
- TextFields: `codeField`, `nomField`, `coefficientField`, `enseignantField`
- Buttons: `ajouterBtn`, `modifierBtn`, `supprimerBtn`, `reinitialiserBtn`
- TableView: `moduleTable` with all 4 columns

Same CRUD pattern as EtudiantController.
Validation: coefficient must parse as double > 0.

### NoteController.java
FXML bindings:
- ComboBox: `etudiantCombo` (shows "Prenom Nom - CIN"), `moduleCombo` (shows "Code - Nom")
- TextFields: `noteCCField` (0–20), `noteExamenField` (0–20)
- Button: `calculerBtn`, `sauvegarderBtn`
- Labels: `moyenneLabel`, `mentionLabel`, `moyenneGeneraleLabel`
- TableView: `notesTable` with columns: Module, Note CC, Note Examen, Moyenne, Mention

Logic:
- On etudiantCombo select: load all notes for that student into table, update moyenneGeneraleLabel
- On moduleCombo select: if a note already exists for this student+module, pre-fill CC and exam fields
- `calculerBtn`: compute moyenne = noteCC*0.4 + noteExamen*0.6, show in moyenneLabel + mentionLabel
- `sauvegarderBtn`: validate fields (0–20), save Note via DAO, refresh table, update moyenneGenerale

---

## FXML files

### connexion.fxml
Layout: centered VBox with:
- App title label "GestNotes"
- TextField for username
- PasswordField for password
- Button "Se connecter"
- Red Label for error message (initially hidden/empty)
Use CSS styling: background color #2c3e50, white text, styled button.

### main.fxml
Layout: BorderPane
- LEFT: VBox sidebar with buttons (Étudiants, Modules, Notes, Déconnexion) — dark background
- TOP: HBox header with app name + logged user info
- CENTER: StackPane `contentArea` where sub-views are loaded dynamically

### etudiant.fxml
Layout: VBox
- TOP: HBox with title + search TextField
- LEFT: GridPane form (all student fields, ComboBoxes for niveau/filiere/groupe)
- RIGHT: VBox with buttons (Ajouter, Modifier, Supprimer, Réinitialiser)
- BOTTOM: TableView with all student columns

### module.fxml
Layout: VBox
- GridPane form (code, nom, coefficient, enseignant)
- Buttons row (Ajouter, Modifier, Supprimer, Réinitialiser)
- TableView

### note.fxml
Layout: VBox
- TOP: HBox — etudiantCombo + moduleCombo
- MIDDLE: HBox — noteCCField + noteExamenField + Calculer button + result labels
- Label: "Moyenne générale: X.XX — Mention: Y"
- BOTTOM: TableView of all notes for selected student

---

## Main.java

```java
public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestnotes/connexion.fxml"));
        Scene scene = new Scene(loader.load(), 500, 350);
        stage.setTitle("GestNotes — Connexion");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}
```

---

## Validation rules (implement in controllers)

| Field | Rule |
|-------|------|
| Required fields | Show Alert.ERROR if empty |
| CIN | Unique — check with DAO before insert; check excluding self on update |
| Email | Must contain "@" |
| Telephone | `telephone.matches("[0-9]+")` |
| Notes | `value >= 0 && value <= 20` — parse as double, catch NumberFormatException |
| Coefficient | Parse as double, must be > 0 |

For all validation errors: use `Alert(Alert.AlertType.ERROR)` with clear message.
For delete confirmation: use `Alert(Alert.AlertType.CONFIRMATION)`.

---

## Styling

Add a `styles.css` in resources and load it in each FXML:
```css
.root { -fx-font-family: "Segoe UI", Arial, sans-serif; -fx-font-size: 13px; }
.sidebar { -fx-background-color: #2c3e50; }
.sidebar-btn { -fx-text-fill: white; -fx-background-color: transparent; -fx-cursor: hand; }
.sidebar-btn:hover { -fx-background-color: #34495e; }
.header { -fx-background-color: #3498db; -fx-padding: 10; }
.header-title { -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; }
TableView { -fx-selection-bar: #3498db; }
.btn-primary { -fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; }
.btn-danger  { -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; }
```

---

## Build and run commands (inside Docker container)

```bash
# First build (downloads dependencies)
mvn clean compile

# Run the app (needs X11 display)
mvn javafx:run

# Package as fat jar (optional)
mvn clean package
```

---

## Generation order

Generate files in this exact order so dependencies are always satisfied:
1. `pom.xml`
2. `src/main/java/com/gestnotes/model/` — all 4 model classes
3. `src/main/java/com/gestnotes/Session.java`
4. `src/main/java/com/gestnotes/dao/DatabaseConnection.java`
5. `src/main/java/com/gestnotes/dao/` — all 4 DAO classes
6. `src/main/resources/com/gestnotes/styles.css`
7. `src/main/resources/com/gestnotes/connexion.fxml`
8. `src/main/resources/com/gestnotes/main.fxml`
9. `src/main/resources/com/gestnotes/etudiant.fxml`
10. `src/main/resources/com/gestnotes/module.fxml`
11. `src/main/resources/com/gestnotes/note.fxml`
12. `src/main/java/com/gestnotes/controller/ConnexionController.java`
13. `src/main/java/com/gestnotes/controller/MainController.java`
14. `src/main/java/com/gestnotes/controller/EtudiantController.java`
15. `src/main/java/com/gestnotes/controller/ModuleController.java`
16. `src/main/java/com/gestnotes/controller/NoteController.java`
17. `src/main/java/com/gestnotes/Main.java`

After generating all files, run `mvn clean compile` and fix any compilation errors before stopping.
