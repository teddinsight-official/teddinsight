package ng.com.teddinsight.teddinsight_app.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners;
import ng.com.teddinsight.teddinsight_app.models.DesignerDesigns;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners.DesignTemplateClicked;
import ng.com.teddinsight.teddinsightchat.models.User;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;
import static ng.com.teddinsight.teddinsight_app.activities.DCSHomeActivity.LOG_TAG;


public class DesignerHomeFragment extends Fragment implements EasyPermissions.PermissionCallbacks, DesignTemplateClicked {

    private static final int PERMISSIONS_REQUEST_CODE = 1001;
    public static final String DESIGN_PATH = "/Teddinsight/Design And Content";
    private int GALLERY = 1, CAMERA = 2;

    public static Fragment NewInstance() {
        return new DesignerHomeFragment();
    }

    public static DesignerHomeFragment NewInstance(String role) {
        Bundle bundle = new Bundle();
        bundle.putString("role", role);
        DesignerHomeFragment designerHomeFragment = new DesignerHomeFragment();
        designerHomeFragment.setArguments(bundle);
        return designerHomeFragment;
    }

    @BindView(R.id.designs_grid)
    RecyclerView designGrid;
    @BindView(R.id.new_design_button)
    FloatingActionButton newDesignButton;
    @BindView(R.id.designs_title)
    TextView designTitle;
    @BindView(R.id.d_swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.home_title)
    TextView homeTitle;
    private String templateTitle;
    private Uri contentURI;
    private Uri imageDownloadUri;
    private DatabaseReference designRef;
    private DesignerAdapter adapter;
    boolean isNotDesign;
    SharedPreferences preferences;
    String path = "/TeddinsightDnC/Images";
    Listeners.ShowEditImageActivity showEditImageActivity;
    private Context mContext;
    private ProgressDialog progressDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home_dcs, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestCameraPermission();
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        refreshLayout.setRefreshing(true);
        refreshLayout.setColorSchemeColors(Color.RED, Color.BLUE, Color.GREEN, Color.CYAN);
        refreshLayout.setOnRefreshListener(this::getDesigns);
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("role"))
            isNotDesign = bundle.getString("role", User.USER_DESIGNER).equals(User.USER_CONTENT);
        if (isNotDesign) {
            homeTitle.setText(User.USER_CONTENT);
            designTitle.setText(getString(R.string.adoi));
            newDesignButton.setVisibility(View.GONE);
        } else {
            homeTitle.setText(User.USER_DESIGNER);
            designTitle.setText(getString(R.string.upload_designs));
        }
        progressDialog = new ProgressDialog(mContext);
        designRef = FirebaseDatabase.getInstance().getReference(DesignerDesigns.DESIGNS);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        designGrid.setLayoutManager(linearLayoutManager);
        adapter = new DesignerAdapter(!isNotDesign, this);
        designGrid.setAdapter(adapter);
        getDesigns();
    }

    private void getDesigns() {
        designRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<DesignerDesigns> designerDesignsList = new ArrayList<>();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    DesignerDesigns dd = snap.getValue(DesignerDesigns.class);
                    designerDesignsList.add(dd);
                }
                adapter.swapData(designerDesignsList);
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                refreshLayout.setRefreshing(false);
            }
        });
    }

    private void requestCameraPermission() {
        if (!EasyPermissions.hasPermissions(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(this,
                    "This app needs to use your camera and read storage. Please accept all permissions else app will misbehave",
                    PERMISSIONS_REQUEST_CODE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @OnClick(R.id.new_design_button)
    public void newDesign() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        final EditText input = new EditText(mContext);
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
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }


    public void choosePhotoFromGallary() {
//        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//        startActivityForResult(galleryIntent, GALLERY);
        CropImage.startPickImageActivity(mContext, this);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    public static void uploadFile(Uri contentURI, Context context, String templateTitle, boolean isUpdate, String key, DesignerDesigns designerDesigns) {
        if (contentURI != null) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("designer/designs" + templateTitle);
            UploadTask uploadTask = imageRef.putFile(contentURI);
            AlertDialog.Builder progressDialog = new AlertDialog.Builder(context);

            View v = LayoutInflater.from(context).inflate(R.layout.upload_dialog_layout, null, false);
            final ProgressBar progressBar = v.findViewById(R.id.progressBar);
            final TextView tv = v.findViewById(R.id.upload_title);
            final TextView uploadPercentTextView = v.findViewById(R.id.upload_percentage);
            final TextView finalize = v.findViewById(R.id.finalize_textview);
            uploadPercentTextView.setText("0 %");
            tv.setText(context.getString(R.string.initial_upload));
            progressDialog.setView(v)
                    .setCancelable(false);
            uploadTask.addOnProgressListener(taskSnapshot -> {
                long progress = (long) (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                int p = ExtraUtils.safeLongToInt(progress);
                String t = "" + taskSnapshot.getBytesTransferred() + "/" + taskSnapshot.getTotalByteCount() + " bytes";
                tv.setText(t);
                uploadPercentTextView.setText(p + " %");
                progressBar.setProgress(p);
            });
            AlertDialog dialog = progressDialog.create();
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
                    Uri imageDownloadUri = task.getResult();
                    assert imageDownloadUri != null;
                    if (isUpdate)
                        updateTemplateDetails(context, dialog, key, imageDownloadUri, designerDesigns);
                    else
                        saveDetailsToDatabase(dialog, templateTitle, imageDownloadUri, context);
                } else {
                    dialog.cancel();
                    Toast.makeText(context, "An Error Occurred while uploading Image, Try Again!", Toast.LENGTH_LONG).show();
                }
            });

            DialogInterface.OnClickListener progressDialogClickListener = (dialog1, which) -> {
                //uploadTask.cancel();
                dialog1.cancel();
            };
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Hide", progressDialogClickListener);
            dialog.show();
        }
    }

    public static void updateTemplateDetails(Context context, AlertDialog dialog, String id, Uri imageUri, DesignerDesigns designerDesigns) {
        designerDesigns.setImageUrl(imageUri.toString());
        DatabaseReference designRef = FirebaseDatabase.getInstance().getReference("designer/designs").child(id);
        designerDesigns.setVisibleToAdmin(true);
        designRef.setValue(designerDesigns).addOnCompleteListener(task -> {
            dialog.cancel();
            if (task.isSuccessful()) {
                Toast.makeText(context, "Design Updated Successfully", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "An Error Occurred, Please Try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void saveDetailsToDatabase(AlertDialog dialog, String templateTitle, Uri imageDownloadUri, Context context) {
        DatabaseReference designRef = FirebaseDatabase.getInstance().getReference("designer/designs");
        String key = designRef.push().getKey();
        assert key != null;
        DesignerDesigns designerDesigns = new DesignerDesigns(key, templateTitle, false, imageDownloadUri.toString());
        designRef.child(key).updateChildren(designerDesigns.toMap()).addOnCompleteListener(task -> {
            dialog.cancel();
            if (task.isSuccessful()) {
                Toast.makeText(context, "Design Uploaded Successfully", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "An Error Occurred, Please Try again", Toast.LENGTH_LONG).show();
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
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
            if (data != null) {
                contentURI = data.getData();
                Activity activity = getActivity();
                if (activity != null)
                    CropImage.activity(contentURI).setGuidelines(CropImageView.Guidelines.ON).start(mContext, this);
            }

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Toast.makeText(mContext, "dsf", Toast.LENGTH_SHORT).show();
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                contentURI = result.getUri();
                startCompression(contentURI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(mContext, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCompression(Uri uri) {
        progressDialog.setMessage("Compressing Image, Please wait");
        progressDialog.show();
        File imageFile = new File(uri.getPath());
        Disposable sc = new Compressor(mContext)
                .compressToFileAsFlowable(imageFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file -> {
                    progressDialog.dismiss();
                    contentURI = Uri.fromFile(file);
                    uploadFile(contentURI, mContext, templateTitle, false, null, null);
                }, throwable -> {
                    progressDialog.dismiss();
                    throwable.printStackTrace();
                    Log.e(LOG_TAG, throwable.getLocalizedMessage());
                    Toast.makeText(mContext, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(mContext, "You must accept all permissions to enable all app features", Toast.LENGTH_LONG).show();
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
        requestCameraPermission();
    }

    @Override
    public void onTemplateClicked(boolean isDesigner, DesignerDesigns designerDesigns) {
        if (isDesigner) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            assert getFragmentManager() != null;
            Fragment prev = getFragmentManager().findFragmentByTag("edit");
            if (prev != null) {
                ft.remove(prev);
            }
            DesignEditorFragment.NewInstance(designerDesigns).show(ft, "edit");
        } else {
            if (!designerDesigns.isCanEdit()) {
                showText("Please wait till image is fully downloaded before editing");
                return;
            }
            showEditImageActivity.showEditImageActivity(designerDesigns);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        showEditImageActivity = (Listeners.ShowEditImageActivity) context;
        this.mContext = context;
    }

    public class DesignerAdapter extends RecyclerView.Adapter<DesignerAdapter.DesignViewHolder> {


        List<DesignerDesigns> designerDesignsList;
        boolean isDesigner;
        DesignTemplateClicked designTemplateClicked;

        DesignerAdapter(boolean isDesigner, DesignTemplateClicked clicked) {
            this.isDesigner = isDesigner;
            designerDesignsList = new ArrayList<>();
            this.designTemplateClicked = clicked;
        }

        @NonNull
        @Override
        public DesignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_designer_designs, parent, false);
            return new DesignViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DesignViewHolder holder, int position) {
            DesignerDesigns designerDesigns = designerDesignsList.get(getItemCount() - position - 1);
            holder.templateTitleView.setText(designerDesigns.getTemplateName());
            String imageSaveName = designerDesigns.templateName.concat(String.valueOf(designerDesigns.dateUploaded));
            holder.imageIndicator.setText(getString(R.string.loading_image));
            holder.imageIndicator.setVisibility(View.VISIBLE);
            holder.imageIndicator.setOnClickListener(v -> loadImage(designerDesigns, holder));
            loadImage(designerDesigns, holder);

            /*holder.imageIndicator.setOnClickListener(v -> {
                downloadImage(designerDesigns.imageUrl, holder.templateImageView, holder.imageIndicator, imageSaveName, designerDesigns);
            });

            if (isDesigner) {

            } else {
                try {
                    File n = new File(Environment.getExternalStorageDirectory() + DESIGN_PATH + "/" + designerDesigns.templateName + designerDesigns.dateUploaded + ".png");
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(n));
                    if (b == null)
                        downloadImage(designerDesigns.imageUrl, holder.templateImageView, holder.imageIndicator, imageSaveName, designerDesigns);
                    else {
                        holder.templateImageView.setImageBitmap(b);
                        holder.imageIndicator.setVisibility(View.INVISIBLE);
                        designerDesigns.setCanEdit(true);
                    }

                } catch (FileNotFoundException e) {
                    downloadImage(designerDesigns.imageUrl, holder.templateImageView, holder.imageIndicator, imageSaveName, designerDesigns);
                    e.printStackTrace();
                    Log.e(TAG, "File not found " + e.getLocalizedMessage());
                }

            }*/
        }

        private void loadImage(DesignerDesigns designerDesigns, DesignViewHolder holder) {
            Picasso.get().load(designerDesigns.getImageUrl())
                    .into(holder.templateImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.imageIndicator.setVisibility(View.GONE);
                            designerDesigns.setCanEdit(true);
                        }

                        @Override
                        public void onError(Exception e) {
                            holder.imageIndicator.setVisibility(View.VISIBLE);
                            holder.imageIndicator.setText(getString(R.string.tap_to_retry));
                            designerDesigns.setCanEdit(false);
                        }
                    });
        }

        private void downloadImage(String url, ImageView view, Button imageIndicator, String imgName, DesignerDesigns design) {
            imageIndicator.setText(getString(R.string.load));
            imageIndicator.setVisibility(View.VISIBLE);
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    view.setImageBitmap(bitmap);
                    imageIndicator.setVisibility(View.INVISIBLE);
                    storeImage(bitmap, imgName, design);
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Toast.makeText(mContext, "Can't load image", Toast.LENGTH_SHORT).show();
                    imageIndicator.setText("An Error Occurred, Tap to retry");
                    imageIndicator.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            Picasso.get().load(url)
                    .into(target);
        }

        private void storeImage(Bitmap image, String imageName, DesignerDesigns designerDesign) {
            Log.e(TAG, "Start saving");
            File pictureFile = getOutputMediaFile(imageName);
            if (pictureFile == null) {
                Log.e(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                image.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();
                Log.e(TAG, "File stored");
                designerDesign.setCanEdit(true);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }

        private File getOutputMediaFile(String mImageName) {
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + DESIGN_PATH);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }
            File mediaFile;
            mImageName = mImageName.concat(".png");
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
            return mediaFile;
        }

        @Override
        public int getItemCount() {
            return designerDesignsList.size();
        }

        void swapData(List<DesignerDesigns> designs) {
            this.designerDesignsList = designs;
            this.notifyDataSetChanged();
        }

        class DesignViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.template_image)
            ImageView templateImageView;
            @BindView(R.id.template_title)
            TextView templateTitleView;
            @BindView(R.id.image_indicator)
            Button imageIndicator;

            DesignViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(v -> {
                    designTemplateClicked.onTemplateClicked(isDesigner, designerDesignsList.get(getItemCount() - getAdapterPosition() - 1));
                });
            }
        }

    }

}
