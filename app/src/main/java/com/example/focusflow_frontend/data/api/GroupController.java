package com.example.focusflow_frontend.data.api;

import com.example.focusflow_frontend.data.model.CtGroupUser;
import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.GroupWithUsersRequest;
import com.example.focusflow_frontend.data.model.User;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GroupController {

    @GET("api/group/user/{userId}")
    Call<List<Group>> getGroupsOfUser(@Path("userId") int userId);

    @POST("/api/group")
    Call<Group> createGroup(@Body Group group);

    @POST("/api/group/create-with-users")
    Call<ResponseBody> createGroupWithUsers(@Body GroupWithUsersRequest request);

    @GET("/api/group/{id}")
    Call<Group> getGroupById(@Path("id") int id);

    @DELETE("/api/group/{id}")
    Call<Void> deleteGroup(@Path("id") int id);

    @POST("/api/group/{groupId}/members")
    Call<Void> addMembersToGroup(@Path("groupId") int groupId, @Body List<Integer> userIds);

    @GET("api/group-user/{id}")
    Call<CtGroupUser> getCtById(@Path("id") int id);

    @GET("/api/group-user/group/{groupId}/users")
    Call<List<User>> getUsersInGroup(@Path("groupId") int groupId);

    @GET("api/group-user/getCtId")
    Call<Integer> getCtIdByUserAndGroup(@Query("userId") int userId, @Query("groupId") int groupId);

    @DELETE("/api/group-user/{groupId}/members/{userId}")
    Call<Void> removeUserFromGroup(@Path("groupId") int groupId, @Path("userId") int userId);
}
