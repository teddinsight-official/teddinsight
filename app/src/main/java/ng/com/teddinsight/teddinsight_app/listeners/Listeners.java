package ng.com.teddinsight.teddinsight_app.listeners;

import ng.com.teddinsight.teddinsight_app.models.DesignerDesigns;
import ng.com.teddinsight.teddinsight_app.models.Tasks;

public class Listeners {

    public interface DesignTemplateClicked {
        void onTemplateClicked(boolean isDesigner, DesignerDesigns designerDesigns);
    }

    public interface TaskItemClicked{
        void onTaskItemClicked(boolean isDesigner, Tasks tasks);
    }

}
