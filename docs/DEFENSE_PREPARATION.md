# Defense Preparation — Build Your Setup

## Demo flow (about 3–4 minutes)
1. Start from project root: `java -cp out setup.web.WebServer` and open `http://localhost:8080`.
2. Open the seeded Gaming Rig and show selected CPU, motherboard, RAM, GPU, storage and PSU.
3. Toggle an upgrade (RGB / assembly / warranty) and explain Decorator changes the price composition.
4. Switch Standard → Student → Volume and compare totals (Strategy).
5. Explain compatibility messages and Activity log (Observer: CompatibilityWatcher + ActivityLog).
6. Save/export the build report; place an order and show its status; cancel it.
7. Mention that builds/orders are file-persisted and survive restart.

## Pattern explanation in one sentence + code location
- **Builder** — `src/setup/domain/PcBuild.java`: assembles immutable PcBuild step by step and validates at `build()`.
- **Factory Method** — `src/setup/domain/ComponentFactory.java`: common validation in `create()`, concrete factories override `make()` for component types.
- **Decorator** — `src/setup/pricing/Upgrades.java`: wraps `PriceItem` to add independent upgrade costs/description.
- **Facade** — `src/setup/app/SetupFacade.java`: single API hides catalog, repositories, checker, calculator and event bus from web layer.
- **Strategy** — `src/setup/pricing/PricingStrategy.java`: Standard, Student and Volume algorithms can be switched at runtime.
- **Observer** — `src/setup/events/BuildEventBus.java`: publishes changes to subscribers; ActivityLog and CompatibilityWatcher react.

## Likely instructor questions
- Why Builder? It avoids partially constructed configurations and validates at the construction boundary.
- Why Factory Method? Creation details for component subtypes are separated from callers; common validation is centralized.
- Why Decorator? Multiple upgrades can be combined without a growing set of subclasses for every combination.
- Why Strategy? Pricing algorithms can be switched without changing PriceCalculator.
- Why Observer? Event publishers do not need to know every listener.
- Why Facade? WebServer uses a small application API rather than coordinating many internal services.
- What are limitations? Compatibility rules are currently limited (socket and PSU power); catalog is static; storage is simple text-file persistence; no authentication; UI is a compact single-page view.

## Live coding ideas (practice, do not just memorize)
- Add a new pricing strategy implementing `PricingStrategy`, register it in `all()` and explain how it is selected.
- Add a new upgrade decorator and its code/label mapping.
- Add a new `BuildListener` and subscribe it to `BuildEventBus`.
- Add a catalog component and show it in the UI.

## Before defense
- Replace Student A/B/C placeholders with real names and actual contributions.
- Confirm each person can explain and modify at least two patterns independently.
- Run the automated tests on the defense machine.
- Check browser UI end-to-end, including report export and order cancellation.
- Update AI disclosure truthfully to match course policy.
