package me.kindeep.thisisacalender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
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

import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class MeetingActivity extends AppCompatActivity {

    Meeting meeting;
    TextView startDate;
    TextView endDate;
    EditText titleEditText;
    Button editMeetingBtn;
    TextView contactName;
    TextView contactPhone;
    Button doneBtn;
    Data data;

    public static final int EDIT_MEETING = 235;

    static final int SET_START = 1;
    static final int SET_END = 2;
    static final int PICK_CONTACT_REQUEST = 3;

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        meeting = (Meeting) savedInstanceState.getSerializable("meeting");
        super.onRestoreInstanceState(savedInstanceState);
        updateMeetingViews();
        updateContactViews();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("meeting", meeting);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        data = new Data(this);

        String meetingId = null;
        Intent intent = getIntent();
        Date meetingStartDate = new Date();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                meetingId = extras.getString("meeting_id");

                if (extras.containsKey("date")) {
                    // Add Meeting intent
                    setTitle(R.string.title_add_meeting_activity);
                    meetingStartDate = new Date(extras.getLong("date"));
                    Log.e("START", meetingStartDate + "");
                    meeting = new Meeting(meetingStartDate);
                    findViewById(R.id.delete_meeting_btn).setVisibility(View.INVISIBLE);
                } else {
                    // Edit meeting intent
                    setTitle(R.string.title_edit_meeting_activity);
                    if (meetingId != null) {
                        Meeting tempMeeting = data.getMeetingById(meetingId);
                        if (tempMeeting != null) {
                            meeting = tempMeeting;
                        }
                    }
                }
            }
        }


        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        titleEditText = findViewById(R.id.meeting_title);
        editMeetingBtn = findViewById(R.id.edit_meeting_title_btn);
        contactName = findViewById(R.id.meeting_contact_name);
        contactPhone = findViewById(R.id.meeting_contact_number);
        doneBtn = findViewById(R.id.done_btn);

        editMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                meeting.setTitle(titleEditText.getText().toString());
                titleEditText.setEnabled(!titleEditText.isEnabled());
                if (titleEditText.isEnabled()) {
                    titleEditText.requestFocus();
                    titleEditText.setFocusableInTouchMode(true);
                    editMeetingBtn.setText(R.string.title_edit_done);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(titleEditText, InputMethodManager.SHOW_FORCED);
                } else {
                    editMeetingBtn.setText(R.string.title_edit);
                }
            }
        });


        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.addOrUpdateMeeting(meeting);
                Log.e("MEETINGS", "Add meeting: " + meeting);
                Log.e("MEETINGS", data.getMeetingList().toString());
                finishActivity(EDIT_MEETING);
                finish();
            }
        });

        updateMeetingViews();
        updateContactViews();
    }

    public void setEndDate(View v) {
        promptDate(SET_END);
    }

    public void setStartDate(View v) {
        promptDate(SET_START);
    }


    public void pickContactIntent(View v) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    public void openContact(View v) {
        // Open contact using the default contacts viewer
        if (meeting.getContactId() != null) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(meeting.getContactId()));
                intent.setData(uri);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Something went wrong with opening the contact.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No contact associated with event!", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Displays dialogs to select date and time, meeting is updated with the selected date/time
     *
     * @param type SET_START - Set meeting start time
     *             SET_END - Set meeting end time
     */
    void promptDate(final int type) {

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
                    new TimePickerDialog(MeetingActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minuteOfHOur) {
                            Calendar cal = new GregorianCalendar(year, month, day, hourOfDay, minuteOfHOur);

                            Date date = new Date(cal.getTimeInMillis());
                            if (type == SET_START)
                                meeting.setStart(date);
                            if (type == SET_END)
                                meeting.setEnd(date);
                            updateMeetingViews();
                        }
                    }, 0, 0, true).show();
                } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                }
                updateMeetingViews();
            }
        };
        Calendar cal = Calendar.getInstance();

        if (type == SET_START) {
            cal.setTime(meeting.getStart());
        } else if (type == SET_END) {
            cal.setTime(meeting.getEnd());
        }
        datePickerDialog.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    void updateMeetingViews() {
        startDate.setText(meeting.getStart().toString());
        endDate.setText(meeting.getEnd().toString());
        titleEditText.setText(meeting.getTitle());
    }

    void updateContactViews() {
        if (this.meeting.getContactId() != null) {
            Contact contact = Data.getContactById(MeetingActivity.this, this.meeting.getContactId());
            if (contact != null) {
                contactPhone.setText(contact.getPhone());
                contactName.setText(contact.getName());
            }
        }

        if (meeting.getContactId() == null) {
            findViewById(R.id.add_contact).setVisibility(View.VISIBLE);
            findViewById(R.id.contact_parent).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.add_contact).setVisibility(View.INVISIBLE);
            findViewById(R.id.contact_parent).setVisibility(View.VISIBLE);

        }
    }

    public void deleteContact(View v) {
        meeting.setContactId(null);
        updateContactViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) { //User picked a contact; didn't cancel out
                // The Intent's data Uri identifies which contact was selected.

                String[] projection = {
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                };

                Cursor cursor = getContentResolver().query(data.getData(), projection,
                        null, null, null);

                try {
                    cursor.moveToFirst();
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    this.meeting.setContactId(id);
                } catch (NullPointerException e) {
                    Log.e("PICK_CONTACT_REQUEST", "Error with picking");
                }
            }
            updateContactViews();
        }
    }

    public void deleteMeeting(View v) {
        data.deleteMeeting(meeting);
        finishActivity(EDIT_MEETING);
        finish();
    }

    public void cancelEditMeeting(View v) {
        finishActivity(EDIT_MEETING);
        finish();
    }

}
