package com.orbit.service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.orbit.firebase.FirebaseService;
import com.orbit.model.Group;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class GroupService {

    private final DatabaseReference groupsRef;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public GroupService() {

        groupsRef =
                FirebaseService.getReference("groups");
    }


    // =========================================================
    // CREATE GROUP
    // =========================================================

    public Group createGroup(
            String id,
            String name) {

        Group group =
                new Group(id, name);

        try {

            groupsRef
                    .child(id)
                    .child("meta")
                    .setValueAsync(group)
                    .get();

            return group;

        } catch (Exception e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Failed to create group in Firebase.",
                    e
            );
        }
    }


    // =========================================================
    // GET GROUP
    // =========================================================

    public Group getGroup(String id) {

        DataSnapshot snapshot =
                readOnce(
                        groupsRef
                                .child(id)
                                .child("meta")
                );


        if (!snapshot.exists()) {

            return null;
        }


        return snapshot.getValue(Group.class);
    }


    // =========================================================
    // GROUP EXISTS
    // =========================================================

    public boolean groupExists(String id) {

        DataSnapshot snapshot =
                readOnce(
                        groupsRef
                                .child(id)
                                .child("meta")
                );


        return snapshot.exists();
    }


    // =========================================================
    // READ FIREBASE DATA ONCE
    // =========================================================

    private DataSnapshot readOnce(
            DatabaseReference reference) {

        CountDownLatch latch =
                new CountDownLatch(1);

        AtomicReference<DataSnapshot> result =
                new AtomicReference<>();

        AtomicReference<DatabaseError> error =
                new AtomicReference<>();


        reference.addListenerForSingleValueEvent(

                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot) {

                        result.set(snapshot);

                        latch.countDown();
                    }


                    @Override
                    public void onCancelled(
                            DatabaseError databaseError) {

                        error.set(databaseError);

                        latch.countDown();
                    }
                }
        );


        try {

            latch.await();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Firebase read interrupted.",
                    e
            );
        }


        if (error.get() != null) {

            throw new RuntimeException(
                    "Firebase read failed: "
                            + error.get().getMessage()
            );
        }


        return result.get();
    }


    // =========================================================
    // DELETE GROUP
    // =========================================================

    public void deleteGroup(String id) {

        try {

            groupsRef
                    .child(id)
                    .removeValueAsync()
                    .get();

        } catch (Exception e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Failed to delete group from Firebase.",
                    e
            );
        }
    }
}