package ng.com.teddinsight.teddinsight_app.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.fragments.ConversationFragment;
import ng.com.teddinsight.teddinsight_app.fragments.DesignerHomeFragment;
import ng.com.teddinsight.teddinsight_app.fragments.TaskFragment;

public class DesignerActivity extends AppCompatActivity {
    public static final String FRAGMENT_HOME = "home";
    public static final String FRAGMENT_TASK = "task";
    public static final String FRAGMENT_CONVERSATION = "conversatiom";

    FragmentManager fragmentManager;

    private String currentFragment = FRAGMENT_HOME;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        if (currentFragment.equals(FRAGMENT_HOME))
                            return false;
                        currentFragment = FRAGMENT_HOME;
                        replaceFragmentContainerContent(DesignerHomeFragment.NewInstance());
                        return true;
                    case R.id.navigation_dashboard:
                        if (currentFragment.equals(FRAGMENT_TASK))
                            return false;
                        currentFragment = FRAGMENT_TASK;
                        replaceFragmentContainerContent(TaskFragment.NewInstance());
                        return true;
                    case R.id.navigation_notifications:
                        if (currentFragment.equals(FRAGMENT_CONVERSATION))
                            return false;
                        currentFragment = FRAGMENT_CONVERSATION;
                        replaceFragmentContainerContent(ConversationFragment.NewInstance());
                        return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designer);

        fragmentManager = getSupportFragmentManager();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        replaceFragmentContainerContent(DesignerHomeFragment.NewInstance());
    }

    void replaceFragmentContainerContent(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.leave, R.anim.pop_enter, R.anim.pop_leave);
        fragmentTransaction.replace(R.id.designer_contents, fragment).commit();
    }

}
