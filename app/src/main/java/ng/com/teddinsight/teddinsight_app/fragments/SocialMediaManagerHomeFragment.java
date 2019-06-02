package ng.com.teddinsight.teddinsight_app.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.adapter.SocialAccountsAdapter;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners;
import ng.com.teddinsight.teddinsight_app.models.SocialAccounts;

public class SocialMediaManagerHomeFragment extends Fragment {

    @BindView(R.id.designs_grid)
    RecyclerView socialAccountsRecycler;
    @BindView(R.id.new_design_button)
    FloatingActionButton addNewSocialAccount;
    @BindView(R.id.designs_title)
    TextView designTitle;
    @BindView(R.id.d_swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.home_title)
    TextView homeTitle;
    SocialAccountsAdapter adapter;
    private DatabaseReference socialAccountRef;
    public static final String SOCIAL_ACCOUNT_PATH = "socialAccounts";
    Listeners.SocialAccountsListener socialAccountsListener;
    private Context mContext;

    public static SocialMediaManagerHomeFragment NewInstance() {
        return new SocialMediaManagerHomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_dcs, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeTitle.setText(mContext.getString(R.string.social_manager));
        designTitle.setText(mContext.getString(R.string.social_accounts));
        refreshLayout.setRefreshing(true);
        refreshLayout.setColorSchemeColors(Color.RED, Color.BLUE, Color.GREEN, Color.CYAN);
        refreshLayout.setOnRefreshListener(this::getSocialAccounts);
        socialAccountRef = FirebaseDatabase.getInstance().getReference(SOCIAL_ACCOUNT_PATH);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        socialAccountsRecycler.setLayoutManager(linearLayoutManager);
        socialAccountsRecycler.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        adapter = new SocialAccountsAdapter(socialAccountsListener);
        socialAccountsRecycler.setAdapter(adapter);
        getSocialAccounts();
    }

    private void getSocialAccounts() {
        socialAccountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<SocialAccounts> socialAccountsList = new ArrayList<>();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    SocialAccounts socialAccount = snap.getValue(SocialAccounts.class);
                    socialAccountsList.add(socialAccount);
                }
                adapter.swapData(socialAccountsList);
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                refreshLayout.setRefreshing(false);
            }
        });
    }

    @OnClick(R.id.new_design_button)
    public void addNewSocialAccountToDatabase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setCancelable(false);
        View accountTypeView = getLayoutInflater().inflate(R.layout.add_new_account_dialog, null, false);
        builder.setView(accountTypeView)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        ImageButton instButton = accountTypeView.findViewById(R.id.instagram_button);
        ImageButton twitterButton = accountTypeView.findViewById(R.id.twitter_button);
        AlertDialog dialog = builder.show();
        instButton.setOnClickListener(v -> {
            dialog.cancel();
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            Fragment prev = getChildFragmentManager().findFragmentByTag("ig");
            if (prev != null)
                fragmentTransaction.remove(prev);

            WebViewFragment.NewInstance("https://api.instagram.com/oauth/authorize/?client_id=54525af8e1934759a6652e6e2f10966b&redirect_uri=https://teddinsight.com.ng/redirects.php&response_type=token")
                    .show(fragmentTransaction, "ig");
        });
        twitterButton.setOnClickListener(v -> {
            dialog.cancel();
            socialAccountsListener.onTwitterButtonClicked();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        socialAccountsListener = (Listeners.SocialAccountsListener) context;
        mContext = context;
    }
}
