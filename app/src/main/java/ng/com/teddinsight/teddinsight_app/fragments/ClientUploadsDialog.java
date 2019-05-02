package ng.com.teddinsight.teddinsight_app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.models.ClientUpload;

public class ClientUploadsDialog extends DialogFragment {
    @BindView(R.id.client_list_recyclerView)
    RecyclerView clientListRecyclerView;
    @BindView(R.id.empty_view)
    TextView emptyView;
    InitializeFileDownload initializeFileDownload;
    ClientUploadAdapter clientUploadAdapter;
    String businessName;


    public static ClientUploadsDialog NewInstance(String businessName) {
        ClientUploadsDialog clientUploadsDialog = new ClientUploadsDialog();
        Bundle bundle = new Bundle();
        bundle.putString("businessName", businessName);
        clientUploadsDialog.setArguments(bundle);
        return clientUploadsDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clientUploadAdapter = new ClientUploadAdapter();
        clientListRecyclerView.setAdapter(clientUploadAdapter);
        clientListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        clientListRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        businessName = getArguments().getString("businessName", "");
        getUploads();
    }

    private void getUploads() {
        FirebaseDatabase.getInstance().getReference("clientUploads").child(businessName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ClientUpload> uploads = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ClientUpload clientUpload = snapshot.getValue(ClientUpload.class);
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
                Toast.makeText(getContext(), "An error occurred while loading client list", Toast.LENGTH_LONG).show();
            }
        });
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        initializeFileDownload = (InitializeFileDownload) context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
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
            holder.download.setOnClickListener(v -> {
                initializeFileDownload.onFileUDownloadInitiliazed(clientUpload.getFileName(), clientUpload.getFileUrl());
            });
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
            @BindView(R.id.download)
            ImageButton download;

            public ClientUploadsViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    public interface InitializeFileDownload {
        void onFileUDownloadInitiliazed(String fileName, String url);
    }
}
