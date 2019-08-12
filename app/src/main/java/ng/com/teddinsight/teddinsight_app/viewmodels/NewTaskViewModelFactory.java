package ng.com.teddinsight.teddinsight_app.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;
import ng.com.teddinsight.teddinsight_app.models.Tasks;

public class NewTaskViewModelFactory implements ViewModelProvider.Factory {

    private Tasks tasks;
    private ClientCalendar clientCalendar;

    public NewTaskViewModelFactory(Tasks tasks, ClientCalendar clientCalendar) {
        this.tasks = tasks;
        this.clientCalendar = clientCalendar;
    }


    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NewTaskViewModel.class))
            return (T) new NewTaskViewModel(tasks, clientCalendar);
        throw new IllegalArgumentException("Cannot create viewmodel");
    }
}
