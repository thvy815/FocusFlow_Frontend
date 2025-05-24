package com.example.focusflow_frontend.data.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class GroupViewModel extends ViewModel {

    // LiveData chứa danh sách tất cả nhóm
    private final MutableLiveData<List<Group>> groupList = new MutableLiveData<>();

    // LiveData chứa danh sách nhóm sau khi lọc (search)
    private final MutableLiveData<List<Group>> filteredGroups = new MutableLiveData<>();

    // Lấy danh sách nhóm đã lọc (để hiển thị khi tìm kiếm)
    public LiveData<List<Group>> getFilteredGroups() {
        return filteredGroups;
    }

    // Lấy danh sách tất cả nhóm
    public LiveData<List<Group>> getAllGroups() {
        return groupList;
    }

    // Tạo dữ liệu nhóm mẫu ban đầu
    private void loadGroups() {
        List<Group> list = new ArrayList<>();
        list.add(new Group("A", "A", "A"));
        list.add(new Group("B", "B", "B"));
        groupList.setValue(list);
    }

    // Lấy danh sách nhóm và đồng thời tải dữ liệu mẫu
    public LiveData<List<Group>> getGroupList() {
        loadGroups();
        return groupList;
    }

    // Thêm một nhóm mới vào danh sách
    public void addGroup(Group group) {
        List<Group> current = groupList.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(group);
        groupList.setValue(current);
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
            if (group.getGroup_name().toLowerCase().contains(keyword.toLowerCase())) {
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

    // LiveData chứa danh sách tất cả nhóm
    private final MutableLiveData<List<Task>> taskList = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> filteredTask = new MutableLiveData<>();

    // Lấy danh sách tất cả task
    public LiveData<List<Task>> getAllTask() {
        return taskList;
    }

    public LiveData<List<Task>> filterTask() {
        return filteredTask;
    }


    // Tạo dữ liệu nhóm mẫu ban đầu
    private void loadTask() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(1, 101, 0, "Viết báo cáo tuần", "Hoàn thành báo cáo công việc tuần này", "2025-05-25"));
        tasks.add(new Task(2, 101, 0, "Chuẩn bị thuyết trình", "Chuẩn bị nội dung họp nhóm", "2025-05-28"));
        tasks.add(new Task(3, 102, 1, "Fix bug", "Sửa lỗi màn login", "2025-05-27"));
        tasks.add(new Task(4, 103, 1, "Code chat", "Tính năng chat nhóm", "2025-06-01"));
        taskList.setValue(tasks);
    }

    // Lấy danh sách nhóm và đồng thời tải dữ liệu mẫu
    public LiveData<List<Task>> getTaskList() {
        loadTask();
        //lay cac task co ct_id trung voi id duong truyen toi tu fragment
        return taskList;
    }

    // Thêm một nhóm mới vào danh sách
    public void addTask(Task task) {
        List<Task> current = taskList.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(task);
        taskList.setValue(current);
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
}
