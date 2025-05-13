package com.example.focusflow_frontend.presentation.streak;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;

import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.presentation.mission.MissionFragment;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StreakFragment extends Fragment {

    private ImageView fireIcon;
    private TextView streakCountTextView;
    private CalendarView calendarView;
    private ImageButton buttonNextFragment;
    private final Set<LocalDate> streakDates = new HashSet<>();

    private StreakViewModel viewModel;
    private final LocalDate todayDate = LocalDate.now();

    public StreakFragment() { /* Required empty constructor */ }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_streak, container, false);

        // Bind views
        fireIcon = view.findViewById(R.id.fire_icon);
        streakCountTextView = view.findViewById(R.id.streak_count);
        calendarView = view.findViewById(R.id.calendarView);
        buttonNextFragment = view.findViewById(R.id.buttonNextFragment);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(StreakViewModel.class);

        // Observe LiveData
        viewModel.getDatesLive().observe(getViewLifecycleOwner(), this::renderDates);
        viewModel.getStreakCountLive().observe(getViewLifecycleOwner(), this::renderStreakCount);

        // Setup calendar boundaries and first day of week
        calendarView.setup(
                YearMonth.from(todayDate.minusMonths(1)),
                YearMonth.from(todayDate.plusMonths(1)),
                DayOfWeek.SUNDAY
        );

        // Header binder (Month + Year)
        calendarView.setMonthHeaderBinder(
                new MonthHeaderFooterBinder<MonthHeaderContainer>() {
                    @NonNull
                    @Override
                    public MonthHeaderContainer create(@NonNull View view) {
                        return new MonthHeaderContainer(view);
                    }

                    @Override
                    public void bind(
                            @NonNull MonthHeaderContainer container,
                            @NonNull CalendarMonth month
                    ) {
                        TextView header = container.getView().findViewById(R.id.monthHeaderText);
                        String text = month.getYearMonth()
                                .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
                        header.setText(
                                text.substring(0,1).toUpperCase() + text.substring(1)
                        );
                    }
                }
        );

        // Day binder
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View dayView) {
                return new DayViewContainer(dayView);
            }

            @Override
            public void bind(
                    @NonNull DayViewContainer container,
                    @NonNull CalendarDay day
            ) {
                container.textView.setText(
                        String.valueOf(day.getDate().getDayOfMonth())
                );
                if (day.getPosition() == DayPosition.MonthDate) {
                    if (streakDates.contains(day.getDate())) {
                        container.textView.setTextColor(Color.parseColor("#FF5722"));
                    } else {
                        container.textView.setTextColor(Color.BLACK);
                    }
                } else {
                    container.textView.setText("");
                }
            }
        });

        calendarView.scrollToDate(todayDate);

        // Dummy data for testing
        List<Task> dummyTasks = new ArrayList<>();
        dummyTasks.add(new Task(LocalDate.now().minusDays(4), true));
        dummyTasks.add(new Task(LocalDate.now().minusDays(3), true));
        dummyTasks.add(new Task(LocalDate.now().minusDays(2), false));
        dummyTasks.add(new Task(LocalDate.now().minusDays(1), true));
        dummyTasks.add(new Task(LocalDate.now(), false));
        syncTasks(dummyTasks);

        // Navigate to MissionFragment
        buttonNextFragment.setOnClickListener(v -> {
            FragmentTransaction tx = getParentFragmentManager().beginTransaction();
            tx.replace(R.id.fragment_container, new MissionFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    /**
     * Sync external task list (e.g. from Pomo module) to update calendar & streak count
     */
    public void syncTasks(List<Task> tasks) {
        viewModel.checkTasks(tasks);
    }

    private void renderDates(List<LocalDate> dates) {
        streakDates.clear();
        streakDates.addAll(dates);
        calendarView.notifyCalendarChanged();
    }

    private void renderStreakCount(Integer count) {
        if (count == null) return;
        streakCountTextView.setText(String.valueOf(count));
        if (count > 0) {
            fireIcon.setImageResource(R.drawable.ic_fire_color);
        } else {
            fireIcon.setImageResource(R.drawable.ic_fire_gray);
        }
    }

    public static class DayViewContainer extends ViewContainer {
        public final TextView textView;
        public DayViewContainer(@NonNull View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
        }
    }

    public static class MonthHeaderContainer extends ViewContainer {
        public MonthHeaderContainer(@NonNull View view) {
            super(view);
        }
    }
}
