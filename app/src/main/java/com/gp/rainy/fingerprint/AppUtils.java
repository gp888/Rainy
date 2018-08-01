package com.gp.rainy.fingerprint;


public class AppUtils {
    public static String getMethodName() {
        try {
            StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
            return stacktrace[3].getMethodName();
        } catch (Exception e) {

        }
        return null;
    }
}
