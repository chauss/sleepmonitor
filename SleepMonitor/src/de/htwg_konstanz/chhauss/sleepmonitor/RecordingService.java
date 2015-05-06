package de.htwg_konstanz.chhauss.sleepmonitor;

import static java.lang.Math.max;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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

public class RecordingService extends Service {
	
	public static final String START_RECORDING_ACTION = "RecordingService.action.startRecording";
	public static final String STOP_RECORDING_ACTION = "RecordingService.action.stopRecording";
	
	private static final String DATE_TIME_FORMAT = "dd_MM_yyyy_HH_mm_ss";
	private static final String devNull = "/dev/null";
	
	private Recorder rec;
	private SimpleDateFormat dateFormatter;
	private VolumeScanner volumeScanner;
	private DatabaseAdapter dba;
	
	private String recordPath;
	private String recordID;
	private Boolean recordVolumeData;
	private Boolean recordToRecordFile;
	private double noiseScanInterval;
	private double acc_x;
	private double acc_y;
	private double acc_z;
	Timer timer;
	
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
	}

	private void initAccelerometer() {
	    SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
	    
	    List<Sensor> sensorList = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensorList.size() > 0) {
            sm.registerListener(new SensorEventListener() {
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
            },
                                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                SensorManager.SENSOR_DELAY_GAME);
        } else {
            //TODO was passiert wenn man keinen accelerometer hat
        }
    }

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		
		if(action.equals(START_RECORDING_ACTION)) {
		    initAccelerometer();
			Bundle extras = intent.getExtras();
			recordVolumeData = extras.getBoolean("recordVolumeData");
			recordToRecordFile = extras.getBoolean("recordToRecordFiles");
			noiseScanInterval = extras.getDouble("noiseScanInterval");
			
			startRecording();
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
		
		if(recordVolumeData) {
			rID = recordID;
		}
		
		return new Record(rPath, rID, rName);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void startRecording() {
    	createRecorderInstance(recordID);
    	
    	try {
    		rec.start();
    	} catch (IOException e) {
    		Toast.makeText(this, R.string.couldNotCreateRecordFile, Toast.LENGTH_LONG).show();
    		stopRecording();
    		return;
    	}
    	dba = new DatabaseAdapter(this);
    	// Start volumeScanner if Volume Data shall be recorded
		if(recordVolumeData) {
			volumeScanner = new VolumeScanner(recordID);
			volumeScanner.start();
		}
		
		// Start Accelerometer scanning
		timer = new Timer();
		TimerTask t = new TimerTask() {
            
            @Override
            public void run() {
                Date date = new Date();
                synchronized(RecordingService.class) {
                    dba.insertAccelerometerValues(dateFormatter.format(date), acc_x, acc_y, acc_z, recordID);
                    acc_x = acc_y = acc_z = 0;
                }
            }
        };
        timer.schedule(t, (long) (noiseScanInterval * 1000), (long) (noiseScanInterval * 1000));
	}

	private void createRecorderInstance(String recordID) {
		if(recordToRecordFile) {
    		rec = new Recorder(recordPath);
    	} else {
    		rec = new Recorder(devNull);
    	}
	}
	
	private void stopRecording() {
		if(volumeScanner != null){
			volumeScanner.stopScanning();
			volumeScanner = null;
		}
		if(timer != null) {
		    timer.cancel();
		}
		if(dba != null) {
		    dba.closeDatabase();
		}
		rec.stop();
	}
	
	class VolumeScanner extends Thread {
		
		private boolean done;
		private String recordID;
		
		private VolumeScanner(String recordID) {
			done = false;
			this.recordID = recordID;
		}

		@Override
		public void run() {
			int volume;
			Date date;
			
			while(!done) {
				date = new Date();
				volume = (int) rec.getAmplitudeEMA();
				
				dba.insertVolume(dateFormatter.format(date), volume, recordID);
				try {
					sleep((int) (noiseScanInterval * 1000));
				} catch (InterruptedException e) {
					continue;
				}
				
				// TODO change sleepTime due to the value of volume
			}
		}
		
		public void stopScanning() {
			done = true;
		}
	}
}
