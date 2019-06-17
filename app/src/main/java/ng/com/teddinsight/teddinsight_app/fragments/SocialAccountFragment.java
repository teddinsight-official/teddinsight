package ng.com.teddinsight.teddinsight_app.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.TwitterCollection;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.AccountService;

import org.parceler.Parcels;

import java.lang.reflect.Type;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.activities.DCSHomeActivity;
import ng.com.teddinsight.teddinsight_app.activities.PostScheduleActivity;
import ng.com.teddinsight.teddinsight_app.models.InstagramResponseData;
import ng.com.teddinsight.teddinsight_app.models.SocialAccounts;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsight_app.utils.InstagramCountsDeserializer;
import ng.com.teddinsight.teddinsight_app.utils.InstagramDataDeSerializer;
import ng.com.teddinsight.teddinsight_app.utils.InstagramUserApi;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SocialAccountFragment extends Fragment {

    @BindView(R.id.account_type)
    ImageView accountTypeImageView;
    @BindView(R.id.account_username)
    TextView accountUsernameTextView;
    @BindView(R.id.current_followers)
    TextView followerCountTextView;
    @BindView(R.id.current_following)
    TextView followingCountTextView;
    @BindView(R.id.date_registered)
    TextView dateRegisteredTextView;
    @BindView(R.id.followers_at_registration)
    TextView followersAtRegistrationTextView;
    @BindView(R.id.followers_now)
    TextView followersNowTextView;
    @BindView(R.id.followers_gained)
    TextView followersGainedTextView;
    @BindView(R.id.growth_percentage)
    TextView growthPercentageTextView;
    @BindView(R.id.schedule_post_button)
    Button schedulePostButton;
    @BindView(R.id.snackbar_root)
    View snackbarView;
    Snackbar snackbar;

    private SocialAccounts socialAccount;
    private ProgressDialog progressDialog;
    private Context mContext;


    public static SocialAccountFragment NewInstance(SocialAccounts socialAccounts) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SocialMediaManagerHomeFragment.SOCIAL_ACCOUNT_PATH, Parcels.wrap(socialAccounts));
        SocialAccountFragment socialAccountFragment = new SocialAccountFragment();
        socialAccountFragment.setArguments(bundle);
        return socialAccountFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social_account, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle fragmentArgs = getArguments();
        if (fragmentArgs.containsKey(SocialMediaManagerHomeFragment.SOCIAL_ACCOUNT_PATH))
            socialAccount = Parcels.unwrap(fragmentArgs.getParcelable(SocialMediaManagerHomeFragment.SOCIAL_ACCOUNT_PATH));
        else {
            getActivityCast().onBackPressed();
        }
        setUpViews();
    }

    @OnClick(R.id.link_to_client)
    public void linkToClientAccount() {
        getActivityCast().replaceFragmentContainerContent(ClientListFragment.NewInstance(ng.com.teddinsight.teddinsightchat.models.User.USER_CLIENT, socialAccount), true);
    }

    private void setUpViews() {
        progressDialog = new ProgressDialog(getActivityCast());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Gathering information from " + socialAccount.getAccountType() + " for account " + socialAccount.getAccountUsername());
        if (socialAccount.getAccountType().equals(SocialAccounts.ACCOUNT_TYPE_INSTAGRAM)) {
            accountTypeImageView.setImageResource(R.drawable.ic_instagram);
            getCurrentIgDetails();
        } else {
            accountTypeImageView.setImageResource(R.drawable.ic_action_twitter);
            getCurrentTwitterDetails();
        }
        accountUsernameTextView.setText(socialAccount.getAccountUsername());
        followersAtRegistrationTextView.setText(String.valueOf(socialAccount.getFollowersCountOnRegistration()));
        dateRegisteredTextView.setText(ExtraUtils.getHumanReadableString(socialAccount.getDateLogged(), true));
        schedulePostButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivityCast(), PostScheduleActivity.class);
            intent.putExtra(SocialMediaManagerHomeFragment.SOCIAL_ACCOUNT_PATH, Parcels.wrap(socialAccount));
            getActivityCast().startActivity(intent);
        });
    }

    private void getCurrentTwitterDetails() {
        TwitterAuthToken authToken = new TwitterAuthToken(socialAccount.getTwitterUserToken(), socialAccount.getTwitterSecreteToken());
        TwitterSession session = new TwitterSession(authToken, socialAccount.getTwitterUserId(), socialAccount.getAccountUsername());
        TwitterApiClient twitterApiClient = new TwitterApiClient(session);
        AccountService accountService = twitterApiClient.getAccountService();
        progressDialog.show();
        accountService.verifyCredentials(true, true, true).enqueue(new Callback<User>() {
            @Override
            public void success(Result<User> result) {
                progressDialog.dismiss();
                User user = result.data;
                followerCountTextView.setText(String.valueOf(user.followersCount));
                followingCountTextView.setText(String.valueOf(user.friendsCount));
                followersNowTextView.setText(String.valueOf(user.followersCount));
                long followersGained = user.followersCount - socialAccount.getFollowersCountOnRegistration();
                followersGainedTextView.setText(String.valueOf(followersGained));
                String percentGrowth = String.valueOf(((followersGained / socialAccount.getFollowersCountOnRegistration()) * 100)).concat(" %");
                growthPercentageTextView.setText(percentGrowth);
            }

            @Override
            public void failure(TwitterException exception) {
                progressDialog.dismiss();
                snackbar = Snackbar.make(snackbarView, exception.getMessage().concat(" If you are sure it's not a network error, try adding account again"),
                        Snackbar.LENGTH_INDEFINITE)
                        .setActionTextColor(ContextCompat.getColor(getActivityCast(), R.color.yellow_900))
                        .setAction("Dismiss", v -> snackbar.dismiss());
                snackbar.show();
            }
        });
    }

    private void getCurrentIgDetails() {
        progressDialog.show();
        InstagramDataDeSerializer<InstagramResponseData> instagramDataDeSerializer =
                new InstagramDataDeSerializer<>();
        InstagramCountsDeserializer<InstagramResponseData.InstagramAccountCount> countsDeserializer = new InstagramCountsDeserializer<>();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(InstagramResponseData.class, instagramDataDeSerializer)
                .registerTypeAdapter(InstagramResponseData.InstagramAccountCount.class, countsDeserializer).create();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(InstagramUserApi.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        Call<InstagramResponseData> instagramResponseDataCall = retrofit.create(InstagramUserApi.class).getInstagramDataWithAccessToken(socialAccount.getInstagramAccessToken());
        instagramResponseDataCall.enqueue(new retrofit2.Callback<InstagramResponseData>() {
            @Override
            public void onResponse(Call<InstagramResponseData> call, Response<InstagramResponseData> response) {
                InstagramResponseData instagramResponseData = response.body();
                InstagramResponseData.InstagramAccountCount instagramAccountCount = instagramResponseData.instagramAccountCount;
                followerCountTextView.setText(String.valueOf(instagramAccountCount.followersCount));
                followingCountTextView.setText(String.valueOf(instagramAccountCount.followingCount));
                followersNowTextView.setText(String.valueOf(instagramAccountCount.followersCount));
                long followersGained = instagramAccountCount.followersCount - socialAccount.getFollowersCountOnRegistration();
                followersGainedTextView.setText(String.valueOf(followersGained));
                float percentage = (float) followersGained / socialAccount.getFollowersCountOnRegistration();
                String percentGrowth = String.valueOf(Math.round(percentage * 100)).concat(" %");
                growthPercentageTextView.setText(percentGrowth);
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<InstagramResponseData> call, Throwable t) {
                progressDialog.dismiss();
                snackbar = Snackbar.make(snackbarView, t.getMessage().concat(" If you are sure it's not a network error, try adding account again"),
                        Snackbar.LENGTH_INDEFINITE)
                        .setActionTextColor(ContextCompat.getColor(getActivityCast(), R.color.yellow_900))
                        .setAction("Dismiss", v -> snackbar.dismiss());
                snackbar.show();
            }
        });
    }

    private DCSHomeActivity getActivityCast() {
        return (DCSHomeActivity) getActivity();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (snackbar != null) {
            if (snackbar.isShown())
                snackbar.dismiss();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
}
