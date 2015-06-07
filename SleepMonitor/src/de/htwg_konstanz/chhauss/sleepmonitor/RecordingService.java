package de.htwg_konstanz.chhauss.sleepmonitor;

import static java.lang.Math.max;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class RecordingService extends Service implements SensorEventListener {
	
	public static final String START_RECORDING_ACTION = "RecordingService.action.startRecording";
	public static final String STOP_RECORDING_ACTION = "RecordingService.action.stopRecording";
	
	private static final int CHECK_WAITING_CONDITION_PERIOD = 10; //s
	
	private static final String DATE_TIME_FORMAT = "dd_MM_yyyy_HH_mm_ss";
	private static final String devNull = "/dev/null";
	
	private static final Object ENOUGH_LOCK = new Object();

	private double enough_movement_to_wake;
	private int enough_noise_to_wake;
	
	private Recorder rec;
	private SimpleDateFormat dateFormatter;
	private DatabaseAdapter dba;
	SensorManager sm;
	private boolean reachedInterval = false;
	
	private String recordPath;
	private String recordID;
	private Boolean recordUserData;
	private Boolean recordToRecordFile;
	private double noiseScanInterval;
	private double acc_x;
	private double acc_y;
	private double acc_z;
	
	private boolean enoughNoiseOrMovementToWake = false;
	
	Timer acc_timer;
	Timer volume_timer;
	Timer waitTillAlarmInterval_timer;
	Timer wakingCheck_timer;
	
	@Override
	public void onCreate() {
		super.onCreate();
		dateFormatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
		
		String file_base = Environment.getExternalStorageDirectory().getAbsolutePath() +
					       getString(R.string.app_directory) +
					       getString(R.string.record_directory);
		
		// Create RecordID and recordPath
    	Date date = new Date();
    	recordID = dateFormatter.format(date);
    	recordPath = file_base + "/" + recordID + getString(R.string.record_file_ending);
    	
    	//
    	SharedPreferences sp = getSharedPreferences(AlarmClock.ALARM_PREFERENCES, Context.MODE_PRIVATE);
    	enough_noise_to_wake = sp.getInt(AlarmClock.ALARM_NOISE_KEY, 0);
    	enough_movement_to_wake = Double.longBitsToDouble(sp.getLong(AlarmClock.ALARM_MOVEMENT_KEY, 0));
	}

	private void initAlarm() {
		SharedPreferences sp = getSharedPreferences(AlarmClock.ALARM_PREFERENCES, Context.MODE_PRIVATE);
    	boolean alarmIsOn = sp.getBoolean(AlarmClock.ALARMSTATE_KEY, false);
    	int startH = sp.getInt(AlarmClock.ALARM_STARTHOUR_KEY, 0);
    	int startM = sp.getInt(AlarmClock.ALARM_STARTMIN_KEY, 0);
    	int endH = sp.getInt(AlarmClock.ALARM_ENDHOUR_KEY, 0);
    	int endM = sp.getInt(AlarmClock.ALARM_ENDMIN_KEY, 0);
    	
    	if(!alarmIsOn) {
    		return;
    	}
    	
    	DateTime now = new DateTime();
    	DateTime start = new DateTime().withHourOfDay(startH).withMinuteOfHour(startM);
		DateTime end = new DateTime().withHourOfDay(endH).withMinuteOfHour(endM);
		
		if(start.isBefore(now)) {
			start = start.plusDays(1);
		}
		
		while(end.isBefore(start)) {
			end = end.plusDays(1);
		}
		
		final Interval alarmInterval = new Interval(start, end);
		waitTillAlarmInterval_timer = new Timer();
		TimerTask waitTillAlarmInterval_task = new TimerTask() {
            
            @Override
            public void run() {
            	synchronized (ENOUGH_LOCK) {
            		reachedInterval = true;
            	}
            	wakingCheck_timer = new Timer();
            	TimerTask checkWakingConditions = new TimerTask() {

					@Override
					public void run() {
						synchronized (ENOUGH_LOCK) {
							DateTime now = new DateTime();
							if(enoughNoiseOrMovementToWake || !alarmInterval.contains(now)) {
								Intent intent = new Intent(RecordingService.this, AlarmReceiver.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
								stopRecording();
								// Make sure timer does not schedule another alarm
								wakingCheck_timer.cancel();
								wakingCheck_timer.purge();
								wakingCheck_timer = null;
								stopForeground(true);
								stopSelf();
							}
						}
					}
            	};
            	wakingCheck_timer.schedule(checkWakingConditions, 0, CHECK_WAITING_CONDITION_PERIOD * 1000);
            }
        };
        waitTillAlarmInterval_timer.schedule(waitTillAlarmInterval_task, start.getMillis() - now.getMillis());
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopRecording();
		if(waitTillAlarmInterval_timer != null) {
			waitTillAlarmInterval_timer.cancel();
			waitTillAlarmInterval_timer.purge();
		}
		if(wakingCheck_timer != null) {
			wakingCheck_timer.cancel();
			wakingCheck_timer.purge();
		}
	}

	private void initAccelerometer() {
	    sm = (SensorManager) getSystemService(SENSOR_SERVICE);
	    
	    List<Sensor> sensorList = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensorList.size() > 0) {
            sm.registerListener(this,
                                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                SensorManager.SENSOR_DELAY_GAME);
        } else {
            //TODO was passiert wenn man keinen accelerometer hat
        }
    }
	
	@Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            synchronized(RecordingService.class) {
                acc_x = max(acc_x, event.values[0]);
                acc_y = max(acc_y, event.values[1]);
                acc_z = max(acc_z, event.values[2]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}


    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
    	String action = intent.getAction();;
		
		if(action.equals(START_RECORDING_ACTION)) {
		    initAccelerometer();
			Bundle extras = intent.getExtras();
			recordUserData = extras.getBoolean("recordUserData");
			recordToRecordFile = extras.getBoolean("recordToRecordFiles");
			noiseScanInterval = extras.getDouble("noiseScanInterval");
			
			startRecording();
			initAlarm();
			startForeground(1, getRecordingNotification());
			
		} else if(action.equals(STOP_RECORDING_ACTION)) {
		    stopRecording();

		    Intent startRecordDetailsIntent = new Intent(this, RecordDetails.class);
			startRecordDetailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startRecordDetailsIntent.putExtra("record", createRecordForRecordDetailsIntent());
			startActivity(startRecordDetailsIntent);
			
			stopForeground(true);
			stopSelf();
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private Notification getRecordingNotification() {
		Intent i = new Intent(this, RecordingService.class);
		i.setAction(STOP_RECORDING_ACTION);
		PendingIntent stopRecordingIntent = PendingIntent.getService(this, 0, i, 0);
		
		Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		
		Notification recNotification = new NotificationCompat.Builder(this)
			.setTicker(getString(R.string.startedRecording))
			.setContentTitle(getString(R.string.app_name))
			.setContentText(getString(R.string.recording))
			.setSmallIcon(R.drawable.ic_recording)
			.setLargeIcon(icon)
			.setOngoing(true)
			.setUsesChronometer(true)
			.addAction(R.drawable.ic_stop_recording, getString(R.string.stopRecording), stopRecordingIntent)
			.build();
		return recNotification;
	}

	private Record createRecordForRecordDetailsIntent() {
		String rPath = null;
		String rID = null;
		String rName = recordID;
		
		if(recordToRecordFile) {
			rPath = recordPath;
		}
		
		if(recordUserData) {
			rID = recordID;
		}
		
		return new Record(rPath, rID, rName);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void startRecording() {
    	createRecorderInstance();
    	
    	try {
    		rec.start();
    	} catch (IOException e) {
    		Toast.makeText(this, R.string.couldNotCreateRecordFile, Toast.LENGTH_LONG).show();
    		stopRecording();
    		return;
    	}
    	dba = new DatabaseAdapter(this);
    	
    	if(recordUserData) {
			startVolumeTimer();
			startAccTimer();
    	}
	}

	private void startAccTimer() {
		acc_timer = new Timer();
		TimerTask acc_task = new TimerTask() {
            
            @Override
            public void run() {
                Date date = new Date();
                double movement = 0;
                synchronized(RecordingService.class) {
                	movement = Math.abs(acc_x) + Math.abs(acc_y) + Math.abs(acc_z) - 9.81;
                    dba.insertAccelerometerValues(dateFormatter.format(date), acc_x, acc_y, acc_z, recordID);
                    acc_x = acc_y = acc_z = 0;
                }
                synchronized(ENOUGH_LOCK) {
                	if(reachedInterval && movement >= enough_movement_to_wake) {
                		enoughNoiseOrMovementToWake = true;
                	}
                }
            }
        };
        acc_timer.schedule(acc_task, 0, (long) (noiseScanInterval * 1000));
	}

	private void startVolumeTimer() {
		volume_timer = new Timer();
		TimerTask volume_task = new TimerTask() {

			@Override
			public void run() {
				Date date = new Date();
				int noiseLevel = (int) rec.getAmplitudeEMA();
				dba.insertVolume(dateFormatter.format(date), noiseLevel, recordID);
				synchronized(ENOUGH_LOCK) {
					if(reachedInterval && noiseLevel >= enough_noise_to_wake) {
                		enoughNoiseOrMovementToWake = true;
                	}
                }
			}
		};
		volume_timer.schedule(volume_task, 0, (long) (noiseScanInterval * 1000));
	}

	private void createRecorderInstance() {
		if(recordToRecordFile) {
    		rec = new Recorder(recordPath);
    	} else {
    		rec = new Recorder(devNull);
    	}
	}
	
	private void stopRecording() {
		if(volume_timer != null){
			volume_timer.cancel();
			volume_timer.purge();
			volume_timer = null;
		}
		if(acc_timer != null) {
		    acc_timer.cancel();
		    acc_timer.purge();
		    acc_timer = null;
		}
		if(dba != null) {
		    dba.closeDatabase();
		    dba = null;
		}
		if(sm != null) {
			sm.unregisterListener(this);
			sm = null;
		}
		if(rec != null) {
			rec.stop();
			rec = null;
		}
	}
}
