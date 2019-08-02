package ng.com.teddinsight.teddinsight_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import ng.com.teddinsight.teddinsight_app.databinding.ClientTaskItemBinding;
import ng.com.teddinsight.teddinsight_app.models.Tasks;

public class ClientCalendarTaskAdapter extends ListAdapter<Tasks, ClientCalendarTaskAdapter.ClientCalendarTaskViewHolder> {


    public ClientCalendarTaskAdapter(@NonNull DiffUtil.ItemCallback<Tasks> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public ClientCalendarTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ClientCalendarTaskViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientCalendarTaskViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ClientCalendarTaskViewHolder extends RecyclerView.ViewHolder {
        private ClientTaskItemBinding binding;

        private ClientCalendarTaskViewHolder(@NonNull ClientTaskItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static ClientCalendarTaskViewHolder from(ViewGroup parent) {
            ClientTaskItemBinding binding = ClientTaskItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ClientCalendarTaskViewHolder(binding);
        }

        public void bind(Tasks tasks) {
            binding.setTask(tasks);
            binding.executePendingBindings();
        }

    }

    public static class ClientCalendarTaskDiffUtil extends DiffUtil.ItemCallback<Tasks> {

        @Override
        public boolean areItemsTheSame(@NonNull Tasks oldItem, @NonNull Tasks newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Tasks oldItem, @NonNull Tasks newItem) {
            return oldItem.equals(newItem);
        }
    }
}
