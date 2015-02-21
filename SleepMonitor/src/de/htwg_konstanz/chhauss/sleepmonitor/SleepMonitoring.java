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

	private static final String RECORD_BASE_NAME = "/sleepRecord_";
	
	private static final String DATE_TIME_FORMAT = "dd_MM_yyyy_HH_mm_ss";
	
	private boolean recording;
	private Button recordBtn;
	private Recorder rec;
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleepmonitoring);
        System.out.println("oncreate sleepmonitoring");
        
    	recording = false;
    	recordBtn = (Button) findViewById(R.id.recordBtn);
    	
    	SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
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
	}
    
	private void stopRecording() {
		rec.stop();
		
		recording = false;
		recordBtn.setText(R.string.startRecording);
		
		Toast.makeText(this, R.string.stoppedRecording, Toast.LENGTH_SHORT).show();
	}
    
}
