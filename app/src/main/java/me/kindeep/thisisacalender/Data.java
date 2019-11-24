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
import java.util.Date;
import java.util.List;

public class Data extends SQLiteOpenHelper {
    public static final int REQUEST_READ_CONTACTS = 1;
    public static final int DATABASE_VERSION = 2;
    public static final String DB_NAME = "calenderisthis";
    public static final String DB_TABLE = "meetings";
    public static final int DB_VERSION = 1;
    private static final String CREATE_TABLE = "CREATE TABLE " + DB_TABLE +
            " (meeting_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT, start_date INTEGER, end_date INTEGER, contact_id TEXT);";


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

    List<Meeting> getMeetingList() {
        String[] fields = new String[]{"meeting_id", "title", "start_date", "end_date", "contact_id"};
        SQLiteDatabase datareader = getReadableDatabase();
        Cursor cursor = datareader.query(DB_TABLE, fields,
                null, null, null, null, "start_date");
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
        newMeeting.put("title", meeting.getTitle());
        newMeeting.put("start_date", meeting.getStart().getTime());
        newMeeting.put("end_date", meeting.getStart().getTime());
        newMeeting.put("contact_id", meeting.getContactId());

        if (meeting.getMeetingId() == null) {
            dataChanger.insert(DB_TABLE, null, newMeeting);

        } else {
            dataChanger.update(DB_TABLE, newMeeting, "meeting_id=" + meeting.getMeetingId(), null);
        }
        dataChanger.close();
    }

    void removeMeeting(int meetingId) {

    }

    public static Contact getContactById(Activity activity, String id) {
        try {
            Context context = activity.getApplicationContext();
            // Check permissions
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {

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
            }
            return new Contact("null", "null", "null");
        } catch (Exception e) {
            Log.e("DATA", e.getStackTrace().toString());
            return null;
        }
    }
}
