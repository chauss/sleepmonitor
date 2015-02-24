package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	
	private String recordPath;
	private String recordID;
	private Boolean recordVolumeData;
	private Boolean recordToRecordFile;
	
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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		
		if(action.equals(START_RECORDING_ACTION)) {
			Bundle extras = intent.getExtras();
			recordVolumeData = extras.getBoolean("recordVolumeData");
			recordToRecordFile = extras.getBoolean("recordToRecordFiles");
			
			startRecording();
			startForeground(1, getRecordingNotification());
			
		} else if(action.equals(STOP_RECORDING_ACTION)) {
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
		
		Record r = new Record(rPath, rID, rName);
		return r;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		stopRecording();
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
    	
    	// Start volumeScanner if Volume Data shall be recorded
		if(recordVolumeData) {
			volumeScanner = new VolumeScanner(this, recordID);
			volumeScanner.start();
		}
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
		rec.stop();
	}
	
	class VolumeScanner extends Thread {
		
		private int sleepTime = 1000;
		private boolean done;
		private String recordID;
		private DatabaseAdapter dba;
		
		private VolumeScanner(Context context, String recordID) {
			done = false;
			this.recordID = recordID;
			dba = new DatabaseAdapter(context);
		}

		@Override
		public void run() {
			int volume;
			Date date;
			
			while(!done) {
				date = new Date();
				volume = (int) rec.getAmplitude();
				
				dba.insertVolume(dateFormatter.format(date), volume, recordID);
				try {
					sleep(sleepTime);
				} catch (InterruptedException e) {
					continue;
				}
				
				// TODO change sleepTime due to the value of volume
			}
			dba.closeDatabase();
		}
		
		public void stopScanning() {
			done = true;
		}
	}
}
