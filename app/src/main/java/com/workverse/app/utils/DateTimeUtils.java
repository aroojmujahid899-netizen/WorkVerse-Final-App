package com.workverse.app.utils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class DateTimeUtils {
    public static String getCurrentDate(){
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
    public static String getCurrentTime(){
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }
    public static String getCurrentDateTime(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
    public static String formatTimestamp(long ts){
        return new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(new Date(ts));
    }
    public static String getTimeAgo(long ts){
        long diff = System.currentTimeMillis() - ts;
        if(diff < 60000) return "Just now";
        if(diff < 3600000) return (diff/60000)+" min ago";
        if(diff < 86400000) return (diff/3600000)+" hr ago";
        return (diff/86400000)+" days ago";
    }
}