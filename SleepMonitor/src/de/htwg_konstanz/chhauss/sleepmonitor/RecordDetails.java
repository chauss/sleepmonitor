package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;
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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
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
	private SeekBar recSeekBar;
	private SeekBarProgress seekBarProgress;
	
	private boolean playing = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_details);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
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
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(record.getPath() != null) {
			try {
				initMediaPlayer();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setRecordFileData();
		initPlaybackControls();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(record.getPath() != null) {
			stopPlayback();
			releaseMediaPlayer();
		}
	}
	
	private void initPlaybackControls() {
		recSeekBar = (SeekBar) findViewById(R.id.playbackSeekBar);

		if(record.getPath() == null) {
			recSeekBar.setEnabled(false);
			return;
		}
		
		recSeekBar.setMax(mediaPlayer.getDuration());
		recSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(mediaPlayer != null && fromUser) {
					mediaPlayer.seekTo(progress);
				}
			}
		});
	}
	
	private void initMediaPlayer() throws Exception {
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
	}
	
	private void releaseMediaPlayer() {
		mediaPlayer.reset();
		mediaPlayer.release();
		mediaPlayer = null;
	}

	private void setRecordFileData() {
		if(record.getPath() != null) {
			System.out.println(record.getPath());
			File file = new File(record.getPath());
			double size = (double) file.length() / BYTES_TO_KILOBYTES;
			
			double duration = (double) mediaPlayer.getDuration() / MILISEC_TO_SEC;
			
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
				playRecord();
			} else {
				stopPlayback();
			}
			break;
		case R.id.showLineChartBtn:
			showLineChartForRecord();
			break;
		}
	}

	private void playRecord() {
		if(seekBarProgress == null) {
			seekBarProgress = new SeekBarProgress();
		}
		
		mediaPlayer.start();
		seekBarProgress.start();
		
		playing = true;
		playRecordBtn.setText(R.string.stopPlayback);
		showLineChartBtn.setEnabled(false);
	}
	
	private void stopPlayback() {
		if(mediaPlayer == null || seekBarProgress == null) {
			return;
		}
		
		mediaPlayer.pause();
		mediaPlayer.seekTo(0);
		seekBarProgress.stopProcessing();
		seekBarProgress = null;
		recSeekBar.setProgress(0);
		
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
	
	class SeekBarProgress extends Thread {
		
		private Boolean done = false;
		
		@Override
		public void run() {
			int mCurrentPosition;
			
			while(!done && mediaPlayer != null) {
				mCurrentPosition = mediaPlayer.getCurrentPosition();
	            recSeekBar.setProgress(mCurrentPosition);
			}
		}
		
		public void stopProcessing() {
			done = true;
		}
	}
}
