package ng.com.teddinsight.teddinsight_app.utils;

import ng.com.teddinsight.teddinsight_app.models.InstagramResponseData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface InstagramUserApi {

    String BASE_URL = "https://api.instagram.com";

    @GET("/v1/users/self/")
    Call<InstagramResponseData> getInstagramDataWithAccessToken(@Query("access_token") String accessToken);
}
