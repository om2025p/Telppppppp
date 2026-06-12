package org.thunderdog.challegram.tele;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface DeviceDao {
    @Insert
    void insert(DeviceEntity report);

    @Query("SELECT DISTINCT normalizedModel FROM device_reports ORDER BY normalizedModel ASC")
    List<String> getAllModels();

    @Query("SELECT * FROM device_reports WHERE normalizedModel = :model ORDER BY timestamp DESC")
    List<DeviceEntity> getReportsForModel(String model);

    @Query("SELECT * FROM device_reports WHERE normalizedModel = :model AND fileType = :type ORDER BY timestamp DESC")
    List<DeviceEntity> getReportsByType(String model, String type);

    @Query("DELETE FROM device_reports WHERE timestamp < :threshold")
    void deleteOldReports(long threshold);
}
