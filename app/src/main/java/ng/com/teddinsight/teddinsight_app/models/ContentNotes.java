package ng.com.teddinsight.teddinsight_app.models;

import androidx.annotation.Nullable;

import org.parceler.Parcel;


@Parcel(Parcel.Serialization.BEAN)
public class ContentNotes {

    private String key;
    private String title;
    private String note;
    private long createdAt = System.currentTimeMillis();
    private long invertedTimeStamp = -1 * createdAt;
    private long updatedAt = createdAt;
    private ContentNotesStatus contentNotesStatus;
    public String titleTransitionName;
    public String noteTransitionName;
    public String viewer;
    private boolean reviewedByAdmin;

    public static final String TABLE_NAME = "contentNotes";

    public ContentNotes() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        if (title == null)
            title = "";
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        if (note == null)
            note = "";
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getInvertedTimeStamp() {
        return invertedTimeStamp;
    }

    public void setInvertedTimeStamp(long invertedTimeStamp) {
        this.invertedTimeStamp = invertedTimeStamp;
    }

    public ContentNotesStatus getContentNotesStatus() {
        return contentNotesStatus;
    }

    public void setContentNotesStatus(ContentNotesStatus contentNotesStatus) {
        this.contentNotesStatus = contentNotesStatus;
    }

    public boolean isReviewedByAdmin() {
        return reviewedByAdmin;
    }

    public void setReviewedByAdmin(boolean reviewedByAdmin) {
        this.reviewedByAdmin = reviewedByAdmin;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }


    public enum ContentNotesStatus {
        APPROVED("are you sure you want to approve this note"),
        DISAPPROVED("are you sure you want to disapprove this note"),
        PENDING("note pending"),
        DELETED("are you sure you want to delete this note"),
        SAVED("save note now?"),
        ERROR("error message");

        private String message;

        public String getMessage() {
            return this.message;
        }

        ContentNotesStatus(String a) {
            this.message = a;
        }
    }
}
