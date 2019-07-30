package ng.com.teddinsight.teddinsight_app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ng.com.teddinsight.teddinsight_app.models.ContentNotes;

public class NoteViewModelFactory implements ViewModelProvider.Factory {
    private Application application;
    private ContentNotes contentNotes;

    public NoteViewModelFactory(Application application, ContentNotes contentNotes) {
        this.application = application;
        this.contentNotes = contentNotes;
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NoteViewModel.class))
            return (T) new NoteViewModel(application, contentNotes);
        throw new IllegalArgumentException("cannot create note viewmodel");
    }
}