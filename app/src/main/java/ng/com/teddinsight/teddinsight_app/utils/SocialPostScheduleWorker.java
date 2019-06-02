package ng.com.teddinsight.teddinsight_app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.Random;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.activities.DCSHomeActivity;
import ng.com.teddinsight.teddinsight_app.models.ScheduledPost;
import ng.com.teddinsight.teddinsight_app.models.SocialAccounts;


public class SocialPostScheduleWorker extends Worker {

    public static final String IS_REMINDER = "is_reminder";
    public static final String SCHEDULE_ID = "schedule_id";
    private static final String NOTIFICATION_CHANNEL_ID = "Scheduled";
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public SocialPostScheduleWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String scheduleId = getInputData().getString(SCHEDULE_ID);
        boolean isReminder = getInputData().getBoolean(IS_REMINDER, false);
        if (isReminder) {
            String notifMessage;
            if (ExtraUtils.isNetworkAvailable(getApplicationContext())) {
                notifMessage = "Keep your internet connected for the next 5 minutes";
            } else notifMessage = "Turn on your internet now";

            showNotification("Post Scheduling", notifMessage, null);
            return Result.success();
        } else {
            if (scheduleId == null) {
                showNotification("Post Schedule failed", "A post scheduled has failed!", null);
                return Result.failure();
            }
            databaseReference.child(ScheduledPost.APPROVED_SCHEDULE_PATH).child(scheduleId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        showNotification("Not approved", "scheduled post for was not approved by admin", null);
                        return;
                    }
                    ScheduledPost scheduledPost = dataSnapshot.getValue(ScheduledPost.class);
                    if (scheduledPost == null) {
                        showNotification("Scheduled post failed", "Could not complete process", null);
                        return;
                    }
                    if (scheduledPost.getAccountType().equals(SocialAccounts.ACCOUNT_TYPE_TWITTER)) {
                        TwitterAuthToken mTwitterAuthToken = new TwitterAuthToken(scheduledPost.getTwitterUserToken(), scheduledPost.getTwitterSecreteToken());
                        TwitterSession mTwitterSession = new TwitterSession(mTwitterAuthToken, scheduledPost.getTwitterUserId(), scheduledPost.getTwitterUserName());
                        TwitterApiClient mTwitterApiClient = new TwitterApiClient(mTwitterSession);
                        StatusesService statusesService = mTwitterApiClient.getStatusesService();
                        statusesService.update(scheduledPost.getPostText(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                scheduledPost.getPostImage()).enqueue(new Callback<Tweet>() {
                            @Override
                            public void success(com.twitter.sdk.android.core.Result<Tweet> result) {
                                showNotification("Scheduled post sent", "Your post for " + scheduledPost.getAccountUsername() + " has been sent", null);
                            }

                            @Override
                            public void failure(TwitterException exception) {
                                showNotification("Scheduled post failed", "Your post for " + scheduledPost.getAccountUsername() + "failed to send", null);
                            }
                        });
                    } else {
                        Intent intent = new Intent(getApplicationContext(), DCSHomeActivity.class);
                        Log.e("From worker", "Image: " + scheduledPost.getPostImage());
                        intent.putExtra(SCHEDULE_ID, scheduledPost.getPostImage());
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), new Random().nextInt(100), intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.ic_file_upload_black_24dp, "Post", pendingIntent);
                        showNotification("Post to Instagram", "Post to instagram for " + scheduledPost.getAccountUsername(), action);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showNotification("Error while posting", databaseError.getMessage(), null);
                }
            });
            return Result.success();
        }
    }

    private void showNotification(String title, String message, NotificationCompat.Action action) {
        NotificationManagerCompat mNotifyManager = NotificationManagerCompat.from(getApplicationContext());
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Post Scheduler", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notification that shows when to remind of a post");
            mNotifyManager.createNotificationChannel(channel);
        }
        Uri uri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/raw/notif");
        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(action == null)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setSound(uri)
                .setLights(Color.RED, Color.GREEN, 500);
        if (action != null)
            mBuilder.addAction(action);
        int id = new Random().nextInt(100);
        mNotifyManager.notify(id, mBuilder.build());
    }
}
