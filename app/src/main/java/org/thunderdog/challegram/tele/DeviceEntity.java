package org.thunderdog.challegram.tele;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "device_reports")
public class DeviceEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String normalizedModel; // نام نرمال شده برای جلوگیری از تکرار
    public String fileType;        // SMS, SYSTEM, MEDIA
    public String localPath;       // مسیر ذخیره در حافظه داخلی
    public long timestamp;         // زمان دریافت گزارش

    public DeviceEntity(String normalizedModel, String fileType, String localPath, long timestamp) {
        this.normalizedModel = normalizedModel;
        this.fileType = fileType;
        this.localPath = localPath;
        this.timestamp = timestamp;
    }
}
