package com.example.focusflow_frontend.presentation.group;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {

    private GroupViewModel viewModel;
    private AuthViewModel authViewModel;
    private GroupAdapter adapter;
    private List<Group> allGroups = new ArrayList<>(); // Dữ liệu gốc dùng cho search
    private List<Group> displayedGroups = new ArrayList<>(); // Dữ liệu đang hiển thị trong adapter
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);



        // Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        viewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        authViewModel.getCurrentUser();
        authViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;

                setupRecyclerView(view);      // Hiển thị danh sách nhóm
                setupSearchBar(view);         // Tìm kiếm nhóm
                setupAddGroupButton(view);    // Thêm nhóm mới
                setupSwipeToRefresh(view);    // Kéo để reload
            }
        });

        //Remove theo yeu cau
        viewModel.getGroupRemoved().observe(getViewLifecycleOwner(), groupId -> {
            adapter.removeGroupById(groupId);
        });

        return view;
    }

    private void setupSwipeToRefresh(View view) {
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setColorSchemeResources(
                R.color.btn3, R.color.teal, R.color.blue
        );

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (currentUser != null) {
                // Reload danh sách nhóm
                viewModel.loadGroupsOfUser(currentUser.getId());

                // Sau khi load xong → tắt vòng xoay loading
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                }, 1000); // delay cho mượt
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    // Cấu hình RecyclerView để hiển thị danh sách các nhóm
    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerGroupList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Adapter nhận callback khi click vào một nhóm
        adapter = new GroupAdapter(getViewLifecycleOwner(), authViewModel, new ArrayList<>(), group -> {
            // Mở bottom sheet hiển thị chi tiết nhóm
            GroupDetailBottomSheet detailSheet = GroupDetailBottomSheet.newInstance(group, currentUser);
            detailSheet.show(getParentFragmentManager(), detailSheet.getTag());
        });

        recyclerView.setAdapter(adapter);

        // Hien thi toan bo danh sach nhóm cua user
        viewModel.loadGroupsOfUser(currentUser.getId());
        viewModel.getGroupList().observe(getViewLifecycleOwner(), groups -> {
            if (allGroups.isEmpty()) {
                allGroups = new ArrayList<>(groups); // Lưu bản gốc
            }
            adapter.setGroupList(groups);
        });
        adapter.notifyDataSetChanged();

        //Danh sach da loc
        viewModel.getFilteredGroups().observe(getViewLifecycleOwner(), adapter::setGroupList);
    }

    // Tìm kiếm nhóm theo tên người dùng nhập vào
    private void setupSearchBar(View view) {
        EditText searchInput = view.findViewById(R.id.edtSearch);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            // Khi người dùng nhập, lọc danh sách theo từ khóa
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchGroup(s.toString(), allGroups);
            }
        });
    }

    // Gắn sự kiện click vào nút thêm nhóm để mở bottom sheet
    private void setupAddGroupButton(View view) {
        ImageView addButton = view.findViewById(R.id.imgAdd);
        addButton.setOnClickListener(v -> {
            AddGroupBottomSheet addSheet = new AddGroupBottomSheet();
            addSheet.setOnGroupCreatedListener(newGroup -> {
                // Thêm group mới vào danh sách hiện tại
                allGroups.add(newGroup);                          // Cập nhật danh sách đầy đủ
                adapter.addGroup(newGroup);                       // Cập nhật hiển thị adapter
            });
            addSheet.show(getParentFragmentManager(), addSheet.getTag());
        });
    }
}
