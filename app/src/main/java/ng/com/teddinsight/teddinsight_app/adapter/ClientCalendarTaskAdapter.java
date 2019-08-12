package ng.com.teddinsight.teddinsight_app.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import ng.com.teddinsight.teddinsight_app.databinding.ClientTaskItemBinding;
import ng.com.teddinsight.teddinsight_app.models.Tasks;

import static ng.com.teddinsight.teddinsight_app.utils.ExtraUtils.getColor;

public class ClientCalendarTaskAdapter extends ListAdapter<Tasks, ClientCalendarTaskAdapter.ClientCalendarTaskViewHolder> {


    public OnTaskClicked onTaskClicked;

    public ClientCalendarTaskAdapter(@NonNull DiffUtil.ItemCallback<Tasks> diffCallback, OnTaskClicked onTaskClicked) {
        super(diffCallback);
        this.onTaskClicked = onTaskClicked;
    }

    @NonNull
    @Override
    public ClientCalendarTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ClientCalendarTaskViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientCalendarTaskViewHolder holder, int position) {
        holder.bind(getItem(position), onTaskClicked);
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

        public void bind(Tasks tasks, OnTaskClicked onTaskClicked) {
            binding.setTask(tasks);
            binding.itemview.setOnClickListener(v -> onTaskClicked.onTaskClicked(tasks));
            binding.executePendingBindings();
        }

    }

    @BindingAdapter("setTaskDrawable")
    public static void setTaskDrawable(TextView textView, int status) {
        GradientDrawable magnitudeCircle = (GradientDrawable) textView.getBackground();
        int magnitudeColor = getColor(status);
        magnitudeCircle.setColor(magnitudeColor);
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

    public interface OnTaskClicked {
        void onTaskClicked(Tasks tasks);
    }
}
