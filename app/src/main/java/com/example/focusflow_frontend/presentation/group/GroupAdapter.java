package com.example.focusflow_frontend.presentation.group;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.AuthViewModel;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Adapter dùng để hiển thị danh sách các nhóm (Group) trong RecyclerView
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    // Danh sách các nhóm cần hiển thị
    private List<Group> groupList;
    private AuthViewModel authViewModel;
    private final LifecycleOwner lifecycleOwner;
    private final Map<Integer, User> userCache = new HashMap<>();

    // Listener để xử lý sự kiện khi người dùng nhấn vào 1 item
    private final OnGroupClickListener listener;

    // Interface cho sự kiện click vào một Group
    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    // Constructor của Adapter
    public GroupAdapter(LifecycleOwner lifecycleOwner, AuthViewModel authViewModel, List<Group> groupList, OnGroupClickListener listener) {
        this.lifecycleOwner = lifecycleOwner;
        this.authViewModel = authViewModel;
        this.groupList = groupList != null ? groupList : new ArrayList<>();
        this.listener = listener;
    }

    // Phương thức để cập nhật lại danh sách nhóm và thông báo cho RecyclerView vẽ lại
    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList != null ? groupList : new ArrayList<>();
        notifyDataSetChanged(); // thông báo dữ liệu đã thay đổi
    }

    public void addGroup(Group group) {
        groupList.add(group);
        notifyItemInserted(groupList.size() - 1);
    }

    // Tạo ViewHolder - gọi khi cần tạo item mới (item_group.xml)
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    // Gán dữ liệu từ Group vào các View trong ViewHolder
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position); // lấy group theo vị trí
        int leaderId = group.getLeaderId();

        // Hiển thị tên nhóm và trưởng nhóm
        holder.groupName.setText(group.getGroupName());
        holder.groupLeader.setText(String.valueOf(group.getLeaderId()));

        if (userCache.containsKey(leaderId)) {
            holder.groupLeader.setText(userCache.get(leaderId).getUsername());
        } else {
            authViewModel.getUserByIdLiveData(leaderId).observe(lifecycleOwner, user -> {
                if (user != null && user.getId() == leaderId) {
                    userCache.put(leaderId, user);
                    holder.groupLeader.setText(user.getUsername());
                }
            });
        }

        // Xử lý sự kiện khi người dùng click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onGroupClick(group);
        });
    }

    // Trả về số lượng item trong danh sách
    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public void removeGroupById(int id) {
        Iterator<Group> iterator = groupList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId() == id) {
                iterator.remove();
                notifyDataSetChanged();
                break;
            }
        }
    }

    // ViewHolder lưu tham chiếu đến các thành phần UI trong mỗi item
    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupName, groupLeader;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các thành phần giao diện từ layout item_group.xml
            groupName = itemView.findViewById(R.id.txtGroupName);
            groupLeader = itemView.findViewById(R.id.txtLeader);
        }
    }

}
