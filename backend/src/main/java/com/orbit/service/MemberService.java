package com.orbit.service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.orbit.firebase.FirebaseService;
import com.orbit.model.Member;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class MemberService {

    private final DatabaseReference groupsRef;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public MemberService() {

        groupsRef =
                FirebaseService.getReference("groups");
    }


    // =========================================================
    // CREATE MEMBER
    // =========================================================

    public Member createMember(
            String groupId,
            String memberId,
            String name,
            String avatar,
            String color
    ) {

        Member member =
                new Member(
                        memberId,
                        name,
                        avatar,
                        color
                );

        try {

            groupsRef
                    .child(groupId)
                    .child("members")
                    .child(memberId)
                    .setValueAsync(member)
                    .get();

            return member;

        } catch (
                InterruptedException |
                ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Failed to create member in Firebase.",
                    e
            );
        }
    }


    // =========================================================
    // GET MEMBER
    // =========================================================

    public Member getMember(
            String groupId,
            String memberId
    ) {

        DataSnapshot snapshot =
                readSnapshot(
                        groupsRef
                                .child(groupId)
                                .child("members")
                                .child(memberId)
                );

        if (!snapshot.exists()) {

            return null;
        }

        return snapshot.getValue(
                Member.class
        );
    }


    // =========================================================
    // GET ALL MEMBERS
    // =========================================================

    public Map<String, Member> getMembers(
            String groupId
    ) {

        DataSnapshot snapshot =
                readSnapshot(
                        groupsRef
                                .child(groupId)
                                .child("members")
                );

        Map<String, Member> members =
                new ConcurrentHashMap<>();

        for (DataSnapshot child :
                snapshot.getChildren()) {

            Member member =
                    child.getValue(
                            Member.class
                    );

            if (member != null) {

                members.put(
                        child.getKey(),
                        member
                );
            }
        }

        return members;
    }


    // =========================================================
    // REMOVE MEMBER
    // =========================================================

    public boolean removeMember(
            String groupId,
            String memberId
    ) {

        DatabaseReference memberRef =
                groupsRef
                        .child(groupId)
                        .child("members")
                        .child(memberId);

        DataSnapshot snapshot =
                readSnapshot(memberRef);

        if (!snapshot.exists()) {

            return false;
        }

        try {

            memberRef
                    .removeValueAsync()
                    .get();

            return true;

        } catch (
                InterruptedException |
                ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Failed to remove member from Firebase.",
                    e
            );
        }
    }


    // =========================================================
    // UPDATE LOCATION
    // =========================================================

    public boolean updateLocation(
            String groupId,
            String memberId,
            double latitude,
            double longitude,
            double accuracy,
            double speed,
            double heading
    ) {

        DatabaseReference memberRef =
                groupsRef
                        .child(groupId)
                        .child("members")
                        .child(memberId);

        DataSnapshot snapshot =
                readSnapshot(memberRef);

        if (!snapshot.exists()) {

            return false;
        }

        Map<String, Object> location =
                new ConcurrentHashMap<>();

        location.put(
                "lat",
                latitude
        );

        location.put(
                "lng",
                longitude
        );

        location.put(
                "accuracy",
                accuracy
        );

        location.put(
                "speed",
                speed
        );

        location.put(
                "heading",
                heading
        );

        location.put(
                "updatedAt",
                System.currentTimeMillis()
        );

        try {

            memberRef
                    .updateChildrenAsync(
                            location
                    )
                    .get();

            return true;

        } catch (
                InterruptedException |
                ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Failed to update member location.",
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