package com.johanes.mymoviefavorite.data;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class UserDataSource {

    private static final String PATH_USERS = "users";

    private static DatabaseReference usersRef;

    private UserDataSource() {
    }

    public interface AuthCallback {
        void onSuccess(String name, String email);

        void onError(String message);
    }

    public static void register(String name, String email, String password, AuthCallback callback) {
        if (callback == null) {
            return;
        }
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            callback.onError("Missing fields.");
            return;
        }
        String key = sanitizeEmail(email);
        if (TextUtils.isEmpty(key)) {
            callback.onError("Invalid email.");
            return;
        }
        DatabaseReference ref = getUsersReference().child(key);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callback.onError("User already exists.");
                    return;
                }
                Map<String, Object> data = new HashMap<>();
                data.put("id", key);
                data.put("name", name);
                data.put("email", email);
                data.put("password", password);
                ref.setValue(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(name, email);
                    } else {
                        callback.onError(task.getException() != null
                                ? task.getException().getMessage()
                                : "Unable to register.");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void login(String email, String password, AuthCallback callback) {
        if (callback == null) {
            return;
        }
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            callback.onError("Missing fields.");
            return;
        }
        String key = sanitizeEmail(email);
        if (TextUtils.isEmpty(key)) {
            callback.onError("Invalid email.");
            return;
        }
        DatabaseReference ref = getUsersReference().child(key);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onError("User not found.");
                    return;
                }
                String storedPassword = snapshot.child("password").getValue(String.class);
                if (storedPassword == null || !storedPassword.equals(password)) {
                    callback.onError("Invalid password.");
                    return;
                }
                String name = snapshot.child("name").getValue(String.class);
                String storedEmail = snapshot.child("email").getValue(String.class);
                String resolvedEmail = TextUtils.isEmpty(storedEmail) ? email : storedEmail;
                String resolvedName = TextUtils.isEmpty(name) ? resolvedEmail : name;
                callback.onSuccess(resolvedName, resolvedEmail);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private static DatabaseReference getUsersReference() {
        if (usersRef == null) {
            usersRef = FirebaseDatabase.getInstance().getReference(PATH_USERS);
        }
        return usersRef;
    }

    @Nullable
    private static String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.US);
        return normalized
                .replace(".", "_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_");
    }
}
