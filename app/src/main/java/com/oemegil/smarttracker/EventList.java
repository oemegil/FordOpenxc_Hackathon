package com.oemegil.smarttracker;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.io.Resources;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by oguzcan.emegil on 5.11.2017.
 */

public class EventList extends ArrayAdapter<EventDto> {
    private Context context;
    private List<EventDto> rentalProperties;

    public EventList(Context context, int resource, List<EventDto> objects) {
        super(context,resource, objects);

        this.context = context;
        this.rentalProperties = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        //get the property we are displaying
        EventDto property = rentalProperties.get(position);

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.tracker_layout, null);

        ImageView eventImage = (ImageView) view.findViewById(R.id.eventImage);
        TextView eventDetail = (TextView) view.findViewById(R.id.eventDetail);
        TextView eventDateTime = (TextView) view.findViewById(R.id.eventDatetime);


        if(property.getEventType()=="OverTaking")
        {
            eventImage.setImageResource(R.drawable.overtaking);
        }
        else
        {
            eventImage.setImageResource(R.drawable.turnright);
        }
        eventDetail.setText(property.getDetail());

        eventDateTime.setText(property.getEventDateTime()+"");
        return view;
    }

    public void add(String detail,String EventType)
    {
        EventDto newEvent=new EventDto();
        newEvent.Detail=detail;
        newEvent.EventType=EventType;
        newEvent.EventDateTime=Calendar.getInstance().getTime();
        rentalProperties.add(newEvent);
        notifyDataSetChanged();
    }
}
