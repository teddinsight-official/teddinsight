package ng.com.teddinsight.teddinsight_app.viewmodels;

import android.app.Application;
import android.view.View;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ng.com.teddinsight.teddinsight_app.adapter.ClientCalendarAdapter;
import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;
import ng.com.teddinsight.teddinsight_app.widgets.TextViewDrawableSize;

public class ClientCalendarViewModel extends ViewModel {
    private Query query;
    private String clientId;
    private DatabaseReference reference;
    private ValueEventListener myValueEventListener;
    private MutableLiveData<List<ClientCalendar>> _clientCalendarList = new MutableLiveData<>();
    private MutableLiveData<String> _message = new MutableLiveData<>();
    private MutableLiveData<Boolean> _requestNewCalendarName = new MutableLiveData<>();
    private MutableLiveData<ClientCalendar> _newClientCalendarItem = new MutableLiveData<>();


    public LiveData<ClientCalendar> newClientCalendarItem() {
        return _newClientCalendarItem;
    }

    public LiveData<Boolean> requestNewCalendarName() {
        return _requestNewCalendarName;
    }

    public LiveData<String> message() {
        return _message;
    }

    public LiveData<List<ClientCalendar>> clientCalendarList() {
        return _clientCalendarList;
    }

    public void addDummyCalendar() {
        _requestNewCalendarName.setValue(true);
    }

    public void addNewCalendarItem(ClientCalendar clientCalendar) {
        _requestNewCalendarName.setValue(false);
        if (clientCalendar == null) {
            _newClientCalendarItem.setValue(clientCalendar);
            return;
        }
        String key = reference.push().getKey();
        clientCalendar.setBeginPublishing(false);
        clientCalendar.setNeedsPublishing(false);
        clientCalendar.setTaskCount(0);
        reference.child(key).setValue(clientCalendar).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                _newClientCalendarItem.setValue(clientCalendar);
            } else {
                _message.setValue("An error occurred " + task.getException().getLocalizedMessage());
            }
        });
    }
    public void stopShowError() {
        _message.setValue(null);
    }

    public ClientCalendarViewModel(String clientId) {
        this.clientId = clientId;
        myValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ClientCalendar> clientCalendarList = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ClientCalendar clientCalendar = snapshot.getValue(ClientCalendar.class);
                        clientCalendar.setKey(snapshot.getKey());
                        clientCalendarList.add(clientCalendar);
                    }
                    _clientCalendarList.setValue(clientCalendarList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                _message.setValue("Error occurred while reading from database " + databaseError.getDetails());
            }
        };
        reference = FirebaseDatabase.getInstance().getReference().child(ClientCalendar.getBaseTableName()).child(clientId);
        query = reference;
        _message.setValue(null);
        _clientCalendarList.setValue(new ArrayList<>());
        query.addValueEventListener(myValueEventListener);
    }

    @BindingAdapter("clientCalenderList")
    public static void setClientCalendarList(RecyclerView recyclerView, List<ClientCalendar> clientCalendarList) {
        ClientCalendarAdapter adapter = (ClientCalendarAdapter) recyclerView.getAdapter();
        if (adapter != null)
            adapter.submitList(clientCalendarList);
    }

    @BindingAdapter("toggleEmptyCalendarVisibility")
    public static void toggleEmptyCalendarVisibility(TextViewDrawableSize textViewDrawableSize, List<ClientCalendar> clientCalendars) {
        if (clientCalendars.isEmpty())
            textViewDrawableSize.setVisibility(View.VISIBLE);
        else
            textViewDrawableSize.setVisibility(View.GONE);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        query.removeEventListener(myValueEventListener);
    }
}
