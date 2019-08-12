package ng.com.teddinsight.teddinsight_app.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ng.com.teddinsight.teddinsight_app.fragments.TaskDialog;
import ng.com.teddinsight.teddinsight_app.models.ContentNotes;
import ng.com.teddinsight.teddinsight_app.models.Tasks;

public class NoteViewModel extends AndroidViewModel {
    private MutableLiveData<ContentNotes> _contentNotes = new MutableLiveData<>();
    private DatabaseReference reference;
    private MutableLiveData<Boolean> _operationDeleteOrDisapproveRequested = new MutableLiveData<>();
    private MutableLiveData<Boolean> _operationSaveOrApprove = new MutableLiveData<>();
    private MutableLiveData<String> _onError = new MutableLiveData<>();
    private SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext());
    private SharedPreferences.Editor editor;
    private String uid = FirebaseAuth.getInstance().getUid();

    public LiveData<String> onError() {
        return _onError;
    }

    private MutableLiveData<String> _onSuccess = new MutableLiveData<>();

    public LiveData<String> onSuccess() {
        return _onSuccess;
    }

    public LiveData<ContentNotes> contentNote() {
        return _contentNotes;
    }

    public LiveData<Boolean> operationDeleteOrDisapproveRequested() {
        return _operationDeleteOrDisapproveRequested;
    }

    public LiveData<Boolean> operationSaveOrApprove() {
        return _operationSaveOrApprove;
    }

    public void updateContentNotes(ContentNotes contentNotes) {
        _contentNotes.setValue(contentNotes);
    }


    public NoteViewModel(@NonNull Application application, ContentNotes contentNotes) {
        super(application);
        _contentNotes.setValue(contentNotes);
        reference = FirebaseDatabase.getInstance().getReference(ContentNotes.TABLE_NAME);
        _onError.setValue(null);
        _onSuccess.setValue(null);
    }

    public void deleteOrDisapproveNote() {
        _operationDeleteOrDisapproveRequested.setValue(true);
    }

    public void saveOrApproveNote() {
        _operationSaveOrApprove.setValue(true);
    }

    public void deleteNote() {
        String key = contentNote().getValue().getKey();
        if (key == null || TextUtils.isEmpty(key)) {
            _onError.setValue("You can't delete a note which has not been saved");
            return;
        }
        reference.child(key).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                _onSuccess.setValue("Note deleted");
            else
                _onError.setValue(task.getException().getLocalizedMessage());
        });
    }

    public void abortOperation() {
        _operationDeleteOrDisapproveRequested.setValue(false);
        _operationSaveOrApprove.setValue(false);
    }

    public void disapproveNote() {
        saveNote("Note disapproved");
    }

    public void saveNote() {
        saveNote("Note saved");
    }


    private void saveNote(String s) {
        ContentNotes contentNotes = _contentNotes.getValue();
        if (TextUtils.isEmpty(contentNotes.getTitle()) || TextUtils.isEmpty(contentNotes.getNote())) {
            _onError.setValue("Note must have a title and body");
            return;
        }
        if (contentNotes.getKey() == null || TextUtils.isEmpty(contentNotes.getKey())) {
            String key = reference.push().getKey();
            contentNotes.setKey(key);
        }
        reference.child(contentNotes.getKey()).setValue(contentNotes).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                _onSuccess.setValue(s);
                if (preferences.contains(TaskDialog.TASK_TO_PERFORM)) {
                    String taskId = preferences.getString(TaskDialog.TASK_TO_PERFORM, "......");
                    FirebaseDatabase.getInstance().getReference().child(Tasks.getTableName()).child(uid).child(taskId).child("status").setValue(1);
                    clearTaskToPerform();
                }
            } else {
                _onError.setValue(task.getException().getLocalizedMessage());
            }
        });

    }

    private void clearTaskToPerform() {
        if (preferences.contains(TaskDialog.TASK_TO_PERFORM)) {
            editor = preferences.edit();
            editor.remove(TaskDialog.TASK_TO_PERFORM);
            editor.apply();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        clearTaskToPerform();
    }

    public void approveNote() {
        saveNote("Note has been approved");
    }

    public void stopSuccess() {
        _onSuccess.setValue(null);
    }

    public void stopError() {
        _onError.setValue(null);
    }
}
