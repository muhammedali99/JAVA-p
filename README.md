# GestNotes — JavaFX Student Management App

Java 21 + JavaFX 21 + SQLite desktop app for managing students, modules and grades.

## Requirements

- Java 21 JDK — download from [adoptium.net](https://adoptium.net) or [oracle.com](https://www.oracle.com/java/technologies/downloads/)
- Apache Maven 3.8+ — download from [maven.apache.org](https://maven.apache.org/download.cgi)

## Run

### Windows

```bat
run.bat
```

Or manually:

```bat
mvn javafx:run
```

### Linux / macOS

```bash
./run.sh
```

Or manually:

```bash
mvn javafx:run
```

> The SQLite database (`gestnotes.db`) is created automatically on first run in the project directory.

## Login credentials

| Username | Password | Role |
|---|---|---|
| admin | admin123 | Administrator (full access) |
| enseignant | ens123 | Teacher (read-only students, can edit notes) |

## Features

- **Étudiants** — Add, edit, delete and search students
- **Modules** — Manage course modules with coefficients
- **Notes** — Enter CC + exam grades, auto-calculate averages and mentions
- **Statistiques** — Class overview: total students, class average, best score, admitted/failed list

## Docker (Linux with X11 only)

```bash
xhost +local:docker
docker compose build
docker compose run --rm app mvn javafx:run
```
