package ng.com.teddinsight.teddinsight_app.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.activities.AdminActivity;
import ng.com.teddinsight.teddinsight_app.models.ScheduledPost;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsightchat.models.User;

public class ScheduledPostFragment extends Fragment {

    @BindView(R.id.list_recyclerView)
    RecyclerView scheduledPostRecycler;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    private String role;
    private Query query;
    private ValueEventListener valueEventListener;
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private ScheduledPostRecyclerAdapter scheduledPostRecyclerAdapter;
    private ScheduledPostItemClickListener scheduledPostItemClickListener;
    private WorkManager workManager;
    private Context mContext;

    public static ScheduledPostFragment NewInstance(String role) {
        Bundle bundle = new Bundle();
        bundle.putString("role", role);
        ScheduledPostFragment scheduledPostFragment = new ScheduledPostFragment();
        scheduledPostFragment.setArguments(bundle);
        return scheduledPostFragment;
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
        role = getArguments().getString("role");
        if (role.equalsIgnoreCase(User.USER_ADMIN)) {
            appBarLayout.setVisibility(View.GONE);
            getAdminActivityCast().setToolbarTitle("Scheduled Post");
        } else
            toolbarTitle.setText(mContext.getString(R.string.schedule_post));
        emptyView.setText(getString(R.string.no_scheduled_post_request));
        scheduledPostRecyclerAdapter = new ScheduledPostRecyclerAdapter();
        scheduledPostRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        scheduledPostRecycler.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        scheduledPostRecycler.setAdapter(scheduledPostRecyclerAdapter);
        scheduledPostItemClickListener = new ScheduledPostItemClickListener() {
            @Override
            void onScheduledPostItemClicked(ScheduledPost scheduledPost) {
                showToast("Scheduled post for " + scheduledPost.getAccountUsername() + " on " + scheduledPost.getAccountType(), Toast.LENGTH_SHORT);
                if (role.equalsIgnoreCase(User.USER_ADMIN))
                    showDialogForAdmin(scheduledPost);
                else
                    showDialogForPr(scheduledPost);
            }
        };
        workManager = WorkManager.getInstance(mContext);
        getScheduledPosts();
    }

    private void showDialogForPr(ScheduledPost scheduledPost) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                showToast("Deleting", Toast.LENGTH_SHORT);
                if (scheduledPost.isHasBeenReviewedByAdmin() && scheduledPost.getStatus().equalsIgnoreCase(ScheduledPost.STATUS_APPROVED)) {
                    rootRef.child(ScheduledPost.APPROVED_SCHEDULE_PATH).child(scheduledPost.getId()).setValue(null);
                }
                rootRef.child(ScheduledPost.SCHEDULE_PATH).child(scheduledPost.getId()).setValue(null).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Deleted", Toast.LENGTH_SHORT);
                        workManager.cancelAllWorkByTag(scheduledPost.getAccountType().toLowerCase().concat(scheduledPost.getAccountUsername()).concat("reminder"));
                        workManager.cancelAllWorkByTag(scheduledPost.getAccountType().toLowerCase().concat(scheduledPost.getAccountUsername()));
                    } else showToast(task.getException().getMessage(), Toast.LENGTH_SHORT);
                });
            }
            dialog.dismiss();
        };
        String message = "Post Description: " + scheduledPost.getPostTitle();
        message += "\n\nPost content: " + scheduledPost.getPostText() + "\n\n This post " + (scheduledPost.isHasBeenReviewedByAdmin() ? "was " + scheduledPost.getStatus() : "has not been reviewed by an Admin");
        builder.setTitle("Your scheduled post")
                .setMessage(message)
                .setPositiveButton("Ok", onClickListener)
                .setNegativeButton("Delete", onClickListener)
                .setNeutralButton("Close Dialog", onClickListener)
                .setCancelable(false);
        builder.show();
    }

    private void showDialogForAdmin(ScheduledPost scheduledPost) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getAdminActivityCast());
        DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                showToast("Approving", Toast.LENGTH_SHORT);
                scheduledPost.setHasBeenReviewedByAdmin(true);
                scheduledPost.setStatus(ScheduledPost.STATUS_APPROVED);
                rootRef.child(ScheduledPost.APPROVED_SCHEDULE_PATH).child(scheduledPost.getId()).setValue(scheduledPost).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        showToast(scheduledPost.getStatus(), Toast.LENGTH_SHORT);
                    else showToast(task.getException().getMessage(), Toast.LENGTH_SHORT);
                });
                rootRef.child(ScheduledPost.SCHEDULE_PATH).child(scheduledPost.getId()).setValue(scheduledPost);
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                showToast("Disapproving", Toast.LENGTH_SHORT);
                scheduledPost.setHasBeenReviewedByAdmin(true);
                scheduledPost.setStatus(ScheduledPost.STATUS_DISAPPROVED);
                rootRef.child(ScheduledPost.SCHEDULE_PATH).child(scheduledPost.getId()).setValue(scheduledPost).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        showToast(scheduledPost.getStatus(), Toast.LENGTH_SHORT);
                    else showToast(task.getException().getMessage(), Toast.LENGTH_SHORT);
                });
            }
            dialog.dismiss();
        };
        String message = "Post Description: " + scheduledPost.getPostTitle();
        message += "\n\nPost content: " + scheduledPost.getPostText() + "\n\nScheduled for: " + ExtraUtils.getHumanReadableString(scheduledPost.getPostTimestamp());
        builder.setTitle("Take action for scheduled post")
                .setMessage(message)
                .setPositiveButton("Approve", onClickListener)
                .setNegativeButton("Disapprove", onClickListener)
                .setNeutralButton("Cancel", onClickListener)
                .setCancelable(false);
        builder.show();
    }

    private void getScheduledPosts() {
        query = rootRef.child(ScheduledPost.SCHEDULE_PATH);
        if (role.equalsIgnoreCase(User.USER_ADMIN))
            query = query.orderByChild("hasBeenReviewedByAdmin").equalTo(false);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ScheduledPost> scheduledPosts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ScheduledPost scheduledPost = snapshot.getValue(ScheduledPost.class);
                    scheduledPost.setId(snapshot.getKey());
                    scheduledPosts.add(scheduledPost);
                }
                scheduledPostRecyclerAdapter.swapData(scheduledPosts);
                emptyView.setVisibility(scheduledPostRecyclerAdapter.getItemCount() < 1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addValueEventListener(valueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (valueEventListener != null)
            query.removeEventListener(valueEventListener);
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


    private AdminActivity getAdminActivityCast() {
        return (AdminActivity) getActivity();
    }


    class ScheduledPostRecyclerAdapter extends RecyclerView.Adapter<ScheduledPostRecyclerAdapter.ScheduledPostViewHolder> {
        List<ScheduledPost> scheduledPostList;

        public ScheduledPostRecyclerAdapter() {
            this.scheduledPostList = new ArrayList<>();
        }

        @NonNull
        @Override
        public ScheduledPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ScheduledPostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduledPostViewHolder holder, int position) {
            ScheduledPost scheduledPost = scheduledPostList.get(getItemCount() - position - 1);
            String postDesc = "Post scheduled for " + scheduledPost.getAccountUsername() + " on " + scheduledPost.getAccountType();
            holder.postDescription.setText(postDesc);
        }

        public void swapData(List<ScheduledPost> scheduledPosts) {
            this.scheduledPostList = scheduledPosts;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return scheduledPostList.size();
        }

        class ScheduledPostViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.client_businessName)
            TextView postDescription;

            public ScheduledPostViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(v -> {
                    scheduledPostItemClickListener.onScheduledPostItemClicked(scheduledPostList.get(getItemCount() - getAdapterPosition() - 1));
                });
            }
        }
    }

    abstract class ScheduledPostItemClickListener {
        abstract void onScheduledPostItemClicked(ScheduledPost scheduledPost);
    }
}
