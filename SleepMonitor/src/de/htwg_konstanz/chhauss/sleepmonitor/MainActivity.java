package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
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


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
	ActionBar actionBar;
	ViewPager viewPager;
	FragmentPageAdapter fpa;
	
	private int group1Id = 1;

	int resetDatabaseId = Menu.FIRST;
	int deleteRecordsId = Menu.FIRST +1;
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(group1Id, resetDatabaseId, resetDatabaseId, getString(R.string.resetDatabase));
	    menu.add(group1Id, deleteRecordsId, deleteRecordsId, getString(R.string.deleteAllRecords));

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
	default:
		return super.onOptionsItemSelected(item);
    }
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
        actionBar.addTab(actionBar.newTab().setText(R.string.sleepMonitoring).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.myRecords).setTabListener(this));
        
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int itemID) {
				actionBar.setSelectedNavigationItem(itemID);
				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    }

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
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
	
	private class FragmentPageAdapter extends FragmentPagerAdapter {

		public FragmentPageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int itemID) {
			switch (itemID) {
			case 0:
				return new SleepMonitoring();
			case 1:
				return new MyRecords();
			default:
				return new SleepMonitoring();
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

	}
}
