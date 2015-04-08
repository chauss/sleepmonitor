package de.htwg_konstanz.chhauss.sleepmonitor;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class SleepMonitoring extends Activity{

	private boolean recording;
	private Button recordBtn;
	
	private Spinner recOpts;
	private Spinner noiseScanIntervals;
	
	private Boolean recordVolumeData;
	private Boolean recordToRecordFiles;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleepmonitoring);
        setTitle(R.string.sleepMonitoring);
        
    	recording = false;
    	recordBtn = (Button) findViewById(R.id.recordBtn);
    	recOpts = (Spinner) findViewById(R.id.recordOptsSpinner);
    	noiseScanIntervals = (Spinner) findViewById(R.id.noiseScanIntervalSpinner);
    	noiseScanIntervals.setSelection(1);
    	
    	prepareRecordOptionsSpinner();
    }

	@Override
	protected void onResume() {
		super.onResume();
		if(serviceIsRunning(RecordingService.class)) {
    		activateRecordingState();
    	} else {
    		deactivateRecordingState();
    	}
	}

	private void prepareRecordOptionsSpinner() {
		recOpts.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch(position) {
				case 0:
					recordVolumeData = true;
					recordToRecordFiles = false;
					break;
				case 1:
					recordVolumeData = false;
					recordToRecordFiles = true;
					break;
				case 2:
					recordVolumeData = true;
					recordToRecordFiles = true;
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
    		
    	});
    	recOpts.setSelection(2);
	}
		
	public void onRecordButtonClicked(View v)  {
		if(!recording){
			Intent starter = new Intent(this, RecordingService.class);
			starter.putExtra("recordVolumeData", recordVolumeData);
			starter.putExtra("recordToRecordFiles", recordToRecordFiles);
			starter.putExtra("noiseScanInterval", getNoiseScanInterval());
			starter.setAction(RecordingService.START_RECORDING_ACTION);
			startService(starter);
			
			activateRecordingState();
			Toast.makeText(this, R.string.startedRecording, Toast.LENGTH_SHORT).show();
		} else {
			stopService(new Intent(this, RecordingService.class));

			deactivateRecordingState();
			Toast.makeText(this, R.string.stoppedRecording, Toast.LENGTH_SHORT).show();
		}
    }
	
	private double getNoiseScanInterval() {
		String tmp = (String) noiseScanIntervals.getSelectedItem();
		return Double.parseDouble(tmp);
	}

	private void activateRecordingState() {
		recording = true;
		recordBtn.setText(R.string.stopRecording);
	}
	
	private void deactivateRecordingState() {
		recording = false;
		recordBtn.setText(R.string.startRecording);
	}
	
	private boolean serviceIsRunning(Class<?> serviceClass) {
	  ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	  for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    if (serviceClass.getName().equals(service.service.getClassName())) {
	        return true;
	    }
	  }
	  return false;
	}
}