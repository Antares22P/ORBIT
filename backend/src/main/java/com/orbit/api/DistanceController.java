package com.orbit.api;

import com.google.gson.Gson;
import com.orbit.DistanceCalculator;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class DistanceController {

    private final Gson gson = new Gson();

    public void calculate(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        String query = exchange.getRequestURI().getQuery();

        if (query == null || query.isEmpty()) {
            sendResponse(exchange, 400,
                    "{\"success\":false,\"message\":\"Missing coordinates\"}");
            return;
        }

        Double lat1 = null;
        Double lon1 = null;
        Double lat2 = null;
        Double lon2 = null;

        String[] parameters = query.split("&");

        for (String parameter : parameters) {

            String[] pair = parameter.split("=");

            if (pair.length != 2) {
                continue;
            }

            String key = pair[0];
            String value = pair[1];

            try {

                switch (key) {

                    case "lat1":
                        lat1 = Double.parseDouble(value);
                        break;

                    case "lon1":
                        lon1 = Double.parseDouble(value);
                        break;

                    case "lat2":
                        lat2 = Double.parseDouble(value);
                        break;

                    case "lon2":
                        lon2 = Double.parseDouble(value);
                        break;
                }

            } catch (NumberFormatException e) {

                sendResponse(exchange, 400,
                        "{\"success\":false,\"message\":\"Invalid coordinates\"}");

                return;
            }
        }

        if (lat1 == null || lon1 == null ||
            lat2 == null || lon2 == null) {

            sendResponse(exchange, 400,
                    "{\"success\":false,\"message\":\"All coordinates are required\"}");

            return;
        }

        double distanceKm =
                DistanceCalculator.calculateDistanceKm(
                        lat1,
                        lon1,
                        lat2,
                        lon2
                );

        double distanceMeters =
                DistanceCalculator.calculateDistanceMeters(
                        lat1,
                        lon1,
                        lat2,
                        lon2
                );

        DistanceResponse response =
                new DistanceResponse(
                        true,
                        distanceKm,
                        distanceMeters
                );

        sendResponse(
                exchange,
                200,
                gson.toJson(response)
        );
    }


    private void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response) throws IOException {

        exchange.getResponseHeaders()
                .set("Content-Type", "application/json");

        exchange.sendResponseHeaders(
                statusCode,
                response.getBytes().length
        );

        OutputStream outputStream =
                exchange.getResponseBody();

        outputStream.write(response.getBytes());
        outputStream.close();
    }


    private static class DistanceResponse {

        boolean success;
        double distanceKm;
        double distanceMeters;

        DistanceResponse(
                boolean success,
                double distanceKm,
                double distanceMeters) {

            this.success = success;
            this.distanceKm = distanceKm;
            this.distanceMeters = distanceMeters;
        }
    }
}