package com.workverse.app.utils;
import android.util.Patterns;
public class ValidationUtils {
    public static boolean isValidEmail(String email){
        return email!=null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    public static boolean isValidPhone(String phone){
        return phone!=null && phone.length()>=10 && phone.matches("[0-9+\\-()\\ ]+");
    }
    public static boolean isValidPassword(String pw){
        return pw!=null && pw.length()>=6;
    }
    public static boolean isNotEmpty(String s){
        return s!=null && !s.trim().isEmpty();
    }
}