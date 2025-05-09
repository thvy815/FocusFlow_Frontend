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

import com.example.focusflow_frontend.presentation.mission.MissionFragment;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;

import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;




import com.example.focusflow_frontend.R;
public class StreakFragment extends Fragment {

    private ImageView fireIcon;
    private TextView streakCountTextView;
    private CalendarView calendarView;
    private ImageButton buttonNextFragment;
    private int streakCount = 0;
    private final Set<LocalDate> streakDates = new HashSet<>();

    LocalDate todayDate = LocalDate.now();



    public StreakFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState) {

        // Inflate layout (XML file from your post)
        View view = inflater.inflate(R.layout.fragment_streak, container, false);


        fireIcon = view.findViewById(R.id.fire_icon);
        streakCountTextView = view.findViewById(R.id.streak_count);
        calendarView = view.findViewById(R.id.calendarView);
        buttonNextFragment = view.findViewById(R.id.buttonNextFragment);
        fireIcon.setImageResource(R.drawable.ic_fire_gray);

        calendarView.setup(
                YearMonth.from(todayDate.minusMonths(1)),
                YearMonth.from(todayDate.plusMonths(1)),
                DayOfWeek.SUNDAY
        );
        calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthHeaderContainer>() {
            @NonNull
            @Override
            public MonthHeaderContainer create(@NonNull View view) {
                return new MonthHeaderContainer(view);
            }

            @Override
            public void bind(@NonNull MonthHeaderContainer container, @NonNull com.kizitonwose.calendar.core.CalendarMonth month) {
                TextView textView = container.getView().findViewById(R.id.monthHeaderText);
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", new java.util.Locale("vi"));
                String formatted = month.getYearMonth().format(formatter);
                // Viết hoa chữ cái đầu
                String capitalized = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
                textView.setText(capitalized);
            }
        });

        calendarView.scrollToDate(todayDate);

        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View dayView) {
                return new DayViewContainer(dayView);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
                container.textView.setText(String.valueOf(day.getDate().getDayOfMonth()));

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


        buttonNextFragment.setOnClickListener(v -> {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            // Chuyển sang Fragment mới (ví dụ: NewFragment)
            MissionFragment newFragment = new MissionFragment();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);  // Thêm vào back stack nếu muốn quay lại Fragment cũ
            transaction.commit();
        });
        return view;
    }


    // Goi ham nay khi hoan thanh task
    public void onTaskCompleted() {
        if (!streakDates.contains(todayDate)) {
            streakCount++;
            streakCountTextView.setText(String.valueOf(streakCount));
            streakDates.add(todayDate);
            fireIcon.setImageResource(R.drawable.ic_fire_color);
            calendarView.notifyDateChanged(todayDate);
        }
    }

    // Container cho moi ngay trong calendar
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
