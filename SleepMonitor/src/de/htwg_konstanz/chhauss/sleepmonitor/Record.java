package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Record extends File{

	private static final long serialVersionUID = 1L;

	public Record(String path) {
		super(path);
	}
	
	@Override
	public String toString() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(this.lastModified());
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy_HH:mm:ss", Locale.getDefault());
		
		return dateFormatter.format(calendar.getTime());
	}

}
