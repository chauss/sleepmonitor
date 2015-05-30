package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class AlarmReceiver extends Activity {
	private static String TAG = "AlarmReceiver";

	private MediaPlayer mediaPlayer;
	private PowerManager.WakeLock wakeLock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "SleepMonitor_AlarmReceiver_WakeLock");
        wakeLock.acquire();
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
        						  WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
        						  WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
        						  WindowManager.LayoutParams.FLAG_FULLSCREEN |
        						  WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
        						  WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_alarmreceiver);
        
        playSound();
	}

	private void playSound() {
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(this, getAlarmUri());
			final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			if(audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mediaPlayer.prepare();
				mediaPlayer.start();
			}
		} catch (IOException e) {
			Log.i("AlarmReceiver", "No audio files are found");
		}
	}

	private Uri getAlarmUri() {
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if(alert == null) {
			alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			if(alert == null) {
				alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			}
		}
		return alert;
	}

	public void stopAlarmBtnClicked(View v) {
		mediaPlayer.stop();
		finish();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		wakeLock.release();
	}

}
