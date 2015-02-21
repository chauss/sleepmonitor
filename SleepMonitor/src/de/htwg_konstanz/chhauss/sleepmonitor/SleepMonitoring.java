package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SleepMonitoring extends Activity{

	private static final String RECORD_BASE_NAME = "/sleepRecord_";
	
	private static final String DATE_TIME_FORMAT = "dd_MM_yyyy_HH_mm_ss";
	
	private boolean recording;
	private Button recordBtn;
	private Recorder rec;
	private SimpleDateFormat dateFormatter;
	
	private VolumeScanner volumeScanner;
	private static final int minSleepTime = 1000;
	private static final int maxSleepTime = 6000;
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleepmonitoring);
        
    	recording = false;
    	recordBtn = (Button) findViewById(R.id.recordBtn);
    	
    	dateFormatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
    	Date date = new Date();
    	rec = new Recorder(Environment.getExternalStorageDirectory().getAbsolutePath() +
    					   getString(R.string.app_directory) +
    					   getString(R.string.record_directory) +
    					   RECORD_BASE_NAME +
    					   dateFormatter.format(date) +
    					   getString(R.string.record_file_ending));
    }
	
	public void onRecordButtonClicked(View v)  {
		if(!recording){
			try {
				startRecording();
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
    			stopRecording();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
		}
    }
    
    private void startRecording() throws IllegalStateException, IOException {
    	try {
    		rec.start();
    	} catch (IOException e) {
    		Toast.makeText(this, R.string.couldNotCreateRecordFile, Toast.LENGTH_LONG).show();
    		e.printStackTrace();
    		System.out.println(e.getMessage());
    		return;
    	}
    	
		recording = true;
		recordBtn.setText(R.string.stopRecording);
		
		Toast.makeText(this, R.string.startedRecording, Toast.LENGTH_SHORT).show();
		
		volumeScanner = new VolumeScanner(this);
		volumeScanner.start();
	}
    
	private void stopRecording() {
		volumeScanner.stopScanning();
		volumeScanner = null;
		rec.stop();
		
		recording = false;
		recordBtn.setText(R.string.startRecording);
		
		Toast.makeText(this, R.string.stoppedRecording, Toast.LENGTH_SHORT).show();
		
		DatabaseAdapter db = new DatabaseAdapter(this);
		db.selectAllByDate("21_02_2015");
	}
    
	class VolumeScanner extends Thread {
		
		private int sleepTime = 1000;
		private DatabaseAdapter dba;
		private boolean done;
		
		private VolumeScanner(Context context) {
			dba = new DatabaseAdapter(context);
			done = false;
		}

		@Override
		public void run() {
			int volume;
			Date date;
			
			while(!done) {
				date = new Date();
				volume = (int) rec.getAmplitude();
				
				dba.insertVolume(dateFormatter.format(date), volume);
				System.out.println("Die Lautstärke ist: " + volume);
				try {
					sleep(sleepTime);
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
