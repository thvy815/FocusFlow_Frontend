package com.example.focusflow_frontend.presentation.calendar;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Streak;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.model.TaskGroupRequest;
import com.example.focusflow_frontend.data.viewmodel.GroupViewModel;
import com.example.focusflow_frontend.data.viewmodel.StreakViewModel;
import com.example.focusflow_frontend.data.viewmodel.TaskViewModel;
import com.example.focusflow_frontend.presentation.pomo.PomodoroFragment;
import com.example.focusflow_frontend.utils.TaskAlarmReceiver;
import com.example.focusflow_frontend.utils.TokenManager;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.kizitonwose.calendar.core.DayPosition;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalendarFragment extends Fragment {
    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>();
    private Set<LocalDate> taskDates = new HashSet<>();
    private TaskViewModel taskViewModel;
    private StreakViewModel streakViewModel;
    private LocalDate selectedDate = LocalDate.now();
    private int userId;
    private TextView tvStreakCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        userId = TokenManager.getUserId(requireContext());

        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        ImageButton btnAddTask = view.findViewById(R.id.btn_add_task);
        tvStreakCount = view.findViewById(R.id.tvStreakCount);

        // Button Add Task
        btnAddTask.setOnClickListener(v -> {
            AddTaskBottomSheet bottomSheet = new AddTaskBottomSheet();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dateString = selectedDate.format(formatter);

            // Đóng gói date vào Bundle
            Bundle bundle = new Bundle();
            bundle.putString("selected_date", dateString);
            bottomSheet.setArguments(bundle);

            bottomSheet.setOnTaskAddedListener(task -> {
                taskViewModel.fetchTasks(userId);
                scheduleNotification(task);
            });

            bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
        });

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Tạo adapter, truyền listener để xử lý khi checkbox được check/uncheck
        taskAdapter = new TaskAdapter(filteredTasks, new TaskAdapter.OnTaskCheckedListener() {
            @Override
            public void onTaskChecked(Task task, boolean isChecked) {
                task.setCompleted(isChecked);
                taskViewModel.updateTask(new TaskGroupRequest(task, null));
                taskAdapter.notifyDataSetChanged(); // đảm bảo re-bind lại adapter

                // Cập nhật local luôn
                taskAdapter.notifyItemChanged(filteredTasks.indexOf(task));
            }
        }, new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                // Truyền task vào để chỉnh sửa
                Bundle bundle = new Bundle();
                bundle.putSerializable("task", task);  // Task phải implements Serializable

                Task previousTask = task;
                AddTaskBottomSheet bottomSheet = new AddTaskBottomSheet();
                bottomSheet.setArguments(bundle);
                bottomSheet.setOnTaskUpdatedListener(updatedTask -> {
                    if (updatedTask != null) {
                        taskAdapter.updateTaskInAdapter(updatedTask);
                        cancelNotification(updatedTask); // 1. Hủy báo thức cũ
                        scheduleNotification(updatedTask); // 2. Tạo lại báo thức mới
                        updateTaskAndRefresh(updatedTask); // Cập nhật taskDates và hiển thị calendar
                    } else {
                        removeTaskAndRefresh(previousTask);
                        cancelNotification(previousTask); // Hủy báo thức cũ
                        taskViewModel.fetchTasks(userId); // Sau khi xóa → load lại danh sách
                    }
                });
                bottomSheet.show(getChildFragmentManager(), "EditTask");
            }
        }, new TaskAdapter.OnTaskLongClickListener() {
            @Override
            public void onTaskLongClick(Task task) {
                showOptionsDialog(task);  // Hiển thị dialog khi long click
            }
        });
        recyclerView.setAdapter(taskAdapter);

        // Set up CalendarView
        LocalDate today = LocalDate.now();
        calendarView.setup(
                YearMonth.from(today.minusMonths(1)), // startMonth
                YearMonth.from(today.plusMonths(1)), // endMonth
                DayOfWeek.MONDAY // firstDayOfWeek
        );
        calendarView.scrollToDate(today);

        // Set up container cho tháng
        calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthHeaderContainer>() {
            @NonNull
            @Override
            public MonthHeaderContainer create(@NonNull View view) {
                return new MonthHeaderContainer(view);
            }

            @Override
            public void bind(@NonNull MonthHeaderContainer container, @NonNull com.kizitonwose.calendar.core.CalendarMonth month) {
                TextView textView = container.getView().findViewById(R.id.monthHeaderText);

                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
                String formatted = month.getYearMonth().format(formatter);

                textView.setText(formatted);
            }
        });

        // Set up container cho ngày
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View dayView) {
                return new DayViewContainer(dayView);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
                container.dayText.setText(String.valueOf(day.getDate().getDayOfMonth()));

                if (day.getPosition() == DayPosition.MonthDate) {
                    container.dayText.setOnClickListener(v -> {
                        selectedDate = day.getDate();
                        filterTasksByDate(day.getDate());
                        calendarView.notifyCalendarChanged(); // Refresh để highlight ngày chọn
                    });

                    // Nếu là ngày được chọn
                    if (day.getDate().equals(selectedDate)) {
                        container.dayText.setBackgroundResource(R.drawable.bg_circle);
                        container.dayText.setTextColor(Color.BLACK);
                    } else if (taskDates.contains(day.getDate())) {
                        container.dayText.setBackgroundResource(R.drawable.circle_task); // ngày có task
                        container.dayText.setTextColor(Color.BLACK);
                    } else {
                        container.dayText.setBackground(null);
                        container.dayText.setTextColor(Color.BLACK);
                    }
                } else {
                    container.dayText.setBackground(null);
                    container.dayText.setTextColor(Color.GRAY);
                }
            }
        });

        // ViewModel
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        streakViewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        taskViewModel.getTaskList().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                allTasks.clear();
                for (Task task : tasks) {
                    updateTaskAndRefresh(task);
                }
            } else {
                Log.d("TaskFilter", "No tasks available.");
            }
        });

        // Đợi update thành công rồi fetch lại danh sách
        taskViewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                taskViewModel.fetchTasks(userId);
            } else {
                Toast.makeText(getContext(), "Update task failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe lỗi
        taskViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
        });

        // Gọi API lấy dữ liệu tất cả các task
        if (userId != -1) {
            taskViewModel.fetchTasks(userId);
        }

        loadStreak(userId);

        return view;
    }

    private void loadStreak(int userId) {
        streakViewModel.getStreakByUser(userId, new StreakViewModel.StreakCallback() {
            @Override
            public void onSuccess(Streak streak) {
                // Hiển thị lên giao diện
                tvStreakCount.setText(String.valueOf(streak.getCurrentStreak()));
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("STREAK_ERROR", errorMessage);
                Toast.makeText(getContext(), "Lỗi khi tải streak: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeTaskAndRefresh(Task task) {
        allTasks.remove(task);                    // Xoá task khỏi danh sách
        removeOldRepeatDates(task);               // Xoá các ngày (có repeat)
        filterTasksByDate(selectedDate);          // Cập nhật lại danh sách hiển thị
        calendarView.notifyCalendarChanged();     // Làm mới CalendarView
    }

    private LocalDate getNextRepeatDate(LocalDate date, String repeat) {
        if (repeat == null || repeat.equalsIgnoreCase("None")) {
            return date.plusYears(100); // để kết thúc vòng lặp
        }

        switch (repeat) {
            case "Daily":
                return date.plusDays(1);
            case "Weekly":
                return date.plusWeeks(1);
            case "Monthly":
                return date.plusMonths(1);
            default:
                return date.plusYears(100); // để thoát vòng lặp nếu lỗi
        }
    }

    private void updateTaskAndRefresh(Task task) {
        // Thay thế task cũ nếu có trong danh sách
        allTasks.removeIf(t -> t.getId() == task.getId() || t.equals(task));
        allTasks.add(task);

        if (task.getDueDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate baseDate = LocalDate.parse(task.getDueDate(), formatter);

            // Xóa các ngày cũ của task ra khỏi taskDates (dọn sạch để thêm lại)
            removeOldRepeatDates(task);

            // Them lại ngày gốc và các ngày lặp mới
            taskDates.add(baseDate);

            String repeat = task.getRepeatStyle();
            if (repeat != null && !repeat.equalsIgnoreCase("None")) {
                YearMonth endMonth = YearMonth.from(LocalDate.now().plusMonths(1)); // giống setup
                LocalDate endDate = endMonth.atEndOfMonth(); // lấy ngày cuối cùng tháng đó

                LocalDate current = baseDate;
                while (true) {
                    current = getNextRepeatDate(current, repeat);
                    if (current.isAfter(endDate)) break;
                    taskDates.add(current);
                }
            }
        }

        filterTasksByDate(selectedDate);
        calendarView.notifyCalendarChanged();
    }

    private void removeOldRepeatDates(Task task) {
        if (task.getDueDate() == null) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate baseDate = LocalDate.parse(task.getDueDate(), formatter);
        String repeat = task.getRepeatStyle();

        // Xóa ngày gốc
        taskDates.remove(baseDate);

        if (repeat == null || repeat.equalsIgnoreCase("None")) return;

        YearMonth endMonth = YearMonth.from(LocalDate.now().plusMonths(1));
        LocalDate endDate = endMonth.atEndOfMonth();

        LocalDate current = baseDate;
        while (true) {
            current = getNextRepeatDate(current, repeat);
            if (current.isAfter(endDate)) break;
            taskDates.remove(current);
        }
    }

    private void filterTasksByDate(LocalDate selectedDate) {
        filteredTasks.clear();

        List<Task> uncompletedTasks = new ArrayList<>();
        List<Task> completedTasks = new ArrayList<>();

        for (Task task : allTasks) {
            if (task.getDueDate() == null) continue;

            LocalDate due = LocalDate.parse(task.getDueDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String repeat = task.getRepeatStyle();
            boolean isMatch = false;

            if (repeat == null || repeat.equalsIgnoreCase("None")) {
                isMatch = due.equals(selectedDate);
            } else {
                if (!selectedDate.isBefore(due)) { // chỉ xét khi selectedDate >= due
                    switch (repeat) {
                        case "Daily":
                            isMatch = true;
                            break;
                        case "Weekly":
                            isMatch = selectedDate.getDayOfWeek() == due.getDayOfWeek();
                            break;
                        case "Monthly":
                            isMatch = selectedDate.getDayOfMonth() == due.getDayOfMonth();
                            break;
                        default:
                            Log.w("DEBUG", "Không hỗ trợ repeat kiểu:  " + repeat);
                    }
                }
            }

            if (isMatch) {
                if (task.isCompleted()) {
                    completedTasks.add(task);
                } else {
                    uncompletedTasks.add(task);
                }
            }

        }

        // Gộp lại: chưa hoàn thành → đã hoàn thành
        filteredTasks.addAll(uncompletedTasks);
        filteredTasks.addAll(completedTasks);

        taskAdapter.notifyDataSetChanged();
    }

    private void showOptionsDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Options for task")
                .setItems(new String[]{"Edit ", "Delete", "Start Pomodoro"}, (dialog, which) -> {
                    if (which == 0) {
                        // Sửa task (giống onTaskClick)
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("task", task);

                        AddTaskBottomSheet bottomSheet = new AddTaskBottomSheet();
                        bottomSheet.setArguments(bundle);
                        bottomSheet.setOnTaskUpdatedListener(updatedTask -> {
                            taskAdapter.updateTaskInAdapter(updatedTask);
                            cancelNotification(updatedTask); // 1. Hủy báo thức cũ
                            scheduleNotification(updatedTask); // 2. Tạo lại báo thức mới
                        });
                        bottomSheet.show(getChildFragmentManager(), "EditTask");
                    } else if (which == 1) {
                        // Huỷ thông báo nếu có
                        cancelNotification(task);

                        // Xóa task khỏi DB
                        taskViewModel.deleteTask(task.getId());
                        taskViewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
                            if (success != null && success) {
                                taskViewModel.fetchTasks(userId);
                            } else {
                                Toast.makeText(getContext(), "Xóa task thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (which == 2) {
                        PomodoroFragment pomodoroFragment = new PomodoroFragment();

                        // Tạo Bundle để truyền dữ liệu
                        Bundle args = new Bundle();
                        args.putInt("TASK_ID", task.getId()); // Đưa taskId vào Bundle
                        pomodoroFragment.setArguments(args); // Gắn Bundle vào Fragment

                        // Chuyển sang Fragment Pomo
                        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container, pomodoroFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                })
                .show();
    }

    private long getReminderOffset(String reminderStyle) {
        switch (reminderStyle) {
            case "5 minutes before":
                return 5 * 60 * 1000;
            case "10 minutes before":
                return 10 * 60 * 1000;
            case "15 minutes before":
                return 15 * 60 * 1000;
            case "30 minutes before":
                return 30 * 60 * 1000;
            case "1 hour before":
                return 60 * 60 * 1000;
            case "1 day before":
                return 24 * 60 * 60 * 1000;
            case "On time":
            default:
                return 0;
        }
    }

    private void scheduleNotification(Task task) {
        String dueDate = task.getDueDate();
        String dueTime = task.getTime();
        String reminder = task.getReminderStyle();
        String repeat = task.getRepeatStyle();

        if (dueDate == null || dueDate.isEmpty()) return;

        // Nếu chỉ có ngày mà thiếu giờ → mặc định 08:00
        if (dueTime == null || dueTime.isEmpty()) dueTime = "08:00";

        // Không đặt thông báo nếu không có nhắc nhở
        if (reminder == null || reminder.equalsIgnoreCase("None")) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            long offset = getReminderOffset(reminder);

            // ⏰ Đặt báo thức
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate baseDate = LocalDate.parse(dueDate, dateFormatter);
            YearMonth endMonth = YearMonth.from(LocalDate.now().plusMonths(1)); // giống setup
            LocalDate endDate = endMonth.atEndOfMonth(); // lấy ngày cuối cùng tháng đó

            int repeatCount = 0;
            LocalDate current = baseDate;

            // Nếu có repeat → đặt thông báo cho các ngày tương lai
            while (!current.isAfter(endDate)) {
                String currentDateTime = current.format(dateFormatter) + " " + dueTime;
                long triggerAtMillis = sdf.parse(currentDateTime).getTime() - offset;

                if (triggerAtMillis > System.currentTimeMillis()) {
                    // Mỗi repeat có requestCode riêng (vd: taskId * 1000 + repeatCount)
                    int requestCode = (repeat == null || repeat.equalsIgnoreCase("None"))
                            ? task.getId()
                            : task.getId() * 1000 + repeatCount;

                    setAlarm(requestCode, task.getTitle(), triggerAtMillis, alarmManager);
                    Log.d("AlarmSet", "✅ Alarm Code: " + requestCode);
                    repeatCount++;
                }

                // Nếu không có repeat → chỉ đặt 1 thông báo
                if (repeat == null || repeat.equalsIgnoreCase("None")) break;

                // tăng ngày lặp
                switch (repeat) {
                    case "Daily": current = current.plusDays(1); break;
                    case "Weekly": current = current.plusWeeks(1); break;
                    case "Monthly": current = current.plusMonths(1); break;
                    default: current = endDate.plusDays(1); break; // thoát vòng lặp
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "❌ Lỗi khi đặt thông báo", Toast.LENGTH_SHORT).show();
            Log.e("NotificationError", "❌ Lỗi khi đặt thông báo: " + e.toString());
        }
    }

    private void setAlarm(int requestCode, String title, long triggerAtMillis, AlarmManager alarmManager) {
        Intent intent = new Intent(getContext(), TaskAlarmReceiver.class);
        intent.putExtra("task_title", title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            Log.d("AlarmSet", "🔔 Đã đặt báo thức lúc: " + new Date(triggerAtMillis));
        }
    }

    private void cancelNotification(Task task) {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Hủy báo thức đơn nếu không lặp
        if (task.getRepeatStyle() == null || task.getRepeatStyle().equalsIgnoreCase("None")) {
            cancelSingleAlarm(task.getId(), task.getTitle(), alarmManager);
            return;
        }

        String repeat = task.getRepeatStyle();
        Log.d("AlarmCancel", "🔁 Cancel task: " + task.getTitle() + " (ID: " + task.getId() + "), repeat: " + repeat);

        // Hủy báo thức lặp (nhiều instance)
        int requestCodeBase = task.getId() * 1000;
        int maxRepeat = 60; // hoặc tính chính xác số lần lặp nếu cần
        for (int i = 0; i < maxRepeat; i++) {
            cancelSingleAlarm(requestCodeBase + i, task.getTitle(), alarmManager);
        }
    }

    private void cancelSingleAlarm(int requestCode, String title, AlarmManager alarmManager) {
        Intent intent = new Intent(getContext(), TaskAlarmReceiver.class);
        intent.putExtra("task_title", title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Log.d("AlarmCancel", "🛑 Hủy alarm với requestCode = " + requestCode);
    }

    // Container cho tháng
    public static class MonthHeaderContainer extends ViewContainer {
        public MonthHeaderContainer(@NonNull View view) {
            super(view);
        }
    }

    // Container cho ngày
    public static class DayViewContainer extends ViewContainer {
        public final TextView dayText;

        public DayViewContainer(@NonNull View view) {
            super(view);
            dayText = view.findViewById(R.id.calendarDayText);
        }
    }
}
