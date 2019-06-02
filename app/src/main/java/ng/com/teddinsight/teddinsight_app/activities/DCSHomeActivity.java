package ng.com.teddinsight.teddinsight_app.activities;


import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.services.AccountService;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.application.AppApplication;
import ng.com.teddinsight.teddinsight_app.fragments.ClientListFragment;
import ng.com.teddinsight.teddinsight_app.fragments.ClientUploadsDialog;
import ng.com.teddinsight.teddinsight_app.fragments.DesignerHomeFragment;
import ng.com.teddinsight.teddinsight_app.fragments.ScheduledPostFragment;
import ng.com.teddinsight.teddinsight_app.fragments.SocialAccountFragment;
import ng.com.teddinsight.teddinsight_app.fragments.SocialMediaManagerHomeFragment;
import ng.com.teddinsight.teddinsight_app.fragments.TaskFragment;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners;
import ng.com.teddinsight.teddinsight_app.models.DesignerDesigns;
import ng.com.teddinsight.teddinsight_app.models.Notifications;
import ng.com.teddinsight.teddinsight_app.models.SocialAccounts;
import ng.com.teddinsight.teddinsight_app.services.FileDownloadService;

import ng.com.teddinsight.teddinsight_app.utils.SocialPostScheduleWorker;
import ng.com.teddinsight.teddinsightchat.fragments.ChatListFragment;

import ng.com.teddinsight.teddinsightchat.fragments.ThreadFragment;
import ng.com.teddinsight.teddinsightchat.models.User;


import static ng.com.teddinsight.teddinsight_app.utils.ExtraUtils.createInstagramIntent;

public class DCSHomeActivity extends AppCompatActivity implements Listeners.ShowEditImageActivity,
        ClientListFragment.ClientItemClickedListener,
        ng.com.teddinsight.teddinsightchat.listeners.Listeners.StaffItemListener,
        ClientUploadsDialog.InitializeFileDownload,
        Listeners.SocialAccountsListener {
    public static final String FRAGMENT_HOME = "home";
    public static final String FRAGMENT_TASK = "task";
    public static final String FRAGMENT_CONVERSATION = "conversatiom";
    public static final String FRAGMENT_CLIENT_UPLOADS = "client_uploads";
    public static final String FRAGMENT_SCHEDULED_POSTS = "scheduled_posts";
    BottomNavigationView navigation;
    BottomNavigationMenuView menuView;
    private View notificationBadge;
    FragmentManager fragmentManager;
    @BindView(R.id.designer_contents)
    View designerContents;
    @BindView(R.id.twitter_login_button)
    TwitterLoginButton twitterLoginButton;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference onlineRef, socialAccountRef;
    DatabaseReference currentUserRef;
    private String currentFragment = FRAGMENT_HOME;
    boolean isNavigationBarHidden = true;
    public static final String CLIENT_PATH = "/Teddinsight/ClientUploads";
    public static final String LOG_TAG = DCSHomeActivity.class.getSimpleName();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
//        Log.e(LOG_TAG, "activity drawer");
//        Toast.makeText(this, "Activity Result Activity", Toast.LENGTH_SHORT).show();
    }

    public static final String NOTIFICATION_CHANNEL_ID = "file_upload";
    private String role;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                if (currentFragment.equals(FRAGMENT_HOME))
                    return false;
                currentFragment = FRAGMENT_HOME;
                if (role.equals(User.USER_SOCIAL))
                    replaceFragmentContainerContent(new SocialMediaManagerHomeFragment(), false);
                else
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
            case R.id.navigation_scheduled:
                if (currentFragment.equalsIgnoreCase(FRAGMENT_SCHEDULED_POSTS))
                    return false;
                currentFragment = FRAGMENT_SCHEDULED_POSTS;
                replaceFragmentContainerContent(ScheduledPostFragment.NewInstance(User.USER_SOCIAL), false);
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
            return;
        }
        Intent i = getIntent();
        role = "";
        if (i != null) {
            if (i.hasExtra(LoginActivity.STAFF_ROLE))
                role = getIntent().getStringExtra(LoginActivity.STAFF_ROLE);
            if (i.hasExtra(SocialPostScheduleWorker.SCHEDULE_ID)) {
                String mediaPath = i.getStringExtra(SocialPostScheduleWorker.SCHEDULE_ID);
                if (mediaPath != null)
                    createInstagramIntent(this, mediaPath);
                NotificationManagerCompat mNotifyManager = NotificationManagerCompat.from(getApplicationContext());
                mNotifyManager.cancelAll();
                finish();
            }
        }
        onlineRef = databaseReference.child(".info/connected");
        currentUserRef = databaseReference.child("/presence/" + user.getUid() + "/online");
        socialAccountRef = databaseReference.child(SocialMediaManagerHomeFragment.SOCIAL_ACCOUNT_PATH);
        fragmentManager = getSupportFragmentManager();
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        float d = getResources().getDisplayMetrics().density;
        int marginBottomInDp = (int) d * 56;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) designerContents.getLayoutParams();
        if (savedInstanceState == null) {
            navigation.setVisibility(View.VISIBLE);
            if (role.equals(User.USER_SOCIAL))
                replaceFragmentContainerContent(new SocialMediaManagerHomeFragment(), false);
            else
                replaceFragmentContainerContent(DesignerHomeFragment.NewInstance(role), false);
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
            if (fragmentManager.getBackStackEntryCount() < 1) {
                navigation.setVisibility(View.VISIBLE);
                params.setMargins(0, 0, 0, marginBottomInDp);
            } else {
                navigation.setVisibility(View.GONE);
                params.setMargins(0, 0, 0, 0);
            }

            designerContents.setLayoutParams(params);
        });

        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Toast.makeText(DCSHomeActivity.this, "Adding twitter account", Toast.LENGTH_LONG).show();
                SocialAccounts socialAccounts = new SocialAccounts();
                socialAccounts.setAccountType(SocialAccounts.ACCOUNT_TYPE_TWITTER);
                socialAccounts.setAccountUsername(result.data.getUserName());
                socialAccounts.setTwitterUserId(result.data.getUserId());
                socialAccounts.setTwitterSecreteToken(result.data.getAuthToken().secret);
                socialAccounts.setTwitterUserToken(result.data.getAuthToken().token);
                socialAccounts.setDateLogged(System.currentTimeMillis());
                sendTwitterDetailsToDatabase(socialAccounts);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(DCSHomeActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                Toast.makeText(DCSHomeActivity.this, "Make sure you have the official twitter app installed, If not reinstall twitter and restart the app", Toast.LENGTH_LONG).show();
            }
        });

        addBadgeView();
    }


    private void sendTwitterDetailsToDatabase(SocialAccounts socialAccounts) {
        TwitterAuthToken authToken = new TwitterAuthToken(socialAccounts.getTwitterUserToken(), socialAccounts.getTwitterSecreteToken());
        TwitterSession session = new TwitterSession(authToken, socialAccounts.getTwitterUserId(), socialAccounts.getAccountUsername());
        TwitterApiClient twitterApiClient = new TwitterApiClient(session);
        AccountService accountService = twitterApiClient.getAccountService();
        accountService.verifyCredentials(true, true, true).enqueue(new Callback<com.twitter.sdk.android.core.models.User>() {
            @Override
            public void success(Result<com.twitter.sdk.android.core.models.User> result) {
                socialAccounts.setFollowersCountOnRegistration(result.data.followersCount);
                socialAccounts.setFollowingCountOnRegistration(result.data.friendsCount);
                socialAccountRef.child("twitter" + socialAccounts.getTwitterUserId()).runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        SocialAccounts accounts = mutableData.getValue(SocialAccounts.class);
                        if (accounts != null) {
                            socialAccounts.setDateLogged(accounts.getDateLogged());
                            socialAccounts.setFollowingCountOnRegistration(accounts.getFollowingCountOnRegistration());
                            socialAccounts.setFollowersCountOnRegistration(accounts.getFollowersCountOnRegistration());
                        }
                        mutableData.setValue(socialAccounts);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        if (databaseError != null)
                            Toast.makeText(DCSHomeActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

//                setValue(socialAccounts).addOnCompleteListener(task -> {
//                    if (task.isSuccessful())
//                        Toast.makeText(DCSHomeActivity.this, "Account added", Toast.LENGTH_SHORT).show();
//                    else
//                        Toast.makeText(DCSHomeActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                });
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void replaceFragmentContainerContent(Fragment fragment, boolean shouldAddBackstack) {
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
        i.putExtra("design", Parcels.wrap(designerDesigns));
        startActivity(i);
    }

    private void addBadgeView() {
        menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        BottomNavigationItemView itemView;
        if (role.equalsIgnoreCase(User.USER_SOCIAL))
            itemView = (BottomNavigationItemView) menuView.getChildAt(2);
        else
            itemView = (BottomNavigationItemView) menuView.getChildAt(3);
        notificationBadge = LayoutInflater.from(DCSHomeActivity.this).inflate(R.layout.notification_badge_layout, menuView, false);
        AppCompatTextView appCompatTextView = notificationBadge.findViewById(R.id.badge);
        itemView.addView(notificationBadge);
        BottomNavigationItemView itemView1;
        if (role.equalsIgnoreCase(User.USER_SOCIAL))
            itemView1 = (BottomNavigationItemView) menuView.getChildAt(1);
        else
            itemView1 = (BottomNavigationItemView) menuView.getChildAt(2);
        View notificationBadge1 = LayoutInflater.from(DCSHomeActivity.this).inflate(R.layout.notification_badge_layout, menuView, false);
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
    public void onClientItemClicked(String businessName) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        Fragment prev = manager.findFragmentByTag("uploadDialog");
        if (prev != null)
            ft.remove(prev);
        ClientUploadsDialog.NewInstance(businessName).show(ft, "uploadDialog");
    }

    @Override
    public void onStaffItemClicked(User currentUser, User chatUser) {
        replaceFragmentContainerContent(ThreadFragment.NewInstance(currentUser, chatUser), true);
    }

    @Override
    public void onClientItemClicked(User client, SocialAccounts socialAccounts) {
        if (socialAccounts != null) {
            Toast.makeText(this, "Linking " + socialAccounts.getAccountType() + " to client", Toast.LENGTH_LONG).show();
            String key = databaseReference.child("clientSocialAccount").push().getKey();
            databaseReference.child("clientSocialAccount").child(client.getId()).child(key).setValue(socialAccounts).addOnCompleteListener(task -> {
                if (task.isSuccessful())
                    Toast.makeText(getApplicationContext(), socialAccounts.getAccountType() + " linked to client", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onFileUDownloadInitiliazed(String fileName, String url) {
        Intent intent = new Intent(this, FileDownloadService.class);
        intent.putExtra("fileName", fileName);
        intent.putExtra("url", url);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Menu menu = navigation.getMenu();
        if (role.equals(User.USER_SOCIAL)) {
            menu.removeItem(R.id.navigation_client_upload);
        } else {
            menu.removeItem(R.id.navigation_scheduled);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "DataSnapshot:" + dataSnapshot);
                if (dataSnapshot.getValue(Boolean.class)) {
                    currentUserRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    currentUserRef.setValue(true);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(LOG_TAG, "DatabaseError:" + databaseError);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        currentUserRef.setValue(ServerValue.TIMESTAMP);
    }

    public void signOut(View view) {
        DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                FirebaseAuth.getInstance().signOut();
                user = FirebaseAuth.getInstance().getCurrentUser();
                //AppApplication.getInstance().clearApplicationData();
                PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
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


    @Override
    public void onSocialAccountItemClicked(SocialAccounts socialAccounts) {
        replaceFragmentContainerContent(SocialAccountFragment.NewInstance(socialAccounts), true);
    }

    @Override
    public void onTwitterButtonClicked() {
        twitterLoginButton.performClick();
    }
}
