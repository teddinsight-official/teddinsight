package ng.com.teddinsight.teddinsight_app.activities;


import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.evernote.android.state.State;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.fragments.AdminHomeFragment;
import ng.com.teddinsight.teddinsight_app.fragments.ClientDetailsFragment;
import ng.com.teddinsight.teddinsight_app.fragments.ClientListFragment;
import ng.com.teddinsight.teddinsight_app.fragments.CustomerSupportFragment;
import ng.com.teddinsight.teddinsight_app.fragments.DesignsFragment;
import ng.com.teddinsight.teddinsight_app.fragments.LogsFragment;
import ng.com.teddinsight.teddinsight_app.fragments.ScheduledPostFragment;
import ng.com.teddinsight.teddinsight_app.models.SocialAccounts;
import ng.com.teddinsight.teddinsightchat.fragments.ChatListFragment;

import ng.com.teddinsight.teddinsightchat.models.User;

public class AdminActivity extends BaseActivity implements ClientListFragment.ClientItemClickedListener {
    Drawer drawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    SharedPreferences preferences;
    String firstName;
    String lastName;
    String email;
    String profileImage;
    private PrimaryDrawerItem conversationDrawerItem;
    private FragmentManager fragmentManager;
    @State
    long currentState;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.contains("firstName")) {
            firstName = preferences.getString("firstName", "First");
            lastName = preferences.getString("lastName", "Last");
            email = preferences.getString("email", "@teddinsight.com");
            profileImage = preferences.getString("profileImageUrl", "https://avatars3.githubusercontent.com/u/1476232?v=3&s=460");
            role = preferences.getString("role", "");
        }
        Typeface typeface = ResourcesCompat.getFont(this, R.font.some_sans);
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withIcon(GoogleMaterial.Icon.gmd_home).withName("Home").withTypeface(typeface);
        conversationDrawerItem = new PrimaryDrawerItem().withName("Conversations").withIdentifier(3).withTypeface(typeface).withIcon(GoogleMaterial.Icon.gmd_chat)
                .withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700));


        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTextColor(Color.WHITE)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(firstName.concat(" " + lastName)).withEmail(email)
                                .withIcon(profileImage)
                                .withTypeface(typeface)
                                .withTextColor(Color.WHITE)
                ).build();
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withSavedInstance(savedInstanceState)
                .withAccountHeader(headerResult)
                .withToolbar(toolbar)
                .addDrawerItems(
                        item1,
                        new PrimaryDrawerItem().withName("Scheduled Posts").withIcon(GoogleMaterial.Icon.gmd_schedule).withIdentifier(2).withTypeface(typeface),
                        new PrimaryDrawerItem().withName("Designs").withIcon(GoogleMaterial.Icon.gmd_image_aspect_ratio).withIdentifier(9).withTypeface(typeface),
                        conversationDrawerItem,
                        new PrimaryDrawerItem().withName("Logs").withIcon(GoogleMaterial.Icon.gmd_note).withIdentifier(4).withTypeface(typeface),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Clients").withIcon(GoogleMaterial.Icon.gmd_people).withIdentifier(5).withTypeface(typeface),
                        new PrimaryDrawerItem().withName("Partners").withIcon(GoogleMaterial.Icon.gmd_nature_people).withIdentifier(6).withTypeface(typeface),
                        new PrimaryDrawerItem().withName("Opened Tickets").withIcon(GoogleMaterial.Icon.gmd_help_outline).withIdentifier(7).withTypeface(typeface),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Log Out").withIcon(GoogleMaterial.Icon.gmd_settings_power).withTypeface(typeface).withIdentifier(8)
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem != null && currentState != drawerItem.getIdentifier()) {
                        long identifier = drawerItem.getIdentifier();
                        currentState = identifier;
                        if (identifier == 3)
                            replaceFragmentContainerContent(ChatListFragment.NewInstance(), false);
                        else if (identifier == 5)
                            replaceFragmentContainerContent(ClientListFragment.NewInstance(true, User.USER_CLIENT), false);
                        else if (identifier == 8)
                            signOut(null);
                        else if (identifier == 6)
                            replaceFragmentContainerContent(ClientListFragment.NewInstance(true, User.USER_PARTNER), false);
                        else if (identifier == 1)
                            replaceFragmentContainerContent(AdminHomeFragment.NewInstance(), false);
                        else if (identifier == 2)
                            replaceFragmentContainerContent(ScheduledPostFragment.NewInstance(User.USER_ADMIN), false);
                        else if (identifier == 7)
                            replaceFragmentContainerContent(CustomerSupportFragment.NewInstance(), false);
                        else if (identifier == 4) {
                            replaceFragmentContainerContent(LogsFragment.NewInstance(), false);
                        }else if (identifier == 9)
                            replaceFragmentContainerContent(DesignsFragment.NewInstance(User.USER_ADMIN), false);
                    }
                    return false;
                })
                .build();
        drawer.setSelection(1, true);

        fragmentManager.addOnBackStackChangedListener(() -> {
            toolbar.setVisibility(fragmentManager.getBackStackEntryCount() > 0 ? View.GONE : View.VISIBLE);
        });

    }

    void replaceFragmentContainerContent(Fragment fragment, boolean shouldAddBackstack) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.leave, R.anim.pop_enter, R.anim.pop_leave);
        fragmentTransaction.replace(R.id.frame_container, fragment);
        if (shouldAddBackstack)
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState = drawer.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onClientItemClicked(String businessName) {

    }

    @Override
    public void onClientItemClicked(User client, SocialAccounts socialAccounts) {
        replaceFragmentContainerContent(ClientDetailsFragment.NewInstance(client), true);
    }

    public void goBack(View view) {
        fragmentManager.popBackStack();
    }

    public void continueAsAnotherUser(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.user_content:
                intent = new Intent(this, DCSHomeActivity.class);
                intent.putExtra(LoginActivity.STAFF_ROLE, User.USER_CONTENT);
                break;
            case R.id.user_designer:
                intent = new Intent(this, DCSHomeActivity.class);
                intent.putExtra(LoginActivity.STAFF_ROLE, User.USER_DESIGNER);
                break;
            case R.id.user_social:
                intent = new Intent(this, DCSHomeActivity.class);
                intent.putExtra(LoginActivity.STAFF_ROLE, User.USER_SOCIAL);
                break;
            case R.id.user_hr:
                intent = new Intent(this, HrHomeActivity.class);
                break;
            default:
                intent = new Intent();
        }
        startActivity(intent);
    }
}