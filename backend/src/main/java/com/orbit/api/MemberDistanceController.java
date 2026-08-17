package com.orbit.api;

import com.google.gson.Gson;
import com.orbit.DistanceCalculator;
import com.orbit.model.Destination;
import com.orbit.model.Member;
import com.orbit.service.DestinationService;
import com.orbit.service.MemberService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class MemberDistanceController {

    private final Gson gson = new Gson();

    private final MemberService memberService;
    private final DestinationService destinationService;

    public MemberDistanceController(
            MemberService memberService,
            DestinationService destinationService) {

        this.memberService = memberService;
        this.destinationService = destinationService;
    }


    public void calculate(
            HttpExchange exchange,
            String groupId,
            String memberId) throws IOException {

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"Method not allowed\"}");

            return;
        }


        // =================================================
        // GET MEMBER
        // =================================================

        Member member =
                memberService.getMember(
                        groupId,
                        memberId);

        if (member == null) {

            sendResponse(
                    exchange,
                    404,
                    "{\"success\":false,\"message\":\"Member not found\"}");

            return;
        }


        // =================================================
        // GET DESTINATION
        // =================================================

        Destination destination =
                destinationService.getDestination(
                        groupId);

        if (destination == null) {

            sendResponse(
                    exchange,
                    404,
                    "{\"success\":false,\"message\":\"Destination not found\"}");

            return;
        }


        // =================================================
        // CALCULATE DISTANCE
        // =================================================

        double distanceKm =
                DistanceCalculator.calculateDistanceKm(
                        member.getLatitude(),
                        member.getLongitude(),
                        destination.getLatitude(),
                        destination.getLongitude()
                );

        double distanceMeters =
                DistanceCalculator.calculateDistanceMeters(
                        member.getLatitude(),
                        member.getLongitude(),
                        destination.getLatitude(),
                        destination.getLongitude()
                );


        // =================================================
        // RESPONSE
        // =================================================

        DistanceResponse response =
                new DistanceResponse(
                        true,
                        groupId,
                        memberId,
                        destination.getName(),
                        distanceKm,
                        distanceMeters
                );

        sendResponse(
                exchange,
                200,
                gson.toJson(response));
    }


    private void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response) throws IOException {

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json");

        byte[] bytes =
                response.getBytes();

        exchange.sendResponseHeaders(
                statusCode,
                bytes.length);

        try (
                OutputStream outputStream =
                        exchange.getResponseBody()) {

            outputStream.write(bytes);
        }
    }


    private static class DistanceResponse {

        boolean success;
        String groupId;
        String memberId;
        String destination;
        double distanceKm;
        double distanceMeters;


        DistanceResponse(
                boolean success,
                String groupId,
                String memberId,
                String destination,
                double distanceKm,
                double distanceMeters) {

            this.success = success;
            this.groupId = groupId;
            this.memberId = memberId;
            this.destination = destination;
            this.distanceKm = distanceKm;
            this.distanceMeters = distanceMeters;
        }
    }
}