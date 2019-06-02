package ng.com.teddinsight.teddinsight_app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.application.AppApplication;
import ng.com.teddinsight.teddinsightchat.fragments.ThreadFragment;
import ng.com.teddinsight.teddinsightchat.listeners.Listeners;
import ng.com.teddinsight.teddinsightchat.models.User;
import ng.com.teddinsight.teddinsightchat.utils.ExtraUtils;

public class BaseActivity extends AppCompatActivity implements Listeners.StaffItemListener {
    private static final String TAG = BaseActivity.class.getCanonicalName();
    DatabaseReference onlineRef;
    DatabaseReference currentUserRef;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_base);
        ExtraUtils.registerRevokeListener(this, LoginActivity.class);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        onlineRef = databaseReference.child(".info/connected");
        currentUserRef = databaseReference.child("/presence/" + user.getUid() + "/online");
    }

    @Override
    protected void onResume() {
        super.onResume();
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                Log.d(TAG, "DataSnapshot:" + dataSnapshot);
                if (dataSnapshot.getValue(Boolean.class)) {
                    currentUserRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    currentUserRef.setValue(true);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(TAG, "DatabaseError:" + databaseError);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        currentUserRef.setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public void onStaffItemClicked(User currentUser, User chatUser) {
        replaceFragmentContainerContent(ThreadFragment.NewInstance(currentUser, chatUser), true);
    }

    void replaceFragmentContainerContent(Fragment fragment, boolean shouldAddBackstack) {
        Toast.makeText(this, "called", Toast.LENGTH_LONG).show();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.leave, R.anim.pop_enter, R.anim.pop_leave);
        fragmentTransaction.replace(R.id.frame_container, fragment);
        if (shouldAddBackstack)
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void signOut(View view) {
        DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                FirebaseAuth.getInstance().signOut();
                user = FirebaseAuth.getInstance().getCurrentUser();
                PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
                //AppApplication.getInstance().clearApplicationData();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            dialog.cancel();
        };
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setCancelable(false)
                .setPositiveButton("Yes", onClickListener)
                .setNegativeButton("No", onClickListener);
        AlertDialog dialog = dialogBuilder.create();
        dialog.setOnShowListener(dialog1 -> {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.green_600));
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setCameraDistance(ContextCompat.getColor(this, R.color.red_600));
        });
        dialog.show();
    }

}
