package com.example.focusflow_frontend.presentation.group;

import static android.view.View.VISIBLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Group;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;

import java.util.ArrayList;
import java.util.List;
public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final Group group;
    private final User user;
    private final List<User> userList = new ArrayList<>();
    private final GroupViewModel viewModel;

    public MemberAdapter(Group group, User user, GroupViewModel viewModel) {
        this.group = group;
        this.user = user;
        this.viewModel = viewModel;
    }

    public void setUserList(List<User> list) {
        userList.clear();
        userList.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_menu, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User member = userList.get(position);
        holder.tvName.setText(member.getUsername());

        // Chỉ hiển thị nút "Xóa" nếu người dùng hiện tại là leader và không phải chính mình
        if (group.getLeaderId() == user.getId() && member.getId() != user.getId()) {
            holder.removeIC.setVisibility(View.VISIBLE);

            holder.removeIC.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Remove member")
                        .setMessage("Are you sure you want to remove " + member.getUsername() + " from the group?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            // Gọi ViewModel để xóa
                            viewModel.removeUserFromGroup(group.getId(), member.getId(),
                                    () -> {
                                        userList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, userList.size());
                                        Toast.makeText(holder.itemView.getContext(),
                                                "Removed successfully", Toast.LENGTH_SHORT).show();
                                    },
                                    () -> Toast.makeText(holder.itemView.getContext(),
                                            "Remove failed", Toast.LENGTH_SHORT).show())
                            ;
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            holder.removeIC.setVisibility(View.GONE);
        }
       // holder.userAVT.setImageResource(user.getAVT());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView userAVT, removeIC;
        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            removeIC = itemView.findViewById(R.id.remove);
            userAVT = itemView.findViewById(R.id.avtUser);
        }
    }
}
