package ng.com.teddinsight.teddinsight_app.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class Tasks implements Parcelable {


    public String id;
    public String taskTitle;
    public String taskDescription;
    public String assignedBy;
    public long assignedOn;
    public long dueDate;
    public int status;
    public boolean isDesigner;
    public boolean reminderSet;
    public int pendingIntentId;
    public String assignedTo;
    public String assignedToRole;
    public String assignedToId;
    public String clientCalendarId;
    public String clientId;
    public long dateCompleted;
    public static final int TASK_COMPLETE = 1;
    public static final int TASK_INCOMPLETE = 0;

    public Tasks() {
    }

    public Tasks setDueDateReturnTask(long dueDate){
        this.setDueDate(dueDate);
        return this;
    }

    public static String getTableName() {
        return "tasks";
    }

    public Tasks(String id, String taskTitle, String taskDescription, String assignedBy, long dueDate) {
        this.id = id;
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.assignedBy = assignedBy;
        this.dueDate = dueDate;
    }

    public int getPendingIntentId() {
        return pendingIntentId;
    }

    public void setPendingIntentId(int pendingIntentId) {
        this.pendingIntentId = pendingIntentId;
    }

    protected Tasks(Parcel in) {
        id = in.readString();
        taskTitle = in.readString();
        taskDescription = in.readString();
        assignedBy = in.readString();
        assignedOn = in.readLong();
        dueDate = in.readLong();
        status = in.readInt();
        isDesigner = in.readByte() != 0;
        reminderSet = in.readByte() != 0;
        pendingIntentId = in.readInt();
    }

    public static final Creator<Tasks> CREATOR = new Creator<Tasks>() {
        @Override
        public Tasks createFromParcel(Parcel in) {
            return new Tasks(in);
        }

        @Override
        public Tasks[] newArray(int size) {
            return new Tasks[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public long getAssignedOn() {
        return assignedOn;
    }

    public void setAssignedOn(long assignedOn) {
        this.assignedOn = assignedOn;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> res = new HashMap<>();
        res.put("id", this.id);
        res.put("taskTitle", this.taskTitle);
        res.put("taskDescription", this.taskDescription);
        res.put("assignedBy", this.assignedBy);
        res.put("assignedOn", ServerValue.TIMESTAMP);
        res.put("dueDate", this.dueDate);
        res.put("status", TASK_INCOMPLETE);
        return res;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(taskTitle);
        dest.writeString(taskDescription);
        dest.writeString(assignedBy);
        dest.writeLong(assignedOn);
        dest.writeLong(dueDate);
        dest.writeInt(status);
        dest.writeByte((byte) (isDesigner ? 1 : 0));
        dest.writeByte((byte) (reminderSet ? 1 : 0));
        dest.writeInt(pendingIntentId);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
}
