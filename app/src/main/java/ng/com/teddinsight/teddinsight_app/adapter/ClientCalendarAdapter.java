package ng.com.teddinsight.teddinsight_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.databinding.ClientCalendarItemBinding;
import ng.com.teddinsight.teddinsight_app.models.ClientCalendar;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;

public class ClientCalendarAdapter extends ListAdapter<ClientCalendar, ClientCalendarAdapter.ClientCalendarViewHolder> {


    private OnClientCalendarItemClick onClientCalendarItemClick;

    public ClientCalendarAdapter(@NonNull DiffUtil.ItemCallback<ClientCalendar> diffCallback, OnClientCalendarItemClick onClientCalendarItemClick) {
        super(diffCallback);
        this.onClientCalendarItemClick = onClientCalendarItemClick;
    }

    @NonNull
    @Override
    public ClientCalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ClientCalendarViewHolder.from(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientCalendarViewHolder holder, int position) {
        holder.bind(getItem(position), onClientCalendarItemClick);
    }


    static class ClientCalendarViewHolder extends RecyclerView.ViewHolder {
        ClientCalendarItemBinding binding;

        public ClientCalendarViewHolder(@NonNull ClientCalendarItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static ClientCalendarViewHolder from(ViewGroup parent) {
            ClientCalendarItemBinding binding = ClientCalendarItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ClientCalendarViewHolder(binding);
        }

        public void bind(ClientCalendar clientCalendar, OnClientCalendarItemClick onClientCalendarItemClick) {
            binding.setClientCalendar(clientCalendar);
            binding.itemview.setOnClickListener(v -> onClientCalendarItemClick.onClientCalendarItemClicked(clientCalendar));
            binding.calendarCreationDate.setText(binding.itemview.getContext().getString(R.string.created_at, ExtraUtils.getHumanReadableString(clientCalendar.getDateCreated(), true)));
            binding.executePendingBindings();
        }

    }

    public static class ClientCalendarDiffUtil extends DiffUtil.ItemCallback<ClientCalendar> {

        @Override
        public boolean areItemsTheSame(@NonNull ClientCalendar oldItem, @NonNull ClientCalendar newItem) {
            return oldItem.getKey().equals(newItem.getKey());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ClientCalendar oldItem, @NonNull ClientCalendar newItem) {
            return oldItem.equals(newItem);
        }
    }

    public interface OnClientCalendarItemClick {
        void onClientCalendarItemClicked(ClientCalendar clientCalendar);
    }
}
