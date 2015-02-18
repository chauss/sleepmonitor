package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends Activity {

	private MediaPlayer mediaPlayer;
	private MediaRecorder recorder;
	private String OUTPUT_FILE;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	OUTPUT_FILE = Environment.getExternalStorageDirectory() + "/sleepRecord.3gpp";
    	
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void buttonToggled(View v)  {
    	switch(v.getId()) {
    	case R.id.startRecording:
    		try {
    			startRecording();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	case R.id.stopRecording:
    		try {
    			stopRecording();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	case R.id.playRecord:
    		try {
    			playRecord();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	case R.id.stopPlayback:
    		try {
    			stopPlayback();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    private void startRecording() throws IllegalStateException, IOException {
    	// Stop recording
		if(recorder != null) {
			recorder.release();
		}
		
		// delete output-file
		File outFile = new File(OUTPUT_FILE);
		
		if(outFile.exists()) {
			outFile.delete();
		}
		
		// Recording
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		recorder.setOutputFile(OUTPUT_FILE);
		recorder.prepare();
		recorder.start();
	}
    
	private void stopRecording() {
		if(recorder != null) {
			recorder.stop();
		}
	}
    
	private void playRecord() throws IllegalStateException, IOException {
		// Stop playback
		if(mediaPlayer != null) {
			mediaPlayer.release();
		}
		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(OUTPUT_FILE);
		mediaPlayer.prepare();
		mediaPlayer.start();
	}
	
	private void stopPlayback() {
		if(mediaPlayer != null)
			mediaPlayer.stop();
	}
}
