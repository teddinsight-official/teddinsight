package ng.com.teddinsight.teddinsight_app.fragments;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsight_app.receivers.ReminderReceiver;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;

import static ng.com.teddinsight.teddinsight_app.fragments.TextEditorDialogFragment.TAG;
import static ng.com.teddinsight.teddinsight_app.models.Tasks.TASK_COMPLETE;

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
    public static final String REMINDER_INTENT_TASK_ID = "task_id";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference().child(Tasks.getTableName()).child(user.getUid());

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

    public void setReminder() {
        Log.e(TAG, "" + tasks.getPendingIntentId());
        if (tasks.reminderSet || tasks.status == TASK_COMPLETE)
            return;
        AlarmManager manager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), ReminderReceiver.class);
        intent.putExtra(REMINDER_INTENT_TASK_ID, tasks.id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), tasks.getPendingIntentId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(5), AlarmManager.INTERVAL_HALF_DAY, pendingIntent);
        taskRef.child(tasks.id).child("reminderSet").setValue(true);
    }

    private void initialize() {
        statusView.setText(ExtraUtils.getStatText(tasks.status));
        assignedOn.setText(ExtraUtils.getHumanReadableString(tasks.getAssignedOn()));
        assignedByView.setText(tasks.assignedBy);
        descriptionView.setText(tasks.getTaskDescription());
        dueDate.setText(ExtraUtils.getHumanReadableString(tasks.getDueDate()));
        stat.setBackgroundColor(ExtraUtils.getColor(tasks.status));
        if (tasks.status == 1) {
            markAsDoneButton.setVisibility(View.GONE);
        }
        org.joda.time.DateTime n = new DateTime();
        n.withMillis(tasks.getDueDate() * 1000);
        DateTime f = new DateTime();
        f.withMillis(System.currentTimeMillis());
        Days d = Days.daysBetween(n, f);
        daysLeft.setText("" + d.getDays());
        setReminder();
    }

    @OnClick(R.id.mark_done)
    public void markAsDone() {
        ProgressDialog progressDialog = new ProgressDialog(getContext(), R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        String user = tasks.isDesigner ? "designer" : "content";
        taskRef.child(tasks.id).child("status").setValue(TASK_COMPLETE).addOnCompleteListener(task -> {
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
