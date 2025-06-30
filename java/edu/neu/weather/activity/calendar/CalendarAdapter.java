package edu.neu.weather.activity.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.neu.weather.R;
import edu.neu.weather.activity.SQLite.DiaryDBHelper;
import edu.neu.weather.activity.utils.LunarDate;
import edu.neu.weather.activity.utils.LunarUtils;

public class CalendarAdapter extends BaseAdapter {
    private Context context;
    private Calendar monthCalendar;
    private Calendar today;
    private DiaryDBHelper dbHelper;
    private List<Calendar> days = new ArrayList<>();
    private Map<String, List<Object>> dayItemsMap = new HashMap<>();

    public CalendarAdapter(Context context, Calendar monthCalendar, Calendar today) {
        this.context = context;
        this.monthCalendar = (Calendar) monthCalendar.clone();
        this.today = (Calendar) today.clone();
        this.dbHelper = new DiaryDBHelper(context);
        calculateDays();
        loadDataForMonth();
    }

    private void calculateDays() {
        days.clear();

        Calendar firstDay = (Calendar) monthCalendar.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);

        int monthDays = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
        int prevMonthDays = (firstDayOfWeek + 5) % 7; // Adjust for Monday as first day

        Calendar prevMonth = (Calendar) firstDay.clone();
        prevMonth.add(Calendar.MONTH, -1);
        int prevMonthDayCount = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = prevMonthDays; i > 0; i--) {
            Calendar day = (Calendar) prevMonth.clone();
            day.set(Calendar.DAY_OF_MONTH, prevMonthDayCount - i + 1);
            days.add(day);
        }

        for (int i = 0; i < monthDays; i++) {
            Calendar day = (Calendar) firstDay.clone();
            day.set(Calendar.DAY_OF_MONTH, i + 1);
            days.add(day);
        }

        int nextMonthDays = 42 - days.size();
        if (nextMonthDays > 0) {
            Calendar nextMonth = (Calendar) firstDay.clone();
            nextMonth.add(Calendar.MONTH, 1);

            for (int i = 0; i < nextMonthDays; i++) {
                Calendar day = (Calendar) nextMonth.clone();
                day.set(Calendar.DAY_OF_MONTH, i + 1);
                days.add(day);
            }
        }
    }

    private void loadDataForMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            for (Calendar day : days) {
                String dayKey = sdf.format(day.getTime());
                List<Object> items = new ArrayList<>();

                String diary = dbHelper.getDiaryForDate(day.getTime());
                if (diary != null) {
                    items.add(new DiaryItem(-1, "日记", diary, day.getTime()));
                }

                List<ScheduleItem> schedules = dbHelper.getSchedulesForDate(day.getTime());
                items.addAll(schedules);

                dayItemsMap.put(dayKey, items);
            }
        } finally {
            dbHelper.close();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        }

        Calendar day = days.get(position);
        String dayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(day.getTime());

        TextView tvDay = convertView.findViewById(R.id.dayText);
        TextView tvLunar = convertView.findViewById(R.id.lunarText);
        View indicatorDot = convertView.findViewById(R.id.indicatorDot);

        // Set day number
        tvDay.setText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)));

        // Set lunar date
        LunarDate lunarDate = LunarUtils.getLunarDate(day);
        tvLunar.setText(lunarDate.getLunarDayName());

        // Highlight today
        if (isSameDay(day, today)) {
            convertView.setBackgroundResource(R.drawable.today_bg);
            tvDay.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvLunar.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            convertView.setBackgroundResource(0);
            tvDay.setTextColor(ContextCompat.getColor(context, R.color.black));
            tvLunar.setTextColor(ContextCompat.getColor(context, R.color.gray));
        }

        // Set colors for days not in current month
        if (day.get(Calendar.MONTH) != monthCalendar.get(Calendar.MONTH)) {
            tvDay.setTextColor(ContextCompat.getColor(context, R.color.grayLight));
            tvLunar.setTextColor(ContextCompat.getColor(context, R.color.grayLight));
        }

        // Show indicator if there are entries
        boolean hasItems = dayItemsMap.containsKey(dayKey) && !dayItemsMap.get(dayKey).isEmpty();
        indicatorDot.setVisibility(hasItems ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    public void updateCalendar(Calendar newCalendar) {
        this.monthCalendar = (Calendar) newCalendar.clone();
        calculateDays();
        loadDataForMonth();
        notifyDataSetChanged();
    }

    public Calendar getDayAtPosition(int position) {
        if (position >= 0 && position < days.size()) {
            return days.get(position);
        }
        return null;
    }

    @Override public int getCount() { return days.size(); }
    @Override public Object getItem(int position) { return days.get(position); }
    @Override public long getItemId(int position) { return position; }
}
