package com.example.focusflow_frontend.presentation.group;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

// BottomSheetDialogFragment dùng để thêm nhóm mới (Add Group) với giao diện dạng bottom sheet
public class AddGroupBottomSheet extends BottomSheetDialogFragment {

    //Hai đoạn code giúp bottom sheet full màn hình (không cần để ý)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
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
        FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(false); // Không thể kéo để thu nhỏ hay đóng
            behavior.setHideable(false);  // Không thể ẩn/đóng bằng cách kéo xuống
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Luôn ở trạng thái mở rộng
        }
    }

    // Khai báo các biến để ứng dụng nè
    private GroupViewModel viewModel;
    private EditText etSearch;    //editText để nhập gmail
    private FlexboxLayout chipGroup;
    private RecyclerView rvSuggestions;
    private SuggestionAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
    private ImageView avtSelection;

    // Interface để truyền dữ liệu về
    public interface OnGroupCreatedListener {
        void onGroupCreated(String groupName, List<User> members);
    }
    private OnGroupCreatedListener listener;

    public void setOnGroupCreatedListener(OnGroupCreatedListener listener) {
        this.listener = listener;
    }

    // Hàm Create (không cần để ý)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_group, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(GroupViewModel.class); // Lấy ViewModel

        etSearch = view.findViewById(R.id.etSearchEmail);
        chipGroup = view.findViewById(R.id.chipGroup);
        rvSuggestions = view.findViewById(R.id.rvSuggestions);

        SetAdapter(rvSuggestions);            // đổ dữ liệu vào Recycle
        etGmailChanged(etSearch, viewModel);    //editText nhập mail có biến đổi
        setSelectedUser(viewModel, view);      //hiển thị ds người đã chọn để lập nhóm
        CreateGroup(view, viewModel);           // Thiết lập nút tạo nhóm

        // Nút back để đóng BottomSheet
        view.findViewById(R.id.btnBack).setOnClickListener(v -> dismiss());
    }

    //Đổ dữ liệu và recycle view
    public void SetAdapter(RecyclerView rvSuggestions) {
        // Thêm dữ liệu mẫu (giả lập, có thể thay bằng API)
        allUsers.add(new User("Vy", "vy@gmail.com", ""));
        allUsers.add(new User("Khang", "khang@gmail.com", ""));
        allUsers.add(new User("Tâm", "tam@gmail.com", ""));
        allUsers.add(new User("Linh", "linh123@gmail.com", ""));

        // Tạo adapter với callback khi chọn user
        adapter = new SuggestionAdapter(new ArrayList<>(), user -> {
            viewModel.addUser(user);    // Thêm user vào danh sách đã chọn trong ViewModel
            etSearch.setText("");       // Xóa text ô tìm kiếm sau khi chọn
        });

        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuggestions.setAdapter(adapter);
    }

    //Hiển thị ds người đã chọn
    public void setSelectedUser(GroupViewModel viewModel, View view) {
        viewModel.getSuggestions().observe(getViewLifecycleOwner(), users -> {
            adapter.updateList(users);
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
    public void etGmailChanged(EditText etSearch, GroupViewModel viewModel) {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Gọi hàm tìm kiếm user theo email trong ViewModel
                viewModel.searchUsers(s.toString(), allUsers);
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // Xử lý sự kiện click tạo nhóm khi bấm nút "Tạo nhóm"
    public void CreateGroup(@NonNull View view, GroupViewModel viewModel) {
        view.findViewById(R.id.btnCreateGroup).setOnClickListener(v -> {
            List<User> selected = viewModel.getSelectedUsers().getValue(); // Lấy danh sách user đã chọn
            String groupName = ((EditText) view.findViewById(R.id.etGroupName)).getText().toString().trim(); // Lấy tên nhóm

            // Kiểm tra dữ liệu nhập
            if (groupName.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên nhóm.", Toast.LENGTH_SHORT).show();
            }
            else if (selected == null || selected.isEmpty()) {
                Toast.makeText(getContext(), "Hãy chọn ít nhất một người để tạo nhóm.", Toast.LENGTH_SHORT).show();
            }
            else {
                //api ở đây nè
                Toast.makeText(getContext(), "Tạo nhóm: " + groupName + " với " + selected.size() + " người", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }
}
