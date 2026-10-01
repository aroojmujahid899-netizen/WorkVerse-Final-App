package com.workverse.app.utils;

import android.content.Context;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.models.Notification;

public class NotificationBadgeHelper {

    public interface CountCallback {
        void onResult(int unreadCount);
    }

    public static void fetchUnreadCount(Context ctx, CountCallback callback) {
        SharedPrefManager spm = SharedPrefManager.getInstance(ctx);
        String role = spm.getRole();
        String myUid = spm.getUid();
        long lastSeen = spm.getLastNotifSeen();
        String myName = spm.getFullName();
        if (myName == null) myName = "";

        final String finalRole = role != null ? role.trim() : "";
        final String finalMyName = myName;

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_NOTIFICATIONS).get()
                .addOnSuccessListener(snap -> {
                    int count = 0;
                    for (QueryDocumentSnapshot d : snap) {
                        Notification n = d.toObject(Notification.class);
                        if (n.getTimestamp() <= lastSeen) continue;

                        String targetRole = n.getTargetRole();
                        String sender = n.getSenderName();
                        String targetUserId = n.getTargetUserId();

                        boolean matches;

                        if (finalRole.equalsIgnoreCase("Admin")) {
                            matches = "Admin".equalsIgnoreCase(targetRole)
                                    || "All".equalsIgnoreCase(targetRole)
                                    || finalMyName.equalsIgnoreCase(sender)
                                    || "Admin".equalsIgnoreCase(sender);

                        } else if (finalRole.equalsIgnoreCase("CEO")) {
                            matches = "CEO".equalsIgnoreCase(targetRole)
                                    || "All".equalsIgnoreCase(targetRole);

                        } else if (finalRole.equalsIgnoreCase("Manager")) {
                            matches = "Manager".equalsIgnoreCase(targetRole)
                                    || "All".equalsIgnoreCase(targetRole);

                        } else if (finalRole.equalsIgnoreCase("Employee")) {
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