package com.example.focusflow_frontend.presentation.group;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.model.User;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;

import java.util.List;

public class TaskGroupAdapter extends RecyclerView.Adapter<TaskGroupAdapter.TaskViewHolder> {
    private List<Task> tasks; // Danh sách các task
    private final OnTaskCheckedChangeListener listener; // Lắng nghe thay đổi checkbox
    private OnTaskClickListener clickListener;
    private final GroupViewModel groupViewModel;
    private final LifecycleOwner lifecycleOwner;

    // Interface cho sự kiện checkbox
    public interface OnTaskCheckedChangeListener {
        void onTaskCheckedChanged(Task task, boolean isChecked);
    }
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }
    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.clickListener = listener;
    }

    public TaskGroupAdapter(List<Task> tasks,
                            OnTaskCheckedChangeListener listener,
                            OnTaskClickListener clickListener,
                            GroupViewModel groupViewModel,
                            LifecycleOwner lifecycleOwner) {
        this.tasks = tasks;
        this.listener = listener;
        this.clickListener = clickListener;
        this.groupViewModel = groupViewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    // Cập nhật danh sách task mới
    public void setTaskList(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public int getTaskIndexById(int taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == taskId) {
                return i;
            }
        }
        return -1;
    }

    public void updateTaskInAdapter(Task updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeTaskFromAdapter(int taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() != null && tasks.get(i).getId() == taskId) {
                tasks.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void addTaskToAdapter(Task task) {
        tasks.add(task); // thêm vào cuối danh sách
        notifyItemInserted(tasks.size() - 1);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_group, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        // Gán dữ liệu cho item
        holder.txtTitle.setText(task.getTitle());
        holder.txtDeadline.setText(task.getDueDate());

        // Gán trạng thái checkbox và xử lý sự kiện
        holder.checkBox.setOnCheckedChangeListener(null); // Tránh gọi lại sự kiện cũ
        holder.checkBox.setChecked(task.isCompleted());
        holder.checkBox.setOnCheckedChangeListener((btn, isChecked) -> {
            task.setCompleted(isChecked);
            if (listener != null) listener.onTaskCheckedChanged(task, isChecked);
        });

        // Gán priority
        String priorityText = null;
        int priorityColor = 0;
        switch (task.getPriority()) {
            case 1:
                priorityText = "Low";
                priorityColor = android.graphics.Color.parseColor("#4CAF50"); // Xanh lá
                break;
            case 2:
                priorityText = "Medium";
                priorityColor = android.graphics.Color.parseColor("#FF9800"); // Cam
                break;
            case 3:
                priorityText = "High";
                priorityColor = android.graphics.Color.parseColor("#F44336"); // Đỏ
                break;
        }
        if (priorityText != null) {
            holder.priorityName.setText(priorityText);
            holder.priorityName.setTextColor(priorityColor);
            holder.priorityName.setVisibility(View.VISIBLE);
            holder.imageFlag.setColorFilter(priorityColor, android.graphics.PorterDuff.Mode.SRC_IN);
            holder.imageFlag.setVisibility(View.VISIBLE);
        } else {
            holder.priorityName.setVisibility(View.GONE);
            holder.imageFlag.setVisibility(View.GONE);
        }

        // Sự kiện click item task
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTaskClick(tasks.get(holder.getAdapterPosition()));
            }
        });

        // Lấy danh sách user được phân công task
        groupViewModel.getAssignedUsersOfTask(task.getId()).observe(lifecycleOwner, users -> {
            holder.avatarContainer.removeAllViews(); // xóa avatar cũ TRƯỚC khi add mới

            if (users != null && !users.isEmpty()) {
                int maxAvatars = 5;
                int displayCount = Math.min(users.size(), maxAvatars);

                for (int i = 0; i < displayCount; i++) {
                    View avatarView = createAvatarView(holder.avatarContainer, users.get(i));
                    holder.avatarContainer.addView(avatarView);
                }

                // Nếu có nhiều hơn 5 member → hiển thị "+N"
                if (users.size() > maxAvatars) {
                    TextView moreView = new TextView(holder.avatarContainer.getContext());
                    int size = (int) (holder.avatarContainer.getContext().getResources().getDisplayMetrics().density * 40);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                    params.setMargins(8, 0, 8, 0);
                    moreView.setLayoutParams(params);

                    moreView.setText("+" + (users.size() - maxAvatars));
                    moreView.setTextColor(holder.avatarContainer.getContext().getResources().getColor(android.R.color.white));
                    moreView.setBackgroundResource(R.drawable.bg_circle);
                    moreView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    moreView.setGravity(android.view.Gravity.CENTER);
                    moreView.setTextSize(16);
                    holder.avatarContainer.addView(moreView);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    // ViewHolder để ánh xạ các view trong item
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDeadline, priorityName;
        CheckBox checkBox;
        LinearLayout avatarContainer;
        private final ImageView imageFlag;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.title);
            txtDeadline = itemView.findViewById(R.id.deadline);
            checkBox = itemView.findViewById(R.id.checkbox);
            avatarContainer = itemView.findViewById(R.id.avatarContainer);
            imageFlag = itemView.findViewById(R.id.imageFlag);
            priorityName = itemView.findViewById(R.id.priorityName);
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
