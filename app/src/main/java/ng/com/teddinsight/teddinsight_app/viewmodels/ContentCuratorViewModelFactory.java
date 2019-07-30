package ng.com.teddinsight.teddinsight_app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ContentCuratorViewModelFactory implements ViewModelProvider.Factory {
    private Application application;
    private String user;

    public ContentCuratorViewModelFactory(Application application, String user) {
        this.application = application;
        this.user = user;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ContentCuratorHomeViewModel.class)) {
            return (T) new ContentCuratorHomeViewModel(application, user);
        }
        throw new IllegalArgumentException("cannot create viewmodel");
    }
}
