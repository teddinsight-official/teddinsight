package ng.com.teddinsight.teddinsight_app.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DesignerDesigns implements Parcelable {

    public String id;
    public String templateName;
    public boolean isUpdated;
    public String imageUrl;
    public long dateUploaded;
    public long invertedDateUploaded;
    public boolean canEdit = false;

    public DesignerDesigns() {
    }

    public DesignerDesigns(String id, String templateName, boolean isUpdated, String imageUrl) {
        this.id = id;
        this.templateName = templateName;
        this.isUpdated = isUpdated;
        this.imageUrl = imageUrl;
    }

    protected DesignerDesigns(Parcel in) {
        id = in.readString();
        templateName = in.readString();
        isUpdated = in.readByte() != 0;
        imageUrl = in.readString();
        dateUploaded = in.readLong();
        invertedDateUploaded = in.readLong();
        canEdit = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(templateName);
        dest.writeByte((byte) (isUpdated ? 1 : 0));
        dest.writeString(imageUrl);
        dest.writeLong(dateUploaded);
        dest.writeLong(invertedDateUploaded);
        dest.writeByte((byte) (canEdit ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DesignerDesigns> CREATOR = new Creator<DesignerDesigns>() {
        @Override
        public DesignerDesigns createFromParcel(Parcel in) {
            return new DesignerDesigns(in);
        }

        @Override
        public DesignerDesigns[] newArray(int size) {
            return new DesignerDesigns[size];
        }
    };

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public String getId() {
        return id;
    }

    public void setId(String  id) {
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
        return o;
    }

}
