package ng.com.teddinsight.teddinsight_app.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.activities.AdminActivity;
import ng.com.teddinsight.teddinsight_app.models.Log;
import ng.com.teddinsight.teddinsight_app.utils.SwipeToDeleteCallback;
import ng.com.teddinsight.teddinsightchat.models.User;

public class LogsFragment extends Fragment {


    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.list_recyclerView)
    RecyclerView listRecyclerView;
    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;
    private LogsAdapter logsAdapter;
    private DatabaseReference rootRef;
    private Context mContext;

    public static LogsFragment NewInstance() {
        return new LogsFragment();
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
        logsAdapter = new LogsAdapter();
        listRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        listRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        listRecyclerView.setAdapter(logsAdapter);
        rootRef = FirebaseDatabase.getInstance().getReference();
        emptyView.setText(mContext.getString(R.string.no_logs));
        appBarLayout.setVisibility(View.GONE);
        Toast.makeText(mContext, "Swipe log item left to delete", Toast.LENGTH_SHORT).show();
        getLogs();
        enableSwipeToDeleteAndUndo();
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(mContext) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final Log item = logsAdapter.getData().get(position);
                logsAdapter.removeItem(position);
                Handler handler = new Handler();
                Runnable runnable = () -> FirebaseDatabase.getInstance().getReference().child("logs").child(item.id).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        Toast.makeText(mContext, item + " deleted", Toast.LENGTH_SHORT).show();
                    else
                        logsAdapter.restoreItem(item, position);
                });
                handler.postDelayed(runnable, 3500);

                Snackbar snackbar = Snackbar
                        .make(getActivityCast().findViewById(android.R.id.content), "Log Deleted permanently", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", view -> {
                    logsAdapter.restoreItem(item, position);
                    listRecyclerView.scrollToPosition(position);
                    handler.removeCallbacks(runnable);
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(listRecyclerView);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void getLogs() {
        rootRef.child("logs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Log> logs = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String logString = snapshot.getValue(String.class);
                    Log log = new Log();
                    log.id = snapshot.getKey();
                    log.log = logString;
                    logs.add(log);
                }
                logsAdapter.swapLogs(logs);
                emptyView.setVisibility(logsAdapter.getItemCount() < 1 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivityCast().setToolbarTitle("Logs");
    }

    private AdminActivity getActivityCast() {
        return (AdminActivity) getActivity();
    }

    class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogsViewHolder> {
        List<Log> logsList;

        public LogsAdapter() {
            this.logsList = new ArrayList<>();
        }

        @NonNull
        @Override
        public LogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list, parent, false);
            return new LogsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LogsViewHolder holder, int position) {
            holder.log.setText(logsList.get(getItemCount() - position - 1).log);
        }

        @Override
        public int getItemCount() {
            return logsList.size();
        }

        public void restoreItem(Log item, int position) {
            logsList.add(position, item);
            notifyItemInserted(position);
        }

        public void removeItem(int position) {
            logsList.remove(position);
            notifyItemRemoved(position);
        }

        public List<Log> getData() {
            return logsList;
        }


        private void swapLogs(List<Log> logsList) {
            this.logsList = logsList;
            notifyDataSetChanged();
        }


        class LogsViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.client_businessName)
            TextView log;

            private LogsViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                log.setTextColor(ContextCompat.getColor(mContext, R.color.red_600));
            }
        }
    }
}
