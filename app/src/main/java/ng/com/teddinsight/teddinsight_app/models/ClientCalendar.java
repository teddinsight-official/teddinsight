package ng.com.teddinsight.teddinsight_app.models;

import com.google.firebase.database.Exclude;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class ClientCalendar {
    private String key;
    private String name;
    private int taskCount;
    private long dateCreated = System.currentTimeMillis();
    private boolean needsPublishing;
    private boolean beginPublishing;

    public static String getBaseTableName() {
        return "clientCalendarBaseTable";
    }
    public static String getTableName() {
        return "clientCalendarTable";
    }

    public ClientCalendar() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isNeedsPublishing() {
        return needsPublishing;
    }

    public void setNeedsPublishing(boolean needsPublishing) {
        this.needsPublishing = needsPublishing;
    }

    public boolean isBeginPublishing() {
        return beginPublishing;
    }

    public void setBeginPublishing(boolean beginPublishing) {
        this.beginPublishing = beginPublishing;
    }
}
