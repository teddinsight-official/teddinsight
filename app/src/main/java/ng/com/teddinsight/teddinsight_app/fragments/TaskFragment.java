package ng.com.teddinsight.teddinsight_app.fragments;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;

import static ng.com.teddinsight.teddinsight_app.utils.ExtraUtils.getColor;

public class TaskFragment extends Fragment implements Listeners.TaskItemClicked {

    DatabaseReference reference;
    TaskAdapter adapter;
    @BindView(R.id.taask_recyclerView)
    RecyclerView recyclerView;
    boolean isDesigner;
    String user;

    public static Fragment NewInstance() {
        return new TaskFragment();
    }

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_tasks, container, false);
        ButterKnife.bind(this, v);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        if (firebaseUser.getEmail().startsWith("content")) {
            isDesigner = false;
            user = "content";
        } else {
            isDesigner = true;
            user = "designer";
        }
        reference = FirebaseDatabase.getInstance().getReference(user + "/tasks");
        adapter = new TaskAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        fetchTasks();
        return v;
    }

    private void jmkds(){
        DatabaseReference keyref = reference.push();
        String key = keyref.getKey();
        Tasks tasks = new Tasks(key, "Task o","hkspfoijodfv","HR", System.currentTimeMillis());
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
                    tasksList.add(tasks);
                }
                adapter.swapData(tasksList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
            holder.dueDate.setText(ExtraUtils.formatDate(tasks.getDueDate() * 1000));
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
