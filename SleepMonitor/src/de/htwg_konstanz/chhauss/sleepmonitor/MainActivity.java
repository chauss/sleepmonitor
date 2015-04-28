package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        createAppDirectory();
    }

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

    public void onButtonClicked(View v) {
    	switch(v.getId()) {
    	case R.id.toSleepMonitoringBtn:
    		startActivity(new Intent(this, SleepMonitoring.class));
    		break;
    	case R.id.toMyRecordsBtn:
    		startActivity(new Intent(this, MyRecords.class));
    		break;
    	case R.id.resetDatabaseBtn:
    		resetDataBase();
    		break;
    	case R.id.deleteAllRecordsBtn:
    		deleteAllRecords();
    		break;
    	case R.id.noiseMeterBtn:
    		NoiseMeter nm = new NoiseMeter();
    		startActivity(nm.getIntent(this));
    		break;
    	}
    }

    private void resetDataBase() {
		DatabaseAdapter dba = new DatabaseAdapter(this);
		dba.resetDatabase();
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
		Toast.makeText(this,
				       String.format(getString(R.string.succRemovedAllRecordFiles), files.length),
				       Toast.LENGTH_SHORT).show();
	}
}
