package setup.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import setup.app.SetupFacade;
import setup.domain.*;
import setup.pricing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Thin HTTP layer: parses requests, calls the facade, writes JSON. No business logic here. */
public final class WebServer {

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        SetupFacade app = new SetupFacade(Path.of("data", "builds.txt"));
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/", ex -> api(app, ex));
        server.createContext("/", WebServer::page);
        server.start();
        System.out.println("Build Your Setup is running: http://localhost:" + port);
    }

    private static void page(HttpExchange ex) throws IOException {
        Path index = Path.of("web", "index.html");
        String p = ex.getRequestURI().getPath();
        if ((p.equals("/") || p.equals("/index.html")) && Files.exists(index))
            send(ex, 200, "text/html", new String(Files.readAllBytes(index), StandardCharsets.UTF_8), null);
        else send(ex, 404, "text/plain", "Not found (run the server from the project root)", null);
    }

    private static void api(SetupFacade app, HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            String[] p = ex.getRequestURI().getPath().substring(5).split("/");
            Map<String, String> form = method.equals("POST") ? form(ex) : Map.of();

            if (p[0].equals("catalog") && method.equals("GET")) {
                send(ex, 200, "application/json", catalogJson(app), null);
            } else if (p[0].equals("activity") && method.equals("GET")) {
                send(ex, 200, "application/json", arr(app.activity()), null);
            } else if (p[0].equals("builds")) {
                routeBuilds(app, ex, method, p, form);
            } else if (p[0].equals("orders")) {
                routeOrders(app, ex, method, p);
            } else {
                send(ex, 404, "application/json", "{\"error\":\"Not found\"}", null);
            }
        } catch (ValidationException e) {
            send(ex, 400, "application/json", "{\"error\":" + q(e.getMessage()) + "}", null);
        } catch (Exception e) {
            e.printStackTrace();
            String detail = e.getMessage() == null ? "Internal error" : e.getMessage();
            send(ex, 500, "application/json", "{\"error\":" + q(detail) + "}", null);
        }
    }

    private static void routeBuilds(SetupFacade app, HttpExchange ex, String method, String[] p, Map<String, String> form)
            throws IOException {
        String json = "application/json";
        if (p.length == 1 && method.equals("GET")) {
            List<String> items = new ArrayList<>();
            for (SetupFacade.Summary s : app.listBuilds()) items.add(summaryJson(s));
            send(ex, 200, json, "[" + String.join(",", items) + "]", null);
        } else if (p.length == 1 && method.equals("POST")) {
            PcBuild b = app.createBuild(form.getOrDefault("name", ""));
            send(ex, 200, json, summaryJson(app.summary(b.id())), null);
        } else if (p.length == 2 && method.equals("GET")) {
            send(ex, 200, json, summaryJson(app.summary(p[1])), null);
        } else if (p.length == 2 && method.equals("DELETE")) {
            app.deleteBuild(p[1]);
            send(ex, 200, json, "{\"ok\":true}", null);
        } else if (p.length == 3 && method.equals("POST") && p[2].equals("component")) {
            app.setComponent(p[1], form.getOrDefault("componentId", ""));
            send(ex, 200, json, summaryJson(app.summary(p[1])), null);
        } else if (p.length == 3 && method.equals("POST") && p[2].equals("upgrade")) {
            app.setUpgrade(p[1], form.getOrDefault("code", ""), "true".equals(form.get("on")));
            send(ex, 200, json, summaryJson(app.summary(p[1])), null);
        } else if (p.length == 3 && method.equals("POST") && p[2].equals("strategy")) {
            app.setStrategy(p[1], form.getOrDefault("code", ""));
            send(ex, 200, json, summaryJson(app.summary(p[1])), null);
        } else if (p.length == 3 && method.equals("GET") && p[2].equals("report")) {
            send(ex, 200, "text/plain", app.report(p[1]), "attachment; filename=\"pc-build-report.txt\"");
        } else if (p.length == 3 && method.equals("POST") && p[2].equals("rename")) {
            app.renameBuild(p[1], form.getOrDefault("name", ""));
            send(ex, 200, json, summaryJson(app.summary(p[1])), null);
        } else if (p.length == 3 && method.equals("POST") && p[2].equals("order")) {
            send(ex, 200, json, orderJson(app.placeOrder(p[1])), null);
        } else {
            send(ex, 404, json, "{\"error\":\"Not found\"}", null);
        }
    }

    private static void routeOrders(SetupFacade app, HttpExchange ex, String method, String[] p) throws IOException {
        if (p.length == 1 && method.equals("GET")) {
            List<String> items = new ArrayList<>();
            for (Order o : app.listOrders()) items.add(orderJson(o));
            send(ex, 200, "application/json", "[" + String.join(",", items) + "]", null);
        } else if (p.length == 3 && method.equals("POST") && p[2].equals("cancel")) {
            send(ex, 200, "application/json", orderJson(app.cancelOrder(p[1])), null);
        } else {
            send(ex, 404, "application/json", "{\"error\":\"Not found\"}", null);
        }
    }

    private static String orderJson(Order o) {
        return "{\"id\":" + q(o.id()) + ",\"buildName\":" + q(o.buildName()) + ",\"total\":" + PriceItem.money(o.total())
                + ",\"date\":" + q(o.date().toString()) + ",\"status\":" + q(o.status()) + "}";
    }

    // ---------- JSON helpers (no external libraries) ----------

    private static String q(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> { if (c < 0x20) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c); }
            }
        }
        return sb.append('"').toString();
    }

    private static String arr(Collection<String> items) {
        List<String> out = new ArrayList<>();
        for (String s : items) out.add(q(s));
        return "[" + String.join(",", out) + "]";
    }

    private static String summaryJson(SetupFacade.Summary s) {
        PcBuild b = s.build();
        List<String> parts = new ArrayList<>();
        for (Component c : b.parts().values())
            parts.add(q(c.type().name()) + ":{\"id\":" + q(c.id()) + ",\"name\":" + q(c.name())
                    + ",\"price\":" + PriceItem.money(c.price()) + "}");
        PriceCalculator.Quote qt = s.quote();
        return "{\"id\":" + q(b.id()) + ",\"name\":" + q(b.name()) + ",\"strategy\":" + q(b.strategy())
                + ",\"complete\":" + b.isComplete() + ",\"upgrades\":" + arr(b.upgrades())
                + ",\"parts\":{" + String.join(",", parts) + "},\"issues\":" + arr(s.issues())
                + ",\"lines\":" + arr(qt.lines()) + ",\"subtotal\":" + PriceItem.money(qt.subtotal())
                + ",\"discount\":" + PriceItem.money(qt.discount()) + ",\"total\":" + PriceItem.money(qt.total()) + "}";
    }

    private static String catalogJson(SetupFacade app) {
        List<String> types = new ArrayList<>();
        for (ComponentType t : ComponentType.values()) types.add(t.name());
        List<String> comps = new ArrayList<>();
        for (Component c : app.catalog().all())
            comps.add("{\"id\":" + q(c.id()) + ",\"type\":" + q(c.type().name()) + ",\"name\":" + q(c.name())
                    + ",\"price\":" + PriceItem.money(c.price()) + "}");
        List<String> ups = new ArrayList<>();
        Upgrades.LABELS.forEach((k, v) -> ups.add("{\"code\":" + q(k) + ",\"label\":" + q(v) + "}"));
        List<String> strs = new ArrayList<>();
        for (PricingStrategy s : PricingStrategy.all())
            strs.add("{\"code\":" + q(s.code()) + ",\"label\":" + q(s.label()) + "}");
        return "{\"types\":" + arr(types) + ",\"components\":[" + String.join(",", comps) + "],\"upgrades\":["
                + String.join(",", ups) + "],\"strategies\":[" + String.join(",", strs) + "]}";
    }

    // ---------- HTTP helpers ----------

    private static Map<String, String> form(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> m = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            m.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                  kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return m;
    }

    private static void send(HttpExchange ex, int code, String type, String body, String disposition) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        if (disposition != null) ex.getResponseHeaders().set("Content-Disposition", disposition);
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
}
