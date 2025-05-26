package com.example.focusflow_frontend.presentation.group;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {

    private GroupViewModel viewModel;
    private GroupAdapter adapter;
    private List<Group> allGroups = new ArrayList<>();
    private User user = new User("Vy","vv@","12345");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group, container, false);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        setupRecyclerView(view);      // Hiển thị danh sách nhóm
        setupSearchBar(view);         // Tìm kiếm nhóm
        setupAddGroupButton(view);    // Thêm nhóm mới

        //Remove theo yeu cau
        viewModel.getGroupRemoved().observe(getViewLifecycleOwner(), groupId -> {
            adapter.removeGroupById(groupId);
        });

        return view;
    }

    // Cấu hình RecyclerView để hiển thị danh sách các nhóm
    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerGroupList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Adapter nhận callback khi click vào một nhóm
        adapter = new GroupAdapter(new ArrayList<>(), group -> {
            // Mở bottom sheet hiển thị chi tiết nhóm
            GroupDetailBottomSheet detailSheet = GroupDetailBottomSheet.newInstance(group, user);
            detailSheet.show(getParentFragmentManager(), detailSheet.getTag());
        });

        recyclerView.setAdapter(adapter);

        // Hien thi toan bo danh sach
        viewModel.getGroupList().observe(getViewLifecycleOwner(), groups -> {
            allGroups = groups; // Lưu lại danh sách đầy đủ để lọc sau này
            adapter.setGroupList(groups);
        });
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
            addSheet.show(getParentFragmentManager(), addSheet.getTag());
        });
    }
}
