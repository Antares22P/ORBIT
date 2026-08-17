package com.orbit.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseService {

    private static FirebaseDatabase database;

    private FirebaseService() {
        // Prevent creating objects of this utility class
    }


    // =========================================================
    // INITIALIZE FIREBASE
    // =========================================================

    public static void initialize() throws IOException {

        // Prevent duplicate initialization
        if (!FirebaseApp.getApps().isEmpty()) {

            database =
                    FirebaseDatabase.getInstance();

            return;
        }


        // Service account JSON
        String serviceAccountPath =
                "credentials/firebase-service-account.json";


        FileInputStream serviceAccount =
                new FileInputStream(
                        serviceAccountPath
                );


        FirebaseOptions options =
                FirebaseOptions.builder()

                        .setCredentials(
                                GoogleCredentials
                                        .fromStream(
                                                serviceAccount
                                        )
                        )

                        .setDatabaseUrl(
                                "https://orbitpoint-c9ee5-default-rtdb.firebaseio.com"
                        )

                        .build();


        FirebaseApp.initializeApp(
                options
        );


        database =
                FirebaseDatabase.getInstance();


        System.out.println(
                "================================="
        );

        System.out.println(
                "       FIREBASE CONNECTED"
        );

        System.out.println(
                "================================="
        );
    }


    // =========================================================
    // GET DATABASE
    // =========================================================

    public static FirebaseDatabase getDatabase() {

        if (database == null) {

            throw new IllegalStateException(
                    "Firebase has not been initialized."
            );
        }


        return database;
    }


    // =========================================================
    // GET DATABASE REFERENCE
    // =========================================================

    public static DatabaseReference getReference(
            String path
    ) {

        return getDatabase()
                .getReference(path);
    }
}