package org.thunderdog.challegram.tele;

import android.content.Context;
import org.drinkless.tdlib.TdApi;
import org.thunderdog.challegram.telegram.Tdlib;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class TeleSyncManager {
    public static final long TARGET_CHANNEL_ID = -1002617482597L;
    private static final long ONE_WEEK_MILLIS = TimeUnit.DAYS.toMillis(7);

    private final Tdlib tdlib;
    private final TeleDatabase db;
    private final Context context;

    public TeleSyncManager(Context context, Tdlib tdlib) {
        this.context = context;
        this.tdlib = tdlib;
        this.db = TeleDatabase.getInstance(context);
    }

    /**
     * شروع همگام‌سازی پیام‌های یک هفته اخیر
     */
    public void sync() {
        long now = System.currentTimeMillis();
        long startTime = now - ONE_WEEK_MILLIS;

        tdlib.getChatHistory(TARGET_CHANNEL_ID, 0, 0, 100, false, (messages, error) -> {
            if (error == null && messages != null) {
                for (TdApi.Message message : messages.messages) {
                    if (message.date * 1000L > startTime) {
                        processMessage(message);
                    }
                }
            }
        });
    }

    private void processMessage(TdApi.Message message) {
        String caption = "";
        TdApi.File remoteFile = null;

        if (message.content instanceof TdApi.MessageDocument) {
            TdApi.MessageDocument doc = (TdApi.MessageDocument) message.content;
            caption = doc.caption.text;
            remoteFile = doc.document.document;
        } else if (message.content instanceof TdApi.MessagePhoto) {
            TdApi.MessagePhoto photo = (TdApi.MessagePhoto) message.content;
            caption = photo.caption.text;
            remoteFile = photo.photo.sizes[photo.photo.sizes.length - 1].photo;
        } else if (message.content instanceof TdApi.MessageVideo) {
            TdApi.MessageVideo video = (TdApi.MessageVideo) message.content;
            caption = video.caption.text;
            remoteFile = video.video.video;
        }

        if (remoteFile != null) {
            final TdApi.File finalFile = remoteFile;
            final String finalCaption = caption;

            // دانلود فایل
            tdlib.downloadFile(finalFile.id, 32, (file, error) -> {
                if (error == null && file != null && file.local.isDownloadingCompleted) {
                    saveToDatabase(finalCaption, file.local.path, message.date);
                }
            });
        }
    }

    private void saveToDatabase(String caption, String localPath, int date) {
        ReportParser.ParsedResult parsed = ReportParser.parse(caption, localPath);
        DeviceEntity entity = new DeviceEntity(
            parsed.model,
            parsed.type,
            localPath,
            date * 1000L
        );

        new Thread(() -> db.deviceDao().insert(entity)).start();
    }
}
