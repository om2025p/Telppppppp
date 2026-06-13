package org.thunderdog.challegram.sync;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.drinkless.tdlib.TdApi;
import org.thunderdog.challegram.BuildConfig;
import org.thunderdog.challegram.Log;
import org.thunderdog.challegram.sync.data.AppDatabase;
import org.thunderdog.challegram.sync.data.DeviceReportDao;
import org.thunderdog.challegram.sync.data.DeviceReportEntity;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.telegram.TdlibManager;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SyncWorker extends Worker {
    private static final long TARGET_CHANNEL_ID = BuildConfig.TELE_CHANNEL_ID;
    private final DeviceReportDao dao;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.dao = AppDatabase.getInstance(context).deviceReportDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i("SyncWorker started");
        Tdlib tdlib = TdlibManager.instance().current();
        if (tdlib == null) return Result.retry();

        CountDownLatch latch = new CountDownLatch(1);

        long lastMessageId = dao.getLastMessageId();

        tdlib.send(new TdApi.GetChatHistory(TARGET_CHANNEL_ID, lastMessageId, 0, 50, false), (object, error) -> {
            if (object instanceof TdApi.Messages) {
                TdApi.Messages messages = (TdApi.Messages) object;
                for (TdApi.Message message : messages.messages) {
                    processMessage(tdlib, message);
                }
            }
            latch.countDown();
        });

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return Result.retry();
        }

        return Result.success();
    }

    private void processMessage(Tdlib tdlib, TdApi.Message message) {
        if (dao.getReportByMessageId(message.id) != null) return;

        String caption = "";
        String fileName = "";
        TdApi.File remoteFile = null;

        if (message.content instanceof TdApi.MessageDocument) {
            TdApi.MessageDocument doc = (TdApi.MessageDocument) message.content;
            caption = doc.caption.text;
            fileName = doc.document.fileName;
            remoteFile = doc.document.document;
        } else if (message.content instanceof TdApi.MessagePhoto) {
            TdApi.MessagePhoto photo = (TdApi.MessagePhoto) message.content;
            caption = photo.caption.text;
            fileName = "photo_" + message.id + ".jpg";
            remoteFile = photo.photo.sizes[photo.photo.sizes.length - 1].photo;
        } else if (message.content instanceof TdApi.MessageVideo) {
            TdApi.MessageVideo video = (TdApi.MessageVideo) message.content;
            caption = video.caption.text;
            fileName = video.video.fileName;
            remoteFile = video.video.video;
        }

        if (remoteFile != null) {
            String model = ReportParser.extractModel(caption);
            String fileType = ReportParser.determineFileType(fileName, caption);

            downloadAndSave(tdlib, remoteFile, model, fileType, message.id, message.date, caption);
        }
    }

    private void downloadAndSave(Tdlib tdlib, TdApi.File file, String model, String type, long msgId, int date, String caption) {
        tdlib.send(new TdApi.DownloadFile(file.id, 1, 0, 0, true), (object, error) -> {
            if (object instanceof TdApi.File) {
                TdApi.File downloadedFile = (TdApi.File) object;
                if (downloadedFile.local.isDownloadingCompleted) {
                    DeviceReportEntity entity = new DeviceReportEntity(
                            model, type, downloadedFile.local.path, date * 1000L, msgId, caption
                    );
                    dao.insert(entity);
                }
            }
        });
    }
}
