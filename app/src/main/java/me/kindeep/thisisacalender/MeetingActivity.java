package me.kindeep.thisisacalender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.content.Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;


public class MeetingActivity extends AppCompatActivity {

    Meeting meeting = new Meeting();
    TextView startDate;
    TextView endDate;
    EditText titleEditText;
    Button editMeetingBtn;
    TextView contactName;
    TextView contactPhone;
    Button doneBtn;
    Data data;

    static final int SET_START = 1;
    static final int SET_END = 2;
    static final int PICK_CONTACT_REQUEST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        data = new Data(this);

        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        titleEditText = findViewById(R.id.meeting_title);
        editMeetingBtn = findViewById(R.id.edit_meeting_title_btn);
        contactName = findViewById(R.id.meeting_contact_name);
        contactPhone = findViewById(R.id.meeting_contact_number);
        doneBtn = findViewById(R.id.done_btn);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptDate(SET_START);
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDate(SET_END);
            }
        });

        editMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                titleEditText.setEnabled(!titleEditText.isEnabled());
                if (titleEditText.isEnabled()) {
                    titleEditText.requestFocus();
                    titleEditText.setFocusableInTouchMode(true);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(titleEditText, InputMethodManager.SHOW_FORCED);
                }
            }
        });

        findViewById(R.id.edit_contact_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.addOrUpdateMeeting(meeting);
                Log.e("MEETINGS", data.getMeetingList().toString());
                finish();
            }
        });

        updateMeetingViews();
        updateContactViews();
    }

    void promptDate(final int type) {
        Toast.makeText(this, "Set the goodym dyt", Toast.LENGTH_SHORT).show();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this) {
            int yearOfCentury = 0;
            int monthOfDay = 0;
            int dayOfMonth = 0;

            @Override
            public void onDateChanged(@NonNull DatePicker view, int yearOfCentury, int monthOfDay, int dayOfMonth) {
                this.yearOfCentury = yearOfCentury;
                this.monthOfDay = monthOfDay;
                this.dayOfMonth = dayOfMonth;
            }

            @Override
            public void onClick(@NonNull DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    super.onClick(dialog, which);
                    final int year = yearOfCentury;
                    final int month = monthOfDay;
                    final int day = dayOfMonth;
                    Toast.makeText(MeetingActivity.this, "YESSS" + which, Toast.LENGTH_SHORT).show();
                    new TimePickerDialog(MeetingActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minuteOfHOur) {
                            Calendar cal = new GregorianCalendar(year, month, day, hourOfDay, minuteOfHOur);

                            Date date = new Date(cal.getTimeInMillis());
                            Log.e("DATE", "Set date to : " + Arrays.asList(year, month, day, hourOfDay, minuteOfHOur));
                            if (type == SET_START)
                                meeting.setStart(date);
                            if (type == SET_END)
                                meeting.setEnd(date);
                            updateMeetingViews();
                        }
                    }, 0, 0, true).show();
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                    Toast.makeText(MeetingActivity.this, "BYEEEEE", Toast.LENGTH_SHORT).show();
                }
                updateMeetingViews();
            }
        };

        datePickerDialog.show();
    }

    void updateMeetingViews() {
        startDate.setText(meeting.getStart().toString());
        endDate.setText(meeting.getEnd().toString());
        titleEditText.setText(meeting.getTitle());
    }

    void updateContactViews() {
        Contact contact = getContactById(this.meeting.getContactId());
        if(contact != null) {
            contactPhone.setText(contact.getPhone());
            contactName.setText(contact.getName());
        }
    }

    public Contact getContactById(String id) {
        return Data.getContactById(MeetingActivity.this, id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) { //User picked a contact; didn't cancel out
                // The Intent's data Uri identifies which contact was selected.
                Toast.makeText(this, data.toString(), Toast.LENGTH_LONG).show();

                String[] projection = {
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                };

                Cursor cursor = getContentResolver().query(data.getData(), projection,
                        null, null, null);

                Log.e("CONTACTS", "All rows: " + Arrays.asList(cursor.getColumnNames()));

                cursor.moveToFirst();

                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                this.meeting.setContactId(id);
            } else {
            }
            updateContactViews();
        }
    }

    public void deleteMeeting(View v) {

    }
}
