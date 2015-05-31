package de.htwg_konstanz.chhauss.sleepmonitor;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AlarmClock extends Fragment implements OnValueChangeListener, OnClickListener {

	public static final String ALARM_PREFERENCES = "sm_alarm_preferences";
	public static final String ALARMSTATE_KEY = "alarmstate_sp_key";
	public static final String ALARM_STARTHOUR_KEY = "alarm_starthour_sp_key";
	public static final String ALARM_STARTMIN_KEY = "alarm_startmin_sp_key";
	public static final String ALARM_ENDHOUR_KEY = "alarm_endhour_sp_key";
	public static final String ALARM_ENDMIN_KEY = "alarm_endmin_sp_key";
	
	private static final int DEFAULT_START_HOUR = 7;
	private static final int DEFAULT_START_MINUTE = 30;
	private static final int DEFAULT_END_HOUR = 8;
	private static final int DEFAULT_END_MINUTE = 0;
	
	private NumberPicker startHourNP;
	private NumberPicker startMinuteNP;
	private NumberPicker endHourNP;
	private NumberPicker endMinuteNP;
	private ToggleButton alarmOnOffBtn;
	private TextView alarmIntervalTV;
	
	private int startHour;
	private int startMinute;
	private int endHour;
	private int endMinute;
	
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
		
		setupNumberPickers();
		
		return v;
	}
	
	private void saveAlarmInformation(boolean state) {
		SharedPreferences.Editor spEditor = thisActivity.getSharedPreferences(ALARM_PREFERENCES, 
				                                                              Context.MODE_PRIVATE).edit();
		spEditor.putBoolean(ALARMSTATE_KEY, state);
		spEditor.putInt(ALARM_STARTHOUR_KEY, startHour);
		spEditor.putInt(ALARM_STARTMIN_KEY, startMinute);
		spEditor.putInt(ALARM_ENDHOUR_KEY, endHour);
		spEditor.putInt(ALARM_ENDMIN_KEY, endMinute);
		spEditor.commit();
	}
	
	private boolean getAlarmState() {
		SharedPreferences sp = thisActivity.getSharedPreferences(ALARM_PREFERENCES, Context.MODE_PRIVATE);
		startHour = sp.getInt(ALARM_STARTHOUR_KEY, DEFAULT_START_HOUR);
		startMinute = sp.getInt(ALARM_STARTMIN_KEY, DEFAULT_START_MINUTE);
		endHour = sp.getInt(ALARM_ENDHOUR_KEY, DEFAULT_END_HOUR);
		endMinute = sp.getInt(ALARM_ENDMIN_KEY, DEFAULT_END_MINUTE);
		return sp.getBoolean(ALARMSTATE_KEY, false);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		boolean alarmState = getAlarmState();
		enableAlarmTimeChange(!alarmState);
		alarmOnOffBtn.setChecked(alarmState);
		initNumberPickers();
		updateAlarmIntervalTV();
	}

	private void initNumberPickers() {
		startHourNP.setValue(startHour);
		startMinuteNP.setValue(startMinute);
		endHourNP.setValue(endHour);
		endMinuteNP.setValue(endMinute);
	}
	
	@Override
	public void onClick(View v) {
		if(alarmOnOffBtn.isChecked()) {
			enableAlarmTimeChange(false);
			saveAlarmInformation(true);
		} else {
			enableAlarmTimeChange(true);
			saveAlarmInformation(false);
		}
	}

	private void enableAlarmTimeChange(boolean enabled) {
		startHourNP.setEnabled(enabled);
		startMinuteNP.setEnabled(enabled);
		endHourNP.setEnabled(enabled);
		endMinuteNP.setEnabled(enabled);
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
	
	private void setupNumberPickers() {
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
