package ng.com.teddinsight.teddinsight_app.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.models.DesignerDesigns;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


import static android.app.Activity.RESULT_CANCELED;


public class DesignerHomeFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private static final int PERMISSIONS_REQUEST_CODE = 1001;
    private int GALLERY = 1, CAMERA = 2;

    public static final Fragment NewInstance() {
        return new DesignerHomeFragment();
    }

    @BindView(R.id.designs_grid)
    GridView designGrid;
    private String templateTitle;
    Uri contentURI;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;
    private Uri imageDownloadUri;
    DatabaseReference designRef;

    View v;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_home_designer, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        storageRef = storage.getReference();
        designRef = FirebaseDatabase.getInstance().getReference("designer");
    }

    private void requestCameraPermission() {
        if (!EasyPermissions.hasPermissions(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(this,
                    "This app needs to use your camera and read storage. Please accept all permissions else app will misbehave",
                    PERMISSIONS_REQUEST_CODE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @OnClick(R.id.new_design_button)
    public void newDesign() {
        requestCameraPermission();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setTitle("Template Title");
        alertDialog.setIcon(R.drawable.ic_text_fields_24dp);
        alertDialog.setView(input);
        alertDialog.setPositiveButton("Proceed", (dialog, which) -> {
            templateTitle = input.getText().toString().trim();
            dialog.dismiss();
            if (TextUtils.isEmpty(templateTitle)) {
                showText("Enter a title for your template");
            } else
                choosePhotoFromGallary();

        });

        alertDialog.show();
    }


    private void showText(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }


    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    private void uploadFile() {
        if (contentURI != null) {
            StorageReference imageRef = storageRef.child("designer/" + templateTitle);
            UploadTask uploadTask = imageRef.putFile(contentURI);
            AlertDialog.Builder progressDialog = new AlertDialog.Builder(getContext());
            View v = LayoutInflater.from(getContext()).inflate(R.layout.upload_dialog_layout, null, false);
            final ProgressBar progressBar = v.findViewById(R.id.progressBar);
            final TextView tv = v.findViewById(R.id.upload_title);
            final TextView uploadPercentTextView = v.findViewById(R.id.upload_percentage);
            final TextView finalize = v.findViewById(R.id.finalize_textview);
            uploadPercentTextView.setText("0 %");
            tv.setText(getString(R.string.initial_upload));
            progressDialog.setView(v);

            AlertDialog dialog = progressDialog.show();
            uploadTask.addOnProgressListener(taskSnapshot -> {
                long progress = (long) (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                int p = ExtraUtils.safeLongToInt(progress);
                Log.e("TAG", "P: " + p);
                String t = "" + taskSnapshot.getBytesTransferred() + "/" + taskSnapshot.getTotalByteCount() + " bytes";
                tv.setText(t);
                uploadPercentTextView.setText(p + " %");
                progressBar.setProgress(p);
            });

            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    uploadPercentTextView.setVisibility(View.GONE);
                    finalize.setVisibility(View.VISIBLE);
                    imageDownloadUri = task.getResult();
                    saveDetailsToDatabase(dialog);
                } else {
                    dialog.cancel();
                    showText("An Error Occurred while uploading Image, Try Again!");
                }
            });
        }
    }

    private void saveDetailsToDatabase(AlertDialog dialog) {
        DesignerDesigns designerDesigns = new DesignerDesigns(templateTitle, false, imageDownloadUri.toString());
        String key = designRef.push().getKey();
        assert key != null;
        designRef.child(key).updateChildren(designerDesigns.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.cancel();
                if (task.isSuccessful()) {
                    showText("Design Uploaded Successfully");
                } else {
                    showText("An Error Occurred, Please Try again");
                }
            }
        });
    }

    public static String getFileExtension(Context context, Uri uri) {
        String extension;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }

        return extension;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), contentURI);
                    uploadFile();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(getContext(), "You must accept all permissions to enable all app features", Toast.LENGTH_LONG).show();
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
        requestCameraPermission();
    }

}
