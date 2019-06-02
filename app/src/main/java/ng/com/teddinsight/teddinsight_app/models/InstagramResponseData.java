package ng.com.teddinsight.teddinsight_app.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class InstagramResponseData {
    @SerializedName("id")
    public String instagramId;
    @SerializedName("username")
    public String instagramUsername;
    @SerializedName("profile_picture")
    public String instagramProfilePicture;
    @SerializedName("full_name")
    public String instagramFullName;
    @SerializedName("bio")
    public String instagramBio;
    @SerializedName("is_business")
    public boolean isInstagramBusinessAccount;

    @SerializedName("counts")
    public InstagramAccountCount instagramAccountCount;

    public class InstagramAccountCount {
        @SerializedName("media")
        public long mediaCount;
        @SerializedName("follows")
        public long followingCount;
        @SerializedName("followed_by")
        public long followersCount;

        @NonNull
        @Override
        public String toString() {
            return "Media Count: " + mediaCount + " followerCount: " + followersCount + " followingCount: " + followingCount;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return instagramId.concat(instagramBio).concat(instagramFullName).concat(instagramUsername).concat(instagramProfilePicture);
    }
}
