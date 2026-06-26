package com.workverse.app.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {
    private static FirebaseFirestore db;
    private static FirebaseAuth auth;

    public static FirebaseFirestore getDb() {
        if (db == null) db = FirebaseFirestore.getInstance();
        return db;
    }

    public static FirebaseAuth getAuth() {
        if (auth == null) auth = FirebaseAuth.getInstance();
        return auth;
    }

    // Firestore Collections
    public static final String COL_USERS         = "users";
    public static final String COL_EMPLOYEES     = "employees";
    public static final String COL_MANAGERS      = "managers";
    public static final String COL_ATTENDANCE    = "attendance";
    public static final String COL_LEAVES        = "leave_requests";
    public static final String COL_NOTIFICATIONS = "notifications";
    public static final String COL_FEEDBACK      = "feedback";
    public static final String COL_PERFORMANCE   = "performance";
    public static final String COL_SALES         = "sales_reports";
    public static final String COL_ROLES         = "roles";
    public static final String COL_FAQS          = "faqs";

    // User Roles
    public static final String ROLE_ADMIN    = "Admin";
    public static final String ROLE_CEO      = "CEO";
    public static final String ROLE_MANAGER  = "Manager";
    public static final String ROLE_EMPLOYEE = "Employee";
}
