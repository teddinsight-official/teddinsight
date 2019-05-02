package ng.com.teddinsight.teddinsight_app.listeners;

import ng.com.teddinsight.teddinsight_app.models.DesignerDesigns;
import ng.com.teddinsight.teddinsight_app.models.Tasks;
import ng.com.teddinsight.teddinsight_app.models.Tickets;
import ng.com.teddinsight.teddinsightchat.models.User;

public class Listeners {

    public interface DesignTemplateClicked {
        void onTemplateClicked(boolean isDesigner, DesignerDesigns designerDesigns);
    }

    public interface TaskItemClicked {
        void onTaskItemClicked(boolean isDesigner, Tasks tasks);
    }

    public interface ShowEditImageActivity {
        void showEditImageActivity(DesignerDesigns designerDesigns);
    }

    public interface TicketItemClicked {
        void onTicketItemClicked(Tickets tickets);
    }

}
