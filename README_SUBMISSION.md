# Build Your Setup — Submission README

## Overview
A Java PC configurator with browser GUI. Users select six PC component types, check compatibility, apply optional upgrades, choose a pricing strategy, save builds, place/cancel orders and export a text build report.

## Requirements
- Java 17 or newer
- Browser (Chrome/Edge/Firefox)
- No external Java libraries

## Run
From the project root:
```bash
javac -d out $(find src -name "*.java")
java -cp out setup.web.WebServer
```
Open **http://localhost:8080**.

## Tests
```bash
javac -d out $(find src test -name "*.java")
java -cp out setup.TestRunner
```
Expected: 10 automated scenarios PASS (see `docs/TEST_SCENARIOS.md`).

## Sample data and persistence
The application seeds sample builds if the build data file is empty. Builds and orders are stored in:
- `data/builds.txt`
- `data/builds.orders.txt`

Files are created automatically at runtime. Do not delete them while the application is running.

## Main user journey
1. Select or create a build.
2. Select CPU, motherboard, RAM, GPU, storage and PSU.
3. Add optional upgrades.
4. Choose Standard, Student or Volume pricing.
5. Review compatibility and total.
6. Export report or place order; cancel an order if needed.

## Patterns
Builder (`PcBuild.Builder`), Factory Method (`ComponentFactory`), Decorator (`Upgrades`), Facade (`SetupFacade`), Strategy (`PricingStrategy`), Observer (`BuildEventBus`).

## Project documentation
- `docs/Build_Your_Setup_Final_Report.pdf` — report
- `docs/class-diagram.png` and `.mmd` — class diagram
- `docs/sequence-diagram.png` and `.mmd` — sequence diagram
- `docs/TEST_SCENARIOS.md` — test cases and results
- `docs/screenshot-main.png` — illustrative interface screenshot for report
- `docs/DEFENSE_PREPARATION.md` — demo flow and defense notes

## Team / AI disclosure — REQUIRED TO COMPLETE
- Replace Student A/B/C with actual names and actual contributions in report and README.
- Verify the AI-use disclosure is accurate and complete according to course policy.
- Add GitHub/GitLab repository URL after you create it.
