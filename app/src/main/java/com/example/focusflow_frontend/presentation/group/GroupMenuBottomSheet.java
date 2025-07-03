package com.example.focusflow_frontend.presentation.group;

import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;
import com.example.focusflow_frontend.utils.TokenManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class GroupMenuBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_GROUP = "group";
    private Group group;
    private static final String ARG_USER = "user";
    private User user;
    private MemberAdapter adapter;
    private GroupViewModel viewModel;

    private boolean checkPermission() {
        if (user == null || group == null || user.getId() != group.getLeaderId()) {
            Toast.makeText(getContext(), "This action is only available to the group leader", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static GroupMenuBottomSheet newInstance(Group group, User user) {
        GroupMenuBottomSheet fragment = new GroupMenuBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_GROUP, group);
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(dlg -> {
            FrameLayout bottomSheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet
            );
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheet.setOnTouchListener((v, e) -> true); // Disable swipe down
            }
        });
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(false);
            behavior.setHideable(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_group_menu, container, false);
        super.onCreate(savedInstanceState);

        TextView group_name = view.findViewById(R.id.group_name);
        if (getArguments() != null) {
            group = (Group) getArguments().getSerializable(ARG_GROUP);

            group_name.setText(group.getGroupName());
            user = (User) getArguments().getSerializable(ARG_USER);
        }

        viewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerMembers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Truyền group vào Adapter
        adapter = new MemberAdapter(group, user, viewModel);
        recyclerView.setAdapter(adapter);

        viewModel.fetchUsersInGroup(group.getId());

        viewModel.getUsersInGroup().observe(getViewLifecycleOwner(), users -> {
            Log.d("GroupMenuBottomSheet", "Số lượng user: " + users.size());
            adapter.setUserList(users);
        });

        //Khi bam them nguoi:
        LinearLayout btnAdd = view.findViewById(R.id.add_layout);
        btnAdd.setOnClickListener(v -> {
            if (!checkPermission()) return;
            addMemberClick();
        });

        //Hien roi nhom:
        LinearLayout btnOut = view.findViewById(R.id.out_layout);
        btnOut.setOnClickListener(v->outGroupClick());

        //Giai tan nhom
        Button btnGiaiTan = view.findViewById(R.id.btnLeaveGroup);
        if (user.getId() == group.getLeaderId()){
            btnGiaiTan.setVisibility(VISIBLE);
            btnGiaiTan.setOnClickListener(v->DisbandGroup());
        }

        //Back:
        ImageView imBack = view.findViewById(R.id.btnBack);
        imBack.setOnClickListener(v -> {dismiss();});

        return view;
    }

    private void searchClick(GroupViewModel viewModel){
        viewModel.requestFocusOnSearch(true); // gửi tín hiệu cho Detail
        dismiss();
    }
    private boolean isNotification = true;

    private void addMemberClick(){
        AddMemberBottomSheet addSheet = new AddMemberBottomSheet();

        Bundle args = new Bundle();
        args.putSerializable("group", group);
        addSheet.setArguments(args);

        addSheet.setOnMembersAddedListener(() -> {
            Log.d("GroupMenuBottomSheet", "Callback được gọi!");
            // Reload danh sách thành viên
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d("GroupMenuBottomSheet", "Thực hiện fetchUsersInGroup");
                viewModel.fetchUsersInGroup(group.getId());
            }, 500);
        });

        addSheet.show(getParentFragmentManager(), "AddMemberBottomSheet");
    }
    public interface OnLeaveGroupListener {
        void onLeaveGroup();
    }

    private OnLeaveGroupListener leaveGroupListener;
    public void setOnLeaveGroupListener(OnLeaveGroupListener listener) {
        this.leaveGroupListener = listener;
    }

    // Gọi khi người dùng bấm nút "Out"
    private void handleLeaveGroup() {
        if (leaveGroupListener != null) {
            leaveGroupListener.onLeaveGroup();
        }
        dismiss(); // đóng chính nó
    }
    private void outGroupClick(){
        new AlertDialog.Builder(requireContext())
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave this group?")
                .setPositiveButton("Leave", (dialog, which) -> {
                    viewModel.removeUserFromGroup(group.getId(), user.getId(),
                            () ->
                            {
                                Toast.makeText(requireContext(), "You have left the group", Toast.LENGTH_SHORT).show();
                                viewModel.setGroupRemoved(group.getId());
                                handleLeaveGroup();
                                dismiss(); // Đóng BottomSheet sau khi setup Handler
                            },
                            () -> Toast.makeText(requireContext(), "Failed to leave group", Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private Runnable onGroupDisbanded;
    public void setOnGroupDisbanded(Runnable runnable) {
        this.onGroupDisbanded = runnable;
    }
    private void DisbandGroup(){
        new AlertDialog.Builder(requireContext())
                .setTitle("Disband Group")
                .setMessage("Are you sure you want to disband this group?")
                .setPositiveButton("Disband", (dialog, which) -> {
                    viewModel.deleteGroup(group.getId()); // Gọi API DELETE
                    viewModel.setGroupRemoved(group.getId());
                    Toast.makeText(requireContext(), "Group disbanded", Toast.LENGTH_SHORT).show();

                    handleLeaveGroup();
                    dismiss(); // Đóng BottomSheet sau khi setup Handler
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
