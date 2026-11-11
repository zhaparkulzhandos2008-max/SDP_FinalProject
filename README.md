# Build Your Setup

PC configurator: choose components, add upgrades, select a pricing strategy, view totals and compatibility warnings, export a text report, and place/cancel orders.

## Requirements
- JDK 17 or newer (JDK 21 recommended)
- Windows: `run.bat` / `test.bat`
- macOS/Linux: `./run.sh` / `./test.sh`

## Run locally (Windows)
1. Install JDK 17+ and ensure `java` and `javac` are on PATH.
2. Double-click `run.bat` (or run it from a terminal).
3. Open **http://localhost:8080** in your browser.
4. Keep the terminal open while using the app. Press Ctrl+C to stop.

## Run locally (macOS/Linux)
```bash
./run.sh
```
Then open http://localhost:8080.

**Important:** Open the app through `http://localhost:8080`, not by double-clicking `web/index.html`. The API is served by Java; opening the HTML as a `file://` URL will cause API errors/404.

## Automated tests
Windows: `test.bat`
macOS/Linux:
```bash
./test.sh
```
The test runner contains 10 scenarios.

## Persistence
Data is stored in `data/builds.txt` and `data/builds.orders.txt`. Files are created automatically. Keep backups if you want to preserve local demo data.

## Architecture
`domain` (model and rules) · `pricing` · `events` · `storage` (catalog and file repositories) · `app` (facade) · `web` (HTTP API) · `web/index.html` (UI).

## Design patterns (6)
| Pattern | Type | Location | Purpose |
|---|---|---|---|
| Factory Method | Creational | `domain/ComponentFactory` | Create validated component types |
| Builder | Creational | `domain/PcBuild.Builder` | Assemble and validate a build |
| Decorator | Structural | `pricing/Upgrades` | Combine price upgrades |
| Facade | Structural | `app/SetupFacade` | Simple API for the UI layer |
| Strategy | Behavioral | `pricing/PricingStrategy` | Switch discount calculation |
| Observer | Behavioral | `events/BuildEventBus` | Notify activity and compatibility listeners |

## Documentation
See `docs/` for the report, UML diagrams, test scenarios, and defense notes. Review team member names and AI-use disclosure before submission.
