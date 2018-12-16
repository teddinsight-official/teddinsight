package ng.com.teddinsight.teddinsight_app.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.models.User;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int RC_SIGN_IN = 100;
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
    User userInfo;
    public static final int PERMISSION_REQUESTS= 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference();
        dialog = new ProgressDialog(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void databaseSetup() {
        User user = new User("oluwatayo", "Oluwatayo", "Adegboye", "oluwatayoadegboye@gmail.com", "partner", "token");
        Map<String, Object> userValues = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + User.getTableName() + "/qzzryZxqZZhkbAksVo7aL2k7rF73/", userValues);
        ref.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(MainActivity.this, "done", Toast.LENGTH_LONG).show();
                else {
                    Toast.makeText(MainActivity.this, "exp: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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

    private void getUserDetailsFromDb() {
        String uid = mUser.getUid();
        ref.child("/" + User.getTableName() + "/" + uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userInfo = dataSnapshot.getValue(User.class);
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

    private void chooseDestination(String role){
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        switch (role) {
            case "designer":
                redirect(DesignerActivity.class);
                break;
            case "content":
                redirect(ContentActivity.class);
                break;
            default:
                mUser = null;
                mAuth.signOut();
                showToast("User role not defined");
                break;
        }
    }

    private void redirect(Class destination) {
        startActivity(new Intent(this, destination));
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            if (preferences.contains("role")){
                String role = preferences.getString("role", "");
                chooseDestination(role);
            }else{
                Log.e("TAG", "shit didn't save to preference");
            }
        }else{
            Log.e("TAG", "user is null");
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}
