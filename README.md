# 🧱 Tetris — JavaFX Edition

### *Stack it. Clear it. Don't top out.*

[![Java](https://img.shields.io/badge/Java-25+-ED8B00?style=for-the-badge\&logo=openjdk\&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-UI-0abab5?style=for-the-badge\&logo=java\&logoColor=white)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge\&logo=apachemaven\&logoColor=white)](https://maven.apache.org/)
[![Build Status](https://img.shields.io/badge/Build-Passing-3fb950?style=for-the-badge)]()
[![License](https://img.shields.io/badge/License-Academic-blue?style=for-the-badge)]()

A JavaFX implementation of the classic Tetris game, developed using object-oriented design principles. The project demonstrates abstraction, encapsulation, interfaces, inheritance, Java records, and modular game functionality.

---

## 📚 Table of Contents

* [🎮 Features](#-features)
* [🏗️ Architecture](#️-architecture)
* [🧰 Tech Stack](#-tech-stack)
* [🚀 Getting Started](#-getting-started)
* [📁 Project Structure](#-project-structure)
* [🔀 Git Workflow](#-git-workflow)
* [🏷️ Versioning and Tags](#️-versioning-and-tags)
* [👥 Team & Roles](#-team--roles)

---

## 🎮 Features

### Application

* 🎬 Splash screen with group and course information
* 🏠 Main menu with Play, Configuration, High Scores, and Exit options
* ⚙️ Configuration screen for game settings
* 🏆 High-score screen for viewing recorded scores
* 🚪 Exit confirmation dialog

### Gameplay

* 📐 Classic Tetris playfield
* 🧩 Seven Tetromino types: I, J, L, O, S, T, and Z
* ⬅️➡️⬇️ Piece movement controls
* 🔄 Piece rotation
* 🌊 Timer-driven automatic piece movement
* 🧹 Line clearing
* 🎯 Score tracking
* ⏸️ Pause and resume functionality using `P`
* 🎮 Game-over handling
* 🔁 Replay functionality

### Object-Oriented Design

* 🧩 `AbstractTetromino` provides a common abstraction for Tetromino pieces
* 🔌 `Movable` defines movement behaviour
* 📦 Java `record` types are used for value data (`Position`, `Block`, `ScoreEntry`, `OpMove`)
* 🔒 Encapsulation is used to protect game state
* 🏛️ Inheritance is used for the different Tetromino implementations

---

## 🏗️ Architecture

The project is organised as a **Model–View–Controller** application. Each layer sits in
its own package and depends only on the layer below it.

```text
          ┌──────────────────────────────┐
          │            view              │  JavaFX screens and rendering
          │  ScreenView, AbstractScreen  │  — draws, never decides
          │  GameScreen, BoardRenderer   │
          └───────┬──────────────▲───────┘
     reports      │              │  GameEvent
     user intent  │              │  (Observer)
          ┌───────▼──────────────┴───────┐
          │         controller           │  Clock, input routing, navigation
          │  GameController, InputHandler│  — decides, owns no rules
          │  ScreenNavigator, command/   │
          └───────────────┬──────────────┘
                          │ calls
          ┌───────────────▼──────────────┐
          │            model             │  Rules and state. No JavaFX import
          │  GameModel, Board, Score     │  anywhere in this package, so it is
          │  tetromino/, state/, observer/│  unit-testable without a UI.
          └──────────────────────────────┘
```

Supporting packages: `player/` (Human / AI / External strategies),
`service/` (singletons for config, scores and audio, over a generic JSON repository)
and `network/` (the `TetrisServer.jar` client).

### Patterns in use

| Pattern       | Where                                                          |
| ------------- | -------------------------------------------------------------- |
| MVC           | `model` / `view` / `controller` packages                        |
| Singleton     | `ConfigService`, `HighScoreService`, `AudioManager`              |
| Factory       | `TetrominoFactory`, `PlayerFactory`, `CommandFactory`            |
| Observer      | `Observable` / `GameObserver` — model notifies views             |
| State         | `GameState` → `Ready` / `Running` / `Paused` / `GameOver`         |
| Command       | `Command` + one class per player action                          |
| Strategy      | `Player` → `HumanPlayer` / `AIPlayer` / `ExternalPlayer`         |
| Facade        | `ScreenNavigator` over the JavaFX `Stage`, `AudioManager` over media |
| Template Method | `AbstractScreen.getRoot()`, `AbstractTetromino.initialiseShape()` |

---

## 🧰 Tech Stack

| Component               | Technology                               |
| ----------------------- | ---------------------------------------- |
| Programming Language    | ☕ Java 25+                               |
| User Interface          | 🎨 JavaFX                                |
| Build Tool              | 📦 Maven                                 |
| JSON Persistence        | 🗂️ Jackson Databind                      |
| Unit Testing            | 🧪 JUnit 5 + Mockito                     |
| Coverage                | 📈 JaCoCo                                |
| Version Control         | 🐙 Git                                   |
| Repository Hosting      | 🐙 GitHub                                |
| Development Environment | 💻 IntelliJ IDEA                         |
| Testing/Review          | 🔍 GitHub Pull Requests and Code Reviews |

---

## 🚀 Getting Started

### Prerequisites

Make sure the following are installed:

* ☕ JDK 25 or later (any distribution — also all that's needed to run the packaged jar)
* 📦 Maven 3.8 or later
* 💻 IntelliJ IDEA or another Java IDE
* 🐙 Git

### Clone the Repository

```bash
git clone https://github.com/BivekChaudhary876/tetris_game.git
```

Navigate into the project:

```bash
cd tetris_game
```

### Run the Application

Using Maven:

```bash
mvn clean javafx:run
```

The application should start with the splash screen and then display the main menu.

#### Running the Milestone 1 build

While the gameplay is being migrated into the MVC layers, the Milestone 1 application
is still available under the `legacy` profile, so the two can be compared side by side:

```bash
mvn javafx:run -Plegacy     # Milestone 1 build  (au.edu.Griffith.legacy.Main)
mvn javafx:run              # new MVC build      (au.edu.Griffith.TetrisApp)
```

No edit to `pom.xml` is needed to switch — the profile overrides the `app.mainClass`
property. Both the profile and the `legacy` package are removed once migration is complete.

### Run from IntelliJ IDEA

1. Open the `tetris_game` folder in IntelliJ IDEA.
2. Allow IntelliJ to import the Maven project.
3. Allow Maven dependencies to finish downloading.
4. Locate `Main.java`.
5. Run the application using the IntelliJ Run configuration.

### Build the Runnable Jar

The game is packaged as a single `TetrisJava.jar`, run with `java -jar`.

It is a **self-contained (fat) jar**: it contains our classes, resources (audio, CSS,
images), Jackson and JavaFX, including JavaFX's native libraries for Windows, Linux and
macOS. The same file therefore runs on all three.

**Requirement to run it:** any JDK 25 or later (Oracle, Temurin, …). No JavaFX
installation is needed. The console shows a harmless
*"Unsupported JavaFX configuration: classes were loaded from 'unnamed module'"* warning,
because JavaFX is loaded from the jar rather than as modules.

The jar's entry point is `au.edu.Griffith.Launcher`, a plain class that calls
`TetrisApp.main`. It exists because `java -jar` refuses to start a main class that
extends `javafx.application.Application` when JavaFX is on the classpath.

**Build** (from the project root):

```bash
mvn clean package
```

Or, in IntelliJ: Maven tool window → **Lifecycle** → run `clean`, then `package`.
Add `-DskipTests` to the command to skip the test suite.

**Run:**

```bash
java -jar target/TetrisJava.jar
```

Run it from the folder that should hold the game's data: `data/config.json` and
`JavaTetrisScore.json` are read and written relative to the current directory, so
running from the project root reuses your existing settings and scores.

For a submission, build from an up-to-date `main` and copy `TetrisJava.jar` out of
`target/` — it is deleted on every `clean`. The jar is a build output and is
intentionally not committed (see [Ignored Files](#ignored-files)).

---

## 📁 Project Structure

The repository contains the Java source code, resources, Maven configuration, documentation, and Git configuration.

<details>
<summary><strong>Click to expand full file tree</strong></summary>

```text
tetris_game/
├── src/
│   ├── main/
│   │   ├── java/au/edu/Griffith/
│   │   │   ├── TetrisApp.java              🚪 Application entry point; wires the layers together
│   │   │   │
│   │   │   ├── model/                      🧠 MODEL — rules and state, zero JavaFX
│   │   │   │   ├── GameModel.java          🎯 One field: board + score + state; the Observer subject
│   │   │   │   ├── Board.java              📐 Grid, collision checks, line clearing
│   │   │   │   ├── Score.java              🎯 Points, level and lines-erased rules
│   │   │   │   ├── HighScoreTable.java     🏆 Top-ten ranking rules
│   │   │   │   ├── GameConfig.java         ⚙️ Settings, as persisted to config.json
│   │   │   │   ├── Movable.java            🔌 Movement contract implemented by GameModel
│   │   │   │   ├── GameStatus.java         🏷️ enum READY / RUNNING / PAUSED / GAME_OVER
│   │   │   │   ├── PlayerType.java         🏷️ enum HUMAN / AI / EXTERNAL
│   │   │   │   ├── Position.java           📦 record — a cell coordinate
│   │   │   │   ├── Block.java              📦 record — a locked cell
│   │   │   │   ├── ScoreEntry.java         📦 record — one high-score row (Comparable)
│   │   │   │   ├── tetromino/              🧩 The seven pieces, their enum and their factory
│   │   │   │   ├── state/                  🔄 State pattern: Ready / Running / Paused / GameOver
│   │   │   │   └── observer/               📡 Observer pattern: Observable, GameObserver, GameEvent
│   │   │   │
│   │   │   ├── view/                       🎨 VIEW — JavaFX only; draws, never decides
│   │   │   │   ├── ScreenView.java         🔌 Contract every screen implements
│   │   │   │   ├── AbstractScreen.java     🏛️ Shared stylesheet and build sequence
│   │   │   │   ├── SplashScreen.java       🎬 Group and course details
│   │   │   │   ├── MainMenuScreen.java     🏠 Play / Configuration / High Scores / Exit
│   │   │   │   ├── ConfigurationScreen.java ⚙️ Settings controls
│   │   │   │   ├── HighScoreScreen.java    🏆 Top-ten table
│   │   │   │   ├── GameScreen.java         🎮 One or two fields; observes the models
│   │   │   │   ├── BoardRenderer.java      🖌️ Canvas painting; the only TetrominoType → Color step
│   │   │   │   └── SidePanel.java          📊 Player type, level, score, next piece
│   │   │   │
│   │   │   ├── controller/                 🎛️ CONTROLLER — clock, input, navigation
│   │   │   │   ├── GameController.java     ⏱️ Session clock; drives models and players
│   │   │   │   ├── ScreenNavigator.java    🧭 Facade over the Stage; owns window sizing
│   │   │   │   ├── InputHandler.java       ⌨️ Key → action maps, one per player
│   │   │   │   ├── MainMenuController.java 🏠 Menu use cases
│   │   │   │   ├── ConfigurationController.java ⚙️ Draft-edit and save settings
│   │   │   │   ├── HighScoreController.java 🏆 Table rows, clear, record new score
│   │   │   │   └── command/                📜 Command pattern: one class per player action
│   │   │   │
│   │   │   ├── player/                     🕹️ Strategy: Human / AI / External + PlayerFactory
│   │   │   ├── service/                    💾 Singletons over a generic JSON Repository<T>
│   │   │   ├── network/                    🌐 TetrisServer client (localhost:3000)
│   │   │   └── legacy/                     ⚠️ Milestone 1 code — delete once fully migrated
│   │   │
│   │   └── resources/
│   │       ├── css/tetris.css              🎨 Shared stylesheet (replaces inline -fx- strings)
│   │       └── splash-image.png            🖼️ Splash artwork
│   │
│   └── test/java/au/edu/Griffith/          🧪 JUnit 5, parameterized tests, stubs and Mockito mocks
│
├── data/                                   💾 config.json and scores.json (generated, git-ignored)
├── .gitignore                              🚫 Ignored IDE, build and runtime files
├── pom.xml                                 📦 Maven: JavaFX, Jackson, JUnit, Mockito, JaCoCo
└── README.md                               📖 Project documentation
```

</details>

### Ignored Files

The following generated or IDE-specific files are intentionally excluded from the repository:

```text
.idea/
target/
*.iml
*.class
data/
JavaTetrisScore.json
TetrisServer.jar
TetrisJava.jar
```

These files are generated locally by IntelliJ IDEA or Maven and are not required to build the project from source. `data/` holds the `config.json` and `scores.json` written at runtime, so each machine keeps its own settings and scores.

---

### Note for team members

Because `JavaTetrisScore.json` is ignored, it will not arrive with a clone or pull.
Create it yourself in the project root, beside `pom.xml`, and paste in:

```json
{
  "scores" : [ ]
}
```

The game also writes this file itself the first time a score is recorded, so an
empty table is a valid starting state either way.

`TetrisServer.jar` also has to be copied in manually — get it from the course site
and place it in the project root. The external player needs it running on
`localhost:3000`.

## 🔀 Git Workflow

The project follows a feature-branch and Pull Request workflow.

```text
Create Feature Branch
        │
        ▼
   Develop Feature
        │
        ▼
      Commit
        │
        ▼
    Push Branch
        │
        ▼
  Create Pull Request
        │
        ▼
   Code Review
        │
        ▼
   PR Approval
        │
        ▼
   Merge into main
```

### Development Workflow

1. Update the local `main` branch.

```bash
git checkout main
git pull origin main
```

2. Create a feature branch.

```bash
git checkout -b feature/feature-name
```

3. Make and test the required changes.

4. Commit the changes using a clear commit message.

```bash
git add .
git commit -m "Add feature description"
```

5. Push the feature branch.

```bash
git push -u origin feature/feature-name
```

6. Create a Pull Request from the feature branch into `main`.

7. A team member reviews the Pull Request.

8. Address any requested changes.

9. Merge the approved Pull Request into `main`.

---

## 🏷️ Versioning and Tags

Git tags are used to identify important project milestones and finalized versions.

The project can use annotated tags such as:

```text
milestone-1-submission
milestone-2-submission
final-submission
```

Tags should be created on the finalized `main` branch after the relevant milestone has been completed and reviewed.

Example:

```bash
git checkout main
git pull origin main
git tag -a milestone-1-submission -m "Milestone 1 submission"
git push origin milestone-1-submission
```

Tags provide a stable reference to the exact version of the project submitted for each milestone.

---

## 👥 Team & Roles

| Team Member                   | Role                |
| ----------------------------- | ------------------- |
| 🧑‍💻 **Aries Dave Bantigue** | Programmer          |
| ✍️ **Anshumaan Saraf**        | Documentation Lead  |
| 🗂️ **Bivek Chaudhary**       | Project Manager     |
| 🏛️ **Dean Busooa**           | Architect           |
| 🧪 **Liz Mary Tharian**       | Tester & Programmer |

---

## 📌 Project Information

**Project:** Tetris — JavaFX Edition
**Repository:** `tetris_game`
**Language:** Java
**Framework:** JavaFX
**Build System:** Maven
**Version Control:** Git + GitHub

---

## 📖 Academic Project

This project was developed as part of the **Object Oriented Software Development** course and demonstrates the application of object-oriented programming concepts and collaborative software development practices.

---
