package ng.com.teddinsight.teddinsight_app.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class Job implements Parcelable {


    private String jobId;
    private int status;
    private long postedOn;
    private long offerExpiresOn;
    private long dueOn;
    private String jobType;
    private String jobDescription;
    private int progress;
    private int pendingIntentId;


    public Job() {
    }

    public Job(String jobId, String jobType, String jobDescription) {
        this.jobId = jobId;
        this.jobType = jobType;
        this.jobDescription = jobDescription;
    }

    protected Job(Parcel in) {
        jobId = in.readString();
        status = in.readInt();
        postedOn = in.readLong();
        offerExpiresOn = in.readLong();
        dueOn = in.readLong();
        jobType = in.readString();
        jobDescription = in.readString();
        progress = in.readInt();
    }

    public static final Creator<Job> CREATOR = new Creator<Job>() {
        @Override
        public Job createFromParcel(Parcel in) {
            return new Job(in);
        }

        @Override
        public Job[] newArray(int size) {
            return new Job[size];
        }
    };

    public static String getTableName() {
        return "jobs";
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("jobId", this.jobId);
        map.put("status", 0);
        map.put("postedOn", ServerValue.TIMESTAMP);
        map.put("offerExpiresOn", ServerValue.TIMESTAMP);
        map.put("dueOn", ServerValue.TIMESTAMP);
        map.put("jobType", this.jobType);
        map.put("jobDescription", this.jobDescription);
        map.put("progress", 0);
        return map;
    }

    public Map<String, Object> updateMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("jobId", this.jobId);
        map.put("status", this.status);
        map.put("postedOn", this.postedOn);
        map.put("offerExpiresOn", this.offerExpiresOn);
        map.put("dueOn", this.dueOn);
        map.put("jobType", this.jobType);
        map.put("jobDescription", this.jobDescription);
        map.put("progress", this.progress);
        return map;
    }


    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getPostedOn() {
        return postedOn;
    }

    public void setPostedOn(long postedOn) {
        this.postedOn = postedOn;
    }

    public long getOfferExpiresOn() {
        return offerExpiresOn;
    }

    public void setOfferExpiresOn(long offerExpiresOn) {
        this.offerExpiresOn = offerExpiresOn;
    }

    public long getDueOn() {
        return dueOn;
    }

    public void setDueOn(long dueOn) {
        this.dueOn = dueOn;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public int getPendingIntentId() {
        return pendingIntentId;
    }

    public void setPendingIntentId(int pendingIntentId) {
        this.pendingIntentId = pendingIntentId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(jobId);
        dest.writeInt(status);
        dest.writeLong(postedOn);
        dest.writeLong(offerExpiresOn);
        dest.writeLong(dueOn);
        dest.writeString(jobType);
        dest.writeString(jobDescription);
        dest.writeInt(progress);
    }
}
