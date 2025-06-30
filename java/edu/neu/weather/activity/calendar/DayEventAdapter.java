package edu.neu.weather.activity.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.neu.weather.R;

public class DayEventAdapter extends RecyclerView.Adapter<DayEventAdapter.EventViewHolder> {
    private List<ScheduleItem> schedules = new ArrayList<>();
    private String diary;

    public void setData(List<ScheduleItem> schedules, String diary) {
        this.schedules = schedules != null ? schedules : new ArrayList<>();
        this.diary = diary;
    }

    @Override
    public int getItemViewType(int position) {
        if (diary != null && position == 0) {
            return 0; // Diary type
        }
        return 1; // Schedule type
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(
                viewType == 0 ? R.layout.item_day_diary : R.layout.item_day_schedule,
                parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            holder.bindDiary(diary);
        } else {
            int schedulePos = diary != null ? position - 1 : position;
            holder.bindSchedule(schedules.get(schedulePos));
        }
    }

    @Override
    public int getItemCount() {
        return schedules.size() + (diary != null ? 1 : 0);
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvContent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
        }

        public void bindDiary(String diary) {
            tvTitle.setText("日记");
            tvContent.setText(diary);
        }

        public void bindSchedule(ScheduleItem schedule) {
            tvTitle.setText(schedule.getTitle());
            tvContent.setText(schedule.getContent());
        }
    }
}
