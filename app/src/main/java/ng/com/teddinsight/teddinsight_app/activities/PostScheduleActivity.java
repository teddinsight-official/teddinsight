package ng.com.teddinsight.teddinsight_app.activities;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.MediaService;
import com.twitter.sdk.android.core.services.StatusesService;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;

import java.util.Calendar;
import java.util.HashMap;

import java.util.Map;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.zelory.compressor.Compressor;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import io.reactivex.schedulers.Schedulers;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.fragments.DesignsFragment;
import ng.com.teddinsight.teddinsight_app.fragments.SocialMediaManagerHomeFragment;
import ng.com.teddinsight.teddinsight_app.models.ScheduledPost;
import ng.com.teddinsight.teddinsight_app.models.SocialAccounts;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsight_app.utils.SocialPostScheduleWorker;
import ng.com.teddinsight.teddinsightchat.models.User;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class PostScheduleActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, View.OnClickListener {

    private static final String LOG_TAG = PostScheduleActivity.class.getSimpleName();
    @BindView(R.id.compose_box)
    TextInputEditText mComposeBoxEdittext;
    @BindView(R.id.media1)
    ImageView mMedia1ImageView;
    @BindView(R.id.media2)
    ImageView mMedia2ImageView;
    @BindView(R.id.media3)
    ImageView mMedia3ImageView;
    @BindView(R.id.media4)
    ImageView mMedia4ImageView;
    @BindView(R.id.radio)
    RadioGroup mRadioGroup;
    @BindView(R.id.time_picker)
    EditText mPostTimeEdittext;
    TwitterAuthToken mTwitterAuthToken;
    TwitterSession mTwitterSession;
    TwitterApiClient mTwitterApiClient;
    MediaService mTwitterMediaService;
    Calendar now = Calendar.getInstance();
    Calendar postTime = now;
    private long postTimestamp = System.currentTimeMillis();
    private SocialAccounts socialAccounts;
    private ImageView selectedImageView;
    private ProgressDialog progressDialog;
    private File compressedImage;
    private Map<String, String> mediaIds;
    @BindView(R.id.post_title)
    TextInputEditText postTitleEdittext;
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference(ScheduledPost.SCHEDULE_PATH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_schedule);
        ButterKnife.bind(this);
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> mPostTimeEdittext.setVisibility(checkedId == R.id.post_later ? View.VISIBLE : View.INVISIBLE));
        if (getIntent() != null) {
            socialAccounts = Parcels.unwrap(getIntent().getParcelableExtra(SocialMediaManagerHomeFragment.SOCIAL_ACCOUNT_PATH));
        }
        if (socialAccounts == null)
            finish();
        else {
            setUp();
        }
    }

    private void setUp() {
        progressDialog = new ProgressDialog(this);
        mediaIds = new HashMap<>();
        mTwitterAuthToken = new TwitterAuthToken(socialAccounts.getTwitterUserToken(), socialAccounts.getTwitterSecreteToken());
        mTwitterSession = new TwitterSession(mTwitterAuthToken, socialAccounts.getTwitterUserId(), socialAccounts.getAccountUsername());
        mTwitterApiClient = new TwitterApiClient(mTwitterSession);
        mTwitterMediaService = mTwitterApiClient.getMediaService();
        if (socialAccounts.getAccountType().equalsIgnoreCase(SocialAccounts.ACCOUNT_TYPE_INSTAGRAM)) {
            mMedia2ImageView.setVisibility(View.GONE);
            mMedia3ImageView.setVisibility(View.GONE);
            mMedia4ImageView.setVisibility(View.GONE);
        }
        mMedia1ImageView.setOnClickListener(this);
        mMedia2ImageView.setOnClickListener(this);
        mMedia3ImageView.setOnClickListener(this);
        mMedia4ImageView.setOnClickListener(this);
    }


    @OnClick(R.id.time_picker)
    public void showPickerDialog(View view) {
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(getSupportFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        TimePickerDialog.newInstance(this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                false).show(getSupportFragmentManager(), "Timepickerdialog");
        postTime.set(Calendar.YEAR, year);
        postTime.set(Calendar.MONTH, monthOfYear);
        postTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        postTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        postTime.set(Calendar.MINUTE, minute);
        postTime.set(Calendar.SECOND, second);
        postTimestamp = postTime.getTimeInMillis();
        mPostTimeEdittext.setText(ExtraUtils.getHumanReadableString(postTimestamp, false));
    }

    @Override
    public void onClick(View v) {
        selectedImageView = (ImageView) v;
        CropImage.startPickImageActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
            Log.e(LOG_TAG, "res");
            if (data != null) {
                Uri imageUri = data.getData();
                CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).start(this);
            } else
                Toast.makeText(getApplicationContext(), "image not found", Toast.LENGTH_SHORT).show();
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                startCompression(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void startCompression(Uri uri) {
        progressDialog.setMessage("Compressing Image, Please wait");
        progressDialog.show();
        File imageFile = new File(uri.getPath());
        Disposable sc = new Compressor(this)
                .compressToFileAsFlowable(imageFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file -> {
                    compressedImage = file;
                    if (socialAccounts.getAccountType().equalsIgnoreCase(SocialAccounts.ACCOUNT_TYPE_TWITTER))
                        prepareMediaForUploadToTwitter(getMediaRequestBodyAsFlowable(compressedImage));
                    else {
                        selectedImageView.setImageURI(Uri.fromFile(compressedImage));
                        progressDialog.dismiss();
                    }
                }, throwable -> {
                    progressDialog.dismiss();
                    throwable.printStackTrace();
                    Log.e(LOG_TAG, throwable.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void prepareMediaForUploadToTwitter(Flowable<RequestBody> requestBodyFlowable) {
        progressDialog.setMessage("Uploading media to Twitter, please wait !!!");
        Disposable schedule = requestBodyFlowable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::uploadMediaToTwitter, throwable -> {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadMediaToTwitter(RequestBody media) {
        mTwitterMediaService.upload(media, null, null).enqueue(new Callback<Media>() {
            @Override
            public void success(Result<Media> result) {
                progressDialog.dismiss();
                selectedImageView.setImageBitmap(BitmapFactory.decodeFile(compressedImage.getPath()));
                mediaIds.put(String.valueOf(selectedImageView.getId()), result.data.mediaIdString);
            }

            @Override
            public void failure(TwitterException exception) {
                progressDialog.dismiss();
                showToast(exception.getLocalizedMessage(), Toast.LENGTH_LONG);
            }
        });
    }

    public Flowable<RequestBody> getMediaRequestBodyAsFlowable(File compressedImage) {
        return Flowable.defer((Callable<Flowable<RequestBody>>) () -> Flowable.just(getMediaBytes(compressedImage)));
    }

    private RequestBody getMediaBytes(File compressedImage) {
        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");
        Bitmap bmp = BitmapFactory.decodeFile(compressedImage.getPath());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] media = stream.toByteArray();
        bmp.recycle();
        return RequestBody.create(MEDIA_TYPE_PNG, media);
    }

    private void showToast(String message, int toastLength) {
        Toast.makeText(getApplicationContext(), message, toastLength).show();
    }

    @OnClick(R.id.schedule_post_button)
    public void schedulePost() {
        String tweet = mComposeBoxEdittext.getText().toString().trim();
        if (TextUtils.isEmpty(tweet) && socialAccounts.getAccountType().equalsIgnoreCase(SocialAccounts.ACCOUNT_TYPE_TWITTER)) {
            showToast("Tweet cannot be empty", Toast.LENGTH_LONG);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : mediaIds.values()) {
            stringBuilder.append(s).append(",");
        }
        String medias = stringBuilder.toString();
        ScheduledPost scheduledPost;
        if (mRadioGroup.getCheckedRadioButtonId() == R.id.post_now) {
            if (socialAccounts.getAccountType().equalsIgnoreCase(SocialAccounts.ACCOUNT_TYPE_TWITTER)) {
                progressDialog.setMessage("Tweeting");
                progressDialog.show();
                StatusesService statusesService = mTwitterApiClient.getStatusesService();
                Call<Tweet> tweetCall = statusesService.update(tweet, null, null, null, null, null, null, null, medias);
                tweetCall.enqueue(new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> result) {
                        progressDialog.dismiss();
                        showToast("Tweet sent", Toast.LENGTH_LONG);
                        Log.e(LOG_TAG, result.response.toString());
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        progressDialog.dismiss();
                        showToast(exception.getLocalizedMessage(), Toast.LENGTH_LONG);
                        Log.e(LOG_TAG, exception.getLocalizedMessage());
                    }
                });
            } else {
                if (compressedImage != null)
                    ExtraUtils.createInstagramIntent(this, compressedImage.getPath());
                else
                    showToast("Please select an image", Toast.LENGTH_LONG);
            }
        } else {
            String postTitle = postTitleEdittext.getText().toString().trim();
            if (TextUtils.isEmpty(postTitle)) {
                showToast("please enter a title/description for this post", Toast.LENGTH_SHORT);
                postTitleEdittext.setError("Cannot be blank");
                postTitleEdittext.requestFocus();
                return;
            }
            if (socialAccounts.getAccountType().equalsIgnoreCase(SocialAccounts.ACCOUNT_TYPE_TWITTER)) {
                scheduledPost = new ScheduledPost(
                        socialAccounts.getAccountType(),
                        socialAccounts.getAccountType().toLowerCase().concat("" + socialAccounts.getTwitterUserId()),
                        socialAccounts.getAccountUsername(),
                        tweet,
                        medias,
                        false,
                        false,
                        postTimestamp,
                        socialAccounts.getTwitterUserToken(),
                        socialAccounts.getTwitterUserId(),
                        socialAccounts.getTwitterUserName(),
                        socialAccounts.getTwitterSecreteToken());
            } else {
                if (compressedImage == null) {
                    showToast("Please select an image", Toast.LENGTH_LONG);
                    return;
                }
                scheduledPost = new ScheduledPost(socialAccounts.getAccountType(),
                        socialAccounts.getAccountType().toLowerCase().concat(socialAccounts.getInstagramId()),
                        socialAccounts.getAccountUsername(),
                        "",
                        compressedImage.getPath(),
                        false,
                        false,
                        System.currentTimeMillis(),
                        socialAccounts.getInstagramId());
            }
            scheduledPost.setPostTitle(postTitle);
            String key = reference.push().getKey();
            Data.Builder builder = new Data.Builder();
            String id = String.valueOf(socialAccounts.getAccountType().equals(SocialAccounts.ACCOUNT_TYPE_TWITTER) ? socialAccounts.getTwitterUserId() : socialAccounts.getInstagramId());
            builder.putString(SocialPostScheduleWorker.SCHEDULE_ID, key);
            Data.Builder builderReminder = new Data.Builder();
            builderReminder.putBoolean(SocialPostScheduleWorker.IS_REMINDER, true);
            Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            OneTimeWorkRequest reminderRequest =
                    new OneTimeWorkRequest.Builder(SocialPostScheduleWorker.class)
                            .setInitialDelay((postTimestamp - System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)), TimeUnit.MILLISECONDS)
                            .addTag(scheduledPost.getAccountType().toLowerCase().concat(scheduledPost.getAccountUsername()).concat("reminder"))
                            .setInputData(builderReminder.build())
                            .build();
            OneTimeWorkRequest postRequest =
                    new OneTimeWorkRequest.Builder(SocialPostScheduleWorker.class)
                            .setInitialDelay((postTimestamp - System.currentTimeMillis()), TimeUnit.MILLISECONDS)
                            .addTag(scheduledPost.getAccountType().toLowerCase().concat(scheduledPost.getAccountUsername()))
                            .setConstraints(constraints)
                            .setInputData(builder.build())
                            .build();
            WorkManager manager = WorkManager.getInstance(this);
            reference.child(key).setValue(scheduledPost).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    manager.enqueue(reminderRequest);
                    manager.enqueue(postRequest);
                    showToast("Post scheduled for " + ExtraUtils.getHumanReadableString(postTimestamp, false), Toast.LENGTH_LONG);
                    finish();
                } else
                    showToast(task.getException().getMessage(), Toast.LENGTH_LONG);
            });

        }
    }

    public void seeAndDownload(View view) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("download");
        if (prev != null)
            transaction.remove(prev);
        DesignsFragment.NewInstance(User.USER_SOCIAL).show(transaction, "download");
    }
}
