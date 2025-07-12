package com.evolutionnext.infrastructure.in.order;

import com.evolutionnext.application.commands.AddOrderItem;
import com.evolutionnext.application.commands.ChangeOrderItem;
import com.evolutionnext.application.commands.CreateOrder;
import com.evolutionnext.application.commands.DeleteOrder;
import com.evolutionnext.application.commands.SubmitOrder;
import com.evolutionnext.port.in.ForClientSubmitOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrdersServer {

    private final ForClientSubmitOrder forClientSubmitOrder;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(OrdersServer.class);

    public OrdersServer(ForClientSubmitOrder forClientSubmitOrder) {
        this.forClientSubmitOrder = forClientSubmitOrder;
    }

    public void start(InetSocketAddress addr) throws IOException {
        HttpServer server = HttpServer.create(addr, 0);

        server.createContext("/order.html", http -> {
            if (http.getRequestMethod().equalsIgnoreCase("GET")) {
                logger.info("Received GET request for order.html");
            }
            try (InputStream is = OrdersServer.class.getClassLoader().getResourceAsStream("order.html")) {
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
            logger.info("Sending to path: {}", path);
            // If the path is "/", serve "index.html" by default
            if (path.equals("/")) {
                path = "index.html";
            } else {
                // Remove leading "/" to get the actual file name
                path = path.substring(1);
            }

            logger.info("Looking for file in classpath: {}", path);
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


        server.createContext("/order", http -> {
            String path = http.getRequestURI().getPath();
            logger.info("Received /order context to path: {}", path);

            if (path.equals("/order/submit") && "POST".equalsIgnoreCase(http.getRequestMethod())) {
                logger.info("Received POST request to submit order");
                String body = new String(http.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> jsonMap = objectMapper.readValue(body, new TypeReference<>() {
                });
                UUID orderId = UUID.fromString(jsonMap.get("orderId"));
                forClientSubmitOrder.submit(new SubmitOrder(orderId, Instant.now()));
                http.sendResponseHeaders(204, -1);
            } else if (path.matches("/order/([^/]+)/items/([^/]+)") && "PATCH".equalsIgnoreCase(http.getRequestMethod())) {
                logger.info("Received PATCH request to update item in order");
                String[] parts = path.split("/");
                UUID orderId = UUID.fromString(parts[2]);
                UUID orderItemId = UUID.fromString(parts[4]);

                String body = new String(http.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> data = parseForm(body);

                long productId = Long.parseLong(data.get("productId"));
                int quantity = Integer.parseInt(data.get("quantity"));
                BigDecimal price = new BigDecimal(data.get("price"));
                forClientSubmitOrder.submit(new ChangeOrderItem(orderId, orderItemId,
                    productId, quantity, price, Instant.now()));
                http.sendResponseHeaders(204, -1); // No content on success
            } else if (path.matches("/order/([^/]+)/items") && "POST".equalsIgnoreCase(http.getRequestMethod())) {
                logger.info("Received POST request to add item to order");
                String[] parts = path.split("/");
                UUID orderId = UUID.fromString(parts[2]);
                String body = new String(http.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> data = parseForm(body);
                UUID orderItemId = UUID.randomUUID();
                long productId = Long.parseLong(data.get("productId"));
                int quantity = Integer.parseInt(data.get("quantity"));
                BigDecimal price = new BigDecimal(data.get("price"));
                forClientSubmitOrder.submit(new AddOrderItem(orderId, orderItemId,
                    productId, quantity, price, Instant.now()));
                String response = objectMapper.writeValueAsString(Map.of("orderItemId", orderItemId));
                sendJson(http, response);
            } else if (path.matches("/order/([^/]+)/items") && "GET".equalsIgnoreCase(http.getRequestMethod())) {
                logger.info("Received GET request to retrieve items in order");
                methodNotAllowed(http);
            } else if (path.matches("/order/([^/]+)") && "DELETE".equalsIgnoreCase(http.getRequestMethod())) {
                logger.info("Received DELETE request to cancel or delete the order");
                String[] parts = path.split("/");
                UUID orderId = UUID.fromString(parts[2]);
                forClientSubmitOrder.submit(new DeleteOrder(orderId));
                logger.info("Order {} successfully deleted or canceled", orderId);
                http.sendResponseHeaders(204, -1);
            } else if ("POST".equalsIgnoreCase(http.getRequestMethod())) {
                logger.info("Received POST request to create a new order");
                try {
                    String body = new String(http.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    logger.info("Received body: {}", body);
                    Map<String, String> jsonMap = objectMapper.readValue(body, new TypeReference<>() {});
                    logger.info("Parsed JSON: {}", jsonMap);
                    UUID orderId = UUID.fromString(jsonMap.get("orderId"));
                    forClientSubmitOrder.submit(new CreateOrder(orderId, Instant.now()));
                    http.sendResponseHeaders(201, -1);
                    http.close();
                } catch (Exception e) {
                    logger.error("Error processing order creation request", e);
                    http.sendResponseHeaders(400, -1);
                    http.close();
                }
            } else {
                methodNotAllowed(http);
            }
        });
        server.setExecutor(null); // Use the default executor
        server.start();
        logger.info("Orders server running at http://localhost:{}/", addr.getPort());
    }

    private void sendJson(HttpExchange http, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        http.getResponseHeaders().set("Content-Type", "application/json");
        http.sendResponseHeaders(200, bytes.length);
        http.getResponseBody().write(bytes);
        http.close();
    }

    private void methodNotAllowed(HttpExchange http) throws IOException {
        logger.warn("Received unsupported HTTP method: {}", http.getRequestMethod());
        http.sendResponseHeaders(405, -1);
        http.close();
    }

    private static Map<String, String> parseForm(String body) {
        return Arrays.stream(body.split("&"))
            .map(pair -> pair.split("=", 2))
            .collect(Collectors.toMap(p -> URLDecoder.decode(p[0], StandardCharsets.UTF_8),
                p -> URLDecoder.decode(p[1], StandardCharsets.UTF_8)));
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
