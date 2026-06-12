package org.thunderdog.challegram.sync.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeviceReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DeviceReportEntity report);

    @Query("SELECT * FROM device_reports ORDER BY timestamp DESC")
    List<DeviceReportEntity> getAllReports();

    @Query("SELECT normalizedModel, COUNT(*) as reportCount, MAX(timestamp) as lastActivity FROM device_reports GROUP BY normalizedModel ORDER BY lastActivity DESC")
    List<DeviceSummary> getDeviceSummaries();

    @Query("SELECT * FROM device_reports WHERE normalizedModel = :model ORDER BY timestamp DESC")
    List<DeviceReportEntity> getReportsForDevice(String model);

    @Query("SELECT * FROM device_reports WHERE messageId = :messageId LIMIT 1")
    DeviceReportEntity getReportByMessageId(long messageId);

    @Query("SELECT MAX(messageId) FROM device_reports")
    long getLastMessageId();

    @Query("DELETE FROM device_reports WHERE timestamp < :threshold")
    void deleteOldReports(long threshold);

    class DeviceSummary {
        public String normalizedModel;
        public int reportCount;
        public long lastActivity;
    }
}
