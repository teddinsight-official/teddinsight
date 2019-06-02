package ng.com.teddinsight.teddinsight_app.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;

import com.downloader.PRDownloader;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

public class NotificationService extends IntentService {

    public NotificationService() {
        super("Intent Service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.hasExtra("notificationId") && intent.hasExtra("downloadId")) {
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
            managerCompat.cancel(intent.getIntExtra("notificationId", 0));
            PRDownloader.cancel(intent.getIntExtra("downloadId", 0));
        }
    }
}
