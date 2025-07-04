package com.example.focusflow_frontend.presentation.group;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

        if (member.getId() == group.getLeaderId()) {
            holder.ivCrownIcon.setVisibility(View.VISIBLE);
        } else {
            holder.ivCrownIcon.setVisibility(View.GONE);
        }

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

        // Set avt
        holder.avatarContainer.removeAllViews();
        View avatarView = createAvatarView(holder.avatarContainer, member);
        holder.avatarContainer.addView(avatarView);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView removeIC, ivCrownIcon;
        LinearLayout avatarContainer;
        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            removeIC = itemView.findViewById(R.id.remove);
            avatarContainer = itemView.findViewById(R.id.avatarContainer);
            ivCrownIcon = itemView.findViewById(R.id.leader);
        }
    }
    private View createAvatarView(ViewGroup parent, User user) {
        Context context = parent.getContext();
        String avatarUrl = user.getAvatarUrl();

        ImageView imageView = new ImageView(context);
        int size = (int) (context.getResources().getDisplayMetrics().density * 40); // 40dp
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(8, 0, 8, 0);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackgroundResource(R.drawable.bg_circle);

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            try {
                if (avatarUrl.startsWith("url:") || avatarUrl.startsWith("http")) {
                    String finalUrl = avatarUrl.startsWith("url:") ? avatarUrl.substring(4) : avatarUrl;

                    // Nếu là localhost → đổi thành 10.0.2.2 để load được từ Android Emulator
                    if (finalUrl.contains("localhost")) {
                        finalUrl = finalUrl.replace("localhost", "10.0.2.2");
                    }

                    Glide.with(context)
                            .load(finalUrl)
                            .circleCrop()
                            .placeholder(R.drawable.avatar1)
                            .error(R.drawable.avatar1)
                            .into(imageView);

                } else if (avatarUrl.startsWith("uri:")) {
                    Uri uri = Uri.parse(avatarUrl.substring(4));
                    Glide.with(context)
                            .load(uri)
                            .circleCrop()
                            .placeholder(R.drawable.avatar1)
                            .error(R.drawable.avatar1)
                            .into(imageView);

                } else if (avatarUrl.startsWith("res:")) {
                    int resId = Integer.parseInt(avatarUrl.substring(4));
                    imageView.setImageResource(resId);
                } else {
                    imageView.setImageResource(R.drawable.avatar1);
                }
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.avatar1);
            }

            return imageView;

        } else {
            // Không có avatar → dùng avatar mặc định luôn
            imageView.setImageResource(R.drawable.avatar1);
            return imageView;
        }
    }
}
