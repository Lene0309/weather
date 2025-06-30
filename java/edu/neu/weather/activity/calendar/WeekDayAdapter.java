package edu.neu.weather.activity.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import edu.neu.weather.R;

public class WeekDayAdapter extends BaseAdapter {
    private Context context;
    private List<WeekDayItem> weekDays;

    public WeekDayAdapter(Context context, List<WeekDayItem> weekDays) {
        this.context = context;
        this.weekDays = weekDays;
    }

    @Override
    public int getCount() {
        return weekDays.size();
    }

    @Override
    public Object getItem(int position) {
        return weekDays.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_week_day, parent, false);
            holder = new ViewHolder();
            holder.tvDay = convertView.findViewById(R.id.tvDay);
            holder.tvLunar = convertView.findViewById(R.id.tvLunar);
            holder.tvHoliday = convertView.findViewById(R.id.tvHoliday);
            holder.tvSolarTerm = convertView.findViewById(R.id.tvSolarTerm);
            holder.dotIndicator = convertView.findViewById(R.id.indicatorDot);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        WeekDayItem dayItem = weekDays.get(position);

        SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d", Locale.getDefault());

        String dayText = dayFormat.format(dayItem.getDate()) + "\n" + dateFormat.format(dayItem.getDate());
        holder.tvDay.setText(dayText);
        holder.tvLunar.setText(dayItem.getLunarDate().getLunarDayName());

        if (dayItem.getHoliday() != null && !dayItem.getHoliday().isEmpty()) {
            holder.tvHoliday.setText(dayItem.getHoliday());
            holder.tvHoliday.setVisibility(View.VISIBLE);
        } else {
            holder.tvHoliday.setVisibility(View.GONE);
        }

        if (dayItem.getSolarTerm() != null && !dayItem.getSolarTerm().isEmpty()) {
            holder.tvSolarTerm.setText(dayItem.getSolarTerm());
            holder.tvSolarTerm.setVisibility(View.VISIBLE);
        } else {
            holder.tvSolarTerm.setVisibility(View.GONE);
        }

        boolean hasContent = (dayItem.getDiary() != null && !dayItem.getDiary().isEmpty()) ||
                !dayItem.getSchedules().isEmpty();
        holder.dotIndicator.setVisibility(hasContent ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    static class ViewHolder {
        TextView tvDay;
        TextView tvLunar;
        TextView tvHoliday;
        TextView tvSolarTerm;
        View dotIndicator;
    }

    public Calendar getDayAtPosition(int position) {
        if (position >= 0 && position < weekDays.size()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(weekDays.get(position).getDate());
            return calendar;
        }
        return null;
    }
}
