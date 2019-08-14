package ng.com.teddinsight.teddinsight_app.services;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.application.AppApplication;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsight_app.receivers.ReminderReceiver;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;

import static ng.com.teddinsight.teddinsight_app.fragments.TaskDialog.REMINDER_INTENT_TASK_ID;
import static ng.com.teddinsight.teddinsight_app.fragments.TextEditorDialogFragment.TAG;
import static ng.com.teddinsight.teddinsight_app.models.Tasks.TASK_COMPLETE;


public class MyFirebaseService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "channel_high_dee";
    private static final String NOTIFICATION_GROUP = "ng.com.teddinsight.teddinsight_app.TEDDINSIGHT_NOTIFICATIONS";
    public static final String TAG = FirebaseMessagingService.class.getCanonicalName();
    private SharedPreferences preferences;
    FirebaseUser user;
    DatabaseReference taskRef;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;
        taskRef = FirebaseDatabase.getInstance().getReference().child(Tasks.getTableName()).child(user.getUid());
        if (remoteMessage.getData() != null) {
            if (preferences == null)
                preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Log.e(TAG, "remote message received and not null");
            Map<String, String> remoteMessageData = remoteMessage.getData();
            String title = remoteMessageData.get("title");
            String body = remoteMessageData.get("body");
            if (remoteMessageData.containsKey("task_id") && remoteMessageData.containsKey("pending_intent_id")) {
                String taskId = remoteMessageData.get("task_id");
                String pid = remoteMessageData.get("pending_intent_id");
                setReminder(taskId, Integer.parseInt(pid));
            }
            if (preferences.getBoolean(AppApplication.AppLifecycleListener.CAN_SHOW_NOTIFICATION, true)) {
                createNotif(title, body);
            } else {
                ExtraUtils.playSound(getApplication());
            }
        } else {
            Log.e(TAG, "remote message data is null");
            //createNotif(title, body);
        }
        //createNotif();
    }

    private void setReminder(String taskId, int pid) {
        AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), ReminderReceiver.class);
        intent.putExtra(REMINDER_INTENT_TASK_ID, taskId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), pid, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(5), AlarmManager.INTERVAL_HALF_DAY, pendingIntent);
        taskRef.child(taskId).child("reminderSet").setValue(true);
        Log.e("TAG", "reminder set");
    }

    private void createNotif(String title, String body) {
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary_dark))
                .setSound(getSoundUri(getApplicationContext()))
                .setLights(Color.BLUE, 500, 1000)
                .setContentText(body)
                .setGroup(NOTIFICATION_GROUP)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(new Random().nextInt(), builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Uri getSoundUri(Context context) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.chime);
    }

}
