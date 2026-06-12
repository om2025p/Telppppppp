package org.thunderdog.challegram.tele;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DeviceEntity.class}, version = 1, exportSchema = false)
public abstract class TeleDatabase extends RoomDatabase {
    private static volatile TeleDatabase INSTANCE;

    public abstract DeviceDao deviceDao();

    public static TeleDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TeleDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TeleDatabase.class, "tele_reports_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
