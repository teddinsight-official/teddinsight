package ng.com.teddinsight.teddinsight_app.viewmodels;

import android.text.TextUtils;
import android.util.FloatProperty;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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
    private User userData;
    private long dueDate = System.currentTimeMillis();

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

    public NewTaskViewModel(Tasks tasks) {
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
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User u = snapshot.getValue(User.class);
                        users.add(u);
                    }
                }
                _users.setValue(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void setDeadline() {
        _shouldRequestNewDeadline.setValue(true);
    }

    public void submit(String taskTitle, String taskDescription) {
        Tasks tasks = tasks().getValue();
        //Log.e("TAG", tasks().getValue().getId());
        if (TextUtils.isEmpty(taskDescription) || TextUtils.isEmpty(taskTitle) || userData == null) {
            _message.setValue("Task should have a title and description");
            return;
        }
        tasks.taskDescription = taskDescription;
        tasks.taskTitle = taskTitle;
        tasks.setDueDate(dueDate);
        tasks.setAssignedBy("Admin");
        tasks.assignedToRole = userData.getRole();
        tasks.assignedTo = userData.getFirstName().concat(" ").concat(userData.getLastName());
        tasks.assignedToId = userData.getId();
        Log.e("TAG", "" + tasks.getId());
        if (tasks.getId() == null) {
            String key = taskRef.push().getKey();
            tasks.setId(key);
        }
        taskRef.child(tasks.getId()).setValue(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
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
        this.userData = userData;
    }

}
