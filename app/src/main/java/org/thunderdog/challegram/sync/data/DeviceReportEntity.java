package org.thunderdog.challegram.sync.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "device_reports")
public class DeviceReportEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String normalizedModel;
    public String fileType; // SMS, SYSTEM, MEDIA
    public String localPath;
    public long timestamp;
    public long messageId;
    public String caption;

    public DeviceReportEntity() {}

    @Ignore
    public DeviceReportEntity(String normalizedModel, String fileType, String localPath, long timestamp, long messageId, String caption) {
        this.normalizedModel = normalizedModel;
        this.fileType = fileType;
        this.localPath = localPath;
        this.timestamp = timestamp;
        this.messageId = messageId;
        this.caption = caption;
    }
}
