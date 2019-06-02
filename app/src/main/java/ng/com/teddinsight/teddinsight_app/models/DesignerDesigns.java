package ng.com.teddinsight.teddinsight_app.models;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import org.parceler.Parcel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Parcel(Parcel.Serialization.BEAN)
public class DesignerDesigns {

    public String id;
    public String templateName;
    public boolean isUpdated;
    public String imageUrl;
    public long dateUploaded;
    public long invertedDateUploaded;
    public boolean canEdit = false;
    public String status;
    private boolean visibleToAdmin;
    public static final String DESIGNS = "designer/designs";
    public static final String APPROVED_DESIGNS = "approvedDesigns";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_DISAPPROVED = "disapproved";
    public static final String STATUS_PENDING = "pending";

    public DesignerDesigns() {
    }

    public DesignerDesigns(String id, String templateName, boolean isUpdated, String imageUrl) {
        this.id = id;
        this.templateName = templateName;
        this.isUpdated = isUpdated;
        this.imageUrl = imageUrl;
    }

    public boolean isVisibleToAdmin() {
        return visibleToAdmin;
    }

    public void setVisibleToAdmin(boolean visibleToAdmin) {
        this.visibleToAdmin = visibleToAdmin;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean updated) {
        this.isUpdated = updated;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Exclude
    public Map<String, Object> toMap() {

        Map<String, Object> o = new HashMap<>();
        o.put("templateName", this.templateName);
        o.put("isUpdated", this.isUpdated);
        o.put("imageUrl", this.imageUrl);
        o.put("id", this.id);
        o.put("dateUploaded", ServerValue.TIMESTAMP);
        o.put("invertedDateUploaded", -1 * new Date().getTime());
        o.put("visibleToAdmin", true);
        return o;
    }

}
