package com.example.focusflow_frontend.presentation.group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Adapter dùng để hiển thị danh sách các nhóm (Group) trong RecyclerView
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    // Danh sách các nhóm cần hiển thị
    private List<Group> groupList;

    // Listener để xử lý sự kiện khi người dùng nhấn vào 1 item
    private final OnGroupClickListener listener;

    // Interface cho sự kiện click vào một Group
    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    // Constructor của Adapter
    public GroupAdapter(List<Group> groupList, OnGroupClickListener listener) {
        this.groupList = groupList != null ? groupList : new ArrayList<>();
        this.listener = listener;
    }

    // Phương thức để cập nhật lại danh sách nhóm và thông báo cho RecyclerView vẽ lại
    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList != null ? groupList : new ArrayList<>();
        notifyDataSetChanged(); // thông báo dữ liệu đã thay đổi
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

        // Hiển thị tên nhóm và truong nhom
        holder.groupName.setText(group.getGroup_name());
        holder.groupLeader.setText(group.getLeader_id());

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

    public void removeGroupById(String id) {
        Iterator<Group> iterator = groupList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(id)) {
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
