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

public class AddGroupBottomSheet extends BottomSheetDialogFragment {

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
            behavior.setDraggable(false);
            behavior.setHideable(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private GroupViewModel viewModel;
    private EditText etSearch;
    private FlexboxLayout chipGroup;
    private RecyclerView rvSuggestions;
    private SuggestionAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
    private ImageView avtSelection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(GroupViewModel.class);
        etSearch = view.findViewById(R.id.etSearchEmail);
        chipGroup = view.findViewById(R.id.chipGroup);
        rvSuggestions = view.findViewById(R.id.rvSuggestions);
        avtSelection = view.findViewById(R.id.select_avt);
        //Set adapter
        SetAdapter(rvSuggestions);
        //Gmail editText changed
        etGmailChanged(etSearch, viewModel);
        //User selected
        setSelectedUser(viewModel,view);
        // Create group click:
        CreateGroup(view, viewModel);
        //Select AVT:
        avtSelection.setOnClickListener(v->setAvtSelection(view));

        view.findViewById(R.id.btnBack).setOnClickListener(v -> dismiss());
    }
// Do du lieu vao RecycleView (gmail)
    public void SetAdapter(RecyclerView rvSuggestions){
        // Sample users (simulate API data)
        allUsers.add(new User("Vy", "vy@gmail.com", ""));
        allUsers.add(new User("Khang", "khang@gmail.com", ""));
        allUsers.add(new User("Tâm", "tam@gmail.com", ""));
        allUsers.add(new User("Linh", "linh123@gmail.com", ""));

        // Setup RecyclerView
        adapter = new SuggestionAdapter(new ArrayList<>(), user -> {
            viewModel.addUser(user);
            etSearch.setText("");
        });
        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuggestions.setAdapter(adapter);
    }
    //Dua co email duoc chon vao khung co san (ben layout)
    public void setSelectedUser(GroupViewModel viewModel, View view){
        // Observe suggestions
        viewModel.getSuggestions().observe(getViewLifecycleOwner(), users -> {
            adapter.updateList(users);
        });

        // Observe selected users
        viewModel.getSelectedUsers().observe(getViewLifecycleOwner(), users -> {
            chipGroup.removeAllViews();
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
    //Loc gmail theo dia chi mail
    public void etGmailChanged(EditText etSearch, GroupViewModel viewModel){
        // Handle text change
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchUsers(s.toString(), allUsers);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
    //Create Group
    public void CreateGroup(@NonNull View view, GroupViewModel viewModel) {
        view.findViewById(R.id.btnCreateGroup).setOnClickListener(v -> {
            List<User> selected = viewModel.getSelectedUsers().getValue();
            String groupName = ((EditText) view.findViewById(R.id.etGroupName)).getText().toString().trim();

            if (groupName.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên nhóm.", Toast.LENGTH_SHORT).show();
            } else if (selected == null || selected.isEmpty()) {
                Toast.makeText(getContext(), "Hãy chọn ít nhất một người để tạo nhóm.", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Gửi yêu cầu tạo nhóm đến API hoặc xử lý tạo nhóm ở đây
                Toast.makeText(getContext(), "Tạo nhóm: " + groupName + " với " + selected.size() + " người", Toast.LENGTH_SHORT).show();
                dismiss(); // Đóng BottomSheet
            }
        });
    }
//Chon AVT
    public void setAvtSelection(View view){
        view.findViewById(R.id.select_avt).setOnClickListener(v->{
            AvtSelectionBottomSheet bottomSheet = new AvtSelectionBottomSheet();
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });
    }
}