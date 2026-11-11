# Test scenarios

All 10 scenarios are automated in `test/setup/TestRunner.java`
(`java -cp out setup.TestRunner`). Actual results are from the run of 11 Nov 2026 on Java 21: **10/10 PASS**.
Reference parts: R5 = Ryzen 5 7600 ($229), B650 board ($199), 32GB RAM ($109), RTX 4060 ($299), 1TB SSD ($89), 550W PSU ($65) → base price $990.00.

| # | Scenario (type) | Input | Expected result | Actual result | Status |
|---|---|---|---|---|---|
| 1 | Main successful journey, Decorator | Create build "T"; select R5, B650, 32GB RAM, RTX 4060, 1TB SSD, 550W PSU; then add "Professional assembly" and "Extended warranty" | Base $990.00; with assembly $1040.00; warranty +10% of $1040 → total $1144.00; build complete, no issues | $990.00 → $1144.00; complete, 0 issues | PASS |
| 2 | Switching behaviour, Strategy | Same build, strategy "student", then "volume" | Student: $1144.00 − 10% = $1029.60; volume: subtotal < $2000, no discount → $1144.00 | $1029.60, then $1144.00 | PASS |
| 3 | Reaction to an event, Observer | CPU i5-13600K (LGA1700) + board B650 (AM5) | 1 compatibility issue; a WARNING entry mentioning "socket" appears in the activity log | 1 issue; WARNING "...CPU socket LGA1700 does not fit motherboard socket AM5" logged | PASS |
| 4 | Boundary: weak power supply | i5-13600K (125W) + RX 7800 XT (263W) + 450W PSU | Issue "too weak" (needs 125+263+100 = 488W) | Issue "...450W is too weak: at least 488W needed" | PASS |
| 5 | Saving and reloading | Complete build + "RGB lighting kit"; create a second `SetupFacade` on the same file | Reloaded build is complete and still has the "rgb" upgrade | Build complete, upgrade present | PASS |
| 6 | Invalid input: names | Build name `"   "`, `"a\|b"`, and 41 × "x" | `ValidationException` for each (empty / forbidden char / too long) | 3 of 3 rejected | PASS |
| 7 | Invalid input: unknown ids | Component "nope", upgrade "nope", strategy "nope", build "missing" | `ValidationException` for each | 4 of 4 rejected | PASS |
| 8 | Final output and rule violation, Facade | Export report of an empty build; then of the complete reference build | Empty build rejected; report of the complete one contains `TOTAL:    $990.00` | Rejected; report total $990.00 | PASS |
| 9 | Editing data: rename | Rename build to "New name"; then to `" "` and `"a\|b"` | First succeeds; the other two rejected | Renamed; 2 of 2 rejected | PASS |
| 10 | Order entity lifecycle, persistence | Order an empty build; order the complete build; reload from file; cancel; cancel again | Empty rejected; order total $990.00 saved and reloaded (1 order); cancel → CANCELLED; second cancel rejected | Rejected; $990.00, 1 order after reload; CANCELLED; second cancel rejected | PASS |

**Manual UI check** (HTTP API smoke test, 11 Nov 2026): rename build via `POST /api/builds/{id}/rename` → name changed;
`POST /api/builds/{id}/order` on the sample "Gaming Rig" → order total $1797.40 (7800X3D, B650, 32GB, RTX 4070 Super, 2TB, 750W + RGB + warranty);
`POST /api/orders/{id}/cancel` → status CANCELLED.
