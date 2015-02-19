package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {

	private MediaPlayer mediaPlayer;
	private String OUTPUT_FILE;
	private File record;
	
	private boolean recording;
	private boolean playing;
	
	private Button recordBtn;
	private Button playBtn;
	
	private SoundMeter sm;
	
	private double[] values;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	OUTPUT_FILE = Environment.getExternalStorageDirectory() + "/sleepRecord.3gpp";
    	recording = false;
    	playing = false;
    	recordBtn = (Button) findViewById(R.id.recordBtn);
    	playBtn = (Button) findViewById(R.id.playBtn);
    	record = new File(OUTPUT_FILE);
    	sm = new SoundMeter(OUTPUT_FILE);
    	values = new double[20];
    	
    	checkForRecord();
    	
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void checkForRecord() {
    	if(!record.exists()) {
    		playBtn.setEnabled(false);
    	}
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
    	case R.id.recordBtn:
    		if(!recording){
    			System.out.println("1");
    			try {
    				startRecording();
    			} catch(Exception e) {
    				e.printStackTrace();
    			}
    		} else {
    			try {
        			System.out.println("2");
        			stopRecording();
        		} catch(Exception e) {
        			e.printStackTrace();
        		}
    		}
    		break;
    	case R.id.playBtn:
    		if(!playing) {
    			try {
    				System.out.println("3");
    				playRecord();
    			} catch(Exception e) {
    				e.printStackTrace();
    			}
    		} else {
    			try {
    				System.out.println("4");
    				stopPlayback();
    			} catch(Exception e) {
    				e.printStackTrace();
    			}
    		}
    		break;
    	}
    }
    
    public void onAnalyze(View v) throws IllegalStateException, IOException  {
		sm.start();
		System.out.println("Started SoundMeter");
		sm.getAmplitude();
		try {
			for(int i = 0; i < 20; i++) {
				Thread.sleep(1000);
				values[i] = sm.getAmplitude();
			}
			System.out.println(Arrays.toString(values));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		sm.stop();
		System.out.println("Stopped SoundMeter");
		checkForRecord();
    }
    
    private void startRecording() throws IllegalStateException, IOException {
		// delete old record
		if(record.exists()) {
			record.delete();
		}
		
		// Recording
		sm.start();
		
		recording = true;
		recordBtn.setText(R.string.stopRecording);
		playBtn.setEnabled(false);
	}
    
	private void stopRecording() {
		sm.stop();
		
		recording = false;
		recordBtn.setText(R.string.startRecording);
		checkForRecord();
	}
    
	private void playRecord() throws IllegalStateException, IOException {
		if(!record.exists()) {
			return;  // No record available
		}
		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				stopPlayback();
			}
		});
		
		mediaPlayer.setDataSource(OUTPUT_FILE);
		mediaPlayer.prepare();
		mediaPlayer.start();
		
		playing = true;
		recordBtn.setEnabled(false);
		playBtn.setText(R.string.stopPlayback);
	}
	
	private void stopPlayback() {
		mediaPlayer.stop();
		mediaPlayer.reset();
		mediaPlayer.release();
		
		playing = false;
		recordBtn.setEnabled(true);
		playBtn.setText(R.string.playRecord);
	}
}
