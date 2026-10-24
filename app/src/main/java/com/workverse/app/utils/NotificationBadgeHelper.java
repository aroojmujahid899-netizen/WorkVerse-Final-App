package com.workverse.app.utils;

import android.content.Context;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.workverse.app.models.Notification;

import java.util.HashMap;
import java.util.Map;

public class NotificationBadgeHelper {

    public interface CountCallback {
        void onResult(int unreadCount);
    }

    /**
     * Call this when the notifications screen opens.
     * Saves the "last seen" time locally AND in Firestore, so it survives logout/login.
     */
    public static void markSeen(Context ctx) {
        SharedPrefManager spm = SharedPrefManager.getInstance(ctx);
        long now = System.currentTimeMillis();
        spm.setLastNotifSeen(now);

        String uid = spm.getUid();
        if (uid == null || uid.isEmpty()) return;

        Map<String, Object> data = new HashMap<>();
        data.put("lastNotifSeen", now);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS).document(uid)
                .set(data, SetOptions.merge());
    }

    public static void fetchUnreadCount(Context ctx, CountCallback callback) {
        SharedPrefManager spm = SharedPrefManager.getInstance(ctx);
        String uid = spm.getUid();
        if (uid == null || uid.isEmpty()) {
            callback.onResult(0);
            return;
        }

        final long localLastSeen = spm.getLastNotifSeen();
        final String role = spm.getRole() != null ? spm.getRole().trim() : "";
        final String myName = spm.getFullName() != null ? spm.getFullName() : "";

        // Read the saved "last seen" time from Firestore first
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS).document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    Long remote = userDoc.getLong("lastNotifSeen");
                    long remoteLastSeen = remote != null ? remote : 0L;
                    // Use whichever is newer
                    countUnread(role, uid, myName, Math.max(localLastSeen, remoteLastSeen), callback);
                })
                .addOnFailureListener(e ->
                        countUnread(role, uid, myName, localLastSeen, callback));
    }

    private static void countUnread(String role, String myUid, String myName,
                                    long lastSeen, CountCallback callback) {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_NOTIFICATIONS).get()
                .addOnSuccessListener(snap -> {
                    int count = 0;
                    for (QueryDocumentSnapshot d : snap) {
                        Notification n = d.toObject(Notification.class);

                        // Only notifications newer than the last time the user opened the screen
                        if (n.getTimestamp() <= lastSeen) continue;

                        String targetRole = n.getTargetRole();
                        String sender = n.getSenderName();
                        String targetUserId = n.getTargetUserId();

                        // Notifications sent by the user themself are not "new" for them
                        if (!myName.isEmpty() && myName.equalsIgnoreCase(sender)) continue;

                        boolean matches;

                        if (role.equalsIgnoreCase("Admin")) {
                            matches = "Admin".equalsIgnoreCase(targetRole)
                                    || "All".equalsIgnoreCase(targetRole);

                        } else if (role.equalsIgnoreCase("CEO")) {
                            matches = "CEO".equalsIgnoreCase(targetRole)
                                    || "All".equalsIgnoreCase(targetRole);

                        } else if (role.equalsIgnoreCase("Manager")) {
                            matches = "Manager".equalsIgnoreCase(targetRole)
                                    || "All".equalsIgnoreCase(targetRole);

                        } else if (role.equalsIgnoreCase("Employee")) {
                            if (targetUserId != null && !targetUserId.isEmpty()) {
                                matches = targetUserId.equals(myUid);
                            } else if ("Employee".equalsIgnoreCase(targetRole)) {
                                matches = true;
                            } else {
                                matches = "All".equalsIgnoreCase(targetRole);
                            }

                        } else {
                            matches = false;
                        }

                        if (matches) count++;
                    }
                    callback.onResult(count);
                })
                .addOnFailureListener(e -> callback.onResult(0));
    }
}