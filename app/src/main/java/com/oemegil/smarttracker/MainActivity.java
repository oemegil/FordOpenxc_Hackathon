package com.oemegil.smarttracker;
import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.openxc.VehicleManager;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.TransmissionGearPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TextView changeDetectedText;
    ImageView overtakingImage;
    ImageView turnImage;
    ListView eventList;
    List<Double> lastReceiveAngles=new ArrayList<Double>();
    private boolean isBlocked=false;
    private VehicleManager mVehicleManager;
    private double wheelValue=0;
    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();
    IntentFilter filter;
    boolean isOvertaking=false;
    boolean isTurn=false;
    ArrayList<EventDto> events=new ArrayList<EventDto>();
    EventList eventListAdapter;
    double steeringWheelThreasholdValue,acceleratorThreasholdValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        eventList=(ListView)findViewById(R.id.eventList);
        overtakingImage=(ImageView)findViewById(R.id.overtakingImage);
        turnImage=(ImageView)findViewById(R.id.turnImage);
        SharedPreferencesSettings(true);
        SetThreasholdSettings();
        overtakingImage.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                isOvertaking=!isOvertaking;
                SharedPreferencesSettings(false);
            }
        });

        turnImage.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                isTurn=!isTurn;
                SharedPreferencesSettings(false);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.editThreshold);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences  preferences = getSharedPreferences("com.oemegil.smarttracker", MODE_PRIVATE);
                SharedPreferences.Editor editor = getSharedPreferences("com.oemegil.smarttracker", MODE_PRIVATE).edit();
                EditThreshold dialog=new EditThreshold(MainActivity.this,preferences,editor);
                dialog.show();
                SetThreasholdSettings();

            }
        });
        eventListAdapter= new EventList(this,1, events);
        eventList.setAdapter(eventListAdapter);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();
            mVehicleManager.addListener(SteeringWheelAngle.class,mSteeringWheelAngleListener);
            mVehicleManager.addListener(AcceleratorPedalPosition.class,mAcceleratorPedalPosition);
            mVehicleManager.addListener(TransmissionGearPosition.class,mTransmissionGearPosition);
        }
        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mVehicleManager = null;
        }
    };

    SteeringWheelAngle.Listener mSteeringWheelAngleListener=new SteeringWheelAngle.Listener()
    {
        @Override
        public void receive(Measurement measurement) {
            final SteeringWheelAngle speed = (SteeringWheelAngle) measurement;
            wheelValue=speed.getValue().doubleValue();
            lastReceiveAngles.add(wheelValue);
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                }
            });
        }
    };

    AcceleratorPedalPosition.Listener mAcceleratorPedalPosition=new AcceleratorPedalPosition.Listener()
    {
        @Override
        public void receive(Measurement measurement) {
            final AcceleratorPedalPosition position = (AcceleratorPedalPosition) measurement;
            double pedalPosition=position.getValue().doubleValue();
            if(pedalPosition>=acceleratorThreasholdValue)
            {
                SpeedupDetected();
            }
        }
    };

    TransmissionGearPosition.Listener mTransmissionGearPosition=new TransmissionGearPosition.Listener()
    {

        @Override
        public void receive(Measurement measurement) {
            TransmissionGearPosition position=(TransmissionGearPosition)measurement;
            if(position!=null)
            {
                Log.d("Gear Position",position.toString());
            }
        }
    };

    @Override
    public void onPause() {

        super.onPause();
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if(mVehicleManager != null) {
            // Remember to remove your listeners, in typical Android
            // fashion.
            mVehicleManager.removeListener(EngineSpeed.class,
                    mSteeringWheelAngleListener);
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
        StartSteeringWheelTimer();
    }


    public void StartSteeringWheelTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 5000, 1000); //
    }
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if(isTurn)
                        {
                            List<Double> lastArray=lastReceiveAngles;
                            lastReceiveAngles=new ArrayList<Double>();
                            final double average=calculateAverage(lastArray);
                            if((average<0 && average<steeringWheelThreasholdValue*-1) || (average>0 && average>steeringWheelThreasholdValue))
                            {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        String directionString="";
                                        if(average>0) directionString="The car is turning right";
                                        else directionString="The car is turning left";
                                        eventListAdapter.add("Turn Detected","Turn");
                                        BlockNotification();
                                    }
                                });

                            }
                        }
                    }
                });
            }
        };
    }
    private void BlockNotification()
    {
        SharedPreferences  preferences = getSharedPreferences("com.oemegil.smarttracker", MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreferences("com.oemegil.smarttracker", MODE_PRIVATE).edit();
        if(!isBlocked)
        {
           isBlocked=true;
        }
isBlocked=false;
    }
    private double calculateAverage(List <Double> marks) {
        int sum = 0;
        if(!marks.isEmpty()) {
            for (Double mark : marks) {
                sum += mark;
            }
            return sum / marks.size();
        }
        return sum;
    }

    public void SpeedupDetected()
    {
        if(isOvertaking)
        {
            if(wheelValue<0 && wheelValue<-50)
            {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        eventListAdapter.add("Car overtaking detected","OverTaking");
                    }
                });
                BlockNotification();
            }
        }
    }

    private void SharedPreferencesSettings(boolean firstInit) {

        SharedPreferences  preferences = getSharedPreferences("com.oemegil.smarttracker", MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreferences("com.oemegil.smarttracker", MODE_PRIVATE).edit();
        if(firstInit)
        {
            String overtakingEnabled=preferences.getString("isOverTakingEnabled","");
            String turnEnabled=preferences.getString("isTurnEnabled","");
            if(overtakingEnabled.equals("") || overtakingEnabled.equals("false"))
            {
                isOvertaking=false;
                editor.putString("isOverTakingEnabled", "false");
            }
            else
            {
                isOvertaking=true;
                editor.putString("isOverTakingEnabled", "true");
            }
            if(turnEnabled.equals("") || turnEnabled.equals("false"))
            {
                isTurn=false;
                editor.putString("isTurnEnabled", "false");
            }
            else
            {
                isTurn=true;
                editor.putString("isTurnEnabled", "true");

            }
            editor.commit();
        }
        else
        {
            if(isOvertaking) editor.putString("isOverTakingEnabled", "true");
                else  editor.putString("isOverTakingEnabled", "false");
             if(isTurn) editor.putString("isTurnEnabled", "true");
                else  editor.putString("isTurnEnabled", "false");

                editor.commit();
        }

        InitImage();
    }

    public void InitImage()
    {
        if(isTurn)  turnImage.setImageResource(R.drawable.turnright);
        else turnImage.setImageResource(R.drawable.turnright_deactive);

        if(isOvertaking) overtakingImage.setImageResource(R.drawable.overtaking);
        else  overtakingImage.setImageResource(R.drawable.overtaking_deactive);
    }

    private void SetThreasholdSettings() {

        SharedPreferences  preferences = getSharedPreferences("com.oemegil.smarttracker", MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreferences("com.oemegil.smarttracker", MODE_PRIVATE).edit();
        String steeringWheelThreasholdValuestr=preferences.getString("swThreashold","");
        String acceleratorThreasholdValuestr=preferences.getString("aThreashold","");
        if(steeringWheelThreasholdValuestr=="")
        {
            editor.putString("swThreashold","100");
            editor.commit();
            steeringWheelThreasholdValuestr=preferences.getString("swThreashold","");
        }
        if(acceleratorThreasholdValuestr=="")
        {
            editor.putString("aThreashold","50");
            editor.commit();
            acceleratorThreasholdValuestr=preferences.getString("aThreashold","");
        }

        steeringWheelThreasholdValue=Double.parseDouble(steeringWheelThreasholdValuestr);
        acceleratorThreasholdValue=Double.parseDouble(acceleratorThreasholdValuestr);
    }

}