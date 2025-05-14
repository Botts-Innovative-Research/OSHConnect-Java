package org.sensorhub.oshconnect.util;

public class Utilities {
    public static String joinPath(String first, String... more) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        for (String part : more) {
            if (sb.toString().endsWith("/")) {
                // Remove trailing slash
                sb.deleteCharAt(sb.length() - 1);
            }
            if (!part.startsWith("/")) {
                // Add slash between parts if not already present
                sb.append("/");
            }
            sb.append(part);
        }
        return sb.toString();
    }
}
