package de.htwg_konstanz.chhauss.sleepmonitor;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AlarmClock extends Fragment implements OnValueChangeListener, OnClickListener {

	private NumberPicker startHourNP;
	private NumberPicker startMinuteNP;
	private NumberPicker endHourNP;
	private NumberPicker endMinuteNP;
	private ToggleButton alarmOnOffBtn;
	private TextView alarmIntervalTV;
	
	private int startHour = 7;
	private int startMinute = 30;
	private int endHour = 8;
	private int endMinute = 0;
	
	private Activity thisActivity;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		thisActivity = activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_alarmclock, container, false);
		
		startHourNP = (NumberPicker) v.findViewById(R.id.start_hourNP);
		startMinuteNP = (NumberPicker) v.findViewById(R.id.start_minuteNP);
		endHourNP = (NumberPicker) v.findViewById(R.id.end_hourNP);
		endMinuteNP = (NumberPicker) v.findViewById(R.id.end_minuteNP);
		alarmIntervalTV = (TextView) v.findViewById(R.id.alarmIntervalTV);
		alarmOnOffBtn = (ToggleButton) v.findViewById(R.id.alarmOnOffBtn);
		alarmOnOffBtn.setOnClickListener(this);
		
		initNumberPickers();
		updateAlarmIntervalTV();
		
		return v;
	}
	
	private void startAlarm() {
		AlarmManager am = (AlarmManager) thisActivity.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, getAlarmPendingIntent());
		
		Toast.makeText(thisActivity, "Alarm goes of in 5 seconds", Toast.LENGTH_SHORT).show();
	}
	
	private void cancelAlarm() {
		AlarmManager am = (AlarmManager) thisActivity.getSystemService(Context.ALARM_SERVICE);
		am.cancel(getAlarmPendingIntent());
		
		Toast.makeText(thisActivity, "Alarm canceled", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onClick(View v) {
		if(alarmOnOffBtn.isChecked()) {
			startAlarm();
			enableAlarmTimeChange(false);
		} else {
			cancelAlarm();
			enableAlarmTimeChange(true);
		}
	}

	private void enableAlarmTimeChange(boolean enabled) {
		startHourNP.setEnabled(enabled);
		startMinuteNP.setEnabled(enabled);
		endHourNP.setEnabled(enabled);
		endMinuteNP.setEnabled(enabled);
	}
	
	private PendingIntent getAlarmPendingIntent() {
		Intent intent = new Intent(thisActivity, AlarmReceiver.class);
		return PendingIntent.getActivity(thisActivity, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	
	private void updateAlarmIntervalTV() {
		DateTime start = new DateTime().withHourOfDay(startHour).withMinuteOfHour(startMinute);
		DateTime end = new DateTime().withHourOfDay(endHour).withMinuteOfHour(endMinute);
		
		if(end.isBefore(start)) {
			end = end.plusDays(1);
		}
		
		Interval diff = new Interval(start, end);
		
		alarmIntervalTV.setText(String.format("Interval: %d:%02d - %d:%02d (%dh %dmin)",
										      startHour, startMinute, endHour, endMinute,
										      diff.toPeriod().getHours(), diff.toPeriod().getMinutes()));
	}
	
	private void initNumberPickers() {
		startHourNP.setMinValue(0);
		startHourNP.setMaxValue(23);
		startHourNP.setOnValueChangedListener(this);
		
		startMinuteNP.setMinValue(0);
		startMinuteNP.setMaxValue(59);
		startMinuteNP.setOnValueChangedListener(this);
		
		endHourNP.setMinValue(0);
		endHourNP.setMaxValue(23);
		endHourNP.setOnValueChangedListener(this);
		
		endMinuteNP.setMinValue(0);
		endMinuteNP.setMaxValue(59);
		endMinuteNP.setOnValueChangedListener(this);
		
		startHourNP.setValue(startHour);
		startMinuteNP.setValue(startMinute);
		endHourNP.setValue(endHour);
		endMinuteNP.setValue(endMinute);
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		if(picker == startHourNP) {
			startHour = newVal;
		} else if(picker == startMinuteNP) {
			startMinute = newVal;
		} else if(picker == endHourNP) {
			endHour = newVal;
		} else if(picker == endMinuteNP) {
			endMinute = newVal;
		}
		updateAlarmIntervalTV();
	}
}
