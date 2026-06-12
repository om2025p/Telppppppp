package org.thunderdog.challegram.sync;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReportParserTest {
    @Test
    public void testExtractModel() {
        assertEquals("samsung sm-g990e", ReportParser.extractModel("[Samsung SM-G990E] SMS Report"));
        assertEquals("samsung sm-g990e", ReportParser.extractModel("Report for [SAMSUNG sm-g990e]"));
        assertEquals("unknown", ReportParser.extractModel("Invalid report format"));
    }

    @Test
    public void testDetermineFileType() {
        assertEquals("SMS", ReportParser.determineFileType("report.html", "SMS Report [Model]"));
        assertEquals("SMS", ReportParser.determineFileType("sms_log.txt", "Logs"));
        assertEquals("SYSTEM", ReportParser.determineFileType("full_report.html", "Device status"));
        assertEquals("MEDIA", ReportParser.determineFileType("photo.jpg", "[Model] captured image"));
        assertEquals("MEDIA", ReportParser.determineFileType("video.mp4", "Security footage"));
        assertEquals("UNKNOWN", ReportParser.determineFileType("unknown.dat", "binary data"));
    }
}
