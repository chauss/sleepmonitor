package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SleepMonitoring extends Activity{

	private static final String DATE_TIME_FORMAT = "dd_MM_yyyy_HH_mm_ss";
	
	private boolean recording;
	private Button recordBtn;
	private Recorder rec;
	private SimpleDateFormat dateFormatter;
	private String file_base;
	
	private VolumeScanner volumeScanner;
	private static final int minSleepTime = 1000;
	private static final int maxSleepTime = 6000;
	
	private DatabaseAdapter dba;
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleepmonitoring);
        
    	recording = false;
    	recordBtn = (Button) findViewById(R.id.recordBtn);
    	
    	dateFormatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
    	
    	file_base = Environment.getExternalStorageDirectory().getAbsolutePath() +
				   getString(R.string.app_directory) +
				   getString(R.string.record_directory);

    	dba = new DatabaseAdapter(this);
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
    	Date date = new Date();
    	String recordID = dateFormatter.format(date);
    	rec = new Recorder(file_base + "/" +
    					   recordID +
    					   getString(R.string.record_file_ending));
    	
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
		
		volumeScanner = new VolumeScanner(recordID);
		volumeScanner.start();
	}
    
	private void stopRecording() {
		volumeScanner.stopScanning();
		volumeScanner = null;
		rec.stop();
		dba.closeDatabase();
		
		recording = false;
		recordBtn.setText(R.string.startRecording);
		
		Toast.makeText(this, R.string.stoppedRecording, Toast.LENGTH_SHORT).show();
	}
	
	class VolumeScanner extends Thread {
		
		private int sleepTime = 1000;
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
				volume = (int) rec.getAmplitude();
				
				dba.insertVolume(dateFormatter.format(date), volume, recordID);
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