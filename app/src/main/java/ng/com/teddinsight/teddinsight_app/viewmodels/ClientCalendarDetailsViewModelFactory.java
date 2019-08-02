package ng.com.teddinsight.teddinsight_app.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;

public class ClientCalendarDetailsViewModelFactory implements ViewModelProvider.Factory {
    private ClientCalendar clientCalendar;

    public ClientCalendarDetailsViewModelFactory(ClientCalendar clientCalendar) {
        this.clientCalendar = clientCalendar;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ClientCalendarDetailsViewModel.class)) {
            return (T) new ClientCalendarDetailsViewModel(clientCalendar);
        }
        throw new IllegalArgumentException("Cannot create viewmodel");
    }
}
