package com.example.focusflow_frontend.presentation.group;

import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
        FrameLayout bottomSheet = dialog.findViewById(
                com.google.android.material.R.id.design_bottom_sheet
        );
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

            group_name.setText(group.getGroup_name());
            user = (User) getArguments().getSerializable(ARG_USER);

        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerMembers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Truyền group vào Adapter
        adapter = new MemberAdapter(group, user);
        recyclerView.setAdapter(adapter);

        loadMembers();
        GroupViewModel viewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        // Khi bam vao tim kiem: hien ra trang detail --> tim kiem task
        LinearLayout btnSearch = view.findViewById(R.id.search_layout);
        btnSearch.setOnClickListener(v -> searchClick(viewModel));

        //Khi bam bat/tat thong bao:
        LinearLayout btnNotification = view.findViewById(R.id.noti_layout);
        btnNotification.setOnClickListener(v->notificationClick(view));

        //Khi bam them nguoi:
        LinearLayout btnAdd = view.findViewById(R.id.add_layout);
        btnAdd.setOnClickListener(v->addMemberClick());

        //Hien roi nhom:
        LinearLayout btnOut = view.findViewById(R.id.out_layout);
        btnOut.setOnClickListener(v->outGroupClick());
        //Giai tan nhom
        Button btnGiaiTan = view.findViewById(R.id.btnLeaveGroup);
   //     if (user.getId().equals(group.getLeader_id())){
            btnGiaiTan.setVisibility(VISIBLE);
            btnGiaiTan.setOnClickListener(v->DisbandGroup());
      //  }
        //Back:
        ImageView imBack = view.findViewById(R.id.btnBack);
        imBack.setOnClickListener(v -> {dismiss();});

        return view;
    }
//lay du lieu tu db:
    private void loadMembers() {
        List<User> members = new ArrayList<>();

        // Tạo danh sách user mẫu
        members.add(new User("1", "leader@example.com", "pass123"));
        members.add(new User("1", "member1@example.com", "pass123"));
        members.add(new User("1", "member2@example.com", "pass123"));
        members.add(new User("1", "member3@example.com", "pass123"));

        // Gán leader_id trùng userId của Leader
        group.setLeader_id("1");
        adapter.setUserList(members);

//        if (group != null && group.getMembers() != null) {
//            List<User> members = group.getMembers();
//            adapter.setData(members);
//        }
    }

    private void searchClick(GroupViewModel viewModel){
        viewModel.requestFocusOnSearch(true); // gửi tín hiệu cho Detail
        dismiss();
    }
    private boolean isNotification = true;
    private void notificationClick(View view){
        ImageView imv = view.findViewById(R.id.noti);
        TextView txt = view.findViewById(R.id.txtNoti);
        isNotification = !isNotification;
        //Bat thong  bao
        if (isNotification){
            imv.setImageResource(R.drawable.ic_noti);
            txt.setText("Notification");
        }
        //Tat thong bao
        else{
            imv.setImageResource(R.drawable.ic_none_noti);
            txt.setText("None");
        }
    }
    private void addMemberClick(){
        AddMemberBottomSheet addSheet = new AddMemberBottomSheet();
        addSheet.show(getParentFragmentManager(), addSheet.getTag());
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
                    Toast.makeText(requireContext(), "You have left the group", Toast.LENGTH_SHORT).show();
                    handleLeaveGroup();
                    dismiss(); // Đóng BottomSheet sau khi setup Handler
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void DisbandGroup(){
        new AlertDialog.Builder(requireContext())
                .setTitle("Disband Group")
                .setMessage("Are you sure you want to disband this group?")
                .setPositiveButton("Disband", (dialog, which) -> {
                    Toast.makeText(requireContext(), "You have disbanded the group", Toast.LENGTH_SHORT).show();
                    handleLeaveGroup();
                    dismiss(); // Đóng BottomSheet sau khi setup Handler
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void removeMemberClick(Context context){
        Toast.makeText(context, "Hàm xóa member dòng 207", Toast.LENGTH_SHORT).show();
    }
}
