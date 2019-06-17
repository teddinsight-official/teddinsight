package ng.com.teddinsight.teddinsight_app.activities;


import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;



import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.fragments.HrHomeFragment;
import ng.com.teddinsight.teddinsight_app.fragments.StaffDetailFragment;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners;
import ng.com.teddinsight.teddinsightchat.fragments.ChatListFragment;

import ng.com.teddinsight.teddinsightchat.models.User;

public class HrHomeActivity extends BaseActivity implements Listeners.HrMainContentListener, Listeners.UserItemClickListener {

    private static final String TAG = HrHomeActivity.class.getSimpleName();
    private DatabaseReference mUserRef;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr_home);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You now have access, please log in again", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        if (savedInstanceState == null) {
            onHrMainContentReplacementRequest(HrHomeFragment.NewInstance(), false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user == null) {
            Log.e(TAG, "user is null");
            Toast.makeText(this, "Please Login Again!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }


    @Override
    public void onHrMainContentReplacementRequest(Fragment fragment, boolean shouldAddBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        if (shouldAddBackStack)
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onUserItemClicked(User user) {
        onHrMainContentReplacementRequest(StaffDetailFragment.NewInstance(user), true);
    }

    public void goBack(View view) {
        onBackPressed();
    }

    public void showStaffList(View view) {
        onHrMainContentReplacementRequest(ChatListFragment.NewInstance(), true);
    }

}
