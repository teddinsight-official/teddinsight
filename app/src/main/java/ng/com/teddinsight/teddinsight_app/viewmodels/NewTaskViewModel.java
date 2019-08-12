package ng.com.teddinsight.teddinsight_app.viewmodels;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsightchat.models.User;

public class NewTaskViewModel extends ViewModel {
    private MutableLiveData<Tasks> _tasks = new MutableLiveData<>();
    private MutableLiveData<List<User>> _users = new MutableLiveData<>();
    private DatabaseReference userRef;
    private DatabaseReference taskRef;
    private MutableLiveData<String> _message = new MutableLiveData<>();
    private MutableLiveData<Boolean> _taskSaveSuccessful = new MutableLiveData<>();
    private MutableLiveData<ClientCalendar> _clientCalendar = new MutableLiveData<>();
    private User userData;
    private long dueDate = System.currentTimeMillis();

    public LiveData<ClientCalendar> calendarLiveData() {
        return _clientCalendar;
    }

    public LiveData<Boolean> taskSaveSuccessful() {
        return _taskSaveSuccessful;
    }

    public LiveData<String> message() {
        return _message;
    }

    public LiveData<List<User>> users() {
        return _users;
    }

    public LiveData<Tasks> tasks() {
        return _tasks;
    }


    private MutableLiveData<Boolean> _shouldRequestNewDeadline = new MutableLiveData<>();

    public LiveData<Boolean> shouldRequestNewDeadline() {
        return _shouldRequestNewDeadline;
    }

    public void stopNewDeadlineRequest() {
        _shouldRequestNewDeadline.setValue(false);
    }

    public void setTaskDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public NewTaskViewModel(Tasks tasks, ClientCalendar clientCalendar) {
        _tasks.setValue(tasks);
        _message.setValue(null);
        taskRef = FirebaseDatabase.getInstance()
                .getReference()
                .child(ClientCalendar.getTableName())
                .child(tasks.clientCalendarId)
                .child(Tasks.getTableName());
        userRef = FirebaseDatabase.getInstance().getReference().child(User.getTableName());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<User> users = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() > 0) {
                    List<String> ignoreUsers = new ArrayList<>();
                    ignoreUsers.add(User.USER_ADMIN);
                    ignoreUsers.add(User.USER_CLIENT);
                    ignoreUsers.add(User.USER_PARTNER);
                    ignoreUsers.add(User.USER_HR);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User u = snapshot.getValue(User.class);
                        if (!ignoreUsers.contains(u.getRole()))
                            users.add(u);
                    }
                }
                Log.e("TAG", "" + users.size());
                _users.setValue(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @BindingAdapter("setButtonText")
    public static void setButtonText(Button buttonText, Tasks tasks) {
        buttonText.setText(tasks.getId() == null ? "Add Task to Calendar" : "Update task info");
        buttonText.setVisibility(tasks.getStatus() == Tasks.TASK_COMPLETE ? View.GONE : View.VISIBLE);
    }

    public void setDeadline() {
        _shouldRequestNewDeadline.setValue(true);
    }

    public void submit(String taskTitle, String taskDescription) {
        Tasks tasks = tasks().getValue();
        //Log.e("TAG", tasks().getValue().getId());
        if (TextUtils.isEmpty(taskDescription) || TextUtils.isEmpty(taskTitle)) {
            _message.setValue("Task should have a title and description");
            return;
        } else if (userData == null) {
            _message.setValue("Please select one user for this task");
            return;
        }
        boolean isNotUpdate = false;
        tasks.taskDescription = taskDescription;
        tasks.taskTitle = taskTitle;
        tasks.setDueDate(dueDate);
        tasks.setAssignedBy("Admin");
        tasks.assignedToRole = userData.getRole();
        tasks.assignedTo = userData.getFirstName().concat(" ").concat(userData.getLastName());
        tasks.assignedToId = userData.getId();
        Random rando = new Random();
        tasks.pendingIntentId = rando.nextInt(1000) + 1;
        Log.e("TAG", "" + tasks.getId());
        if (tasks.getId() == null) {
            isNotUpdate = true;
            tasks.assignedOn = System.currentTimeMillis();
            String key = taskRef.push().getKey();
            tasks.setId(key);
        }
        if (isNotUpdate) {
            FirebaseDatabase.getInstance().getReference().child(ClientCalendar.getBaseTableName())
                    .child(tasks.clientId)
                    .child(tasks.clientCalendarId)
                    .runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            ClientCalendar clientCalendar = mutableData.getValue(ClientCalendar.class);
                            if (clientCalendar != null) {
                                clientCalendar.setTaskCount(clientCalendar.getTaskCount() + 1);
                                mutableData.setValue(clientCalendar);
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                        }
                    });
        }
        _message.setValue("Adding task, please wait...");
        taskRef.child(tasks.getId()).setValue(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                _message.setValue("Task was successfully added to calendar.");
                _taskSaveSuccessful.setValue(true);
            } else
                _message.setValue("An error occurred while saving task: " + task.getException().getLocalizedMessage());
        });

    }

    public void stopTaskSaveSuccessful() {
        _taskSaveSuccessful.setValue(false);
    }

    public void stopMessageDispatch() {
        _message.setValue(null);
    }

    public void setUserData(User userData) {
        Log.e("TAG", "userdata set");
        this.userData = userData;
    }

}
