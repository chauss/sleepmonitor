package de.htwg_konstanz.chhauss.sleepmonitor;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.graphics.Paint.Join;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;

import android.widget.TextView;

public class AlarmClock extends Fragment implements OnValueChangeListener {

	private NumberPicker startHourNP;
	private NumberPicker startMinuteNP;
	private NumberPicker endHourNP;
	private NumberPicker endMinuteNP;
	
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
		
		initNumberPickers();
		updateAlarmIntervalTV();
		
		return v;
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
