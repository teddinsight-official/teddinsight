package ng.com.teddinsight.teddinsight_app.application;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.downloader.PRDownloader;
import com.evernote.android.state.StateSaver;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Twitter;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;

import java.io.File;

import io.fabric.sdk.android.Fabric;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.activities.LoginActivity;
import ng.com.teddinsight.teddinsight_app.utils.AppExecutors;
import ng.com.teddinsight.teddinsightchat.models.User;

public class AppApplication extends Application {

   /* class AppLifecycleTracker implements Application.ActivityLifecycleCallbacks {

        private int numStarted = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (numStarted == 0) {
                Log.e("TAG", "App came to foreground");
            }
            numStarted++;
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            numStarted--;
            if (numStarted == 0) {
                Log.e("TAG", "app went to background");
                Toast.makeText(getApplicationContext(), "background", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }*/

    public class AppLifecycleListener implements LifecycleObserver {

        private SharedPreferences.Editor editor;
        private SharedPreferences sharedPreferences;
        public static final String CAN_SHOW_NOTIFICATION = "show_notification";

        public AppLifecycleListener() {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public void onMoveToForeground() {
            // app moved to foreground
            editor = sharedPreferences.edit();
            editor.putBoolean(CAN_SHOW_NOTIFICATION, false).apply();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onMoveToBackground() {
            // app moved to background
            editor = sharedPreferences.edit();
            editor.putBoolean(CAN_SHOW_NOTIFICATION, true).apply();
        }
    }

    private static AppApplication sPhotoApp;
    private static final String TAG = AppApplication.class.getSimpleName();
    private static AppApplication instance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!FirebaseApp.getApps(getApplicationContext()).isEmpty()) {
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            } catch (DatabaseException e) {
                Log.e(TAG, "" + e.getLocalizedMessage());
            }
        }
        Fabric.with(this, new Crashlytics());
        Twitter.initialize(this);
        StateSaver.setEnabledForAllActivitiesAndSupportFragments(this, true);
        sPhotoApp = this;
        instance = sPhotoApp;
        //registerActivityLifecycleCallbacks(new AppLifecycleTracker());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleListener());
        doInstantationInBackground();

    }

    public void clearApplicationData() {
        File cacheDirectory = getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }
    }

    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }

        return deletedAll;
    }

    public static AppApplication getInstance() {
        return instance;
    }

    public Context getContext() {
        return sPhotoApp.getContext();
    }

    private void doInstantationInBackground() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            PRDownloader.initialize(getApplicationContext());
            Picasso.Builder builder = new Picasso.Builder(getApplicationContext());
            builder.downloader(new OkHttp3Downloader(getApplicationContext(), Integer.MAX_VALUE));
            Picasso built = builder.build();
            built.setIndicatorsEnabled(true);
            built.setLoggingEnabled(true);
            Picasso.setSingletonInstance(built);

            DrawerImageLoader.init(new AbstractDrawerImageLoader() {
                @Override
                public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                    Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
//                Picasso.get().load(uri).placeholder(placeholder).into(imageView);
                }

                @Override
                public void cancel(ImageView imageView) {
                    Glide.with(imageView.getContext()).clear(imageView);
                    //Picasso.get().cancelRequest(imageView);
                }

//            @Override
//            public Drawable placeholder(Context ctx, String tag) {
//                if (tag.equals(DrawerImageLoader.Tags.PROFILE.name()))
//                    return DrawerUIUtils.getPlaceHolder(ctx);
//                else if (tag.equals(DrawerImageLoader.Tags.ACCOUNT_HEADER.name()))
//                    return new IconicsDrawable(ctx).iconText(" ").backgroundColor(colorRes(com.mikepenz.materialdrawer.R.color.primary)).size(dp(56));
//
//                return super.placeholder(ctx, tag);
//            }
            });


            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String id = user.getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child("/" + User.getTableName() + "/" + id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User userInfo = dataSnapshot.getValue(User.class);
                        //assert userInfo != null;
                        if (userInfo == null || !userInfo.hasAccess) {
                            FirebaseAuth.getInstance().signOut();
                            Intent i = new Intent(getBaseContext(), LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.putExtra("revoked", "revoke");
                            startActivity(i);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}