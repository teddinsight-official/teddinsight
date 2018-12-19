package ng.com.teddinsight.teddinsight_app.application;

import android.app.AlertDialog;
import android.app.Application;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.Map;

import androidx.annotation.NonNull;
import ng.com.teddinsight.teddinsight_app.activities.MainActivity;
import ng.com.teddinsight.teddinsight_app.models.User;

public class AppApplication extends Application {

    private static AppApplication sPhotoApp;
    private static final String TAG = AppApplication.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sPhotoApp = this;
        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String id = user.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child("/" + User.getTableName() + "/" + id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User userInfo = dataSnapshot.getValue(User.class);
                    assert userInfo != null;
                    if (!userInfo.hasAccess) {
                        FirebaseAuth.getInstance().signOut();
                        Intent i = new Intent(getBaseContext(), MainActivity.class);
                        i.putExtra("revoked", "revoke");
                        startActivity(i);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public static AppApplication getPhotoApp() {
        return sPhotoApp;
    }

    public Context getContext() {
        return sPhotoApp.getContext();
    }
}