package setup.domain;

import java.time.LocalDate;

/** Third domain entity: a snapshot of a build's price at the moment it was ordered. status: PLACED / CANCELLED. */
public record Order(String id, String buildId, String buildName, double total, LocalDate date, String status) {
    public Order cancelled() { return new Order(id, buildId, buildName, total, date, "CANCELLED"); }
}
