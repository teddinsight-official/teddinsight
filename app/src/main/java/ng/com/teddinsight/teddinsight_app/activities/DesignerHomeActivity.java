package ng.com.teddinsight.teddinsight_app.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.base.BaseActivity;
import ng.com.teddinsight.teddinsight_app.fragments.ClientListFragment;
import ng.com.teddinsight.teddinsight_app.fragments.ClientUploadsDialog;
import ng.com.teddinsight.teddinsight_app.fragments.DesignerHomeFragment;
import ng.com.teddinsight.teddinsight_app.fragments.TaskFragment;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners;
import ng.com.teddinsight.teddinsight_app.models.DesignerDesigns;
import ng.com.teddinsight.teddinsight_app.models.Notifications;
import ng.com.teddinsight.teddinsight_app.services.FileDownloadService;

import ng.com.teddinsight.teddinsightchat.fragments.ChatListFragment;
import ng.com.teddinsight.teddinsightchat.fragments.ThreadFragment;
import ng.com.teddinsight.teddinsightchat.models.User;
import ng.com.teddinsight.teddinsightchat.listeners.Listeners.StaffItemListener;
import ng.com.teddinsight.teddinsightchat.utils.ExtraUtils;

public class DesignerHomeActivity extends BaseActivity implements Listeners.ShowEditImageActivity, StaffItemListener, ClientListFragment.ClientItemClickedListener, ClientUploadsDialog.InitializeFileDownload {
    public static final String FRAGMENT_HOME = "home";
    public static final String FRAGMENT_TASK = "task";
    public static final String FRAGMENT_CONVERSATION = "conversatiom";
    public static final String FRAGMENT_CLIENT_UPLOADS = "client_uploads";
    BottomNavigationView navigation;
    BottomNavigationMenuView menuView;
    private View notificationBadge;
    FragmentManager fragmentManager;
    @BindView(R.id.designer_contents)
    View designerContents;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference onlineRef;
    DatabaseReference currentUserRef;
    private String currentFragment = FRAGMENT_HOME;
    boolean isNavigationBarHidden = true;
    public static final String CLIENT_PATH = "/Teddinsight/ClientUploads";
    public static final String NOTIFICATION_CHANNEL_ID = "file_upload";
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                if (currentFragment.equals(FRAGMENT_HOME))
                    return false;
                currentFragment = FRAGMENT_HOME;
                replaceFragmentContainerContent(DesignerHomeFragment.NewInstance(), false);
                return true;

            case R.id.navigation_dashboard:
                if (currentFragment.equals(FRAGMENT_TASK))
                    return false;
                currentFragment = FRAGMENT_TASK;
                replaceFragmentContainerContent(TaskFragment.NewInstance(), false);
                return true;
            case R.id.navigation_notifications:
                if (currentFragment.equals(FRAGMENT_CONVERSATION))
                    return false;
                currentFragment = FRAGMENT_CONVERSATION;
                replaceFragmentContainerContent(ChatListFragment.NewInstance(), false);
                return true;
            case R.id.navigation_client_upload:
                if (currentFragment.equals(FRAGMENT_CLIENT_UPLOADS))
                    return false;
                currentFragment = FRAGMENT_CLIENT_UPLOADS;
                replaceFragmentContainerContent(ClientListFragment.NewInstance(), false);
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designer);
        ButterKnife.bind(this);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please Login Again!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        ExtraUtils.registerRevokeListener(this, LoginActivity.class);
        onlineRef = databaseReference.child(".info/connected");
        currentUserRef = databaseReference.child("/presence/" + user.getUid() + "/online");
        fragmentManager = getSupportFragmentManager();
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        float d = getResources().getDisplayMetrics().density;
        int marginBottomInDp = (int) d * 56;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) designerContents.getLayoutParams();
        if (savedInstanceState == null) {
            navigation.setVisibility(View.VISIBLE);
            replaceFragmentContainerContent(DesignerHomeFragment.NewInstance(), false);
            params.setMargins(0, 0, 0, marginBottomInDp);
            designerContents.setLayoutParams(params);
        } else {
            boolean showBn = savedInstanceState.getBoolean("bn");
            if (showBn) {
                navigation.setVisibility(View.VISIBLE);
                params.setMargins(0, 0, 0, marginBottomInDp);
                designerContents.setLayoutParams(params);
            } else {
                navigation.setVisibility(View.GONE);
                params.setMargins(0, 0, 0, 0);
                designerContents.setLayoutParams(params);
            }
            currentFragment = savedInstanceState.getString("current");
        }
        fragmentManager.addOnBackStackChangedListener(() -> {
            isNavigationBarHidden = !isNavigationBarHidden;
            if (navigation.getVisibility() == View.VISIBLE) {
                navigation.setVisibility(View.GONE);
                params.setMargins(0, 0, 0, 0);

            } else {
                navigation.setVisibility(View.VISIBLE);
                params.setMargins(0, 0, 0, marginBottomInDp);
            }
            designerContents.setLayoutParams(params);
        });

        addBadgeView();
    }


    void replaceFragmentContainerContent(Fragment fragment, boolean shouldAddBackstack) {
        navigation.setVisibility(View.VISIBLE);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.leave, R.anim.pop_enter, R.anim.pop_leave);
        fragmentTransaction.replace(R.id.designer_contents, fragment);
        if (shouldAddBackstack)
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("current", currentFragment);
        outState.putBoolean("bn", isNavigationBarHidden);
    }

    @Override
    public void showEditImageActivity(DesignerDesigns designerDesigns) {
        Intent i = new Intent(this, EditImageActivity.class);
        i.putExtra("design", designerDesigns);
        startActivity(i);
    }

    private void addBadgeView() {
        menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(1);
        notificationBadge = LayoutInflater.from(DesignerHomeActivity.this).inflate(R.layout.notification_badge_layout, menuView, false);
        AppCompatTextView appCompatTextView = notificationBadge.findViewById(R.id.badge);
        itemView.addView(notificationBadge);
        BottomNavigationItemView itemView1 = (BottomNavigationItemView) menuView.getChildAt(2);
        View notificationBadge1 = LayoutInflater.from(DesignerHomeActivity.this).inflate(R.layout.notification_badge_layout, menuView, false);
        AppCompatTextView appCompatTextView1 = notificationBadge1.findViewById(R.id.badge);
        itemView1.addView(notificationBadge1);
        databaseReference.child(Notifications.getTableName()).child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Notifications notifications = dataSnapshot.getValue(Notifications.class);
                if (notifications != null) {
                    if (notifications.newTaskReceived && !currentFragment.equals(FRAGMENT_TASK)) {
                        notificationBadge.setVisibility(View.VISIBLE);
                    } else {
                        notificationBadge.setVisibility(View.INVISIBLE);
                    }
                    long count = notifications.count;
                    if (count > 0 && !currentFragment.equals(FRAGMENT_CONVERSATION)) {
                        notificationBadge1.setVisibility(View.VISIBLE);
                    } else {
                        notificationBadge1.setVisibility(View.INVISIBLE);
                    }
                } else {
                    notificationBadge1.setVisibility(View.INVISIBLE);
                    notificationBadge.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void goBack(View view) {
        onBackPressed();
    }


    @Override
    protected void onStop() {
        super.onStop();
        currentUserRef.setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Boolean.class)) {
                    currentUserRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    currentUserRef.setValue(true);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onClientItemClicked(String businessName) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        Fragment prev = manager.findFragmentByTag("uploadDialog");
        if (prev != null)
            ft.remove(prev);
        ClientUploadsDialog.NewInstance(businessName).show(ft, "uploadDialog");
    }

    @Override
    public void onFileUDownloadInitiliazed(String fileName, String url) {
        Intent intent = new Intent(this, FileDownloadService.class);
        intent.putExtra("fileName", fileName);
        intent.putExtra("url", url);
        startService(intent);
    }

    public void signOut(View view) {
        DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                FirebaseAuth.getInstance().signOut();
                user = FirebaseAuth.getInstance().getCurrentUser();
                PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
                startActivity(new Intent(DesignerHomeActivity.this, LoginActivity.class));
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

    @Override
    public void onStaffItemClicked(User currentUser, User chatUser) {
        replaceFragmentContainerContent(ThreadFragment.NewInstance(currentUser, chatUser), true);
    }
}
