package ng.com.teddinsight.teddinsight_app.viewmodels;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.adapter.ClientCalendarTaskAdapter;
import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsight_app.widgets.TextViewDrawableSize;

public class ClientCalendarDetailsViewModel extends ViewModel {
    //private ClientCalendar clientCalendar;
    private MutableLiveData<ClientCalendar> _clientCalendar = new MutableLiveData<>();
    private DatabaseReference databaseReference;
    private ValueEventListener taskValueEventListener;
    private Query query;

    private MutableLiveData<List<Tasks>> _listOfCalendarTask = new MutableLiveData<>();
    private MutableLiveData<String> _message = new MutableLiveData<>();
    private MutableLiveData<Tasks> _creatNewTask = new MutableLiveData<>();
    private MutableLiveData<ClientCalendar> _deleteClientCalendar = new MutableLiveData<>();
    private MutableLiveData<ClientCalendar> _dispatchClientCalendar = new MutableLiveData<>();
    private MutableLiveData<Boolean> _finishOperation = new MutableLiveData<>();

    public LiveData<Boolean> finishOperation() {
        return _finishOperation;
    }

    public LiveData<ClientCalendar> startDeleteClientCalendar() {
        return _deleteClientCalendar;
    }

    public LiveData<ClientCalendar> startDispatchClientCalendar() {
        return _dispatchClientCalendar;
    }


    public LiveData<ClientCalendar> getClientCalendar() {
        return _clientCalendar;
    }

    public LiveData<Tasks> creatNewTask() {
        return _creatNewTask;
    }

    public void stopCreateNewTask() {
        _creatNewTask.setValue(null);
    }

    public LiveData<String> message() {
        return _message;
    }

    public LiveData<List<Tasks>> listOfCalendarTask() {
        return _listOfCalendarTask;
    }

    @BindingAdapter("formattedDeadlineDate")
    public static void formatDeadlineDate(TextView textView, long deadlineTimetamp) {
        String text = textView.getContext().getString(R.string.deadline, ExtraUtils.getHumanReadableString(deadlineTimetamp, false));
        textView.setText(text);
    }

    @BindingAdapter("taskLists")
    public static void setTaskList(RecyclerView recyclerView, List<Tasks> tasksList) {
        ClientCalendarTaskAdapter adapter = (ClientCalendarTaskAdapter) recyclerView.getAdapter();
        if (adapter != null)
            adapter.submitList(tasksList);
    }

    @BindingAdapter("setEmptyViewVisibility")
    public static void setEmptyListVisibility(TextViewDrawableSize textView, List lists) {
        if (lists != null && lists.isEmpty())
            textView.setVisibility(View.VISIBLE);
        else
            textView.setVisibility(View.GONE);
    }

    @BindingAdapter("setNewTaskFabVisibility")
    public static void setNewTaskFabVisibility(FloatingActionButton fabVisibility, ClientCalendar clientCalendar) {
        if (clientCalendar.isBeginPublishing())
            fabVisibility.setVisibility(View.GONE);
        else
            fabVisibility.setVisibility(View.VISIBLE);
    }


    public void addDummyTask() {
        Tasks tasks = new Tasks();
        tasks.setTaskTitle("");
        tasks.setTaskDescription("");
        tasks.clientCalendarId = getClientCalendar().getValue().getKey();
        tasks.setDueDate(System.currentTimeMillis());
        _creatNewTask.setValue(tasks);
    }


    public void deleteCalendar() {
        _deleteClientCalendar.setValue(_clientCalendar.getValue());
    }

    public void startCalendarDispatch() {
        _dispatchClientCalendar.setValue(_clientCalendar.getValue());
    }

    public void endFinishOperation() {
        _finishOperation.setValue(false);
    }


    public void finishClientCalendarDeletion() {
        _deleteClientCalendar.setValue(null);
    }

    public void finishClientCalendarDispatch() {
        _dispatchClientCalendar.setValue(null);
    }

    public ClientCalendarDetailsViewModel(ClientCalendar clientCalendar) {
        _clientCalendar.setValue(clientCalendar);
        _listOfCalendarTask.setValue(new ArrayList<>());
        _message.setValue(null);
        taskValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Tasks> tasksList = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Tasks tasks = snapshot.getValue(Tasks.class);
                        tasks.setId(snapshot.getKey());
                        tasksList.add(tasks);
                    }
                } else {
                    _message.setValue("Calendar has no task in it");
                }
                _listOfCalendarTask.setValue(tasksList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                _message.setValue("An error occurred: " + databaseError.getDetails());
            }
        };
        Log.e("TAG", clientCalendar.getName());
        databaseReference = FirebaseDatabase.getInstance().getReference().child(ClientCalendar.getTableName()).child(clientCalendar.getKey()).child(Tasks.getTableName());
        query = databaseReference;
        query.addValueEventListener(taskValueEventListener);
    }

    public void stopMessageDispatch() {
        _message.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        query.removeEventListener(taskValueEventListener);
        //taskValueEventListener = null;
    }

    public void performDelete(ClientCalendar clientCalendar) {
        _message.setValue("Deleting " + clientCalendar.getName());
        FirebaseDatabase.getInstance().getReference().child(ClientCalendar.getBaseTableName()).child(clientCalendar.getKey()).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    _finishOperation.setValue(true);
                    _message.setValue(clientCalendar.getName().concat(" has been deleted"));
                } else {
                    _message.setValue("An error occurred while deleting calendar " + task.getException().getLocalizedMessage());
                }
            }
        });
    }

    public void dispatchNow(ClientCalendar clientCalendar12) {
        clientCalendar12.setNeedsPublishing(false);
        clientCalendar12.setBeginPublishing(true);
        FirebaseDatabase.getInstance().getReference()
                .child(ClientCalendar.getBaseTableName())
                .child(clientCalendar12.getClientId())
                .child(clientCalendar12.getKey())
                .setValue(clientCalendar12)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        _finishOperation.setValue(true);
                        _message.setValue(clientCalendar12.getName().concat(" dispatched successfully"));
                    }
                });
    }
}
