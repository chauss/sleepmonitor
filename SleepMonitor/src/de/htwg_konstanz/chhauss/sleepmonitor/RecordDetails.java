package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RecordDetails extends Activity {
	
	private static final int BYTES_TO_KILOBYTES = 1024;
	private static final int MILISEC_TO_SEC = 1000;
	private static final String KILOBYTE_ENDING = " KB";
	private static final String SECONDS_ENDING = " s";

	private MediaPlayer mediaPlayer;
	private Button playRecordBtn;
	private Button showLineChartBtn;
	private Record record;
	
	private boolean playing = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_details);
		
		record = getIntent().getParcelableExtra("record");
		
		TextView recordTV = (TextView) findViewById(R.id.recordNameTV);
		recordTV.setText("Record: " + record.getName());
		
		playRecordBtn = (Button) findViewById(R.id.playRecordBtn);
		showLineChartBtn = (Button) findViewById(R.id.showLineChartBtn);
		
		if(record.getPath() == null) {
			playRecordBtn.setEnabled(false);
			playRecordBtn.setText(R.string.noRecordFileFound);
		}
		if(record.getID() == null) {
			showLineChartBtn.setEnabled(false);
			showLineChartBtn.setText(R.string.noVolumeDataFound);
		}
		
		setRecordFileData();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		stopPlayback();
	}
	
	private void setRecordFileData() {
		if(record.getPath() != null) {
			File file = new File(record.getPath());
			double size = (double) file.length() / BYTES_TO_KILOBYTES;
			
			// Get Duration of the audiofile
			mediaPlayer = MediaPlayer.create(this, Uri.parse(record.getPath()));
			double duration = (double) mediaPlayer.getDuration() / MILISEC_TO_SEC;
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
			
			TextView sizeTV = (TextView) findViewById(R.id.recordFileSizeTV);
			sizeTV.setText(getString(R.string.recordFileSize)
					       .replace("%SIZE", String.format("%.2f", size) + KILOBYTE_ENDING));
			
			TextView durationTV = (TextView) findViewById(R.id.recordFileDurationTV);
			durationTV.setText(getString(R.string.recrodFileDuration)
							   .replace("%DURATION", String.format("%.2f", duration) + SECONDS_ENDING));
		} else {
			TextView sizeTV = (TextView) findViewById(R.id.recordFileSizeTV);
			sizeTV.setText(getString(R.string.recordFileSize)
					       .replace("%SIZE", getString(R.string.notAvailable)));
			
			TextView durationTV = (TextView) findViewById(R.id.recordFileDurationTV);
			durationTV.setText(getString(R.string.recrodFileDuration)
							   .replace("%DURATION", getString(R.string.notAvailable)));
		}
	}

	public void onButtonClicked(View v) {
		switch(v.getId()) {
		case R.id.playRecordBtn:
			if(!playing) {
				try {
					playRecord();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				stopPlayback();
			}
			break;
		case R.id.showLineChartBtn:
			showLineChartForRecord();
			break;
		}
	}

	private void playRecord() throws IllegalStateException, IOException {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				stopPlayback();
			}
		});
		mediaPlayer.setDataSource(record.getPath());
		mediaPlayer.prepare();
		
		mediaPlayer.start();
		
		playing = true;
		playRecordBtn.setText(R.string.stopPlayback);
		showLineChartBtn.setEnabled(false);
	}
	
	private void stopPlayback() {
		if(mediaPlayer == null) {
			return;
		}
		
		mediaPlayer.stop();
		mediaPlayer.reset();
		mediaPlayer.release();
		mediaPlayer = null;
		
		playing = false;
		playRecordBtn.setText(R.string.playRecord);
		if(record.getID() != null) {
			showLineChartBtn.setEnabled(true);
		}
	}
	
	private void showLineChartForRecord() {
		DatabaseAdapter dba = new DatabaseAdapter(getApplicationContext());
		HashMap<Date, Integer> result = dba.selectAllByRecordID(record.getID());
		
		LineChart lineChart = new LineChart(result);
		Intent lineIntent = lineChart.getIntent(this);
		startActivity(lineIntent);
	}
}
