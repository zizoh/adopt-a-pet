package com.zizohanto.adoptapet.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtils {
    public static boolean isValidEmailAddress(String emailStr) {
        boolean valid = false;
        Matcher matcher = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(emailStr);
        valid = matcher.find();
        return valid;
    }

    public static boolean isValidPhoneNumber(String phoneStr) {
        boolean valid = false;
        Matcher matcher = Pattern.compile("^[0-9-]{13}$").matcher(phoneStr);
        valid = matcher.find();
        return valid;
    }
}
