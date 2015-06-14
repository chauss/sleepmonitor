package de.htwg_konstanz.chhauss.sleepmonitor;

import static java.lang.Math.max;

import java.io.File;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener, SensorEventListener {
	
	private static final String devNull = "/dev/null";
	private static final int calibrationTime = 20;
	private ProgressDialog dialog;
	
	private int calibratedNoise = 0;
	private double calibratedMovement = 0;
	
	ActionBar actionBar;
	ViewPager viewPager;
	FragmentPageAdapter fpa;
	AlarmClock ac;
	SleepMonitoring sm;
	MyRecords mr;
	SensorManager sensorManager;
	
	private int group1Id = 1;

	int resetDatabaseId = Menu.FIRST;
	int deleteRecordsId = Menu.FIRST +1;
	int calibrateId = Menu.FIRST +2;
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(group1Id, resetDatabaseId, resetDatabaseId, getString(R.string.resetDatabase));
	    menu.add(group1Id, deleteRecordsId, deleteRecordsId, getString(R.string.deleteAllRecords));
	    menu.add(group1Id, calibrateId, calibrateId, getString(R.string.calibrate));

	    return super.onCreateOptionsMenu(menu); 
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {

	    switch (item.getItemId()) {
	    case 1:
	    	resetDatabase();
	    	return true;
		case 2:
			deleteAllRecords();
			return true;
		case 3:
			dialog = new ProgressDialog(MainActivity.this);
			initAccelerometer();
			new Thread(new Runnable() {
				@Override
				public void run() {
					calibrate();
				}
			}).start();
			return true;
		default:
			return super.onOptionsItemSelected(item);
	    }
	}
	
	private void initAccelerometer() {
	    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    
	    List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensorList.size() > 0) {
        	sensorManager.registerListener(this,
            							   sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            							   SensorManager.SENSOR_DELAY_GAME);
        } else {
            //TODO was passiert wenn man keinen accelerometer hat
        }
    }

    private void calibrate() {
    	dialog.setTitle("Calibrating...");
    	dialog.setMessage("Please turn around in bed");
    	dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	dialog.setProgress(0);
    	dialog.setMax(calibrationTime);
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				dialog.show();
			}
    	});
    	
    	Thread caliThread = new Thread(new Runnable() {

			@Override
			public void run() {
				Recorder rec = new Recorder(devNull);
				try {
					rec.start();
				} catch (Exception e) {}
				rec.getAmplitudeEMA(); // Get that 0 away from call at first time
				
				while(dialog.getProgress() < dialog.getMax()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
					dialog.incrementProgressBy(1);
				}
				sensorManager.unregisterListener(MainActivity.this);
				sensorManager = null;

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
				dialog.dismiss();
				
				calibratedNoise = (int) rec.getAmplitudeEMA();
				rec.stop();
				calibratedMovement = max(calibratedMovement - 10.2, 0);
				SharedPreferences.Editor spe = getSharedPreferences(AlarmClock.ALARM_PREFERENCES, Context.MODE_PRIVATE).edit();
				spe.putInt(AlarmClock.ALARM_NOISE_KEY, calibratedNoise);
				spe.putLong(AlarmClock.ALARM_MOVEMENT_KEY, Double.doubleToRawLongBits(calibratedMovement));
				spe.commit();
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this,
								       String.format("Calibration successful!\nMovement = %.2f\nNoise = %d", 
								    		         calibratedMovement, calibratedNoise),
								       Toast.LENGTH_SHORT).show();
					}
				});
			}
    	});
    	caliThread.start();
    	try {
			caliThread.join();
		} catch (InterruptedException e) {}
    	
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createAppDirectory();
        
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        fpa = new FragmentPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fpa);
        
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab().setText(R.string.alarmClock).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.sleepMonitoring).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.myRecords).setTabListener(this));
        
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int itemID) {
				actionBar.setSelectedNavigationItem(itemID);
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});
    }

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
		if(mr != null) {
			mr.refreshListAdapter();
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {}
	
	private void createAppDirectory() {
		String extStorageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		File app_dir = new File(extStorageDir + getString(R.string.app_directory));
        if(!app_dir.exists()) {
        	app_dir.mkdir();
        }
        
        File record_dir = new File(extStorageDir +
        						   getString(R.string.app_directory) +
        						   getString(R.string.record_directory));
        if(!record_dir.exists()) {
        	record_dir.mkdir();
        }
	}
	
	private void resetDatabase() {
		DatabaseAdapter dba = new DatabaseAdapter(this);
		dba.resetDatabase();
		if(mr != null) {
			mr.refreshListAdapter();
		}
		Toast.makeText(this, R.string.resetedDatabase, Toast.LENGTH_SHORT).show();
	}
    
	private void deleteAllRecords() {
		File RECORD_DIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
								   getString(R.string.app_directory) +
								   getString(R.string.record_directory));
		File[] files = RECORD_DIR.listFiles();
		for(File file : files) {
			file.delete();
		}
		if(mr != null) {
			mr.refreshListAdapter();
		}
		Toast.makeText(this,
				       String.format(getString(R.string.succRemovedAllRecordFiles), files.length),
				       Toast.LENGTH_SHORT).show();
	}
	
	private class FragmentPageAdapter extends FragmentPagerAdapter {

		public FragmentPageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int itemID) {
			switch (itemID) {
			case 0:
				ac = new AlarmClock();
				return ac;
			case 1:
				sm = new SleepMonitoring();
				return sm;
			case 2:
				mr = new MyRecords();
				return mr;
			default:
				sm = new SleepMonitoring();
				return sm;
			}
		}

		@Override
		public int getCount() {
			return 3;
		}

	}

	@Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            synchronized(RecordingService.class) {
            	calibratedMovement = max(calibratedMovement, event.values[0] + event.values[1] + event.values[2]);
            }
        }
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
