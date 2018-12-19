package ng.com.teddinsight.teddinsight_app.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.models.DesignerDesigns;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsight_app.utils.TimeUtils;

public class TaskDialog extends DialogFragment {

    @BindView(R.id.status)
    TextView statusView;
    @BindView(R.id.desc)
    TextView descriptionView;
    @BindView(R.id.assigned_by)
    TextView assignedByView;
    @BindView(R.id.assigned_on)
    TextView assignedOn;
    @BindView(R.id.due_date)
    TextView dueDate;
    @BindView(R.id.stat)
    TextView stat;
    @BindView(R.id.days_left)
    TextView daysLeft;
    @BindView(R.id.mark_done)
    Button markAsDoneButton;
    Tasks tasks;

    public static TaskDialog NewInstance(Tasks tasks) {
        Bundle b = new Bundle();
        b.putParcelable("tasks", tasks);
        TaskDialog taskDialog = new TaskDialog();
        taskDialog.setArguments(b);
        return taskDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_details, container, false);
        ButterKnife.bind(this, view);
        Bundle dd = getArguments();
        tasks = dd.getParcelable("tasks");
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(v -> getDialog().dismiss());
        toolbar.setTitle(tasks.getTaskTitle());
        toolbar.setTitleTextAppearance(getContext(), R.style.ToolbarTheme);
        initialize();
        return view;
    }

    private void initialize() {
        statusView.setText(ExtraUtils.getStatText(tasks.status));
        assignedOn.setText(ExtraUtils.formatDate(tasks.getAssignedOn()));
        assignedByView.setText(tasks.assignedBy);
        descriptionView.setText(tasks.getTaskDescription());
        dueDate.setText(ExtraUtils.formatDate(tasks.getDueDate() * 1000));
        stat.setBackgroundColor(ExtraUtils.getColor(tasks.status));
        if (tasks.status == 1) {
            markAsDoneButton.setVisibility(View.GONE);
        }
        TimeUtils utils = new TimeUtils(tasks.getDueDate() * 1000);
        LocalDate localDate = new LocalDate();
        org.joda.time.DateTime n = new DateTime();
        n.withMillis(tasks.getDueDate() * 1000);
        DateTime f = new DateTime();
        f.withMillis(System.currentTimeMillis());
        Days d = Days.daysBetween(n, f);
        daysLeft.setText("" + d.getDays());

    }

    @OnClick(R.id.mark_done)
    public void markAsDone() {
        ProgressDialog progressDialog = new ProgressDialog(getContext(), R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        String user = tasks.isDesigner ? "designer" : "content";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(user + "/tasks/" + tasks.getId());
        reference.child("status").setValue(0).addOnCompleteListener(task -> {
            progressDialog.cancel();
            if (task.isSuccessful()) {
                markAsDoneButton.setVisibility(View.GONE);
                statusView.setText(ExtraUtils.getStatText(0));
                stat.setBackgroundColor(ExtraUtils.getColor(0));
            } else {
                Toast.makeText(getContext(), "An Error Occurred, Try Again", Toast.LENGTH_LONG).show();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

}
