package com.sslablk.fixmate;

import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @GET("/api/v1/user/profile/{user_id}")
    Call<GetUserProfileResponse> getUserProfile(
            @Header("x-api-key") String apiKey,
            @Path("user_id") String userId
    );

    @Multipart
    @POST("/api/v1/user/update-profile")
    Call<UpdateUserProfileResponse> updateUserProfile(
            @Header("x-api-key") String apiKey,
            @Part("user_id") RequestBody userId,
            @Part("name") RequestBody name,
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST("/api/v1/post/create")
    Call<CreatePostResponse> createPost(
            @Header("x-api-key") String apiKey,
            @Part("user_id") RequestBody userId,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part List<MultipartBody.Part> images
    );

    @GET("/api/v1/post/get-posts/{user_id}")
    Call<PostResponse> getPostsByUserId(
            @Header("x-api-key") String apiKey,
            @Path("user_id") String userId
    );

    @DELETE("/api/v1/post/delete-post/{post_id}")
    Call<DeletePostResponse> deletePost(
            @Header("x-api-key") String apiKey,
            @Path("post_id") String postId
    );

    @PUT("/api/v1/post/update/{post_id}")
    Call<UpdatePostResponse> updatePost(
            @Header("x-api-key") String apiKey,
            @Path("post_id") String postId,
            @Body Map<String, String> body
    );

    @GET("/api/v1/post/")
    Call<AllPostsResponse> getAllPosts(
            @Header("x-api-key") String apiKey
    );

    @GET("/api/v1/comment/{post_id}")
    Call<CommentResponse> getCommentsByPostId(
            @Header("x-api-key") String apiKey,
            @Path("post_id") String postId
    );

    @POST("/api/v1/comment/{post_id}")
    Call<AddCommentResponse> addCommentToPost(
            @Header("x-api-key") String apiKey,
            @Path("post_id") String postId,
            @Body Map<String, String> body
    );

    @GET("/api/v1/notify/{user_id}")
    Call<NotificationResponse> getNotificationsByUserId(
            @Header("x-api-key") String apiKey,
            @Path("user_id") String userId
    );

}