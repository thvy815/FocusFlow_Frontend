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

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            // Có avatarUrl → ImageView
            ImageView imageView = new ImageView(parent.getContext());
            int size = (int) (parent.getContext().getResources().getDisplayMetrics().density * 40); // 40dp
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(8, 0, 8, 0);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundResource(R.drawable.bg_circle); // viền tròn nếu muốn

            if (avatarUrl.startsWith("res:")) {
                // 🔹 Ảnh từ resource nội bộ
                int resId = Integer.parseInt(avatarUrl.substring(4));
                Glide.with(context)
                        .load(resId)
                        .circleCrop()
                        .placeholder(R.drawable.bg_circle)
                        .into(imageView);
            } else if (avatarUrl.startsWith("uri:")) {
                // 🔹 Ảnh từ album, MediaStore, Google Photos, etc.
                Uri uri = Uri.parse(avatarUrl.substring(4));
                Log.d("AvatarDebug", "Parsed Uri: " + uri.toString());
                Glide.with(context)
                        .load(uri)
                        .circleCrop()
                        .placeholder(R.drawable.bg_circle)
                        .into(imageView);
            } else if (avatarUrl.startsWith("http")) {
                // 🔹 URL online
                Glide.with(context)
                        .load(avatarUrl)
                        .circleCrop()
                        .placeholder(R.drawable.bg_circle)
                        .into(imageView);
            } else {
                // Không rõ định dạng
                imageView.setImageResource(R.drawable.bg_circle);
            }

            return imageView;
        } else {
            // Không có avatarUrl → dùng TextView bo tròn
            TextView avatar = new TextView(parent.getContext());
            int size = (int) (parent.getContext().getResources().getDisplayMetrics().density * 40);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(8, 0, 8, 0);
            avatar.setLayoutParams(params);

            avatar.setText(String.valueOf(user.getUsername().charAt(0)).toUpperCase());
            avatar.setTextColor(parent.getContext().getResources().getColor(android.R.color.black));
            avatar.setBackgroundResource(R.drawable.bg_circle);
            avatar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            avatar.setGravity(android.view.Gravity.CENTER);
            avatar.setTextSize(16);
            int[] colors = {
                    Color.parseColor("#FFB74D"), Color.parseColor("#64B5F6"),
                    Color.parseColor("#81C784"), Color.parseColor("#E57373"),
                    Color.parseColor("#BA68C8")
            };
            int colorIndex = Math.abs(user.getUsername().hashCode()) % colors.length;
            avatar.setBackgroundTintList(ColorStateList.valueOf(colors[colorIndex]));

            return avatar;
        }
    }
}
