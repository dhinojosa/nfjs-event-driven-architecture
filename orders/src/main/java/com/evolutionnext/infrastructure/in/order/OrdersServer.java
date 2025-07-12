package com.evolutionnext.infrastructure.in.order;

import com.evolutionnext.application.commands.AddOrderItem;
import com.evolutionnext.application.commands.ChangeOrderItem;
import com.evolutionnext.application.commands.CreateOrder;
import com.evolutionnext.application.commands.DeleteOrder;
import com.evolutionnext.port.in.ForClientSubmitOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
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

        // Create a new order
        server.createContext("/order", http -> {
            if ("POST".equalsIgnoreCase(http.getRequestMethod())) {
                logger.info("Received POST request to create a new order");
                UUID orderId = UUID.randomUUID();
                String json = objectMapper.writeValueAsString(Map.of("orderId", orderId));
                forClientSubmitOrder.submit(new CreateOrder(orderId, Instant.now()));
                sendJson(http, json);
            } else {
                methodNotAllowed(http);
            }
        });

        // Add an item to an order (POST /order/{uuid}/items)
        server.createContext("/order", http -> {
            String path = http.getRequestURI().getPath();
            if (path.matches("/order/([^/]+)/items") && "POST".equalsIgnoreCase(http.getRequestMethod())) {
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
            } else {
                methodNotAllowed(http);
            }
        });

        // Update an item in an order (PATCH /order/{uuid}/items/{id})
        server.createContext("/order", http -> {
            String path = http.getRequestURI().getPath();
            if (path.matches("/order/([^/]+)/items/([^/]+)") && "PATCH".equalsIgnoreCase(http.getRequestMethod())) {
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
            } else {
                methodNotAllowed(http);
            }
        });

        // Retrieve all items in an order (GET /order/{uuid}/items)
        server.createContext("/order", http -> {
            String path = http.getRequestURI().getPath();
            if (path.matches("/order/([^/]+)/items") && "GET".equalsIgnoreCase(http.getRequestMethod())) {
                logger.info("Received GET request to retrieve items in order");
                methodNotAllowed(http);
            }
        });

        // Delete an order (DELETE /order/{uuid})
        server.createContext("/order", http -> {
            String path = http.getRequestURI().getPath();
            if (path.matches("/order/([^/]+)") && "DELETE".equalsIgnoreCase(http.getRequestMethod())) {
                logger.info("Received DELETE request to cancel or delete the order");
                String[] parts = path.split("/");
                UUID orderId = UUID.fromString(parts[2]);
                forClientSubmitOrder.submit(new DeleteOrder(orderId));
                logger.info("Order {} successfully deleted or canceled", orderId);
                http.sendResponseHeaders(204, -1);
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
            .map(pair -> pair.split("="))
            .collect(Collectors.toMap(pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8)));
    }

    static class OrderDTO {
        private final Map<UUID, OrderItemDTO> items = new HashMap<>();
        private final UUID orderID;

        OrderDTO() {
            this.orderID = UUID.randomUUID();
        }

        void add(UUID itemId, ProductDTO product, int quantity) {
            items.put(itemId, new OrderItemDTO(itemId, product, quantity));
        }

        void update(UUID itemId, ProductDTO product, int quantity) {
            if (items.containsKey(itemId)) {
                items.put(itemId, new OrderItemDTO(itemId, product, quantity));
            } else {
                throw new IllegalStateException("Cannot update a non-existent item");
            }
        }

        List<Map<String, Object>> getItemsList() {
            return items.values().stream().map(OrderItemDTO::toMap).toList();
        }

        boolean remove(UUID itemId) {
            return items.remove(itemId) != null;
        }

        Map<String, Object> toMap() {
            return Map.of(
                "orderId", orderID,
                "items", getItemsList()
            );
        }
    }

    record OrderItemDTO(UUID itemId, ProductDTO product, int quantity) {
        Map<String, Object> toMap() {
            return Map.of("itemId", itemId, "product", product.toMap(), "quantity", quantity);
        }
    }

    record ProductDTO(Long id, String name, BigDecimal price) {
        Map<String, Object> toMap() {
            return Map.of("id", id, "name", name, "price", price);
        }
    }
}
