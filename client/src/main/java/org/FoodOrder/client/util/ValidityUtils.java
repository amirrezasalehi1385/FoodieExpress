package org.FoodOrder.client.util;
import java.util.regex.*;


public class ValidityUtils {
    public static boolean isValidPassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
        return Pattern.matches(pattern, password);
    }

    public static boolean isValidEmail(String email) {
        String pattern = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.matches(pattern, email);
    }public static boolean isValidIranianPhone(String phone) {
        String regex = "^09[0-9]{9}$";
        return phone != null && phone.matches(regex);
    }

}
