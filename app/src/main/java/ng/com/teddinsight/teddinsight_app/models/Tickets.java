package ng.com.teddinsight.teddinsight_app.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class Tickets implements Parcelable {

    public String id;
    public String title;
    public String description;
    public String openedBy;
    public long openedOn;
    public long closedOn;
    public String reviewedBy;
    public boolean reviewed;
    public String category;
    public String status;
    public String senderName;

    public Tickets() {
    }

    public Tickets(String id, String title, String description, String openedBy, String category, String businessName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.openedBy = openedBy;
        this.category = category;
        this.reviewed = false;
        this.status = "open";
        this.senderName = senderName;
    }


    protected Tickets(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        openedBy = in.readString();
        openedOn = in.readLong();
        closedOn = in.readLong();
        reviewedBy = in.readString();
        reviewed = in.readByte() != 0;
        category = in.readString();
        status = in.readString();
        senderName = in.readString();
    }

    public static final Creator<Tickets> CREATOR = new Creator<Tickets>() {
        @Override
        public Tickets createFromParcel(Parcel in) {
            return new Tickets(in);
        }

        @Override
        public Tickets[] newArray(int size) {
            return new Tickets[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("description", description);
        map.put("openedBy", openedBy);
        map.put("openedOn", ServerValue.TIMESTAMP);
        map.put("closedOn", 100);
        map.put("reviewedBy", "N/A");
        map.put("category", category);
        map.put("reviewed", reviewed);
        map.put("status", status);
        map.put("senderName", senderName);
        return map;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(openedBy);
        dest.writeLong(openedOn);
        dest.writeLong(closedOn);
        dest.writeString(reviewedBy);
        dest.writeByte((byte) (reviewed ? 1 : 0));
        dest.writeString(category);
        dest.writeString(status);
        dest.writeString(senderName);
    }
}
