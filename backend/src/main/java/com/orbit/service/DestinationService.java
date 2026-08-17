package com.orbit.service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.orbit.firebase.FirebaseService;
import com.orbit.model.Destination;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class DestinationService {

    private final DatabaseReference groupsRef;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public DestinationService() {

        groupsRef =
                FirebaseService.getReference("groups");
    }


    // =========================================================
    // SET DESTINATION
    // =========================================================

    public Destination setDestination(
            String groupId,
            String name,
            double latitude,
            double longitude
    ) {

        Destination destination =
                new Destination(
                        name,
                        latitude,
                        longitude
                );

        try {

            groupsRef
                    .child(groupId)
                    .child("destination")
                    .setValueAsync(destination)
                    .get();

            return destination;

        } catch (
                InterruptedException |
                ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Failed to save destination in Firebase.",
                    e
            );
        }
    }


    // =========================================================
    // GET DESTINATION
    // =========================================================

    public Destination getDestination(
            String groupId
    ) {

        DataSnapshot snapshot =
                readSnapshot(
                        groupsRef
                                .child(groupId)
                                .child("destination")
                );

        if (!snapshot.exists()) {

            return null;
        }

        return snapshot.getValue(
                Destination.class
        );
    }


    // =========================================================
    // REMOVE DESTINATION
    // =========================================================

    public boolean removeDestination(
            String groupId
    ) {

        DatabaseReference destinationRef =
                groupsRef
                        .child(groupId)
                        .child("destination");

        DataSnapshot snapshot =
                readSnapshot(destinationRef);

        if (!snapshot.exists()) {

            return false;
        }

        try {

            destinationRef
                    .removeValueAsync()
                    .get();

            return true;

        } catch (
                InterruptedException |
                ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Failed to remove destination from Firebase.",
                    e
            );
        }
    }


    // =========================================================
    // FIREBASE SNAPSHOT READER
    // =========================================================

    private DataSnapshot readSnapshot(
            DatabaseReference reference
    ) {

        FutureTask<DataSnapshot> task =
                new FutureTask<>(
                        () -> {

                            final DataSnapshot[] result =
                                    new DataSnapshot[1];

                            final DatabaseError[] error =
                                    new DatabaseError[1];

                            Object lock =
                                    new Object();

                            reference
                                    .addListenerForSingleValueEvent(
                                            new ValueEventListener() {

                                                @Override
                                                public void onDataChange(
                                                        DataSnapshot snapshot
                                                ) {

                                                    synchronized (lock) {

                                                        result[0] =
                                                                snapshot;

                                                        lock.notify();
                                                    }
                                                }


                                                @Override
                                                public void onCancelled(
                                                        DatabaseError databaseError
                                                ) {

                                                    synchronized (lock) {

                                                        error[0] =
                                                                databaseError;

                                                        lock.notify();
                                                    }
                                                }
                                            }
                                    );

                            synchronized (lock) {

                                while (
                                        result[0] == null &&
                                        error[0] == null
                                ) {

                                    lock.wait();
                                }
                            }

                            if (error[0] != null) {

                                throw new RuntimeException(
                                        error[0].getMessage()
                                );
                            }

                            return result[0];
                        }
                );

        task.run();

        try {

            return task.get();

        } catch (
                InterruptedException |
                ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Failed to read Firebase data.",
                    e
            );
        }
    }
}