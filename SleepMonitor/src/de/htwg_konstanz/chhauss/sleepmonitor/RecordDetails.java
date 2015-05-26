package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class RecordDetails extends Activity {
	
	private static final int BYTES_UNIT = 1024;
	private static final int MILISEC_TO_SEC = 1000;
	private static final int SEC_PER_MIN = 60;
	private static final int MILISEC_TO_MIN = 60 * 1000;
	private static final int MIN_PER_HOUR = 60;
	private static final int MILISEC_TO_HOURS = 1000 * 60 * 60;
	private static final int HOURS_PER_DAY = 24;
	private static final int SLEEP_BETWEEN_SEEKBAR_UPDATE = 500; //ms
	private static final String KILOBYTE_ENDING = " KB";
	private static final String MEGABYTE_ENDING = " MB";
	private static final String SECONDS_ENDING = " s";
	private static final String MINUTES_ENDING = " m";
	private static final String HOURS_ENDING = " h";

	private MediaPlayer mediaPlayer;
	private Button playPauseRecordBtn;
	private Button stopRecordBtn;
	private Button showLineChartBtn;
	private Button deleteRecordBtn;
	private Button deleteUserDataBtn;
	private Record record;
	private SeekBar recSeekBar;
	private SeekBarProgress seekBarProgress;
	private TextView curPosTV;
	
	private boolean playing = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_details);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		record = getIntent().getParcelableExtra("record");
		
		TextView recordTV = (TextView) findViewById(R.id.recordNameTV);
		recordTV.setText("Record: " + record.getName());

		curPosTV = (TextView) findViewById(R.id.curPosTV);
		
		playPauseRecordBtn = (Button) findViewById(R.id.playPauseRecordBtn);
		stopRecordBtn = (Button) findViewById(R.id.stopRecordBtn);
		deleteRecordBtn = (Button) findViewById(R.id.deleteRecordBtn);
		deleteUserDataBtn = (Button) findViewById(R.id.deleteUserDataBtn);
		
		showLineChartBtn = (Button) findViewById(R.id.showLineChartBtn);
		
		if(record.getPath() == null) {
			playPauseRecordBtn.setEnabled(false);
			stopRecordBtn.setEnabled(false);
			deleteRecordBtn.setEnabled(false);
		}
		if(record.getID() == null) {
			showLineChartBtn.setEnabled(false);
			showLineChartBtn.setText(R.string.noVolumeDataFound);
			deleteUserDataBtn.setEnabled(false);
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
					
		            curPosTV.setText(getTimeFromMS(progress));
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
			File file = new File(record.getPath());
			String fileSize = getFileSize((double) file.length());
			String duration = getFileDuration((double) mediaPlayer.getDuration());
			
			TextView sizeTV = (TextView) findViewById(R.id.recordFileSizeTV);
			sizeTV.setText(getString(R.string.recordFileSize)
					       .replace("%SIZE", fileSize));
			
			TextView durationTV = (TextView) findViewById(R.id.recordFileDurationTV);
			durationTV.setText(getString(R.string.recrodFileDuration)
							   .replace("%DURATION", duration));
		} else {
			setRecordDetailsNotAvailable();
		}
	}

	private String getFileDuration(double duration) {
		String result = "";
		
		int hours = (int) ((duration / MILISEC_TO_HOURS) % HOURS_PER_DAY);
		int min = (int) ((duration / MILISEC_TO_MIN) % MIN_PER_HOUR);
        int sec = (int) ((duration  / MILISEC_TO_SEC)  % SEC_PER_MIN);
        
        if(hours != 0) {
        	result = String.format(Locale.GERMAN, "%d,%d" + HOURS_ENDING, hours, min);
        } else if(min != 0) {
        	result = String.format(Locale.GERMAN, "%d,%d" + MINUTES_ENDING, min, sec);
        } else {
        	result = String.format(Locale.GERMAN, "%d" + SECONDS_ENDING, sec);
        }
		
		return result;
	}

	private String getFileSize(double size) {
		size = size / BYTES_UNIT;
		String unit = KILOBYTE_ENDING;
		
		if(size > BYTES_UNIT) {
			size = size / BYTES_UNIT;
			unit = MEGABYTE_ENDING;
		}
		
		return String.format("%.2f" + unit, size);
	}

	private void setRecordDetailsNotAvailable() {
		TextView sizeTV = (TextView) findViewById(R.id.recordFileSizeTV);
		sizeTV.setText(getString(R.string.recordFileSize)
				       .replace("%SIZE", getString(R.string.notAvailable)));
		
		TextView durationTV = (TextView) findViewById(R.id.recordFileDurationTV);
		durationTV.setText(getString(R.string.recrodFileDuration)
						   .replace("%DURATION", getString(R.string.notAvailable)));
	}

	public void onButtonClicked(View v) {
		switch(v.getId()) {
		case R.id.playPauseRecordBtn:
			if(!playing) {
				playRecord();
			} else {
				pauseRecord();
			}
			break;
		case R.id.stopRecordBtn:
			stopPlayback();
			break;
		case R.id.showLineChartBtn:
			showLineChartForRecord();
			break;
		case R.id.deleteRecordBtn:
			deleteRecord();
			break;
		case R.id.deleteUserDataBtn:
			deleteUserData();
			break;
		}
	}

	private void deleteUserData() {
		DatabaseAdapter dba = new DatabaseAdapter(this);
		dba.deleteRecordDataByID(record.getID());

		if(record.getPath() == null) {
			Intent i = new Intent(this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		} else {
			showLineChartBtn.setEnabled(false);
			showLineChartBtn.setText(R.string.noVolumeDataFound);
			deleteUserDataBtn.setEnabled(false);
			record.deleteRecordID();
		}
	}

	private void deleteRecord() {
		File RECORD_DIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
			   					   getString(R.string.app_directory) +
		   					   	   getString(R.string.record_directory));
		File[] files = RECORD_DIR.listFiles();
		for(File file : files) {
			if(file.getName().compareTo((record.getName() + getString(R.string.record_file_ending))) == 0) {
				file.delete();
				break;
			}
		}
		if(record.getID() == null) {
			Intent i = new Intent(this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		} else {
			playPauseRecordBtn.setEnabled(false);
			stopRecordBtn.setEnabled(false);
			deleteRecordBtn.setEnabled(false);
			record.deleteRecordPath();
			setRecordDetailsNotAvailable();
			recSeekBar.setEnabled(false);
			stopPlayback();
		}
	}

	private void playRecord() {
		if(seekBarProgress == null) {
			seekBarProgress = new SeekBarProgress();
			seekBarProgress.start();
		} else {
			seekBarProgress.unpause();
		}
		
		mediaPlayer.start();
		
		playing = true;
		playPauseRecordBtn.setText(R.string.pause);
		showLineChartBtn.setEnabled(false);
	}
	
	private void pauseRecord() {
		mediaPlayer.pause();
		seekBarProgress.pause();
		
		playing = false;
		playPauseRecordBtn.setText(R.string.play);
		if(record.getID() != null) {
			showLineChartBtn.setEnabled(true);
		}
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
		curPosTV.setText(R.string.currentPositionStartValue);
		
		playing = false;
		playPauseRecordBtn.setText(R.string.play);
		if(record.getID() != null) {
			showLineChartBtn.setEnabled(true);
		}
	}

	private void showLineChartForRecord() {
		DatabaseAdapter dba = new DatabaseAdapter(getApplicationContext());
		HashMap<Date, Integer> volume_result = dba.selectAllVolumeByRecordID(record.getID());
		HashMap<Date, Double> acc_result = dba.selectAllAccValuesByRecordID(record.getID());
		
		LineChart lineChart = new LineChart(volume_result, acc_result);
		Intent lineIntent = lineChart.getIntent(this);
		startActivity(lineIntent);
	}
	
	private String getTimeFromMS(long ms) {
		StringBuilder sb = new StringBuilder();
		sb.append((ms / MILISEC_TO_HOURS) % HOURS_PER_DAY + ":");
		sb.append((ms / MILISEC_TO_MIN) % MIN_PER_HOUR + ":");
        sb.append((ms / MILISEC_TO_SEC)  % SEC_PER_MIN);
        
        return sb.toString();
	}
	
	class SeekBarProgress extends Thread {
		
		private Boolean done = false;
		private Boolean paused = false;
		private Lock pauseLock = new ReentrantLock();
		private Condition pauseCond = pauseLock.newCondition();
		
		@Override
		public void run() {
			int mCurrentPosition;
			
			while(!done && mediaPlayer != null) {
				pauseLock.lock();
				while(paused) {
					try {
						pauseCond.await();
					} catch (InterruptedException e) {}
					if(done) {
						return;
					}
				}
				mCurrentPosition = mediaPlayer.getCurrentPosition();
	            recSeekBar.setProgress(mCurrentPosition);
	            
	            final String curPos = getTimeFromMS(mCurrentPosition);
	            
	            runOnUiThread(new Runnable() {
					@Override
					public void run() {
						curPosTV.setText(curPos);
					}
	            });
	            pauseLock.unlock();
	            
	            try {
					Thread.sleep(SLEEP_BETWEEN_SEEKBAR_UPDATE);
				} catch (InterruptedException e) {}
			}
		}
		
		public void pause() {
			this.paused = true;
		}
		
		public void unpause() {
			pauseLock.lock();
			this.paused = false;
			this.pauseCond.signal();
			pauseLock.unlock();
		}
		
		public void stopProcessing() {
			pauseLock.lock();
			done = true;
			this.pauseCond.signal();
			pauseLock.unlock();
		}
	}
}
