# GestNotes — JavaFX Student Management App

Java 21 + JavaFX 21 + SQLite desktop app for managing students, modules and grades.

## Requirements

- Docker + Docker Compose
- Linux with X11 (for GUI display)

## Run

```bash
# 1. Allow Docker to use your display (once per session)
xhost +local:docker

# 2. Build the image (first time only)
docker compose build

# 3. Launch the app
docker compose run --rm app mvn javafx:run
```

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

## Data

The SQLite database (`gestnotes.db`) is created automatically on first run in the project directory.
