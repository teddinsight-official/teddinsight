package ng.com.teddinsight.teddinsight_app.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsightchat.models.User;
import pub.devrel.easypermissions.EasyPermissions;

public class LoginActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int RC_SIGN_IN = 100;
    private static final String TAG = LoginActivity.class.getCanonicalName();
    public static final String STAFF_ROLE = "staff_role";
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference ref;
    ProgressDialog dialog;
    SharedPreferences preferences;
    @BindView(R.id.email_address)
    EditText emailEditTextView;
    @BindView(R.id.password)
    EditText passwordEditText;
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.username_frame)
    FrameLayout usernameFrame;
    @BindView(R.id.password_frame)
    FrameLayout passwordFrame;
    @BindView(R.id.forgot_password)
    TextView forgotPassword;
    @BindView(R.id.sign_in_frame)
    FrameLayout signInFrame;
    User userInfo;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public static final int PERMISSION_REQUESTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.animation_distance, outValue, true);
        float value = outValue.getFloat();
        ObjectAnimator animation = ObjectAnimator.ofFloat(logo, "translationY", value);
        animation.setDuration(100);
        animation.start();

        ObjectAnimator upAnimation = ObjectAnimator.ofFloat(logo, "translationY", -value);
        upAnimation.setDuration(2000);
        upAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                usernameFrame.setVisibility(View.VISIBLE);
                passwordFrame.setVisibility(View.VISIBLE);
                forgotPassword.setVisibility(View.VISIBLE);
                signInFrame.setVisibility(View.VISIBLE);
            }
        });
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setTheme(R.style.AppTheme);
                logo.setVisibility(View.VISIBLE);
                upAnimation.start();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference();
        dialog = new ProgressDialog(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void databaseSetup() {
        User user = new User();
        Map<String, Object> userValues = user.toMap(false);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + User.getTableName() + "/qzzryZxqZZhkbAksVo7aL2k7rF73/", userValues);
        ref.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(LoginActivity.this, "done", Toast.LENGTH_LONG).show();
                else {
                    Toast.makeText(LoginActivity.this, "exp: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @OnClick(R.id.login_button)
    public void signIn() {
        String email = emailEditTextView.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast("all fields are required");
            return;
        }
        dialog.setMessage("Authenticating");
        dialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mUser = mAuth.getCurrentUser();
                    getUserDetailsFromDb();
                } else {
                    dialog.dismiss();
                    showToast(task.getException().getLocalizedMessage());
                }
            }
        });

    }

    @OnClick(R.id.forgot_password)
    public void sendPasswordResetMail() {
        String email = emailEditTextView.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            emailEditTextView.setError("Input your email");
            Toast.makeText(this, "Input your email", Toast.LENGTH_LONG).show();
            return;
        }
        dialog.setMessage("Sending password reset mail to " + email);
        dialog.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            dialog.dismiss();
            if (task.isSuccessful())
                Toast.makeText(getApplicationContext(), "Password reset mail sent to " + email, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void getUserDetailsFromDb() {
        String uid = mUser.getUid();
        ref.child("/" + User.getTableName() + "/" + uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("TAG", "" + dataSnapshot.getValue());
                userInfo = dataSnapshot.getValue(User.class);
                if (userInfo == null || !userInfo.hasAccess) {
                    mAuth.signOut();
                    dialog.dismiss();
                    showRevokedDialog();
                    return;
                }
                ref.child("/" + User.getTableName() + "/" + uid).child("deviceToken").setValue(FirebaseInstanceId.getInstance().getToken());
                SharedPreferences.Editor editor = preferences.edit();
                Log.e("TAG", userInfo.getRole());
                Log.e("TAG", userInfo.getEmail());
                for (Map.Entry<String, String> entry : userInfo.toStringMap().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    editor.putString(key, value);
                }
                editor.apply();
                chooseDestination(userInfo.role);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dialog.dismiss();
                showToast(databaseError.getMessage());
            }
        });
    }

    private void chooseDestination(String role) {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "e: " + e.getMessage());
            }
        }
        switch (role) {
            case User.USER_DESIGNER:
                redirect(DCSHomeActivity.class, role);
                break;
            case User.USER_CONTENT:
                redirect(DCSHomeActivity.class, role);
                break;
            case User.USER_HR:
                redirect(HrHomeActivity.class, role);
                break;
            case User.USER_SOCIAL:
                redirect(DCSHomeActivity.class, role);
                break;
            case User.USER_ADMIN:
                redirect(AdminActivity.class, role);
                break;
            default:
                mUser = null;
                mAuth.signOut();
                showToast("User role not defined");
                preferences.edit().clear().apply();
                break;
        }
    }

    private void redirect(Class destination, String staffRole) {
        Intent intent = new Intent(this, destination);
        intent.putExtra(STAFF_ROLE, staffRole);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().hasExtra("revoke")) {
            showRevokedDialog();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, ExtraUtils.getHumanReadableString(System.currentTimeMillis(), false));
//        try {
//            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//            String version = pInfo.versionName;
//            Log.e("log", version);
//            if (!preferences.contains("has2.3Signed")) {
//                if (version.equalsIgnoreCase("2.3"))
//                    FirebaseAuth.getInstance().signOut();
//                user = FirebaseAuth.getInstance().getCurrentUser();
//                SharedPreferences.Editor editor = preferences.edit();
//                editor.putBoolean("has2.3Signed", true);
//                editor.apply();
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (preferences.contains("role")) {
                String role = preferences.getString("role", "");
                chooseDestination(role);
            } else {
                Log.e("TAG", "shit didn't save to preference");
            }
        } else {
            Log.e("TAG", "user is null");
        }
    }

    private void showRevokedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Access Revoked")
                .setMessage("Your access to this app has been revoked by an admin user. Sort it out with them")
                .setPositiveButton("Ok", (dialog, which) -> {
                    dialog.cancel();
                });
        try {
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "e: " + e.getLocalizedMessage());
        }

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}
