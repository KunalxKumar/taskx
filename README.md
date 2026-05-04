# Cyber Security Suite

Cyber Security Suite is a Java Swing-based desktop application designed to provide essential security monitoring and process management tools. It includes an Intrusion Detection System (IDS), an Event Viewer to inspect security alerts, and a Windows Process Killer to manage and terminate system processes.

## Features

- **Intrusion Detection System (IDS)**: Runs a background monitoring engine that captures real-time system events, analyzes them for malicious patterns, and stores alerts.
- **Event Viewer**: A graphical interface that allows users to review the alerts triggered by the IDS, complete with details about the anomalous events.
- **Process Killer**: A task manager-like utility tailored for Windows. It provides a real-time list of running processes along with their memory usage, and allows users to forcibly terminate suspicious or unresponsive processes.

## Technologies Used

- **Language**: Java
- **GUI Framework**: Java Swing / AWT
- **Concurrency**: Multithreading with `LinkedBlockingQueue` for real-time event processing.
- **Data Storage**: File-based alert logging.

## Prerequisites

- **Java Development Kit (JDK)**: Version 8 or higher.
- **Operating System**: Windows (required for the Process Killer module functionality).

## Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Kunalxkumar/taskx.git
   cd taskx
   ```

2. **Compile the project:**
   Make sure to compile the source code from the `src` directory into a `bin` directory (or use an IDE like IntelliJ / Eclipse).
   ```bash
   javac -d bin src/**/*.java
   ```

3. **Run the application:**
   ```bash
   java -cp bin Main
   ```

## Project Structure

```
taskx/
│
├── src/
│   ├── Main.java               # Application entry point
│   ├── gui/                    # Graphical User Interface components
│   │   ├── MainDashboard.java
│   │   ├── EventViewerGUI.java
│   │   └── ProcessKillerGUI.java
│   ├── backend/                # Background services (IDS, Storage, Analyzers)
│   ├── model/                  # Data models (Events, Alerts, Process Info)
│   └── util/                   # Utility classes
│
└── data/                       # Logs and event storage (e.g., alerts.log)
```
