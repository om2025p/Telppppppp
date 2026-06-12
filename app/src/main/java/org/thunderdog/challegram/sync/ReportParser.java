package org.thunderdog.challegram.sync;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportParser {
    private static final Pattern MODEL_PATTERN = Pattern.compile("\\[(.*?)\\]");

    public static String extractModel(String text) {
        if (text == null || text.isEmpty()) return "Unknown";
        Matcher matcher = MODEL_PATTERN.matcher(text);
        if (matcher.find()) {
            String model = matcher.group(1);
            if (model != null) {
                return model.trim().toLowerCase();
            }
        }
        return "unknown";
    }

    public static String determineFileType(String fileName, String caption) {
        String content = (fileName + " " + caption).toLowerCase();
        if (content.contains("sms")) {
            return "SMS";
        } else if (content.endsWith(".html") || content.endsWith(".htm") || content.contains("report")) {
            return "SYSTEM";
        } else if (isMedia(fileName)) {
            return "MEDIA";
        }
        return "UNKNOWN";
    }

    private static boolean isMedia(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
               lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".webm");
    }
}
