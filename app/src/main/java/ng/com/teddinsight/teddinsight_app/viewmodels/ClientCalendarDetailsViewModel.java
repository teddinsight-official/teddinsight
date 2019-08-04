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
import ng.com.teddinsight.teddinsightchat.models.User;

public class ClientCalendarDetailsViewModel extends ViewModel {
    //private ClientCalendar clientCalendar;
    private MutableLiveData<ClientCalendar> _clientCalendar = new MutableLiveData<>();
    private DatabaseReference databaseReference;
    private ValueEventListener taskValueEventListener;
    private Query query;

    private MutableLiveData<List<Tasks>> _listOfCalendarTask = new MutableLiveData<>();
    private MutableLiveData<String> _message = new MutableLiveData<>();
    private MutableLiveData<Tasks> _creatNewTask = new MutableLiveData<>();
    private MutableLiveData<ClientCalendar> _clientCaledarToPublish = new MutableLiveData<>();


    public LiveData<ClientCalendar> getClientCalendar(){
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

    public void addDummyTask() {
        Tasks tasks = new Tasks();
        tasks.setTaskTitle("");
        tasks.setTaskDescription("");
        tasks.clientCalendarId = getClientCalendar().getValue().getKey();
        tasks.setDueDate(System.currentTimeMillis());
        _creatNewTask.setValue(tasks);
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
}
