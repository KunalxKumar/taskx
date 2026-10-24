<p align="center">
  <img src="assets/logo.svg" width="140" alt="TaskX logo">
</p>

<h1 align="center">TaskX Cyber Security Suite</h1>

<p align="center">
  A Java Swing desktop security toolkit combining intrusion monitoring, alert management,
  process inspection, network scanning, and file-integrity verification.
</p>

## Modules

| Module | Purpose |
| --- | --- |
| Intrusion Detection | Collects simulated security events and applies rule-based analysis. |
| Event Viewer | Filters, searches, reviews and exports generated alerts. |
| Process Killer | Windows-oriented process inspection and termination utility. |
| Network Scanner | Multi-threaded TCP port scanning for authorized hosts with service/risk classification. |
| File Integrity Monitor | MD5, SHA-1 and SHA-256 hashing plus directory snapshot comparison. |

## Resilient Storage

TaskX attempts MySQL when the JDBC driver and local database are available. When MySQL is unavailable,
the application transparently uses `data/alerts.log` through `FileAlertStorage`, so the dashboard does not
fail simply because a database service is offline.

Optional database settings can be supplied with JVM properties:

```text
-Dtaskx.db.url=jdbc:mysql://127.0.0.1:3306/taskx
-Dtaskx.db.user=root
-Dtaskx.db.password=your_password
```

The MySQL connector remains an optional runtime dependency.

## Requirements

- JDK 8+
- Windows is recommended for the Process Killer module
- PowerShell for the included build/run scripts
- Optional: MySQL Connector/J when database-backed alert storage is desired
- Optional: JUnit 5 console launcher for running the test suite

## Build

```powershell
.\build.ps1
```

## Run

```powershell
.\run.ps1
```

The run script automatically includes `lib/mysql-connector-j-9.7.0.jar` when that file exists.

## Tests

Place the JUnit 5 console standalone JAR at:

```text
lib/junit-platform-console-standalone.jar
```

Then run:

```powershell
.\test.ps1
```

The suite covers file hashing and integrity states, local TCP scanning, and MySQL-to-file storage fallback.

## Network Scanner Safety

The scanner performs TCP connection checks only. Use it only against systems and networks you own or
have explicit authorization to assess. The dashboard's presets are intended for controlled defensive testing.

## Architecture

```text
MainDashboard
  ├── Intrusion Detection
  │     └── EventCollector -> Analyzer -> ResilientAlertStorage
  ├── Event Viewer
  │     └── ResilientAlertStorage
  ├── Process Killer
  ├── Network Scanner
  │     └── NetworkScanner -> TCP sockets
  └── File Integrity
        └── FileIntegrityChecker -> MessageDigest

ResilientAlertStorage
  ├── MySQL available  -> JdbcAlertStorage
  └── MySQL unavailable -> FileAlertStorage (data/alerts.log)
```

## Branding

The TaskX emblem is stored as a self-contained SVG at `assets/logo.svg` and is designed around a dark
slate shield, electric-cyan cyber core, circuit traces, radar sweep and Java-inspired ember accent.

## Project Structure

```text
taskx/
├── assets/
│   └── logo.svg
├── data/
├── src/
│   ├── Main.java
│   ├── backend/
│   ├── gui/
│   ├── model/
│   └── util/
├── test/
├── build.ps1
├── run.ps1
├── test.ps1
└── README.md
```

## Disclaimer

TaskX is an educational and defensive security toolkit. Only use the network and process-management
features in environments where you have permission to perform the relevant actions.
