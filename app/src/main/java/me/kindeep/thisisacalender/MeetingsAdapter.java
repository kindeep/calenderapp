package me.kindeep.thisisacalender;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Date;

public class MeetingsAdapter extends RecyclerView.Adapter {

    Data data;
    Activity activity;

    MeetingsAdapter(Data data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.meeting_list_item, parent, false);
        return new MeetingHolder(v, activity);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MeetingHolder mHolder = (MeetingHolder) holder;

        final Meeting currMeeting = data.getMeetingList().get(position);

        Date today = Data.justGetDate(new Date());

        Log.e("disable", today + " " + currMeeting.getStart());

        if(currMeeting.getStart().getTime() < today.getTime()) {
            ((CardView)mHolder.parent).setCardBackgroundColor(mHolder.parent.getResources().getColor(R.color.listItemBackgroundDisable));

         } else {
            ((CardView)mHolder.parent).setCardBackgroundColor(mHolder.parent.getResources().getColor(R.color.listItemBackground));
        }

        mHolder.updateView(currMeeting.getMeetingId());

        mHolder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editMeetingIntent = new Intent(activity, MeetingActivity.class);
                editMeetingIntent.putExtra("meeting_id", currMeeting.getMeetingId());
                activity.startActivityForResult(editMeetingIntent, MeetingActivity.EDIT_MEETING);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.getMeetingList().size();
    }


    class MeetingHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView startTextView;
        TextView endTextView;
        Activity activity;
        String meetingId;
        View parent;

        public MeetingHolder(@NonNull View itemView, Activity activity) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.meeting_title);
            startTextView = itemView.findViewById(R.id.meeting_start);
            endTextView = itemView.findViewById(R.id.meeting_end);
            this.activity = activity;
            this.parent = itemView;
        }

        public void updateView(String meetingId) {
            this.meetingId = meetingId;
            if(meetingId != null) {
                Meeting meeting = data.getMeetingById(meetingId);
                titleTextView.setText(meeting.getTitle());
                startTextView.setText(meeting.getStart().toString() + " to");
                endTextView.setText(meeting.getEnd().toString());
            }
        }


        @Override
        public String toString() {
            return super.toString();
        }
    }
}
