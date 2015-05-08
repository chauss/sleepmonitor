package de.htwg_konstanz.chhauss.sleepmonitor;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class SleepMonitoring extends Fragment implements OnClickListener {

	private boolean recording;
	private Button recordBtn;
	
	private Spinner recOpts;
	private Spinner noiseScanIntervals;
	
	private Boolean recordUserData;
	private Boolean recordToRecordFiles;
	
	private Activity thisActivity;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		thisActivity = activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		recording = false;
		View v = inflater.inflate(R.layout.fragment_sleepmonitoring, container, false);
		
		recordBtn = (Button) v.findViewById(R.id.recordBtn);
		recordBtn.setOnClickListener(this);
    	recOpts = (Spinner) v.findViewById(R.id.recordOptsSpinner);
    	prepareRecordOptionsSpinner();
    	noiseScanIntervals = (Spinner) v.findViewById(R.id.noiseScanIntervalSpinner);
    	noiseScanIntervals.setSelection(4);
		
    	
		return v;
	}

	@Override
	public void onResume() {
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
					recordUserData = true;
					recordToRecordFiles = false;
					break;
				case 1:
					recordUserData = false;
					recordToRecordFiles = true;
					break;
				case 2:
					recordUserData = true;
					recordToRecordFiles = true;
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
    		
    	});
    	recOpts.setSelection(2);
	}
		
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.recordBtn:
			onRecordButtonClicked(v);
			break;
		default:
			break;
		}
		
	}
	
	public void onRecordButtonClicked(View v)  {
		if(!recording){
			Intent starter = new Intent(thisActivity, RecordingService.class);
			starter.putExtra("recordUserData", recordUserData);
			starter.putExtra("recordToRecordFiles", recordToRecordFiles);
			starter.putExtra("noiseScanInterval", getNoiseScanInterval());
			starter.setAction(RecordingService.START_RECORDING_ACTION);
			thisActivity.startService(starter);
			
			activateRecordingState();
			Toast.makeText(thisActivity, R.string.startedRecording, Toast.LENGTH_SHORT).show();
		} else {
			thisActivity.stopService(new Intent(thisActivity, RecordingService.class));

			deactivateRecordingState();
			Toast.makeText(thisActivity, R.string.stoppedRecording, Toast.LENGTH_SHORT).show();
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
	  ActivityManager manager = (ActivityManager) thisActivity.getSystemService(Context.ACTIVITY_SERVICE);
	  for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    if (serviceClass.getName().equals(service.service.getClassName())) {
	        return true;
	    }
	  }
	  return false;
	}

}