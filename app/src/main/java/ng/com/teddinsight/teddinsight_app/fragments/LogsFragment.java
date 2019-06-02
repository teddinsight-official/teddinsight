package ng.com.teddinsight.teddinsight_app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
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
        getLogs();
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
                List<String> logs = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String log = snapshot.getValue(String.class);
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

    private AdminActivity getActivityCast(){
        return (AdminActivity) getActivity();
    }

    class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogsViewHolder> {
        List<String> logsList;

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
            holder.log.setText(logsList.get(getItemCount() - position - 1));
        }

        @Override
        public int getItemCount() {
            return logsList.size();
        }


        private void swapLogs(List<String> logsList) {
            this.logsList = logsList;
            notifyDataSetChanged();
        }


        class LogsViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.client_businessName)
            TextView log;

            private LogsViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
