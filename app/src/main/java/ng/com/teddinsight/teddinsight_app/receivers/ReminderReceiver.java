package ng.com.teddinsight.teddinsight_app.receivers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.fragments.TaskDialog;
import ng.com.teddinsight.teddinsight_app.models.Tasks;


public class ReminderReceiver extends BroadcastReceiver {
    public static final String LOG_TAG = ReminderReceiver.class.getSimpleName();
    DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public static final String NOTIFICATION_TASK_CHANNEL_NAME = "Tasks Reminders";
    public static final String NOTIFICATION_TASK_CHANNEL_ID = "tasks";
    public static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Teddinsight task reminder", Toast.LENGTH_SHORT).show();
        String taskId = intent.getStringExtra(TaskDialog.REMINDER_INTENT_TASK_ID);
        Log.e(LOG_TAG, "Receiver called " + taskId);
        if (user == null) {
            Log.e(LOG_TAG, "user is null in receiver");
            return;
        }
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        taskRef.child(Tasks.getTableName()).child(user.getUid()).child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Tasks tasks = dataSnapshot.getValue(Tasks.class);
                if (tasks != null && tasks.status == Tasks.TASK_INCOMPLETE) {
                    sendReminderNotification(tasks, context);
                } else {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, tasks.getPendingIntentId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    manager.cancel(pendingIntent);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendReminderNotification(Tasks tasks, Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_TASK_CHANNEL_ID, NOTIFICATION_TASK_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        Uri notificationSound = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_TASK_CHANNEL_ID);
        builder.setContentTitle(tasks.getTaskTitle() + " reminder")
                .setContentText("You are yet to complete this task")
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification_icon))
                .setSound(notificationSound, AudioManager.STREAM_NOTIFICATION);
        notificationManager.notify(tasks.getPendingIntentId(), builder.build());

    }
}
