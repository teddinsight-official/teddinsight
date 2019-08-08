package ng.com.teddinsight.teddinsight_app.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;

public class CalendarReportViewModelFactory implements ViewModelProvider.Factory {

    private ClientCalendar clientCalendar;

    public CalendarReportViewModelFactory(ClientCalendar clientCalendar) {
        this.clientCalendar = clientCalendar;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CalendarReportViewModel.class))
            return (T) new CalendarReportViewModel(clientCalendar);
        throw new IllegalArgumentException("cannot create viewmodel from supplied class");
    }
}
