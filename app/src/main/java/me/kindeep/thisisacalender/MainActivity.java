package me.kindeep.thisisacalender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    final int PICK_CONTACT_REQUEST = 1;
    final int REQUEST_READ_CONTACTS = 2;
    RecyclerView meetingsRecyclerList;
    Data data;
    CalendarView calender;
    TextView currDate;
    Date currentDateOnCalender = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        meetingsRecyclerList = findViewById(R.id.recycler);
        final DoubleLayoutManager layoutManager = new DoubleLayoutManager(this);
        meetingsRecyclerList.setLayoutManager(layoutManager);

        data = new Data(MainActivity.this);

        meetingsRecyclerList.setAdapter(new MeetingsAdapter(data, this));

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addMeetingIntent = new Intent(MainActivity.this, MeetingActivity.class);
                // long date
                Log.e("DATE", "Sending: " + new Date(calender.getDate()).toString());
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
                currentDateOnCalender = date;
            }
        });
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
            default:

        }
        meetingsRecyclerList.getAdapter().notifyDataSetChanged();

        return true;
    }

    public void postponeSelectedDate() {
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
        Log.e("REQUEST", requestCode + " " + requestCode);
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) { //User picked a contact; didn't cancel out
                // The Intent's data Uri identifies which contact was selected.
                Toast.makeText(this, data.toString(), Toast.LENGTH_LONG).show();

                String[] projection = {
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                };

                Cursor cursor = getContentResolver().query(data.getData(), projection,
                        null, null, null);

                Log.e("CONTACTS", "All rows: " + Arrays.asList(cursor.getColumnNames()));

                cursor.moveToFirst();

                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                String number = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                );
                String name = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                );
            } else {

            }

        }
        if (requestCode == MeetingActivity.EDIT_MEETING) {
            meetingsRecyclerList.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
