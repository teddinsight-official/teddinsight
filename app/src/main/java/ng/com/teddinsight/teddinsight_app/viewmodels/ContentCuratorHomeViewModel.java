package ng.com.teddinsight.teddinsight_app.viewmodels;

import android.app.Application;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ng.com.teddinsight.teddinsight_app.adapter.ContentsNotesAdapter;
import ng.com.teddinsight.teddinsight_app.models.ContentNotes;
import ng.com.teddinsight.teddinsightchat.models.User;

public class ContentCuratorHomeViewModel extends AndroidViewModel {

    private MutableLiveData<List<ContentNotes>> contentNotesMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> newNote = new MutableLiveData<>();
    private DatabaseReference databaseReference;
    private Query query;
    private String user;
    private ValueEventListener eventListener;


    public LiveData<List<ContentNotes>> getContentNotesLiveData() {
        return contentNotesMutableLiveData;
    }

    public LiveData<Boolean> getNewNote() {
        return newNote;
    }

    ContentCuratorHomeViewModel(@NonNull Application application, String user) {
        super(application);
        this.user = user;
        databaseReference = FirebaseDatabase.getInstance().getReference(ContentNotes.TABLE_NAME);
        if (user.equals(User.USER_ADMIN)) {
            query = databaseReference.orderByChild("reviewedByAdmin").equalTo(false);
        } else {
            query = databaseReference.orderByChild("invertedTimeStamp");
        }
        contentNotesMutableLiveData.setValue(new ArrayList<>());
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ContentNotes> contentNotesList = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ContentNotes notes = snapshot.getValue(ContentNotes.class);
                        contentNotesList.add(notes);
                    }
                    contentNotesMutableLiveData.setValue(contentNotesList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addValueEventListener(eventListener);
    }

    @BindingAdapter("notesList")
    public static void getNotesList(RecyclerView recyclerView, List<ContentNotes> contentNotes) {
        ContentsNotesAdapter contentNotesAdapter = (ContentsNotesAdapter) recyclerView.getAdapter();
        if (contentNotesAdapter != null)
            contentNotesAdapter.submitList(contentNotes);
    }

    @BindingAdapter("toggleVisibility")
    public static void toggleVisibility(TextView textView, List<ContentNotes> contentNotes) {
        if (contentNotes.isEmpty())
            textView.setVisibility(View.VISIBLE);
        else
            textView.setVisibility(View.GONE);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        query.removeEventListener(eventListener);
    }

    public void newSampleNote() {
        newNote.setValue(true);
    }

    public void stopAddNewNote() {
        newNote.setValue(false);
    }
}
