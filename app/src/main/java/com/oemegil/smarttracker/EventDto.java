package com.oemegil.smarttracker;


import java.util.Date;

/**
 * Created by oguzcan.emegil on 5.11.2017.
 */

public class EventDto {
    public String Detail;
    public String EventType;
    public Date EventDateTime;

    public String getDetail() { return Detail; }
    public String getEventType() { return EventType; }
    public Date getEventDateTime() { return EventDateTime; }
}
