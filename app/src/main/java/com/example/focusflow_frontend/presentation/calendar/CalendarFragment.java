package com.example.focusflow_frontend.presentation.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.viewmodel.TaskViewModel;
import com.example.focusflow_frontend.utils.TokenManager;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.kizitonwose.calendar.core.DayPosition;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>();
    private TaskViewModel taskViewModel;
    private LocalDate selectedDate = LocalDate.now();
    private int userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        userId = TokenManager.getUserId(requireContext());

        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        ImageButton btnAddTask = view.findViewById(R.id.btn_add_task);

        // Add Task
        btnAddTask.setOnClickListener(v -> {
            AddTaskBottomSheet bottomSheet = new AddTaskBottomSheet();
            bottomSheet.setOnTaskAddedListener(() -> {
                taskViewModel.fetchTasks(userId);
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
                taskViewModel.updateTask(task); // cập nhật DB
                updateTaskOrder(task); // cập nhật UI
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
                    });
                }
            }
        });

        // ViewModel
        taskViewModel = new ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(TaskViewModel.class);

        // Lắng nghe dữ liệu từ ViewModel
        taskViewModel.getTaskList().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                allTasks.clear();
                allTasks.addAll(tasks);
                filterTasksByDate(selectedDate); // cập nhật theo ngày hiện tại
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

        return view;
    }

    private void filterTasksByDate(LocalDate selectedDate) {
        String selectedDateStr = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        filteredTasks.clear();

        List<Task> uncompletedTasks = new ArrayList<>();
        List<Task> completedTasks = new ArrayList<>();

        for (Task task : allTasks) {
            if (task.getDueDate() != null && task.getDueDate().equals(selectedDateStr)) {
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

    private void updateTaskOrder(Task task) {
        filteredTasks.remove(task);
        if (task.isCompleted()) {
            // Nếu hoàn thành → thêm cuối
            filteredTasks.add(task);
        } else {
            // Nếu là task chưa hoàn thành → thêm vào cuối nhóm "chưa hoàn thành"
            int insertIndex = 0;
            for (int i = 0; i < filteredTasks.size(); i++) {
                if (filteredTasks.get(i).isCompleted()) {
                    insertIndex = i;
                    break;
                }
                insertIndex = i + 1;
            }
            filteredTasks.add(insertIndex, task);
        }
        taskAdapter.notifyDataSetChanged();
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
