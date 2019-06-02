package ng.com.teddinsight.teddinsight_app.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners;
import ng.com.teddinsight.teddinsight_app.models.Notifications;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;

import static ng.com.teddinsight.teddinsight_app.utils.ExtraUtils.getColor;

public class TaskFragment extends Fragment implements Listeners.TaskItemClicked {

    DatabaseReference reference;
    TaskAdapter adapter;
    @BindView(R.id.taask_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.t_swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.no_task_yet)
    TextView noTaskTextView;
    boolean isDesigner;
    String user;
    FirebaseUser firebaseUser;
    private Context mContext;

    public static Fragment NewInstance() {
        return new TaskFragment();
    }

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_tasks, container, false);
        ButterKnife.bind(this, v);
        refreshLayout.setRefreshing(true);
        refreshLayout.setColorSchemeColors(Color.RED, Color.BLUE, Color.GREEN, Color.CYAN);
        refreshLayout.setOnRefreshListener(this::fetchTasks);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        if (firebaseUser.getEmail().startsWith("content")) {
            isDesigner = false;
            user = "content";
        } else {
            isDesigner = true;
            user = "designer";
        }
        reference = FirebaseDatabase.getInstance().getReference(Tasks.getTableName()).child(firebaseUser.getUid());
        adapter = new TaskAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);
        //jmkds();
        fetchTasks();
        clearTaskNotification();
        return v;
    }

    private void clearTaskNotification() {
        FirebaseDatabase.getInstance().getReference().child(Notifications.getTableName()).child(firebaseUser.getUid()).child("newTaskReceived").setValue(false);
    }

    private void jmkds() {
        DatabaseReference keyref = reference.push();
        String key = keyref.getKey();
        Tasks tasks = new Tasks(key, "Task o", "hkspfoijodfv", "HR", System.currentTimeMillis());
        reference.child(key).updateChildren(tasks.toMap());

    }

    private void fetchTasks() {
        Query q = reference.orderByChild("status");
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Tasks> tasksList = new ArrayList<>();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Tasks tasks = snap.getValue(Tasks.class);
                    tasks.id = snap.getKey();
                    tasksList.add(tasks);
                }
                adapter.swapData(tasksList);
                if (adapter.getItemCount() > 0)
                    noTaskTextView.setVisibility(View.INVISIBLE);
                else
                    noTaskTextView.setVisibility(View.VISIBLE);
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                refreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onTaskItemClicked(boolean isDesigner, Tasks tasks) {
        tasks.isDesigner = isDesigner;
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        assert getFragmentManager() != null;
        Fragment prev = getFragmentManager().findFragmentByTag("task");
        if (prev != null) {
            ft.remove(prev);
        }
        TaskDialog.NewInstance(tasks).show(ft, "task");

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {


        List<Tasks> tasksList;
        Listeners.TaskItemClicked taskItemClicked;

        TaskAdapter(List<Tasks> tasks, Listeners.TaskItemClicked taskItemClicked) {
            this.tasksList = tasks;
            this.taskItemClicked = taskItemClicked;
        }

        @NonNull
        @Override
        public TaskAdapter.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            return new TaskViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            Tasks tasks = tasksList.get(position);
            holder.asignedBy.setText(tasks.getAssignedBy());
            holder.dueDate.setText(ExtraUtils.getHumanReadableString(tasks.getDueDate()));
            holder.templateTitleView.setText(tasks.getTaskTitle());
            GradientDrawable magnitudeCircle = (GradientDrawable) holder.circle.getBackground();
            int magnitudeColor = getColor(tasks.getStatus());
            magnitudeCircle.setColor(magnitudeColor);
        }


        @Override
        public int getItemCount() {
            return tasksList.size();
        }

        void swapData(List<Tasks> tasks) {
            this.tasksList = tasks;
            this.notifyDataSetChanged();
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.task_title)
            TextView templateTitleView;
            @BindView(R.id.assigned_by)
            TextView asignedBy;
            @BindView(R.id.due)
            TextView dueDate;
            @BindView(R.id.circle)
            TextView circle;

            TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(v -> {
                    taskItemClicked.onTaskItemClicked(isDesigner, tasksList.get(getAdapterPosition()));
                });
            }
        }

    }
}
