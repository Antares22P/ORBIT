package com.orbit;

import com.google.gson.Gson;
import com.orbit.api.DestinationController;
import com.orbit.api.DistanceController;
import com.orbit.api.GroupController;
import com.orbit.api.MemberController;
import com.orbit.api.MemberDistanceController;
import com.orbit.firebase.FirebaseService;
import com.orbit.model.Destination;
import com.orbit.model.Group;
import com.orbit.model.Member;
import com.orbit.service.DestinationService;
import com.orbit.service.GroupService;
import com.orbit.service.MemberService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class App {

    private static final Gson gson = new Gson();

    public static void main(String[] args) {

        try {

            // =================================================
            // FIREBASE
            // =================================================

            FirebaseService.initialize();

            System.out.println("=================================");
            System.out.println("       FIREBASE CONNECTED");
            System.out.println("=================================");

            System.out.println("=================================");
            System.out.println("          ORBIT BACKEND");
            System.out.println("=================================");

            System.out.println("Firebase initialized.");

            // =================================================
            // SERVICES
            // =================================================

            GroupService groupService =
                    new GroupService();

            MemberService memberService =
                    new MemberService();

            DestinationService destinationService =
                    new DestinationService();

            // =================================================
            // CONTROLLERS
            // =================================================

            GroupController groupController =
                    new GroupController(groupService);

            MemberController memberController =
                    new MemberController(memberService);

            DestinationController destinationController =
                    new DestinationController(destinationService);

            DistanceController distanceController =
                    new DistanceController();

            MemberDistanceController memberDistanceController =
                    new MemberDistanceController(
                            memberService,
                            destinationService
                    );

            // =================================================
            // HTTP SERVER
            // =================================================

            HttpServer server =
                    HttpServer.create(
                            new InetSocketAddress(8080),
                            0
                    );

            // =================================================
            // SINGLE API ROUTER
            // =================================================

            server.createContext(
                    "/api",
                    exchange -> {

                        try {

                            addCorsHeaders(exchange);

                            String method =
                                    exchange.getRequestMethod();

                            String path =
                                    exchange.getRequestURI()
                                            .getPath();

                            System.out.println(
                                    method + " " + path
                            );

                            // =================================================
                            // OPTIONS
                            // =================================================

                            if (method.equalsIgnoreCase("OPTIONS")) {

                                exchange.sendResponseHeaders(
                                        204,
                                        -1
                                );

                                return;
                            }

                            // =================================================
                            // DIRECT DISTANCE
                            //
                            // GET /api/distance
                            // =================================================

                            if (method.equalsIgnoreCase("GET")
                                    && path.equals("/api/distance")) {

                                distanceController.calculate(
                                        exchange
                                );

                                return;
                            }

                            // =================================================
                            // POST /api/groups
                            // =================================================

                            if (method.equalsIgnoreCase("POST")
                                    && path.equals("/api/groups")) {

                                createGroup(
                                        exchange,
                                        groupService
                                );

                                return;
                            }

                            // =================================================
                            // GROUP ID
                            //
                            // /api/groups/{groupId}
                            // =================================================

                            if (isGroupPath(path)) {

                                String groupId =
                                        getGroupIdFromPath(path);

                                // ---------------------------------------------
                                // GET GROUP
                                // ---------------------------------------------

                                if (method.equalsIgnoreCase("GET")) {

                                    Group group =
                                            groupController.getGroup(
                                                    groupId
                                            );

                                    if (group == null) {

                                        sendResponse(
                                                exchange,
                                                404,
                                                "{\"error\":\"Group not found\"}"
                                        );

                                        return;
                                    }

                                    sendResponse(
                                            exchange,
                                            200,
                                            gson.toJson(group)
                                    );

                                    return;
                                }
                            }

                            // =================================================
                            // MEMBERS
                            //
                            // /api/groups/{groupId}/members
                            // =================================================

                            if (isMembersPath(path)) {

                                String groupId =
                                        getGroupIdFromMembersPath(
                                                path
                                        );

                                // ---------------------------------------------
                                // POST MEMBER
                                // ---------------------------------------------

                                if (method.equalsIgnoreCase("POST")) {

                                    createMember(
                                            exchange,
                                            memberController,
                                            groupId
                                    );

                                    return;
                                }

                                // ---------------------------------------------
                                // GET MEMBERS
                                // ---------------------------------------------

                                if (method.equalsIgnoreCase("GET")) {

                                    Map<String, Member> members =
                                            memberController.getMembers(
                                                    groupId
                                            );

                                    sendResponse(
                                            exchange,
                                            200,
                                            gson.toJson(members)
                                    );

                                    return;
                                }
                            }

                            // =================================================
                            // SINGLE MEMBER
                            //
                            // /api/groups/{groupId}/members/{memberId}
                            // =================================================

                            if (isSingleMemberPath(path)) {

                                String[] ids =
                                        getGroupAndMemberId(path);

                                String groupId = ids[0];
                                String memberId = ids[1];

                                // ---------------------------------------------
                                // GET MEMBER
                                // ---------------------------------------------

                                if (method.equalsIgnoreCase("GET")) {

                                    Member member =
                                            memberController.getMember(
                                                    groupId,
                                                    memberId
                                            );

                                    if (member == null) {

                                        sendResponse(
                                                exchange,
                                                404,
                                                "{\"error\":\"Member not found\"}"
                                        );

                                        return;
                                    }

                                    sendResponse(
                                            exchange,
                                            200,
                                            gson.toJson(member)
                                    );

                                    return;
                                }

                                // ---------------------------------------------
                                // DELETE MEMBER
                                // ---------------------------------------------

                                if (method.equalsIgnoreCase("DELETE")) {

                                    boolean removed =
                                            memberController.removeMember(
                                                    groupId,
                                                    memberId
                                            );

                                    if (!removed) {

                                        sendResponse(
                                                exchange,
                                                404,
                                                "{\"error\":\"Member not found\"}"
                                        );

                                        return;
                                    }

                                    sendResponse(
                                            exchange,
                                            200,
                                            "{\"message\":\"Member removed successfully\"}"
                                    );

                                    return;
                                }
                            }

                            // =================================================
                            // MEMBER LOCATION
                            //
                            // PUT
                            // /api/groups/{groupId}/members/{memberId}/location
                            // =================================================

                            if (isLocationPath(path)) {

                                if (method.equalsIgnoreCase("PUT")) {

                                    String[] ids =
                                            getGroupAndMemberIdFromLocation(
                                                    path
                                            );

                                    updateMemberLocation(
                                            exchange,
                                            memberController,
                                            ids[0],
                                            ids[1]
                                    );

                                    return;
                                }
                            }

                            // =================================================
                            // MEMBER DISTANCE
                            //
                            // GET
                            // /api/groups/{groupId}/members/{memberId}/distance
                            // =================================================

                            if (isMemberDistancePath(path)) {

                                if (method.equalsIgnoreCase("GET")) {

                                    String[] ids =
                                            getGroupAndMemberIdFromDistancePath(
                                                    path
                                            );

                                    memberDistanceController.calculate(
                                            exchange,
                                            ids[0],
                                            ids[1]
                                    );

                                    return;
                                }
                            }

                            // =================================================
                            // DESTINATION
                            //
                            // /api/groups/{groupId}/destination
                            // =================================================

                            if (isDestinationPath(path)) {

                                String groupId =
                                        getGroupIdFromDestinationPath(
                                                path
                                        );

                                // ---------------------------------------------
                                // POST DESTINATION
                                // ---------------------------------------------

                                if (method.equalsIgnoreCase("POST")) {

                                    createDestination(
                                            exchange,
                                            destinationController,
                                            groupId
                                    );

                                    return;
                                }

                                // ---------------------------------------------
                                // GET DESTINATION
                                // ---------------------------------------------

                                if (method.equalsIgnoreCase("GET")) {

                                    Destination destination =
                                            destinationController
                                                    .getDestination(
                                                            groupId
                                                    );

                                    if (destination == null) {

                                        sendResponse(
                                                exchange,
                                                404,
                                                "{\"error\":\"Destination not found\"}"
                                        );

                                        return;
                                    }

                                    sendResponse(
                                            exchange,
                                            200,
                                            gson.toJson(destination)
                                    );

                                    return;
                                }

                                // ---------------------------------------------
                                // DELETE DESTINATION
                                // ---------------------------------------------

                                if (method.equalsIgnoreCase("DELETE")) {

                                    boolean removed =
                                            destinationController
                                                    .removeDestination(
                                                            groupId
                                                    );

                                    if (!removed) {

                                        sendResponse(
                                                exchange,
                                                404,
                                                "{\"error\":\"Destination not found\"}"
                                        );

                                        return;
                                    }

                                    sendResponse(
                                            exchange,
                                            200,
                                            "{\"message\":\"Destination removed successfully\"}"
                                    );

                                    return;
                                }
                            }

                            // =================================================
                            // ROUTE NOT FOUND
                            // =================================================

                            sendResponse(
                                    exchange,
                                    404,
                                    "{\"error\":\"Route not found\"}"
                            );

                        } catch (Exception e) {

                            e.printStackTrace();

                            try {

                                sendResponse(
                                        exchange,
                                        500,
                                        "{\"error\":\"Internal server error\"}"
                                );

                            } catch (Exception ignored) {
                            }
                        }
                    }
            );

            // =================================================
            // START SERVER
            // =================================================

            server.start();

            System.out.println();
            System.out.println("=================================");
            System.out.println("       ORBIT SERVER STARTED");
            System.out.println("=================================");

            System.out.println(
                    "Server: http://localhost:8080"
            );

            System.out.println(
                    "POST   /api/groups"
            );

            System.out.println(
                    "GET    /api/groups/{groupId}"
            );

            System.out.println(
                    "POST   /api/groups/{groupId}/members"
            );

            System.out.println(
                    "GET    /api/groups/{groupId}/members"
            );

            System.out.println(
                    "GET    /api/groups/{groupId}/members/{memberId}"
            );

            System.out.println(
                    "DELETE /api/groups/{groupId}/members/{memberId}"
            );

            System.out.println(
                    "PUT    /api/groups/{groupId}/members/{memberId}/location"
            );

            System.out.println(
                    "POST   /api/groups/{groupId}/destination"
            );

            System.out.println(
                    "GET    /api/groups/{groupId}/destination"
            );

            System.out.println(
                    "DELETE /api/groups/{groupId}/destination"
            );

            System.out.println(
                    "GET    /api/distance"
            );

            System.out.println(
                    "GET    /api/groups/{groupId}/members/{memberId}/distance"
            );

            System.out.println(
                    "================================="
            );

        } catch (Exception e) {

            System.out.println();
            System.out.println("ORBIT backend failed to start!");

            e.printStackTrace();
        }
    }


    // =========================================================
    // CREATE GROUP
    // =========================================================

    private static void createGroup(
            HttpExchange exchange,
            GroupService groupService) throws IOException {

        String body =
                readRequestBody(exchange);

        if (body == null
                || body.trim().isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Request body is required\"}"
            );

            return;
        }

        Map<String, String> request =
                gson.fromJson(
                        body,
                        Map.class
                );

        if (request == null) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid JSON body\"}"
            );

            return;
        }

        String name =
                request.get("name");

        if (name == null
                || name.trim().isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Group name is required\"}"
            );

            return;
        }

        String groupId =
                "GRP-"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        Group group =
                groupService.createGroup(
                        groupId,
                        name.trim()
                );

        sendResponse(
                exchange,
                201,
                gson.toJson(group)
        );

        System.out.println(
                "Group created: "
                        + group.getId()
        );
    }


    // =========================================================
    // CREATE MEMBER
    // =========================================================

    private static void createMember(
            HttpExchange exchange,
            MemberController memberController,
            String groupId) throws IOException {

        String body =
                readRequestBody(exchange);

        if (body == null
                || body.trim().isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Request body is required\"}"
            );

            return;
        }

        Map<String, String> request =
                gson.fromJson(
                        body,
                        Map.class
                );

        if (request == null) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid JSON body\"}"
            );

            return;
        }

        String name =
                request.get("name");

        String avatar =
                request.get("avatar");

        String color =
                request.get("color");

        if (name == null
                || name.trim().isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Member name is required\"}"
            );

            return;
        }

        if (avatar == null
                || avatar.trim().isEmpty()) {

            avatar = "user";
        }

        if (color == null
                || color.trim().isEmpty()) {

            color = "blue";
        }

        String memberId =
                "MEM-"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        Member member =
                memberController.addMember(
                        groupId,
                        memberId,
                        name.trim(),
                        avatar.trim(),
                        color.trim()
                );

        sendResponse(
                exchange,
                201,
                gson.toJson(member)
        );

        System.out.println(
                "Member created: "
                        + member.getId()
        );
    }


    // =========================================================
    // CREATE DESTINATION
    // =========================================================

    private static void createDestination(
            HttpExchange exchange,
            DestinationController destinationController,
            String groupId) throws IOException {

        String body =
                readRequestBody(exchange);

        if (body == null
                || body.trim().isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Request body is required\"}"
            );

            return;
        }

        Map<String, Object> request =
                gson.fromJson(
                        body,
                        Map.class
                );

        if (request == null) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid JSON body\"}"
            );

            return;
        }

        String name =
                (String) request.get("name");

        Object latitudeValue =
                request.get("latitude");

        Object longitudeValue =
                request.get("longitude");

        if (name == null
                || name.trim().isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Destination name is required\"}"
            );

            return;
        }

        if (latitudeValue == null
                || longitudeValue == null) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Latitude and longitude are required\"}"
            );

            return;
        }

        double latitude;
        double longitude;

        try {

            latitude =
                    ((Number) latitudeValue)
                            .doubleValue();

            longitude =
                    ((Number) longitudeValue)
                            .doubleValue();

        } catch (Exception e) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Latitude and longitude must be numbers\"}"
            );

            return;
        }

        if (latitude < -90
                || latitude > 90) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Latitude must be between -90 and 90\"}"
            );

            return;
        }

        if (longitude < -180
                || longitude > 180) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Longitude must be between -180 and 180\"}"
            );

            return;
        }

        Destination destination =
                destinationController.setDestination(
                        groupId,
                        name.trim(),
                        latitude,
                        longitude
                );

        sendResponse(
                exchange,
                201,
                gson.toJson(destination)
        );

        System.out.println(
                "Destination set for group: "
                        + groupId
        );
    }


    // =========================================================
    // UPDATE MEMBER LOCATION
    // =========================================================

    private static void updateMemberLocation(
            HttpExchange exchange,
            MemberController memberController,
            String groupId,
            String memberId) throws IOException {

        String body =
                readRequestBody(exchange);

        if (body == null
                || body.trim().isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Request body is required\"}"
            );

            return;
        }

        Map<String, Double> request =
                gson.fromJson(
                        body,
                        Map.class
                );

        if (request == null) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Invalid JSON body\"}"
            );

            return;
        }

        Double latitude =
                request.get("latitude");

        Double longitude =
                request.get("longitude");

        Double accuracy =
                request.get("accuracy");

        Double speed =
                request.get("speed");

        Double heading =
                request.get("heading");

        if (latitude == null
                || longitude == null) {

            sendResponse(
                    exchange,
                    400,
                    "{\"error\":\"Latitude and longitude are required\"}"
            );

            return;
        }

        if (accuracy == null) {
            accuracy = 0.0;
        }

        if (speed == null) {
            speed = 0.0;
        }

        if (heading == null) {
            heading = 0.0;
        }

        boolean updated =
                memberController.updateLocation(
                        groupId,
                        memberId,
                        latitude,
                        longitude,
                        accuracy,
                        speed,
                        heading
                );

        if (!updated) {

            sendResponse(
                    exchange,
                    404,
                    "{\"error\":\"Member not found\"}"
            );

            return;
        }

        sendResponse(
                exchange,
                200,
                "{\"message\":\"Location updated successfully\"}"
        );

        System.out.println(
                "Location updated: "
                        + memberId
        );
    }


    // =========================================================
    // PATH HELPERS
    // =========================================================

    private static boolean isGroupPath(
            String path) {

        return path.matches(
                "/api/groups/[^/]+"
        );
    }


    private static boolean isMembersPath(
            String path) {

        return path.matches(
                "/api/groups/[^/]+/members"
        );
    }


    private static boolean isSingleMemberPath(
            String path) {

        return path.matches(
                "/api/groups/[^/]+/members/[^/]+"
        );
    }


    private static boolean isLocationPath(
            String path) {

        return path.matches(
                "/api/groups/[^/]+/members/[^/]+/location"
        );
    }


    private static boolean isMemberDistancePath(
            String path) {

        return path.matches(
                "/api/groups/[^/]+/members/[^/]+/distance"
        );
    }


    private static boolean isDestinationPath(
            String path) {

        return path.matches(
                "/api/groups/[^/]+/destination"
        );
    }


    // =========================================================
    // PATH EXTRACTION
    // =========================================================

    private static String getGroupIdFromPath(
            String path) {

        String prefix =
                "/api/groups/";

        return decode(
                path.substring(
                        prefix.length()
                )
        );
    }


    private static String getGroupIdFromMembersPath(
            String path) {

        String prefix =
                "/api/groups/";

        String suffix =
                "/members";

        return decode(
                path.substring(
                        prefix.length(),
                        path.length()
                                - suffix.length()
                )
        );
    }


    private static String getGroupIdFromDestinationPath(
            String path) {

        String prefix =
                "/api/groups/";

        String suffix =
                "/destination";

        return decode(
                path.substring(
                        prefix.length(),
                        path.length()
                                - suffix.length()
                )
        );
    }


    private static String[] getGroupAndMemberId(
            String path) {

        String prefix =
                "/api/groups/";

        String remaining =
                path.substring(
                        prefix.length()
                );

        String[] parts =
                remaining.split(
                        "/members/",
                        2
                );

        return new String[]{
                decode(parts[0]),
                decode(parts[1])
        };
    }


    private static String[] getGroupAndMemberIdFromLocation(
            String path) {

        String prefix =
                "/api/groups/";

        String suffix =
                "/location";

        String remaining =
                path.substring(
                        prefix.length(),
                        path.length()
                                - suffix.length()
                );

        String[] parts =
                remaining.split(
                        "/members/",
                        2
                );

        return new String[]{
                decode(parts[0]),
                decode(parts[1])
        };
    }


    private static String[] getGroupAndMemberIdFromDistancePath(
            String path) {

        String prefix =
                "/api/groups/";

        String suffix =
                "/distance";

        String remaining =
                path.substring(
                        prefix.length(),
                        path.length()
                                - suffix.length()
                );

        String[] parts =
                remaining.split(
                        "/members/",
                        2
                );

        return new String[]{
                decode(parts[0]),
                decode(parts[1])
        };
    }


    private static String decode(
            String value) {

        try {

            return URLDecoder.decode(
                    value,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            return value;
        }
    }


    // =========================================================
    // READ REQUEST BODY
    // =========================================================

    private static String readRequestBody(
            HttpExchange exchange) throws IOException {

        InputStream inputStream =
                exchange.getRequestBody();

        return new String(
                inputStream.readAllBytes(),
                StandardCharsets.UTF_8
        );
    }


    // =========================================================
    // SEND RESPONSE
    // =========================================================

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response) throws IOException {

        byte[] bytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json"
                );

        exchange.sendResponseHeaders(
                statusCode,
                bytes.length
        );

        try (
                OutputStream outputStream =
                        exchange.getResponseBody()) {

            outputStream.write(bytes);
        }
    }


    // =========================================================
    // CORS
    // =========================================================

    private static void addCorsHeaders(
            HttpExchange exchange) {

        exchange.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Origin",
                        "*"
                );

        exchange.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Methods",
                        "GET, POST, PUT, DELETE, OPTIONS"
                );

        exchange.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Headers",
                        "Content-Type"
                );
    }
}