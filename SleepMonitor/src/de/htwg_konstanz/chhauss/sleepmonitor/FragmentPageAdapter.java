package de.htwg_konstanz.chhauss.sleepmonitor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FragmentPageAdapter extends FragmentPagerAdapter {

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
