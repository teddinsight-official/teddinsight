package ng.com.teddinsight.teddinsight_app.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.models.ClientUpload;
import ng.com.teddinsight.teddinsight_app.services.FileDownloadService;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsightchat.models.User;

import static android.app.Activity.RESULT_OK;


public class ClientUploadsFragment extends Fragment {
    @BindView(R.id.client_list_recyclerView)
    RecyclerView clientListRecyclerView;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;
    ClientUploadAdapter clientUploadAdapter;
    String businessName;
    private static final int CHOOSE_FILE_REQUESTCODE = 102;
    private Context mContext;
    private String dbPath = "";
    public static final String PATH_UPLOAD = "clientUploads";
    public static final String PATH_DOWNLOADS = "clientDownloads";
    DatabaseReference clientUploadRef = FirebaseDatabase.getInstance().getReference(PATH_DOWNLOADS);
    User user;

    public static ClientUploadsFragment NewInstance(User user) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        ClientUploadsFragment clientUploadsFragment = new ClientUploadsFragment();
        clientUploadsFragment.setArguments(bundle);
        return clientUploadsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uploads_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clientUploadAdapter = new ClientUploadAdapter();
        appBarLayout.setVisibility(View.GONE);
        clientListRecyclerView.setAdapter(clientUploadAdapter);
        clientListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        clientListRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        Bundle args = getArguments();
        if (args != null && args.containsKey("user")) {
            user = args.getParcelable("user");
            businessName = user.getBusinessName();
            dbPath = PATH_DOWNLOADS;
            emptyView.setText(String.format("%s%s", mContext.getString(R.string.no_uploads_for_client), user.getBusinessName()));
        } else
            onDetach();
        getUploadsClientDownloads();
    }

    private void getUploadsClientDownloads() {
        FirebaseDatabase.getInstance().getReference(dbPath).child(businessName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ClientUpload> uploads = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ClientUpload clientUpload = snapshot.getValue(ClientUpload.class);
                    clientUpload.setId(snapshot.getKey());
                    uploads.add(clientUpload);
                }
                clientUploadAdapter.swapClientList(uploads);
                if (clientUploadAdapter.getItemCount() > 0)
                    emptyView.setVisibility(View.GONE);
                else
                    emptyView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("An error occurred while loading client uploads", Toast.LENGTH_LONG);
            }
        });
    }

    @OnClick(R.id.fab)
    public void uploadFilesForClient() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        Intent i = Intent.createChooser(intent, "File");
        startActivityForResult(i, CHOOSE_FILE_REQUESTCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_FILE_REQUESTCODE:
                if (resultCode == RESULT_OK) {
                    try {
                        prepareUploadFile(data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                        showToast("The selected file was not found on this device", Toast.LENGTH_LONG);
                    }
                }
                break;
        }
    }

    private void prepareUploadFile(Uri fileUri) throws IOException {
        AssetFileDescriptor afd = mContext.getContentResolver().openAssetFileDescriptor(fileUri, "r");
        long fileSize = afd.getLength();
        afd.close();
        float sizeInMegaBytes = ((float) Math.round((fileSize / (1024 * 1024)) * 10) / 10);
        if (sizeInMegaBytes > 5.1) {
            showToast("The file size should not be larger than 5.0 mb", Toast.LENGTH_LONG);
            return;
        }
        DialogInterface.OnClickListener dialogClickeListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    uploadFile(fileUri);
                    break;
            }
            dialog.cancel();
        };
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setMessage("Upload " + getFileName(fileUri) + " for " + businessName + " ?")
                .setTitle("File Upload")
                .setPositiveButton("Yes", dialogClickeListener)
                .setNegativeButton("Cancel", dialogClickeListener);
        alertDialogBuilder.show();
    }

    private void uploadFile(Uri contentURI) {
        if (contentURI != null) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(dbPath + "/" + businessName + "/" + getFileName(contentURI));
            UploadTask uploadTask = imageRef.putFile(contentURI);
            AlertDialog.Builder progressDialog = new AlertDialog.Builder(mContext);
            View v = LayoutInflater.from(mContext).inflate(R.layout.upload_dialog_layout, null, false);
            final ProgressBar progressBar = v.findViewById(R.id.progressBar);
            final TextView tv = v.findViewById(R.id.upload_title);
            final TextView uploadPercentTextView = v.findViewById(R.id.upload_percentage);
            final TextView finalize = v.findViewById(R.id.finalize_textview);
            uploadPercentTextView.setText("0 %");
            tv.setText(mContext.getString(R.string.initial_upload));
            progressDialog.setView(v).setCancelable(false).setNegativeButton("Cancel", (dialog, which) -> {
                uploadTask.cancel();
                dialog.dismiss();
            });

            AlertDialog dialog = progressDialog.show();
            uploadTask.addOnProgressListener(taskSnapshot -> {
                long progress = (long) (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                int p = ExtraUtils.safeLongToInt(progress);
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
                    Uri imageDownloadUri = task.getResult();
                    assert imageDownloadUri != null;
                    showToast("File Uploaded", Toast.LENGTH_LONG);
                    Map<String, Object> map = new HashMap<>();
                    map.put("fileName", getFileName(contentURI));
                    map.put("fileUrl", imageDownloadUri.toString());
                    map.put("dateUploaded", ServerValue.TIMESTAMP);
                    String key = clientUploadRef.push().getKey();
                    clientUploadRef.child(businessName).child(key).updateChildren(map).addOnCompleteListener(task1 -> {
                        showToast("File Uploaded", Toast.LENGTH_LONG);
                        dialog.cancel();
                    });
                } else {
                    dialog.cancel();
                    showToast("An Error Occurred while uploading Image, Try Again!", Toast.LENGTH_LONG);
                }
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void showToast(String message, int length) {
        if (mContext != null)
            Toast.makeText(mContext, message, length).show();
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    class ClientUploadAdapter extends RecyclerView.Adapter<ClientUploadAdapter.ClientUploadsViewHolder> {
        List<ClientUpload> uploads;

        private ClientUploadAdapter() {
            this.uploads = new ArrayList<>();
        }

        @NonNull
        @Override
        public ClientUploadsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list, parent, false);
            return new ClientUploadsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ClientUploadsViewHolder holder, int position) {
            ClientUpload clientUpload = uploads.get(getItemCount() - position - 1);
            Log.e("LOG", clientUpload.getFileName());
            holder.filename.setText(clientUpload.getFileName());
            holder.date.setText(ExtraUtils.formatDate(clientUpload.getDateUploaded()));
            holder.setClientUpload(clientUpload);
        }


        @Override
        public int getItemCount() {
            return uploads.size();
        }

        public void swapClientList(List<ClientUpload> clientclientListUploads) {
            this.uploads = clientclientListUploads;
            notifyDataSetChanged();
        }

        class ClientUploadsViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.filename)
            TextView filename;
            @BindView(R.id.date)
            TextView date;
            @BindView(R.id.download)
            ImageButton downloadButton;
            ClientUpload clientUpload;

            public ClientUploadsViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                downloadButton.setVisibility(View.GONE);
                itemView.setOnClickListener(v -> {
                    DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            clientUploadRef.child(businessName).child(clientUpload.getId()).removeValue();
                            showToast("Deleted", Toast.LENGTH_SHORT);
                        }
                        dialog.cancel();
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setIcon(R.drawable.ic_delete_black_24dp)
                            .setTitle("Delete this file")
                            .setMessage("Are you sure you want to delete" + clientUpload.getFileName())
                            .setPositiveButton("Delete", onClickListener)
                            .setNegativeButton("Cancel", onClickListener);
                    builder.show();
                });

            }

            public void setClientUpload(ClientUpload clientUpload) {
                this.clientUpload = clientUpload;
            }
        }
    }

}
