package ng.com.teddinsight.teddinsight_app.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.models.DesignerDesigns;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_CANCELED;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class DesignEditorFragment extends DialogFragment implements EasyPermissions.PermissionCallbacks {

    View view;
    @BindView(R.id.template_image)
    ImageView templateImageview;
    private int GALLERY = 1;
    private static final int PERMISSIONS_REQUEST_CODE = 1002;
    DesignerDesigns designerDesigns;
    Uri contentURI;
    boolean changesMade = false;

    public static DesignEditorFragment NewInstance(DesignerDesigns designerDesigns) {
        Bundle b = new Bundle();
        b.putParcelable("design", designerDesigns);
        DesignEditorFragment editorFragment = new DesignEditorFragment();
        editorFragment.setArguments(b);
        return editorFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dialog_design_edit, container, false);
        ButterKnife.bind(this, view);
        Bundle dd = getArguments();
        designerDesigns = dd.getParcelable("design");
        Log.e(TAG, "id: "+designerDesigns.getId());
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(v -> getDialog().dismiss());
        toolbar.setTitle(designerDesigns.getTemplateName());
        toolbar.setTitleTextAppearance(getContext(), R.style.ToolbarTheme);
        initialize();
        return view;
    }

    private void initialize() {
        Picasso.get().load(designerDesigns.getImageUrl()).into(templateImageview);
    }

    @OnClick(R.id.change_image)
    void changeImage() {
        requestCameraPermission();
        choosePhotoFromGallary();
    }

    @OnClick(R.id.save)
    void saveNew(){
        DesignerHomeFragment.uploadFile(contentURI, getContext(), designerDesigns.getTemplateName(), true, designerDesigns.getId(), designerDesigns);
        changesMade = false;
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

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
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
                changesMade = true;
                contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), contentURI);
                    templateImageview.setImageBitmap(bitmap);

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


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    private void showToast(String message, int duration) {
        Toast.makeText(getContext(), message, duration).show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getView() == null){
            return;
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener((v, keyCode, event) -> {

            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                AlertDialog.OnClickListener clickListener = (dialog, which) -> {
                    dialog.dismiss();
                    switch (which){
                        case Dialog.BUTTON_NEGATIVE:
                            Objects.requireNonNull(getDialog()).cancel();
                            break;
                    }
                };
                if (changesMade) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("You have unsaved changes!")
                            .setTitle("Warning")
                            .setPositiveButton("Continue Editing", clickListener)
                            .setNegativeButton("Dismiss", clickListener);

                    builder.show();
                }else {
                    getDialog().cancel();
                }

                return true;
            }
            return false;
        });
    }
}
