package com.workverse.app.utils;
import android.content.Context;
import android.content.SharedPreferences;
public class SharedPrefManager {
    private static final String PREF_NAME = "WorkVersePref";
    private static final String KEY_UID = "uid";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_LAST_NOTIF_SEEN_PREFIX = "lastNotifSeen_";
    private static SharedPrefManager instance;
    private SharedPreferences prefs;
    private SharedPrefManager(Context ctx){
        prefs = ctx.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    public static synchronized SharedPrefManager getInstance(Context ctx){
        if(instance==null) instance = new SharedPrefManager(ctx);
        return instance;
    }
    public void saveUser(String uid, String username, String role, String fullName, String email){
        prefs.edit().putString(KEY_UID,uid).putString(KEY_USERNAME,username)
                .putString(KEY_ROLE,role).putString(KEY_FULL_NAME,fullName)
                .putString(KEY_EMAIL,email).apply();
        // NOTE: no baseline seeding here anymore — seeding on login caused notifications
        // sent before a user's very first login to be wrongly marked as "already read".
    }
    public String getUid(){return prefs.getString(KEY_UID,null);}
    public String getUsername(){return prefs.getString(KEY_USERNAME,null);}
    public String getRole(){return prefs.getString(KEY_ROLE,null);}
    public String getFullName(){return prefs.getString(KEY_FULL_NAME,null);}
    public String getEmail(){return prefs.getString(KEY_EMAIL,null);}
    public boolean isLoggedIn(){return prefs.getString(KEY_UID,null)!=null;}

    /** Per-user (per-uid) last-seen timestamp for notifications.
     *  0 means "never opened the Notifications screen" — in that case ALL
     *  matching notifications (even old ones) correctly count as unread. */
    public long getLastNotifSeen(){
        String uid = getUid();
        if (uid == null) return 0L;
        return prefs.getLong(KEY_LAST_NOTIF_SEEN_PREFIX + uid, 0L);
    }
    public void setLastNotifSeen(long timestamp){
        String uid = getUid();
        if (uid == null) return;
        prefs.edit().putLong(KEY_LAST_NOTIF_SEEN_PREFIX + uid, timestamp).apply();
    }

    public void clear(){prefs.edit().clear().apply();}
}