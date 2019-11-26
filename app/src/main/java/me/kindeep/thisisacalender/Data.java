package me.kindeep.thisisacalender;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.security.auth.callback.Callback;

public class Data extends SQLiteOpenHelper {
    public static final int REQUEST_READ_CONTACTS = 1;
    public static final int DATABASE_VERSION = 2;
    public static final String DB_NAME = "calenderisthis";
    public static final String DB_TABLE = "meetings";
    public static final String MEETING_ID = "meeting_id";
    public static final String MEETING_START_DATE = "start_date";
    public static final String MEETING_END_DATE = "end_date";
    public static final String MEETING_TITLE = "title";
    public static final String MEETING_CONTACT_ID = "contact_id";
    private static final String CREATE_TABLE = "CREATE TABLE " + DB_TABLE + " (" +
            MEETING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            MEETING_TITLE + " TEXT, " +
            MEETING_START_DATE + " INTEGER, " +
            MEETING_END_DATE + " INTEGER, " +
            MEETING_CONTACT_ID + " TEXT);";


    Data(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //How to migrate or reconstruct data from old version to new on upgrade
    }

    Meeting getMeetingById(String meetingID) {
        for (Meeting meeting : getMeetingList()) {
            if (meeting.getMeetingId().equals(meetingID)) {
                return meeting;
            }
        }
        return null;
    }

    // TODO: Would help to implement caching for this method
    List<Meeting> getMeetingList() {
        String[] fields = new String[]{MEETING_ID, MEETING_TITLE, MEETING_START_DATE, MEETING_END_DATE, MEETING_CONTACT_ID};
        SQLiteDatabase datareader = getReadableDatabase();
        Cursor cursor = datareader.query(DB_TABLE, fields,
                null, null, null, null, MEETING_START_DATE);
        cursor.moveToFirst();
        cursor.moveToNext();

        List<Meeting> result = new ArrayList<>();

        while (!cursor.isAfterLast()) {
            String meetingId = cursor.getString(0);
            String title = cursor.getString(1);
            Date start = new Date(cursor.getLong(2));
            Date end = new Date(cursor.getLong(3));
            String contactId = cursor.getString(4);
            result.add(new Meeting(meetingId, title, start, end, contactId));
            cursor.moveToNext();
        }

        cursor.close();

        return result;
    }

    void addOrUpdateMeeting(Meeting meeting) {
        SQLiteDatabase dataChanger = getWritableDatabase();
        ContentValues newMeeting = new ContentValues();
        newMeeting.put(MEETING_TITLE, meeting.getTitle());
        newMeeting.put(MEETING_START_DATE, meeting.getStart().getTime());
        newMeeting.put(MEETING_END_DATE, meeting.getStart().getTime());
        newMeeting.put(MEETING_CONTACT_ID, meeting.getContactId());

        if (meeting.getMeetingId() == null) {
            dataChanger.insert(DB_TABLE, null, newMeeting);

        } else {
            Log.e("DATA", "MEETING ALREADY EXISTS, UPDATE");
            dataChanger.update(DB_TABLE, newMeeting, MEETING_ID + "=" + meeting.getMeetingId(), null);
        }
        dataChanger.close();
    }

    public void removeMeeting(Meeting name) {
        SQLiteDatabase dataChanger = getWritableDatabase();
        dataChanger.delete(DB_TABLE, MEETING_ID + "=?", new String[]{name.getMeetingId()});
    }

    // TODO: convert to a query?
    public int getIndexForDate(Date date) {
        List<Meeting> meetings = getMeetingList();
        if (meetings.size() == 0) {
            return 0;
        }

        long time = date.getTime();

        for (int i = 0; i < meetings.size(); i++) {
            if (meetings.get(i).getStart().getTime() >= time) {
                return i;
            }
        }
        return meetings.size() - 1;
    }

    /**
     * Deletes all stored meetings
     */
    public void deleteAll() {
        for (Meeting meeting : getMeetingList()) {
            removeMeeting(meeting);
        }
    }

    /**
     * Delete all meetings on the day of the provided date
     *
     * @param date
     */
    public void deleteMeetingsOnDate(Date date) {
        for (Meeting meeting : getMeetingsOnDate(date)) {
            removeMeeting(meeting);
        }
    }

    /**
     *
     * @param date
     * @return
     */
    public List<Meeting> getMeetingsOnDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.set(Calendar.HOUR, -12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date startDate = cal.getTime();

        cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 1);

        Date endDate = cal.getTime();

        List<Meeting> result = new ArrayList<>();
        for (Meeting meeting : getMeetingList()) {
            if (meeting.getStart().getTime() <= endDate.getTime() && meeting.getStart().getTime() >= startDate.getTime()) {
                result.add(meeting);
            }
        }

        return result;
    }

    /**
     * Tries to find a contact with the provided id stored in the phone's contacts, prompts for
     * permission if it does not already have permission to access contacts.
     *
     * In case it does not have permission at time of calling, returns null.
     *
     * Also returns null if nothing found.
     * @param activity
     * @param id
     * @return
     */
    public static Contact getContactById(Activity activity, String id) {
        try {
            Context context = activity.getApplicationContext();
            // Check permissions
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                // Has permissions.

                String[] needed = {
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                };

                Cursor cursor = context.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        needed, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "= ?",
                        new String[]{id}, null);

                cursor.moveToFirst();

                String name = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                );

                String number = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                );

                return new Contact(id, name, number);
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_READ_CONTACTS
                );
                return null;
            }
        } catch (Exception e) {
            Log.e("DATA", e.getStackTrace().toString());
            return null;
        }
    }
}
