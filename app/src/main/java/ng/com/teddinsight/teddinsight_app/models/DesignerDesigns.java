package ng.com.teddinsight.teddinsight_app.models;

import java.util.HashMap;
import java.util.Map;

public class DesignerDesigns {

    public int id;
    public String templateName;
    public boolean isUpdated;
    public String imageUrl;
    public String dateUploaded;

    public DesignerDesigns(String templateName, boolean isUpdated, String imageUrl) {
        this.templateName = templateName;
        this.isUpdated = isUpdated;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public Map<String, Object> toMap() {
        Map<String, Object> o = new HashMap<>();
        o.put("templateName", this.templateName);
        o.put("isUpdated", this.isUpdated);
        o.put("imageUrl", this.imageUrl);
        o.put("dateUploaded", this.dateUploaded);
        return o;
    }
}
