package ng.com.teddinsight.teddinsight_app.models;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class ScheduledPost {
    private String id;
    private String accountType;
    private String accountId;
    private String accountUsername;
    private String postText;
    private String postTitle;
    private String postImage;
    private boolean hasBeenReviewedByAdmin;
    private boolean wasSuccesful;
    private long postTimestamp;
    private String twitterUserToken;
    private long twitterUserId;
    private String twitterUserName;
    private String twitterSecreteToken;
    private String instagramId;
    private String status;
    public static final String SCHEDULE_PATH = "scheduledPosts";
    public static final String APPROVED_SCHEDULE_PATH = "approvedScheduledPosts";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_DISAPPROVED = "DISAPPROVED";


    public ScheduledPost() {
    }

    public ScheduledPost(
            String accountType,
            String accountId,
            String accountUsername,
            String postText,
            String postImage,
            boolean hasBeenReviewedByAdmin,
            boolean wasSuccesful,
            long postTimestamp,
            String twitterUserToken,
            long twitterUserId,
            String twitterUserName,
            String twitterSecreteToken) {
        this.accountType = accountType;
        this.accountId = accountId;
        this.accountUsername = accountUsername;
        this.postText = postText;
        this.postImage = postImage;
        this.hasBeenReviewedByAdmin = hasBeenReviewedByAdmin;
        this.wasSuccesful = wasSuccesful;
        this.postTimestamp = postTimestamp;
        this.twitterUserToken = twitterUserToken;
        this.twitterUserId = twitterUserId;
        this.twitterUserName = twitterUserName;
        this.twitterSecreteToken = twitterSecreteToken;
    }

    public ScheduledPost(
            String accountType,
            String accountId,
            String accountUsername,
            String postText,
            String postImage,
            boolean hasBeenReviewedByAdmin,
            boolean wasSuccesful,
            long postTimestamp,
            String instagramId) {
        this.accountType = accountType;
        this.accountId = accountId;
        this.accountUsername = accountUsername;
        this.postText = postText;
        this.postImage = postImage;
        this.hasBeenReviewedByAdmin = hasBeenReviewedByAdmin;
        this.wasSuccesful = wasSuccesful;
        this.postTimestamp = postTimestamp;
        this.instagramId = instagramId;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountUsername() {
        return accountUsername;
    }

    public String getPostText() {
        return postText;
    }

    public String getPostImage() {
        return postImage;
    }

    public boolean isHasBeenReviewedByAdmin() {
        return hasBeenReviewedByAdmin;
    }

    public boolean isWasSuccesful() {
        return wasSuccesful;
    }

    public long getPostTimestamp() {
        return postTimestamp;
    }

    public String getTwitterUserToken() {
        return twitterUserToken;
    }

    public long getTwitterUserId() {
        return twitterUserId;
    }

    public String getTwitterUserName() {
        return twitterUserName;
    }

    public String getTwitterSecreteToken() {
        return twitterSecreteToken;
    }

    public void setPostTimestamp(long postTimestamp) {
        this.postTimestamp = postTimestamp;
    }

    public void setHasBeenReviewedByAdmin(boolean hasBeenReviewedByAdmin) {
        this.hasBeenReviewedByAdmin = hasBeenReviewedByAdmin;
    }

    public void setWasSuccesful(boolean wasSuccesful) {
        this.wasSuccesful = wasSuccesful;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstagramId() {
        return instagramId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }
}
