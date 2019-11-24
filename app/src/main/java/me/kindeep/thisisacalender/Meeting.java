package me.kindeep.thisisacalender;

import androidx.annotation.NonNull;

import java.util.Date;

public class Meeting {
    private String meetingId;
    private Date start;
    private Date end;
    private String contactId;
    private String title = "Untitled";

    Meeting() {
        this(new Date(), new Date());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    Meeting(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    Meeting(String meetingId, String title, Date start, Date end, String contactId) {
        this.meetingId = meetingId;
        this.title = title;
        this.start = start;
        this.end = end;
        this.contactId = contactId;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactId() {
        return contactId;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    @NonNull
    @Override
    public String toString() {
        return "{ title: " + this.title + " meeting_id: " + this.meetingId + "}";
    }
}
