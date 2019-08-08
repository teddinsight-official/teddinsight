package ng.com.teddinsight.teddinsight_app.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.databinding.ReportItemChildBinding;
import ng.com.teddinsight.teddinsight_app.databinding.ReportItemHeaderBinding;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsight_app.viewmodels.CalendarReportViewModel;
import ng.com.teddinsight.teddinsight_app.viewmodels.CalendarReportViewModel.ReportListItem;

public class CalendarReportAdapter extends ListAdapter<ReportListItem, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_BODY = 1;

    public CalendarReportAdapter(@NonNull DiffUtil.ItemCallback<ReportListItem> diffCallback) {
        super(diffCallback);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return CalendarReportHeaderViewHolder.from(parent);
            case VIEW_TYPE_BODY:
                return CalendarReportBodyViewHolder.from(parent);
            default:
                return CalendarReportHeaderViewHolder.from(parent);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isHeader())
            return VIEW_TYPE_HEADER;
        else
            return VIEW_TYPE_BODY;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_BODY:
                ((CalendarReportBodyViewHolder) holder).bind(getItem(position));
                break;
            case VIEW_TYPE_HEADER:
                ((CalendarReportHeaderViewHolder) holder).bind(getItem(position));
                break;
        }
    }

    public static class CalendarReportHeaderViewHolder extends RecyclerView.ViewHolder {
        private ReportItemHeaderBinding binding;

        private CalendarReportHeaderViewHolder(@NonNull ReportItemHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static CalendarReportHeaderViewHolder from(ViewGroup parent) {
            ReportItemHeaderBinding binding = ReportItemHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new CalendarReportHeaderViewHolder(binding);
        }

        public void bind(CalendarReportViewModel.ReportListItem reportItemList) {
            CalendarReportViewModel.HeaderItem reportItem = ((CalendarReportViewModel.HeaderItem) reportItemList);
            binding.headerText.setText(reportItem.getHeaderTagsText());
            switch (reportItem.getHeaderTags()) {
                case COMPLETED:
                    binding.reportHeader.setCardBackgroundColor(Color.parseColor("#64B5F6"));
                    break;
                case INCOMPLETE:
                    binding.reportHeader.setCardBackgroundColor(Color.parseColor("#1565C0"));
                    break;
                case COMPLETEDBEFOREDEADLINE:
                    binding.reportHeader.setCardBackgroundColor(Color.parseColor("#EF6C00"));
                    break;
                case COMPLETEDAFTERDEADLINE:
                    binding.reportHeader.setCardBackgroundColor(Color.parseColor("#FFD54F"));
                    break;
            }
        }
    }

    public static class CalendarReportBodyViewHolder extends RecyclerView.ViewHolder {
        private ReportItemChildBinding binding;

        private CalendarReportBodyViewHolder(@NonNull ReportItemChildBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static CalendarReportBodyViewHolder from(ViewGroup parent) {
            ReportItemChildBinding binding = ReportItemChildBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new CalendarReportBodyViewHolder(binding);
        }

        public void bind(CalendarReportViewModel.ReportListItem reportListItem) {
            CalendarReportViewModel.ReportItem reportItem = (CalendarReportViewModel.ReportItem) reportListItem;
            binding.setReportitem(reportItem);
            binding.executePendingBindings();
        }
    }

    @BindingAdapter("setDateCompleted")
    public static void setDateCompleted(TextView textView, long dateCompleted) {
        if (dateCompleted > 0) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(textView.getContext().getString(R.string.date_completed, ExtraUtils.getHumanReadableString(dateCompleted, false)));
        } else
            textView.setVisibility(View.GONE);
    }

    public static class ReportDiffUtil extends DiffUtil.ItemCallback<ReportListItem> {

        @Override
        public boolean areItemsTheSame(@NonNull ReportListItem oldItem, @NonNull ReportListItem newItem) {
            return oldItem.getUniqueId().equals(newItem.getUniqueId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ReportListItem oldItem, @NonNull ReportListItem newItem) {
            return oldItem.equals(newItem);
        }
    }
}
