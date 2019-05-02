package ng.com.teddinsight.teddinsight_app.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;

import java.util.Random;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;

import static ng.com.teddinsight.teddinsight_app.activities.DesignerHomeActivity.CLIENT_PATH;
import static ng.com.teddinsight.teddinsight_app.activities.DesignerHomeActivity.NOTIFICATION_CHANNEL_ID;

public class FileDownloadService extends IntentService {

    public FileDownloadService() {
        super("File Download Service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!intent.hasExtra("fileName") || !intent.hasExtra("url")) {
            Toast.makeText(getApplicationContext(), "Cannot download file", Toast.LENGTH_LONG).show();
            return;
        }

        String fileName = intent.getStringExtra("fileName");
        String url = intent.getStringExtra("url");
        String dirPath = Environment.getExternalStorageDirectory() + CLIENT_PATH;
        int id = new Random().nextInt(100);
        NotificationManagerCompat mNotifyManager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "File Download", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notification that shows when a file is downloading");
            mNotifyManager.createNotificationChannel(channel);
        }
        int downloadId = PRDownloader.download(url, dirPath, fileName)
                .build()
                .setOnStartOrResumeListener(() -> {
                    Toast.makeText(getApplicationContext(), "Downloading " + fileName + "...", Toast.LENGTH_LONG).show();
                })
                .setOnPauseListener(() -> {

                })
                .setOnCancelListener(() -> {
                    //mNotifyManager.cancel(id);
                })
                .setOnProgressListener(progress -> {
                    long p = (long) (100.0 * progress.currentBytes / progress.totalBytes);
                    int pr = ExtraUtils.safeLongToInt(p);
                    mBuilder.setProgress(100, pr, false);
                    mNotifyManager.notify(id, mBuilder.build());

                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        String fileDownloadComplete = "File downloaded to " + dirPath;
                        Toast.makeText(getApplicationContext(), fileDownloadComplete, Toast.LENGTH_LONG).show();
                        mBuilder.setContentTitle("File Downloaded")
                                .setContentText(fileName)
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(fileDownloadComplete))
                                .setProgress(100, 100, false);
                        mNotifyManager.notify(id, mBuilder.build());

                    }

                    @Override
                    public void onError(Error error) {
                        mNotifyManager.cancel(id);
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                });

        Intent intentPd = new Intent(this, NotificationService.class);
        intentPd.putExtra("notificationId", id);
        intentPd.putExtra("downloadId", downloadId);
        PendingIntent pd = PendingIntent.getService(this, id, intentPd, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action cancelAction = new NotificationCompat.Action(R.drawable.ic_close, "cancel", pd);
        mBuilder.setContentTitle("Downloading " + fileName)
                .setContentText("Download in progress")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(cancelAction)
                .setProgress(100, 0, false)
                .setSmallIcon(R.drawable.ic_file_download);
        mNotifyManager.notify(id, mBuilder.build());
    }
}
