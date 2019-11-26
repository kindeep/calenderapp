package me.kindeep.thisisacalender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    RecyclerView meetingsRecyclerList;
    Data data;
    CalendarView calender;
    TextView currDate;
    private Date currentDateOnCalender = new Date();
    MeetingsAdapter meetingsAdapter;
    DoubleLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data = new Data(MainActivity.this);

        meetingsRecyclerList = findViewById(R.id.recycler);
        
        layoutManager = new DoubleLayoutManager(this);
        meetingsRecyclerList.setLayoutManager(layoutManager);
        layoutManager.setTargetStartPosition(data.getIndexForDate(currentDateOnCalender), 0);


        meetingsAdapter = new MeetingsAdapter(data, this);
        meetingsRecyclerList.setAdapter(meetingsAdapter);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addMeetingIntent = new Intent(MainActivity.this, MeetingActivity.class);
                Log.e("START", "Curr date on cal "+ currentDateOnCalender + "");
                addMeetingIntent.putExtra("date", currentDateOnCalender.getTime());
                startActivityForResult(addMeetingIntent, MeetingActivity.EDIT_MEETING);
            }
        });

        meetingsRecyclerList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstPos = layoutManager.findLastCompletelyVisibleItemPosition();
                if (firstPos >= 0) {
                    Date startDate = data.getMeetingList().get(firstPos).getStart();
                    calender.setDate(startDate.getTime());
                    updateStoredDateOnCalender(startDate);
                    setCalenderListTime(startDate);
                }

            }
        });

        calender = findViewById(R.id.calendarView);
        currDate = findViewById(R.id.curr_date_display);

        currDate.setText(DateFormat.getDateInstance(DateFormat.FULL).format(new Date(calender.getDate())));

        calender.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day, 0, 0);
                Date date = cal.getTime();
                int scrollToIndex = data.getIndexForDate(date);
                layoutManager.scrollToPositionWithOffset(scrollToIndex, 0);
                setCalenderListTime(date);
                currDate.setText(DateFormat.getDateInstance(DateFormat.FULL).format(date));
                updateStoredDateOnCalender(date);
            }
        });
    }

    public void updateStoredDateOnCalender(Date date) {
        currentDateOnCalender = Data.justGetDate(date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all:
                data.deleteAll();
                break;
            case R.id.delete_today:
                data.deleteMeetingsOnDate(currentDateOnCalender);
                break;
            case R.id.postpone_today:
                postponeSelectedDate();
                break;
            case R.id.manage_contacts:
                startActivity(new Intent(MainActivity.this, ContactsActivity.class));
            default:

        }
        meetingsAdapter.notifyDataSetChanged();
        return true;
    }

    public void postponeSelectedDate() {
        Log.e("POSTPONE", data.getMeetingsOnDate(currentDateOnCalender) + "");
        for (Meeting meeting : data.getMeetingsOnDate(currentDateOnCalender)) {
            meeting.postponeMeeting();
            data.addOrUpdateMeeting(meeting);
        }
    }


    void setCalenderListTime(Date date) {
        currDate.setText(DateFormat.getDateInstance(DateFormat.FULL).format(date));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MeetingActivity.EDIT_MEETING) {
            meetingsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
