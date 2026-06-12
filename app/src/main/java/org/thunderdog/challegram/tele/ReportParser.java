package org.thunderdog.challegram.tele;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportParser {
    // الگوی Regex برای استخراج متن بین براکت‌ها [...]
    private static final Pattern MODEL_PATTERN = Pattern.compile("\\[(.*?)\\]");

    public static class ParsedResult {
        public final String model;
        public final String type;

        public ParsedResult(String model, String type) {
            this.model = model;
            this.type = type;
        }
    }

    /**
     * استخراج مدل گوشی و نوع گزارش از متن پیام
     */
    public static ParsedResult parse(String text, String fileName) {
        if (text == null) text = "";
        if (fileName == null) fileName = "";

        String model = "Unknown";
        Matcher matcher = MODEL_PATTERN.matcher(text);
        if (matcher.find()) {
            model = matcher.group(1).trim().toLowerCase();
        }

        String type = "MEDIA"; // پیش‌فرض
        String combined = (text + " " + fileName).toLowerCase();

        if (combined.contains(".html") || combined.contains("report")) {
            if (combined.contains("sms")) {
                type = "SMS";
            } else {
                type = "SYSTEM";
            }
        }

        return new ParsedResult(model, type);
    }
}
