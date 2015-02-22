package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RecordDetails extends Activity {

	private MediaPlayer mediaPlayer;
	private Button playRecordBtn;
	private Button showLineChartBtn;
	private String recordPath;
	private String recordID;
	
	private boolean playing = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_details);
		
		recordPath = getIntent().getStringExtra("recordPath");
		recordID = getIntent().getStringExtra("recordID");
		
		TextView recordTV = (TextView) findViewById(R.id.recordNameTV);
		recordTV.setText("Record: " + recordID);
		
		playRecordBtn = (Button) findViewById(R.id.playRecordBtn);
		showLineChartBtn = (Button) findViewById(R.id.showLineChartBtn);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		stopPlayback();
	}

	public void onButtonClicked(View v) {
		switch(v.getId()) {
		case R.id.playRecordBtn:
			if(!playing) {
				try {
					playRecord(recordPath);
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

	private void playRecord(String record_path) throws IllegalStateException, IOException {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				stopPlayback();
			}
		});
		
		mediaPlayer.setDataSource(record_path);
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
		showLineChartBtn.setEnabled(true);
	}
	
	private void showLineChartForRecord() {
		DatabaseAdapter dba = new DatabaseAdapter(getApplicationContext());
		System.out.println("1");
		HashMap<Date, Integer> result = dba.selectAllByRecordID(recordID);
		
		LineChart lineChart = new LineChart(result);
		Intent lineIntent = lineChart.getIntent(this);
		startActivity(lineIntent);
	}
}
