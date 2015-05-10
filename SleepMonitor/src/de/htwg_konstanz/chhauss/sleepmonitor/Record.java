package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

public class Record implements Parcelable{

	private static final String toStringPattern = "dd.MM.yyyy_HH:mm:ss";
	private String recordPath;
	private String recordID; //The recordID is a link to the volume data in the database
	private String name; // Usually the same as recordID, but this one is a must have

	public Record(String record_path, String recordID, String name) {
		if((record_path == null && recordID == null) || name == null) {
			throw new IllegalArgumentException("Record must have a name and either a recordPath or recordID or both");
		}
		this.recordPath = record_path;
		this.recordID = recordID;
		this.name = name;
	}
	
	public Record(Parcel in) {
		recordPath = in.readString();
		recordID = in.readString();
		this.name = in.readString();
	}
	
	public String getPath() {
		return recordPath;
	}
	
	public String getID() {
		return recordID;
	}
	
	public String getName() {
		return name;
	}
	
	public void deleteRecordID() {
		if(this.recordPath == null) {
			throw new IllegalStateException("Can't delete recordID when the record has no recordPath!");
		}
		this.recordID = null;
	}
	
	public void deleteRecordPath() {
		if(this.recordID == null) {
			throw new IllegalStateException("Can't delete recordPath when the record has no recordID!");
		}
		this.recordPath = null;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat toResultFormatter = new SimpleDateFormat(toStringPattern, Locale.getDefault());
		if(recordID != null) {
			SimpleDateFormat df = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault());
			try {
				Date date = df.parse(recordID);
				return toResultFormatter.format(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} // No else: if the try-catch above fails it tries the way below
		if(recordPath != null) {
			File record = new File(recordPath);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(record.lastModified());
			
			return toResultFormatter.format(calendar.getTime());
		}
		return "Failed to get RecordName";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(recordPath);
		dest.writeString(recordID);
		dest.writeString(name);
	}
	
	public static final Parcelable.Creator<Record> CREATOR = new Parcelable.Creator<Record>() {
		public Record createFromParcel(Parcel in) {
			return new Record(in);
		}
		
		public Record[] newArray(int size) {
			return new Record[size];
		}
	};

}
