package ng.com.teddinsight.teddinsight_app.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ClientCalendarViewModelFactory implements ViewModelProvider.Factory {

    private String clientId;

    public ClientCalendarViewModelFactory(String clientId) {
        this.clientId = clientId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ClientCalendarViewModel.class))
            return (T) new ClientCalendarViewModel(clientId);
        throw new IllegalArgumentException("Invalid viewmodel class");
    }
}
