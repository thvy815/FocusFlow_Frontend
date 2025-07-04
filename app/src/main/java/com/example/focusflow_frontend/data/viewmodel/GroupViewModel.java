package com.example.focusflow_frontend.data.viewmodel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.focusflow_frontend.data.api.GroupController;
import com.example.focusflow_frontend.data.model.CtGroupUser;
import com.example.focusflow_frontend.data.model.CtIdRequest;
import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.GroupWithUsersRequest;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.utils.ApiClient;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Group>> groupList = new MutableLiveData<>();

    // LiveData chứa danh sách nhóm sau khi lọc (search)
    private final MutableLiveData<List<Group>> filteredGroups = new MutableLiveData<>();
    private GroupController groupController;
    private final MutableLiveData<Boolean> groupCreated = new MutableLiveData<>();

    public GroupViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo TaskController thông qua ApiClient
        Context context = getApplication().getApplicationContext();
        groupController = ApiClient.getRetrofit(context).create(GroupController.class);
    }

    // Lấy danh sách nhóm
    public LiveData<List<Group>> getGroupList() {
        return groupList;
    }

    // Lấy danh sách nhóm đã lọc (để hiển thị khi tìm kiếm)
    public LiveData<List<Group>> getFilteredGroups() {
        return filteredGroups;
    }

    public LiveData<Boolean> getGroupCreated() {
        return groupCreated;
    }

    public void loadGroupsOfUser(int userId) {
        groupController.getGroupsOfUser(userId).enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    groupList.setValue(response.body());
                    filteredGroups.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                Log.e("Group", "Error: " + t.getMessage());
            }
        });
    }

    public void addGroup(Group group) {
        groupController.createGroup(group).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(Call<Group> call, Response<Group> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Group> current = groupList.getValue();
                    if (current == null) current = new ArrayList<>();
                    current.add(response.body());
                    groupList.setValue(current);
                    filteredGroups.setValue(current);
                }
            }

            @Override
            public void onFailure(Call<Group> call, Throwable t) {
                Log.e("Group", "Error: " + t.getMessage());
            }
        });
    }

    public void deleteGroup(int id) {
        groupController.deleteGroup(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    List<Group> current = groupList.getValue();
                    if (current != null) {
                        List<Group> updated = new ArrayList<>();
                        for (Group g : current) {
                            if (g.getId() != id) updated.add(g);
                        }
                        groupList.setValue(updated);
                        filteredGroups.setValue(updated);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Group", "Error: " + t.getMessage());
            }
        });
    }

    public void removeUserFromGroup(int groupId, int userId, Runnable onSuccess, Runnable onFailure) {
        groupController.removeUserFromGroup(groupId, userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Sau khi xóa thành công, gọi lại fetch để cập nhật UI
                    fetchUsersInGroup(groupId);
                    if (onSuccess != null) onSuccess.run();
                    Log.d("GroupViewModel", "User removed from group");
                } else {
                    if (onFailure != null) onFailure.run();
                    Log.e("GroupViewModel", "Failed to remove user: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (onFailure != null) onFailure.run();
                Log.e("GroupViewModel", "Error removing user", t);
            }
        });
    }


    private MutableLiveData<Group> createdGroup = new MutableLiveData<>();
    public LiveData<Group> getCreatedGroup() { return createdGroup; }
    public void createGroupWithUsers(String groupName, List<Integer> userIds, Integer leaderId) {
        GroupWithUsersRequest request = new GroupWithUsersRequest(groupName, leaderId, userIds);

        groupController.createGroupWithUsers(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        Gson gson = new Gson();
                        Group group = gson.fromJson(json, Group.class);

                        createdGroup.postValue(group);
                        groupCreated.postValue(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        groupCreated.postValue(false);
                    }
                } else {
                    groupCreated.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                groupCreated.postValue(false);
            }
        });
    }

    // Tìm kiếm nhóm theo từ khóa (tên nhóm)
    public void searchGroup(String keyword, List<Group> groups) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // Nếu từ khóa trống thì trả về toàn bộ danh sách
            filteredGroups.setValue(new ArrayList<>(groups));
            return;
        }

        // Lọc nhóm có tên chứa từ khóa (không phân biệt hoa thường)
        List<Group> result = new ArrayList<>();
        for (Group group : groups) {
            if (group.getGroupName().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(group);
            }
        }
        filteredGroups.setValue(result);
    }

    // ---------------- AddGroup Section ----------------

    // Danh sách người dùng được chọn (để thêm vào nhóm)
    private final MutableLiveData<List<User>> selectedUsers = new MutableLiveData<>(new ArrayList<>());

    // Danh sách người dùng gợi ý dựa trên tìm kiếm
    private final MutableLiveData<List<User>> userSuggestions = new MutableLiveData<>(new ArrayList<>());

    // Lấy danh sách người dùng được chọn
    public LiveData<List<User>> getSelectedUsers() {
        return selectedUsers;
    }

    // Tìm kiếm người dùng theo email
    public void searchUsers(String keyword, List<User> allUsers) {
        List<User> result = new ArrayList<>();

        // Nếu ô tìm kiếm rỗng -> không hiển thị gì hết
        if (keyword == null || keyword.trim().isEmpty()) {
            userSuggestions.setValue(result); // danh sách rỗng
            return;
        }

        // Nếu có từ khóa, lọc như bình thường
        for (User user : allUsers) {
            if (user.getEmail().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(user);
            }
        }
        userSuggestions.setValue(result);
    }

    // Thêm người dùng vào danh sách đã chọn
    public void addUser(User user) {
        List<User> selected = new ArrayList<>(selectedUsers.getValue());
        if (!selected.contains(user)) {
            selected.add(user);
            selectedUsers.setValue(selected);
        }
    }

    // Xóa người dùng khỏi danh sách đã chọn
    public void removeUser(User user) {
        List<User> selected = new ArrayList<>(selectedUsers.getValue());
        selected.remove(user);
        selectedUsers.setValue(selected);
    }

    // Lấy danh sách người dùng gợi ý
    public LiveData<List<User>> getSuggestions() {
        return userSuggestions;
    }

    // ---------------- GroupDetail Section ----------------
    private final MutableLiveData<List<Task>> filteredTask = new MutableLiveData<>();

    public LiveData<List<Task>> filterTask() {
        return filteredTask;
    }

    // Tìm kiếm nhóm theo từ khóa (tên nhóm)
    public void searchTask(String keyword, List<Task> tasks) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // Nếu từ khóa trống thì trả về toàn bộ danh sách
            filteredTask.setValue(new ArrayList<>(tasks));
            return;
        }
        // Lọc nhóm có tên chứa từ khóa (không phân biệt hoa thường)
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(task);
            }
        }
        filteredTask.setValue(result);
    }

    public void clearFilteredTasks(List<Task> tasks) {
        filteredTask.setValue(tasks); // Reset về tất cả
    }

    // Focus Search (Menu -> Detail)
    private final MutableLiveData<Boolean> requestSearchFocus = new MutableLiveData<>();

    public LiveData<Boolean> getRequestSearchFocus() {
        return requestSearchFocus;
    }

    public void requestFocusOnSearch(boolean f) {
        requestSearchFocus.setValue(f);
    }

    //MENU: OUT GROUP
    private final MutableLiveData<Integer> groupRemoved = new MutableLiveData<>();

    public void setGroupRemoved(int groupId) {
        groupRemoved.setValue(groupId);
    }

    public LiveData<Integer> getGroupRemoved() {
        return groupRemoved;
    }

    // CT Group - User
    public LiveData<CtGroupUser> getCtByIdLiveData(int ctId) {
        MutableLiveData<CtGroupUser> liveData = new MutableLiveData<>();
        groupController.getCtById(ctId).enqueue(new Callback<CtGroupUser>() {
            @Override
            public void onResponse(Call<CtGroupUser> call, Response<CtGroupUser> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<CtGroupUser> call, Throwable t) {
                Log.e("CtCheck", "API failed for ctId: " + ctId, t);
                liveData.setValue(null);
            }
        });
        return liveData;
    }

    private final MutableLiveData<List<User>> usersInGroup = new MutableLiveData<>();
    public LiveData<List<User>> getUsersInGroup() { return usersInGroup; }
    public void fetchUsersInGroup(int groupId) {
        groupController.getUsersInGroup(groupId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    usersInGroup.setValue(response.body());
                } else {
                    usersInGroup.setValue(new ArrayList<>()); // Trả về rỗng nếu lỗi
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                usersInGroup.setValue(new ArrayList<>()); // Trả về rỗng nếu gọi thất bại
                Log.e("GroupViewModel", "Lỗi khi lấy users trong group: " + t.getMessage());
            }
        });
    }

    // LiveData để quan sát ctGroupIds trả về
    private MutableLiveData<List<Integer>> ctIdsLiveData = new MutableLiveData<>();
    public LiveData<List<Integer>> getCtIdLiveData() {
        return ctIdsLiveData;
    }
    // Gọi API để lấy danh sách ct_id của các member trong group
    public void getCtIdsForGroupMembers(List<Integer> userIds, int groupId, Consumer<List<Integer>> callback) {
        CtIdRequest request = new CtIdRequest(userIds, groupId);
        groupController.getCtIdsForUsersInGroup(request).enqueue(new Callback<List<Integer>>() {
            @Override
            public void onResponse(Call<List<Integer>> call, Response<List<Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ctIdsLiveData.postValue(response.body());
                    callback.accept(response.body()); // truyền về callback
                } else {
                    ctIdsLiveData.postValue(null);
                    callback.accept(null);
                }
            }

            @Override
            public void onFailure(Call<List<Integer>> call, Throwable t) {
                ctIdsLiveData.postValue(null);
                callback.accept(null);
            }
        });
    }

    private final Map<Integer, MutableLiveData<List<User>>> assignedUsersMap = new HashMap<>();
    public LiveData<List<User>> getAssignedUsersOfTask(int taskId) {
        // Nếu chưa có LiveData thì tạo mới và gán vào map
        if (!assignedUsersMap.containsKey(taskId)) {
            assignedUsersMap.put(taskId, new MutableLiveData<>());
        }

        final MutableLiveData<List<User>> liveData = assignedUsersMap.get(taskId);

        // Nếu chưa có dữ liệu thì mới gọi API
        if (liveData.getValue() == null) {
            fetchAssignedUsers(taskId, liveData);
        }

        return liveData;
    }

    public void refreshAssignedUsersOfTask(int taskId) {
        if (!assignedUsersMap.containsKey(taskId)) {
            assignedUsersMap.put(taskId, new MutableLiveData<>());
        }

        final MutableLiveData<List<User>> liveData = assignedUsersMap.get(taskId);
        fetchAssignedUsers(taskId, liveData);
    }

    public void clearAssignedUsersCache() {
        assignedUsersMap.clear();  // 🧹 clear cache khi mở nhóm mới
    }

    public LiveData<List<User>> getAssignedUsersLiveData(int taskId) {
        return assignedUsersMap.getOrDefault(taskId, new MutableLiveData<>());
    }

    private void fetchAssignedUsers(int taskId, MutableLiveData<List<User>> liveData) {
        groupController.getUsersAssignedToTask(taskId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                } else {
                    liveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e("GroupViewModel", "Lỗi khi lấy assignees task " + taskId + ": " + t.getMessage());
                liveData.postValue(new ArrayList<>());
            }
        });
    }

    // LiveData để theo dõi kết quả thêm thành viên
    private final MutableLiveData<Boolean> addMembersResult = new MutableLiveData<>();
    public LiveData<Boolean> getAddMembersResult() { return addMembersResult; }
    // Hàm gọi API thêm thành viên vào nhóm
    public void addMembersToGroup(int groupId, List<Integer> userIds) {
        groupController.addMembersToGroup(groupId, userIds).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                addMembersResult.setValue(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("GroupViewModel", "Add members failed: " + t.getMessage());
                addMembersResult.setValue(false);
            }
        });
    }
}
