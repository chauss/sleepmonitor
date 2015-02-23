package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

public class RecordingService extends Service {
	
	private static final String DATE_TIME_FORMAT = "dd_MM_yyyy_HH_mm_ss";
	private static final String devNull = "/dev/null";
	
	private Recorder rec;
	private SimpleDateFormat dateFormatter;
	private VolumeScanner volumeScanner;
	
	private String file_base;
	private Boolean recordVolumeData;
	private Boolean recordToRecordFiles;
	
	@Override
	public void onCreate() {
		super.onCreate();
		dateFormatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
		
		file_base = Environment.getExternalStorageDirectory().getAbsolutePath() +
				    getString(R.string.app_directory) +
				    getString(R.string.record_directory);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras();
		recordVolumeData = extras.getBoolean("recordVolumeData");
		recordToRecordFiles = extras.getBoolean("recordToRecordFiles");
		
		startRecording();
		
		return super.onStartCommand(intent, flags, startId);
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
		// Create RecordID
    	Date date = new Date();
    	String recordID = dateFormatter.format(date);
    	
    	createRecorderInstance(recordID);
    	
    	try {
    		rec.start();
    	} catch (IOException e) {
    		Toast.makeText(this, R.string.couldNotCreateRecordFile, Toast.LENGTH_LONG).show();
    		e.printStackTrace();
    		return;
    	}
    	
    	// Start volumeScanner if Volume Data shall be recorded
		if(recordVolumeData) {
			volumeScanner = new VolumeScanner(this, recordID);
			volumeScanner.start();
		}
	}

	private void createRecorderInstance(String recordID) {
		if(recordToRecordFiles) {
    		rec = new Recorder(file_base + "/" +
	    			    	   recordID +
		    				   getString(R.string.record_file_ending));
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
