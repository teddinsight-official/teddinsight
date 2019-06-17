package ng.com.teddinsight.teddinsight_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners;
import ng.com.teddinsight.teddinsight_app.models.Job;
import ng.com.teddinsight.teddinsight_app.models.Receipts;
import ng.com.teddinsight.teddinsight_app.utils.ExtraUtils;
import ng.com.teddinsight.teddinsightchat.models.User;

public class ServicesRecyclerViewAdapter extends RecyclerView.Adapter<ServicesRecyclerViewAdapter.ServivesRecyclerViewHolder> {

    List<Receipts> servicesList;
    List<Job> jobList;
    User user;
    Listeners.ClientServiceClickListener clientServiceClickListener;

    public ServicesRecyclerViewAdapter(User user, Listeners.ClientServiceClickListener clientServiceClickListener) {
        this.servicesList = new ArrayList<>();
        this.jobList = new ArrayList<>();
        this.user = user;
        this.clientServiceClickListener = clientServiceClickListener;
    }

    @NonNull
    @Override
    public ServivesRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ServivesRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.client_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ServivesRecyclerViewHolder holder, int position) {
        if (user.role.equalsIgnoreCase(User.USER_CLIENT)) {
            Receipts service = servicesList.get(getItemCount() - position - 1);
            holder.serviceDescription.setText(service.service.concat(" Package requested on ").concat(ExtraUtils.getHumanReadableString(service.dateIssued)));
        } else {
            Job job = jobList.get(getItemCount() - position - 1);
            holder.serviceDescription.setText(job.getJobType());
        }
    }

    @Override
    public int getItemCount() {
        return user.role.equalsIgnoreCase(User.USER_CLIENT) ? servicesList.size() : jobList.size();
    }

    public void swapData(List<Receipts> servicesList) {
        this.servicesList = servicesList;
        notifyDataSetChanged();
    }

    public void swapJobs(List<Job> jobs) {
        this.jobList = jobs;
        notifyDataSetChanged();
    }

    class ServivesRecyclerViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.client_businessName)
        TextView serviceDescription;

        public ServivesRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                if (user.role.equalsIgnoreCase(User.USER_CLIENT))
                    clientServiceClickListener.onClientServiceClicked(servicesList.get(getItemCount() - getAdapterPosition() - 1));
                else
                    clientServiceClickListener.onJobClicked(jobList.get(getItemCount() - getAdapterPosition() - 1));
            });
        }
    }
}