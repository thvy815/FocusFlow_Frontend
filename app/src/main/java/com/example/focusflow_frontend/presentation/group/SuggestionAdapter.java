package com.example.focusflow_frontend.presentation.group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.User;

import java.util.List;

// Adapter dùng để hiển thị danh sách người dùng gợi ý trong RecyclerView
public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    // Danh sách người dùng để hiển thị
    private List<User> users;

    // Listener để xử lý sự kiện click vào từng user trong danh sách
    private final OnUserClickListener listener;

    // Interface định nghĩa sự kiện click vào user
    public interface OnUserClickListener {
        void onClick(User user);
    }

    // Constructor khởi tạo adapter với danh sách người dùng và listener sự kiện
    public SuggestionAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    // Cập nhật lại danh sách người dùng và thông báo RecyclerView vẽ lại
    public void updateList(List<User> newList) {
        this.users = newList;
        notifyDataSetChanged(); // Thông báo dữ liệu đã thay đổi để cập nhật UI
    }

    // Tạo ViewHolder mới khi cần (khi scroll đến item chưa có view)
    @NonNull
    @Override
    public SuggestionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout cho từng item trong RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_suggestion, parent, false);
        return new ViewHolder(view);
    }

    // Gán dữ liệu User vào ViewHolder tương ứng theo vị trí
    @Override
    public void onBindViewHolder(@NonNull SuggestionAdapter.ViewHolder holder, int position) {
        User user = users.get(position); // Lấy user theo vị trí
        holder.email.setText(user.getEmail()); // Hiển thị email của user

        // Thiết lập sự kiện click vào item để gọi callback listener
        holder.itemView.setOnClickListener(v -> listener.onClick(user));
    }

    // Trả về số lượng phần tử trong danh sách users
    @Override
    public int getItemCount() {
        return users.size();
    }

    // ViewHolder lưu tham chiếu đến các View trong mỗi item để tái sử dụng
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView email;

        ViewHolder(View itemView) {
            super(itemView);
            // Ánh xạ TextView email trong layout item_user_suggestion.xml
            email = itemView.findViewById(R.id.tvEmail);
        }
    }
}
