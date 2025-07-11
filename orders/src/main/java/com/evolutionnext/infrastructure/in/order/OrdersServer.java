package com.evolutionnext.infrastructure.in.order;


import com.evolutionnext.domain.aggregate.OrderId;
import com.evolutionnext.port.in.ForClientSubmitOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class OrdersServer {

    private final ForClientSubmitOrder forClientSubmitOrder;
    private static final ConcurrentHashMap<UUID, OrderDTO> ordersMap = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(OrdersServer.class);

    public OrdersServer(ForClientSubmitOrder forClientSubmitOrder) {
        this.forClientSubmitOrder = forClientSubmitOrder;
    }

    public void start(InetSocketAddress addr) throws IOException {
        HttpServer server = HttpServer.create(addr, 0);
        server.createContext("/orders.html", http -> {
            if (http.getRequestMethod().equalsIgnoreCase("GET")) {
                logger.info("Received GET request for orders.html");
                UUID orderId = UUID.randomUUID();
                ordersMap.put(orderId, new OrderDTO());
                HttpCookie newCookie = new HttpCookie("eda-orderId", orderId.toString());
                http.getResponseHeaders().add("Set-Cookie", newCookie.toString());
            }
            try (InputStream is = OrdersServer.class.getClassLoader().getResourceAsStream("orders.html")) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    http.getResponseHeaders().set("Content-Type", "text/html");
                    http.sendResponseHeaders(200, bytes.length);
                    http.getResponseBody().write(bytes);
                } else {
                    http.sendResponseHeaders(404, -1);
                }
            }
            http.close();
        });

        server.createContext("/", http -> {
            logger.info("Received GET request for /");
            String path = http.getRequestURI().getPath();
            logger.info("   Sending to path: {}", path);
            // If the path is "/", serve "index.html" by default
            if (path.equals("/")) {
                path = "index.html";
            } else {
                // Remove leading "/" to get the actual file name
                path = path.substring(1);
            }


            try (InputStream is = OrdersServer.class.getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    String contentType = URLConnection.guessContentTypeFromName(path);
                    if (contentType == null) contentType = guessMime(Path.of(path));
                    http.getResponseHeaders().set("Content-Type", contentType);
                    http.sendResponseHeaders(200, bytes.length);
                    http.getResponseBody().write(bytes);
                } else {
                    http.sendResponseHeaders(404, -1);
                }
            }
            http.close();
        });

        server.createContext("/catalog.json", http -> {
            logger.info("Received GET request for /catalog.json");
            String json = objectMapper.writeValueAsString(catalog.values());
            sendJson(http, json);
        });

        server.createContext("/order.json", http -> {
            logger.info("Received GET request for /order.json");
            OrderWithId orderWithId = getOrCreateOrder(http);
            String json = objectMapper.writeValueAsString(orderWithId.order().toMap());
            sendJson(http, json);
        });

        server.createContext("/order/add", http -> {
            logger.info("Received POST request for /order/add");
            if (!http.getRequestMethod().equalsIgnoreCase("POST")) {
                http.sendResponseHeaders(405, -1);
                return;
            }
            String body = new String(http.getRequestBody().readAllBytes());
            Map<String, String> data = parseForm(body);
            int id = Integer.parseInt(data.get("id"));
            int qty = Integer.parseInt(data.get("quantity"));
            ProductDTO p = catalog.get(id);
            OrderWithId orderWithId = getOrCreateOrder(http);
            orderWithId.order().add(p, qty);
            http.sendResponseHeaders(200, -1);
            http.close();
        });

        server.createContext("/order/remove", http -> {
            logger.info("Received POST request for /order/remove");
            if (!http.getRequestMethod().equalsIgnoreCase("POST")) {
                http.sendResponseHeaders(405, -1);
                return;
            }
            String body = new String(http.getRequestBody().readAllBytes());
            Map<String, String> data = parseForm(body);
            int id = Integer.parseInt(data.get("id"));
            OrderWithId orderWithId = getOrCreateOrder(http);
            orderWithId.order().remove(id);
            http.sendResponseHeaders(200, -1);
            http.close();
        });

        server.createContext("/order/submit", http -> {
            logger.info("Received POST request for /order/submit");
            UUID orderId = getOrderId(http);
            HttpCookie cookie = new HttpCookie("orderId", "");
            cookie.setMaxAge(0);
            http.getResponseHeaders().add("Set-Cookie", cookie.toString());
            http.getResponseHeaders().add("Location", "/thank-you.html");
            http.sendResponseHeaders(302, -1);
            http.close();
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Orders server running on http://localhost:8080");
    }
    record ProductDTO(int id, String name, double price) {

    }
    record OrderItemDTO(ProductDTO productDTO, int quantity) {

    }

    static class OrderDTO {
        Map<Integer, OrderItemDTO> items = new HashMap<>();


        void add(ProductDTO p, int qty) {
            items.put(p.id(), new OrderItemDTO(p, qty));
        }

        void remove(int id) {
            items.remove(id);
        }

        double total() {
            return items.values().stream().mapToDouble(i -> i.productDTO().price() * i.quantity()).sum();
        }
        Map<String, Object> toMap() {
            return Map.of(
                "items", items.values().stream().map(i -> Map.of(
                    "id", i.productDTO().id(),
                    "name", i.productDTO().name(),
                    "price", i.productDTO().price(),
                    "quantity", i.quantity()
                )).toList(),
                "total", total()
            );
        }

    }

    static final Map<Integer, ProductDTO> catalog = IntStream.rangeClosed(1, 50)
        .boxed()
        .collect(Collectors.toMap(
            i -> i,
            i -> new ProductDTO(i, "Product " + i, 10.0 + i)
        ));


    private static UUID getOrderId(HttpExchange http) {
        List<String> cookies = http.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                for (HttpCookie c : HttpCookie.parse(cookie)) {
                    if (c.getName().equals("eda-orderId")) return UUID.fromString(c.getValue());
                }
            }
        }
        UUID id = UUID.randomUUID();
        HttpCookie newCookie = new HttpCookie("eda-orderId", id.toString());
        http.getResponseHeaders().add("Set-Cookie", newCookie.toString());
        return id;
    }

    private record OrderWithId(OrderDTO order, OrderId orderId) {
    }

    private OrderWithId getOrCreateOrder(HttpExchange http) {
        UUID id = getOrderId(http);
        OrderDTO orderDTO = ordersMap.computeIfAbsent(id, k -> new OrderDTO());
        return new OrderWithId(orderDTO, new OrderId(id));
    }

    private static Map<String, String> parseForm(String body) {
        return Arrays.stream(body.split("&"))
            .map(pair -> pair.split("=", 2))
            .collect(Collectors.toMap(p -> URLDecoder.decode(p[0], StandardCharsets.UTF_8),
                p -> URLDecoder.decode(p[1], StandardCharsets.UTF_8)));
    }

    private static void sendJson(HttpExchange http, String json) throws IOException {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        http.getResponseHeaders().set("Content-Type", "application/json");
        http.sendResponseHeaders(200, data.length);
        http.getResponseBody().write(data);
        http.close();
    }

    private static String guessMime(Path file) {
        String name = file.getFileName().toString();
        if (name.endsWith(".html")) return "text/html";
        if (name.endsWith(".js")) return "application/javascript";
        if (name.endsWith(".css")) return "text/css";
        if (name.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }
}
