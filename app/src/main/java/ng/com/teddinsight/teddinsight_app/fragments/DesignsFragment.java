package ng.com.teddinsight.teddinsight_app.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.activities.AdminActivity;
import ng.com.teddinsight.teddinsight_app.models.DesignerDesigns;
import ng.com.teddinsight.teddinsight_app.services.FileDownloadService;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsightchat.models.User;

public class DesignsFragment extends DialogFragment {

    @BindView(R.id.list_recyclerView)
    RecyclerView listRecyclerView;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private DesignsAdapter designsAdapter;
    private Context mContext;
    private String role;
    private ValueEventListener eventListener;
    private Query query;
    private DesignsClickListener designsClickListener;
    private AlertDialog.Builder builder;
    private ScrollView scrollView;
    private ImageView imageView;

    public static DesignsFragment NewInstance(String role) {
        Bundle bundle = new Bundle();
        bundle.putString("role", role);
        DesignsFragment designsFragment = new DesignsFragment();
        designsFragment.setArguments(bundle);
        return designsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_designs, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        designsAdapter = new DesignsAdapter();
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("role"))
            role = bundle.getString("role");
        listRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        listRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        listRecyclerView.setAdapter(designsAdapter);
        emptyView.setText(mContext.getString(R.string.no_uploaded_designs));
        builder = new AlertDialog.Builder(mContext);
        designsClickListener = this::showDesignDialog;
        if (role.equalsIgnoreCase(User.USER_ADMIN)) {
            appBarLayout.setVisibility(View.GONE);
            getAdminActivityCast().setToolbarTitle("Designs");
        } else {
            toolbarTitle.setText("Designs");
        }
        getDesigns();
    }

    private void showDesignDialog(DesignerDesigns designerDesigns) {
        DialogInterface.OnClickListener adminOnClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    showToast("Approving", Toast.LENGTH_SHORT);
                    rootRef.child(DesignerDesigns.APPROVED_DESIGNS).child(designerDesigns.id).setValue(designerDesigns).addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            showToast("Approved", Toast.LENGTH_SHORT);
                        else
                            showToast(task.getException().toString(), Toast.LENGTH_SHORT);
                    });
                    rootRef.child(DesignerDesigns.DESIGNS).child(designerDesigns.id).removeValue();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    showToast("Disapproving", Toast.LENGTH_SHORT);
                    rootRef.child(DesignerDesigns.DESIGNS).child(designerDesigns.id).child("visibleToAdmin").setValue(false).addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            showToast("Disapproved", Toast.LENGTH_SHORT);
                        else
                            showToast(task.getException().getMessage(), Toast.LENGTH_SHORT);
                    });
                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    showToast("Deleting", Toast.LENGTH_SHORT);
                    rootRef.child(DesignerDesigns.DESIGNS).child(designerDesigns.id).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            showToast("Deleted", Toast.LENGTH_SHORT);
                        else
                            showToast(task.getException().getMessage(), Toast.LENGTH_SHORT);
                    });
                    break;
            }
            dialog.dismiss();
        };
        DialogInterface.OnClickListener prOnClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    downloadImage(designerDesigns);
                    break;
                case DialogInterface.BUTTON_NEUTRAL:
                    rootRef.child(DesignerDesigns.DESIGNS).child(designerDesigns.id).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            showToast("Deleted", Toast.LENGTH_SHORT);
                        else
                            showToast(task.getException().getMessage(), Toast.LENGTH_SHORT);
                    });
                    break;
            }
            dialog.dismiss();
        };
        scrollView = new ScrollView(mContext);
        LinearLayout linLayout = new LinearLayout(mContext);
        linLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lpView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600);
        imageView = new ImageView(mContext);
        imageView.setLayoutParams(lpView);
        Picasso.get().load(designerDesigns.imageUrl).into(imageView);
        linLayout.addView(imageView);
        scrollView.addView(linLayout);
        builder.setView(scrollView);
        if (role.equalsIgnoreCase(User.USER_ADMIN)) {
            builder.setPositiveButton("Approve", adminOnClickListener);
            builder.setNegativeButton("Disapprove", adminOnClickListener);
            builder.setNeutralButton("Delete", adminOnClickListener);
        } else {
            builder.setPositiveButton("Download", prOnClickListener);
            builder.setNeutralButton("Delete", prOnClickListener);
        }
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(mContext, R.color.green_600));
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(mContext, R.color.red_600));
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(mContext, R.color.orange_600));
        });
        dialog.show();
    }

    private void downloadImage(DesignerDesigns designerDesigns) {
        Intent intent = new Intent(mContext, FileDownloadService.class);
        intent.putExtra("fileName", designerDesigns.templateName.concat(".png"));
        intent.putExtra("url", designerDesigns.imageUrl);
        intent.putExtra("dir",  "/Teddinsight/Designs");
        showToast("Downloading file", Toast.LENGTH_SHORT);
        mContext.startService(intent);
        if (getDialog().isShowing())
            getDialog().dismiss();
    }

    private void getDesigns() {
        if (role.equalsIgnoreCase(User.USER_ADMIN))
            query = rootRef.child(DesignerDesigns.DESIGNS).orderByChild("visibleToAdmin").equalTo(true);
        else
            query = rootRef.child(DesignerDesigns.APPROVED_DESIGNS);

        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<DesignerDesigns> designerDesigns = new ArrayList<>();
                DesignerDesigns designerDesign;
                Log.e("LOG", "" + dataSnapshot.getChildrenCount());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    designerDesign = snapshot.getValue(DesignerDesigns.class);
                    designerDesigns.add(designerDesign);
                }
                designsAdapter.swapData(designerDesigns);
                emptyView.setVisibility(designsAdapter.getItemCount() < 1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast(databaseError.getMessage(), Toast.LENGTH_LONG);
            }

        };
        query.addValueEventListener(eventListener);
    }

    private AdminActivity getAdminActivityCast() {
        return (AdminActivity) getActivity();
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
        if (role.equalsIgnoreCase(User.USER_ADMIN)) {
            appBarLayout.setVisibility(View.GONE);
            getAdminActivityCast().setToolbarTitle("Designs");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (eventListener != null && query != null)
            query.removeEventListener(eventListener);

    }

    private void showToast(String message, int length) {
        Toast.makeText(mContext, message, length).show();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getDialog() != null)
            getDialog().getWindow()
                    .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    class DesignsAdapter extends RecyclerView.Adapter<DesignsAdapter.DesignsViewHolder> {

        List<DesignerDesigns> designerDesigns;

        public DesignsAdapter() {
            this.designerDesigns = new ArrayList<>();
        }


        @NonNull
        @Override
        public DesignsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DesignsViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.designs_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DesignsViewHolder holder, int position) {
            holder.bind(designerDesigns.get(getItemCount() - position - 1));
        }

        public void swapData(List<DesignerDesigns> designerDesigns) {
            this.designerDesigns = designerDesigns;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return designerDesigns.size();
        }

        class DesignsViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.poster)
            ImageView poster;
            @BindView(R.id.template_title)
            TextView templateTitle;
            @BindView(R.id.date_uploaded)
            TextView dateUploaded;
            @BindView(R.id.status)
            TextView status;

            public DesignsViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(v -> designsClickListener.onDesignItemClicked(designerDesigns.get(getItemCount() - getAdapterPosition() - 1)));
            }

            private void bind(DesignerDesigns designerDesigns) {
                Picasso.get().load(designerDesigns.imageUrl).into(poster);
                templateTitle.setText(designerDesigns.templateName);
                dateUploaded.setText(ExtraUtils.getHumanReadableString(designerDesigns.dateUploaded));
                status.setText(designerDesigns.isUpdated ? "Has been edited" : "Has not been edited");
            }
        }
    }

    interface DesignsClickListener {
        void onDesignItemClicked(DesignerDesigns designerDesigns);
    }
}
