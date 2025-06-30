package edu.neu.weather.activity.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import edu.neu.weather.R;
import edu.neu.weather.activity.SQLite.DiaryDBHelper;

public class DayDetailAdapter extends BaseAdapter {
    private Context context;
    private List<Object> items;
    private DiaryDBHelper dbHelper;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public DayDetailAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
        this.dbHelper = new DiaryDBHelper(context);
    }


    public void updateData(List<Object> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_day_detail, parent, false);
            holder = new ViewHolder();
            holder.tvTitle = convertView.findViewById(R.id.tvItemTitle);
            holder.tvContent = convertView.findViewById(R.id.tvItemContent);
            holder.tvTime = convertView.findViewById(R.id.tvItemTime);
            holder.btnDelete = convertView.findViewById(R.id.btnDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Object item = items.get(position);
        if (item instanceof DiaryItem) {
            DiaryItem diary = (DiaryItem) item;
            holder.tvTitle.setText("日记");
            holder.tvContent.setText(diary.getContent());
            holder.tvTime.setVisibility(View.GONE);
        } else if (item instanceof ScheduleItem) {
            ScheduleItem schedule = (ScheduleItem) item;
            holder.tvTitle.setText(schedule.getTitle());
            holder.tvContent.setText(schedule.getContent());
            if (schedule.hasReminder() && schedule.getReminderTime() != null) {
                holder.tvTime.setText("提醒时间: " + timeFormat.format(schedule.getReminderTime()));
                holder.tvTime.setVisibility(View.VISIBLE);
            } else {
                holder.tvTime.setVisibility(View.GONE);
            }
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (item instanceof DiaryItem) {
                dbHelper.deleteDiary(((DiaryItem) item).getId());
            } else if (item instanceof ScheduleItem) {
                dbHelper.deleteSchedule(((ScheduleItem) item).getId());
            }
            items.remove(position);
            notifyDataSetChanged();
        });

        return convertView;
    }

    static class ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        Button btnDelete;
    }
}
