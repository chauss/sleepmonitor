package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
	ActionBar actionBar;
	ViewPager viewPager;
	FragmentPageAdapter fpa;

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
		System.out.println("ontabselected");
		refreshMyRecords();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {}
	
	private void refreshMyRecords() {
		MyRecords myRecs = (MyRecords) getSupportFragmentManager().findFragmentById(android.R.id.list);
		System.out.println("myrecs: " + myRecs);
		if(myRecs != null) {
			System.out.println("refreshing");
			myRecs.refreshListAdapter();
		}
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
}
