package ng.com.teddinsight.teddinsight_app.models;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class SocialAccounts {
    private String accountUsername;
    private String accountType;
    private long dateLogged;
    private long followersCountOnRegistration;
    private long followingCountOnRegistration;
    private String twitterUserToken;
    private long twitterUserId;
    private String twitterUserName;
    private String twitterSecreteToken;
    private String instagramAccessToken;
    private String instagramId;


    public static final String ACCOUNT_TYPE_TWITTER = "Twitter";
    public static final String ACCOUNT_TYPE_INSTAGRAM = "Instagram";

    public SocialAccounts() {
    }

    public String getAccountUsername() {
        return accountUsername;
    }

    public void setAccountUsername(String accountUsername) {
        this.accountUsername = accountUsername;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public long getDateLogged() {
        return dateLogged;
    }

    public void setDateLogged(long dateLogged) {
        this.dateLogged = dateLogged;
    }

    public String getTwitterUserToken() {
        return twitterUserToken;
    }

    public void setTwitterUserToken(String twitterUserToken) {
        this.twitterUserToken = twitterUserToken;
    }

    public long getTwitterUserId() {
        return twitterUserId;
    }

    public void setTwitterUserId(long twitterUserId) {
        this.twitterUserId = twitterUserId;
    }

    public String getTwitterUserName() {
        return twitterUserName;
    }

    public void setTwitterUserName(String twitterUserName) {
        this.twitterUserName = twitterUserName;
    }

    public String getTwitterSecreteToken() {
        return twitterSecreteToken;
    }

    public void setTwitterSecreteToken(String twitterSecreteToken) {
        this.twitterSecreteToken = twitterSecreteToken;
    }

    public long getFollowersCountOnRegistration() {
        return followersCountOnRegistration;
    }

    public void setFollowersCountOnRegistration(long followersCountOnRegistration) {
        this.followersCountOnRegistration = followersCountOnRegistration;
    }

    public long getFollowingCountOnRegistration() {
        return followingCountOnRegistration;
    }

    public void setFollowingCountOnRegistration(long followingCountOnRegistration) {
        this.followingCountOnRegistration = followingCountOnRegistration;
    }

    public String getInstagramAccessToken() {
        return instagramAccessToken;
    }

    public void setInstagramAccessToken(String instagramAccessToken) {
        this.instagramAccessToken = instagramAccessToken;
    }

    public String getInstagramId() {
        return instagramId;
    }

    public void setInstagramId(String instagramId) {
        this.instagramId = instagramId;
    }
}
