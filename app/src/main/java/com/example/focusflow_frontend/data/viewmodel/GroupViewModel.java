package com.example.focusflow_frontend.data.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.User;

import java.util.ArrayList;
import java.util.List;
public class GroupViewModel extends ViewModel {

    //ViewModel cua groupList ne
    private final MutableLiveData<List<Group>> groupList = new MutableLiveData<>();
    private final MutableLiveData<List<Group>> filteredGroups = new MutableLiveData<>();

    public LiveData<List<Group>> getFilteredGroups() {
        return filteredGroups;
    }
    public LiveData<List<Group>> getAllGroups() {
        return groupList;
    }
    private void loadGroups() {
        List<Group> list = new ArrayList<>();
        list.add(new Group("A", "A", "A"));
        list.add(new Group("B", "B", "B"));
        groupList.setValue(list);
    }

    public LiveData<List<Group>> getGroupList() {
        loadGroups();
        return groupList;
    }

    public void addGroup(Group group) {
        List<Group> current = groupList.getValue();
        if (current == null) current = new ArrayList<>();
            current.add(group);
        groupList.setValue(current);
    }

    public void searchGroup(String keyword, List<Group> groups) {
        if (keyword == null || keyword.trim().isEmpty()) {
            filteredGroups.setValue(new ArrayList<>(groups));
            return;
        }

        List<Group> result = new ArrayList<>();
        for (Group group : groups) {
            if (group.getGroup_name().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(group);
            }
        }
        filteredGroups.setValue(result);
    }

    //ViewModel cua AddGroup

    private final MutableLiveData<List<User>> selectedUsers = new MutableLiveData<>(new ArrayList<>()); //Cac user duoc chon
    private final MutableLiveData<List<User>> userSuggestions = new MutableLiveData<>(new ArrayList<>()); //Tat ca user goi y
    public LiveData<List<User>> getSelectedUsers() {
        return selectedUsers;
    }
    // Tim email:
    public void searchUsers(String keyword, List<User> allUsers) {
        List<User> result = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getEmail().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(user);
            }
        }
        userSuggestions.setValue(result);
    }
    public void addUser(User user) {
        List<User> selected = new ArrayList<>(selectedUsers.getValue());
        if (!selected.contains(user)) {
            selected.add(user);
            selectedUsers.setValue(selected);
        }
    }
    public void removeUser(User user) {
        List<User> selected = new ArrayList<>(selectedUsers.getValue());
        selected.remove(user);
        selectedUsers.setValue(selected);
    }
    public LiveData<List<User>> getSuggestions() {
        return userSuggestions;
    }
}
