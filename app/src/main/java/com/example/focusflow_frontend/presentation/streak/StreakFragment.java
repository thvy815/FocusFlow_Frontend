package com.example.focusflow_frontend.presentation.streak;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.focusflow_frontend.R;

import com.example.focusflow_frontend.data.model.Streak;
import com.example.focusflow_frontend.data.model.Task;
import com.example.focusflow_frontend.data.viewmodel.StreakViewModel;
import com.example.focusflow_frontend.data.viewmodel.TaskViewModel;
import com.example.focusflow_frontend.utils.TokenManager;
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
    private TextView streakCountTextView, maxStreakTextView;
    private CalendarView calendarView;
    private ImageButton buttonNextFragment;
    private final Set<LocalDate> streakDates = new HashSet<>();
    private StreakViewModel viewModel;
    private int userId;
    private final LocalDate todayDate = LocalDate.now();

    public StreakFragment() { /* Required empty constructor */ }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_streak, container, false);

        userId = TokenManager.getUserId(requireContext());

        // Bind views
        fireIcon = view.findViewById(R.id.fire_icon);
        streakCountTextView = view.findViewById(R.id.streak_count);
        maxStreakTextView = view.findViewById(R.id.max_streak);
        calendarView = view.findViewById(R.id.calendarView);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(StreakViewModel.class);

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
                            @NonNull CalendarMonth month) {
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
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
                container.textView.setText(String.valueOf(day.getDate().getDayOfMonth())
                );
                if (day.getPosition() == DayPosition.MonthDate) {
                    if (streakDates.contains(day.getDate())) {
                        container.textView.setBackgroundResource(R.drawable.circle_task); // ngày có streak
                        container.textView.setTextColor(Color.BLACK);
                    } else {
                        container.textView.setTextColor(Color.BLACK);
                    }
                } else {
                    container.textView.setText("");
                }
            }
        });

        calendarView.scrollToDate(todayDate);

        viewModel.getStreakByUser(userId);

        viewModel.getStreakLive().observe(getViewLifecycleOwner(), streak -> {
            if (streak != null) {
                streakCountTextView.setText(String.valueOf(streak.getCurrentStreak()));
                maxStreakTextView.setText(String.valueOf(streak.getMaxStreak()));

                // Nếu muốn hiển thị validDates (làm đậm ngày có streak):
                streakDates.clear();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (String dateStr : streak.getValidDates()) {
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    streakDates.add(date);
                }
                calendarView.notifyCalendarChanged();
            } else {
                streakCountTextView.setText("0");
                maxStreakTextView.setText("0");
            }
        });

        return view;
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
