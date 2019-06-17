package ng.com.teddinsight.teddinsight_app.listeners;

import androidx.fragment.app.Fragment;

import ng.com.teddinsight.teddinsight_app.models.DesignerDesigns;
import ng.com.teddinsight.teddinsight_app.models.Job;
import ng.com.teddinsight.teddinsight_app.models.Receipts;
import ng.com.teddinsight.teddinsight_app.models.SocialAccounts;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsight_app.models.Tickets;
import ng.com.teddinsight.teddinsightchat.models.User;

public interface Listeners {

    interface DesignTemplateClicked {
        void onTemplateClicked(boolean isDesigner, DesignerDesigns designerDesigns);
    }

    interface TaskItemClicked {
        void onTaskItemClicked(boolean isDesigner, Tasks tasks);
    }

    interface ShowEditImageActivity {
        void showEditImageActivity(DesignerDesigns designerDesigns);
    }

    interface TicketItemClicked {
        void onTicketItemClicked(Tickets tickets);
    }

    interface HrMainContentListener {
        void onHrMainContentReplacementRequest(Fragment fragment, boolean shouldAddBackStack);
    }

    interface UserItemClickListener {
        void onUserItemClicked(User user);
    }

    interface SocialAccountsListener {
        void onSocialAccountItemClicked(SocialAccounts socialAccounts);

        void onTwitterButtonClicked();
    }
    interface ClientServiceClickListener {
        void onClientServiceClicked(Receipts service);

        void onJobClicked(Job job);
    }
}
