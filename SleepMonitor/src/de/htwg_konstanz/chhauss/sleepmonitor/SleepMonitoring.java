package de.htwg_konstanz.chhauss.sleepmonitor;


import android.app.Activity;
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
    	
    	prepareRecordOptionsSpinner();
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
			public void onNothingSelected(AdapterView<?> parent) {
				System.out.println("called on nothing selected");
			}
    		
    	});
    	recOpts.setSelection(2);
	}
	
	public void onRecordButtonClicked(View v)  {
		if(!recording){
			// Start Service
			Intent starter = new Intent(this, RecordingService.class);
			starter.putExtra("recordVolumeData", recordVolumeData);
			starter.putExtra("recordToRecordFiles", recordToRecordFiles);
			startService(starter);
			
			// Set Sleepmonitoring Views to state "Recording"
			recording = true;
			recordBtn.setText(R.string.stopRecording);
			Toast.makeText(this, R.string.startedRecording, Toast.LENGTH_SHORT).show();
		} else {
			stopService(new Intent(this, RecordingService.class));

			// Set Sleepmonitoring Views to state "Not Recording"
			recording = false;
			recordBtn.setText(R.string.startRecording);
			Toast.makeText(this, R.string.stoppedRecording, Toast.LENGTH_SHORT).show();
		}
    }
}