package com.example.focusflow_frontend.presentation.group;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;
import com.example.focusflow_frontend.utils.TokenManager;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

// BottomSheetDialogFragment dùng để thêm nhóm mới (Add Group) với giao diện dạng bottom sheet
public class AddMemberBottomSheet extends BottomSheetDialogFragment {

    //Hai đoạn code giúp bottom sheet full màn hình (không cần để ý)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet
            );
            if (bottomSheet != null) {
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);

                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                bottomSheet.setOnTouchListener((v, event) -> true);
            }
        });
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout bottomSheet = dialog.findViewById(
                com.google.android.material.R.id.design_bottom_sheet
        );
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(false); // Không thể kéo để thu nhỏ hay đóng
            behavior.setHideable(false);  // Không thể ẩn/đóng bằng cách kéo xuống
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Luôn ở trạng thái mở rộng
        }
    }

    // Khai báo các biến để ứng dụng nè
    private GroupViewModel viewModel;
    private AuthViewModel userViewModel;
    private Integer userId;
    private EditText etSearch;    //editText để nhập gmail
    private FlexboxLayout chipGroup;
    private RecyclerView rvSuggestions;
    private SuggestionAdapter adapter;
    private Group group;
    private List<User> allUsers = new ArrayList<>();
    private ImageView avtSelection;

    // Interface để truyền dữ liệu về
    public interface OnMembersAddedListener {
        void onMembersAdded();
    }

    private OnMembersAddedListener listener;

    public void setOnMembersAddedListener(OnMembersAddedListener listener) {
        this.listener = listener;
    }

    // Hàm Create (không cần để ý)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_members, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(GroupViewModel.class); // Lấy ViewModel
        userViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userId = TokenManager.getUserId(requireContext());

        if (getArguments() != null && getArguments().containsKey("group")) {
            group = (Group) getArguments().getSerializable("group");
        }

        if (group != null) {
            viewModel.fetchUsersInGroup(group.getId()); // nạp danh sách member hiện tại
        }

        etSearch = view.findViewById(R.id.etSearchEmail);
        chipGroup = view.findViewById(R.id.chipGroup);
        rvSuggestions = view.findViewById(R.id.rvSuggestions);

        SetAdapter(rvSuggestions);            // đổ dữ liệu vào Recycle
        etGmailChanged(etSearch, viewModel, userViewModel);    //editText nhập mail có biến đổi
        setSelectedUser(viewModel, view);      //hiển thị ds người đã chọn để lập nhóm
        AddMember(view, viewModel);           // Thiết lập nút Add Member

        // Nút back để đóng BottomSheet
        view.findViewById(R.id.btnBack).setOnClickListener(v -> dismiss());
    }

    //Đổ dữ liệu và recycle view
    public void SetAdapter(RecyclerView rvSuggestions) {
        // Tạo adapter với callback khi chọn user
        adapter = new SuggestionAdapter(new ArrayList<>(), user -> {
            viewModel.addUser(user);    // Thêm user vào danh sách đã chọn trong ViewModel
            etSearch.setText("");       // Xóa text ô tìm kiếm sau khi chọn
        });

        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuggestions.setAdapter(adapter);

        // Gọi ViewModel để lấy danh sách người dùng từ server
        userViewModel.fetchAllUsers();
    }

    //Hiển thị ds người đã chọn
    public void setSelectedUser(GroupViewModel viewModel, View view) {
        viewModel.getSuggestions().observe(getViewLifecycleOwner(), users -> {
            adapter.updateList(users);
            rvSuggestions.setVisibility(users.isEmpty() ? View.GONE : View.VISIBLE);
        });

        //tạo chip hiển thị ra layout
        viewModel.getSelectedUsers().observe(getViewLifecycleOwner(), users -> {
            chipGroup.removeAllViews();

            // Tạo chip mới cho từng user đã chọn
            for (User user : users) {
                Chip chip = new Chip(getContext());
                chip.setText(user.getEmail());
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(v -> viewModel.removeUser(user));
                chipGroup.addView(chip);
            }

            TextView countText = view.findViewById(R.id.count);
            countText.setText(String.valueOf(users.size()));
        });
    }

    //Tìm kiếm gmail
    public void etGmailChanged(EditText etSearch, GroupViewModel viewModel, AuthViewModel userViewModel) {
        userViewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            allUsers.clear();
            allUsers.addAll(users);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Lấy danh sách đã chọn
                List<User> selectedUsers = viewModel.getSelectedUsers().getValue();
                List<User> filteredAllUsers = new ArrayList<>();

                // Lấy danh sách user trong group
                List<User> usersInGroup = viewModel.getUsersInGroup().getValue();
                if (usersInGroup == null) usersInGroup = new ArrayList<>();

                for (User user : allUsers) {
                    boolean isCurrentUser = user.getId() == userId;

                    boolean isAlreadySelected = false;
                    if (selectedUsers != null) {
                        for (User selected : selectedUsers) {
                            if (selected.getId() == user.getId()) {
                                isAlreadySelected = true;
                                break;
                            }
                        }
                    }

                    boolean isAlreadyInGroup = false;
                    for (User groupMember : usersInGroup) {
                        if (groupMember.getId() == user.getId()) {
                            isAlreadyInGroup = true;
                            break;
                        }
                    }

                    if (!isCurrentUser && !isAlreadySelected && !isAlreadyInGroup) {
                        filteredAllUsers.add(user);
                    }
                }

                viewModel.searchUsers(s.toString(), filteredAllUsers);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Xử lý sự kiện click tạo nhóm khi bấm nút "Add member"
    public void AddMember(@NonNull View view, GroupViewModel viewModel) {
        view.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            List<User> selected = viewModel.getSelectedUsers().getValue(); // Lấy danh sách user đã chọn
            // Kiểm tra dữ liệu nhập
            if (selected == null || selected.isEmpty()) {
                Toast.makeText(getContext(), "Please choose at least one member to add into group.", Toast.LENGTH_SHORT).show();
            } else {
                List<Integer> userIds = new ArrayList<>();
                for (User user : selected) {
                    userIds.add(user.getId());
                }
                viewModel.addMembersToGroup(group.getId(), userIds);
                Toast.makeText(getContext(), "Added " + selected.size() + " members", Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    Log.d("AddMemberBottomSheet", "Callback gọi về GroupMenu");
                    listener.onMembersAdded();
                }
                dismiss();
            }
        });
    }
}
