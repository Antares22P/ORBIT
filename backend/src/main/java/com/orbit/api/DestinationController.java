package com.orbit.api;

import com.orbit.model.Destination;
import com.orbit.service.DestinationService;

public class DestinationController {

    private final DestinationService destinationService;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public DestinationController(
            DestinationService destinationService) {

        this.destinationService =
                destinationService;
    }


    // =========================================================
    // SET DESTINATION
    // =========================================================

    public Destination setDestination(
            String groupId,
            String name,
            double latitude,
            double longitude) {

        return destinationService.setDestination(
                groupId,
                name,
                latitude,
                longitude
        );
    }


    // =========================================================
    // GET DESTINATION
    // =========================================================

    public Destination getDestination(
            String groupId) {

        return destinationService.getDestination(
                groupId
        );
    }


    // =========================================================
    // REMOVE DESTINATION
    // =========================================================

    public boolean removeDestination(
            String groupId) {

        return destinationService.removeDestination(
                groupId
        );
    }
}