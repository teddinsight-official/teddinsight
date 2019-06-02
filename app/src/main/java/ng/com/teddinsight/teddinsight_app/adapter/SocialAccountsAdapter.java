package ng.com.teddinsight.teddinsight_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsight_app.R;
import ng.com.teddinsight.teddinsight_app.listeners.Listeners;
import ng.com.teddinsight.teddinsight_app.models.SocialAccounts;

public class SocialAccountsAdapter extends RecyclerView.Adapter<SocialAccountsAdapter.SocialAccountsViewHolder> {

    List<SocialAccounts> socialAccounts;
    Listeners.SocialAccountsListener socialAccountsListener;

    public SocialAccountsAdapter(Listeners.SocialAccountsListener socialAccountsListener) {
        this.socialAccounts = new ArrayList<>();
        this.socialAccountsListener = socialAccountsListener;
    }

    @NonNull
    @Override
    public SocialAccountsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.social_accounts_list_item, parent, false);
        return new SocialAccountsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SocialAccountsViewHolder holder, int position) {
        holder.bind(socialAccounts.get(position));
    }

    public void swapData(List<SocialAccounts> socialAccountsList) {
        this.socialAccounts = socialAccountsList;
        this.notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return socialAccounts.size();
    }

    class SocialAccountsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.username)
        TextView usernameTextView;
        @BindView(R.id.account_type)
        ImageView accountTypeImageView;

        public SocialAccountsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bind(SocialAccounts socialAccount) {
            if (socialAccount.getAccountType().equals(SocialAccounts.ACCOUNT_TYPE_INSTAGRAM))
                accountTypeImageView.setImageResource(R.drawable.ic_instagram);
            else
                accountTypeImageView.setImageResource(R.drawable.ic_action_twitter);
            usernameTextView.setText(socialAccount.getAccountUsername());
            itemView.setOnClickListener(v -> socialAccountsListener.onSocialAccountItemClicked(socialAccount));
        }
    }
}
