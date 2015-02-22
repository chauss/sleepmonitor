package de.htwg_konstanz.chhauss.sleepmonitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class DatabaseAdapter {

    private static final String DATE_TIME_FORMAT = "dd_MM_yyyy_HH_mm_ss";
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private SimpleDateFormat dateFormatter;
	
	public DatabaseAdapter(Context context) {
	    String app_dir = Environment.getExternalStorageDirectory() +
	    		         context.getString(R.string.app_directory);
		dbHelper = new DatabaseHelper(context, app_dir);
		dateFormatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
	}
	
	public long insertVolume(String dateAndTime, int volume, String recordID) {
		if(db == null || !db.isOpen()){
			db = dbHelper.getWritableDatabase();
		}
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.DATEANDTIME, dateAndTime);
		cv.put(DatabaseHelper.VOLUME, volume);
		cv.put(DatabaseHelper.RECORD_ID, recordID);
		long id = db.insert(DatabaseHelper.TABLE_NAME, null, cv);
		
		return id;
	}
	
	public HashMap<Date, Integer> selectAllByRecordID(String recordID) {
		if(db == null || !db.isOpen()){
			db = dbHelper.getWritableDatabase();
		}
		String[] columns = {DatabaseHelper.DATEANDTIME, DatabaseHelper.VOLUME, DatabaseHelper.RECORD_ID};
		
		Cursor cursor = db.query(DatabaseHelper.TABLE_NAME,
								 columns,
								 DatabaseHelper.RECORD_ID + " = '" + recordID + "'",
							 	 null, null, null, null);
		
		if(cursor.getColumnCount() == 0) {
		    return null;
		}
		
		int dateTimeColumnIdx = cursor.getColumnIndex(DatabaseHelper.DATEANDTIME);
		int volumeColumnIdx = cursor.getColumnIndex(DatabaseHelper.VOLUME);
		String dateAndTime;
		int volume;
		HashMap<Date, Integer> result = new HashMap<Date, Integer>();
		
		while(cursor.moveToNext()) {
			dateAndTime = cursor.getString(dateTimeColumnIdx);
			volume = cursor.getInt(volumeColumnIdx);
			
			try {
                result.put(dateFormatter.parse(dateAndTime), volume);
            } catch (ParseException e) {
                e.printStackTrace();
            }
		}
		db.close();
		return result;
	}
	
	public void closeDatabase() {
	    if(db != null && db.isOpen()) {
	        db.close();
	    }
	}
	
	static class DatabaseHelper extends SQLiteOpenHelper{
		private static final String DB_NAME = "sleepmonitor.db";
		private static final int DB_VERSION = 1;
		
		private static final String TABLE_NAME = "SLEEPVOLUME";
		private static final String UID = "_id";
		private static final String DATEANDTIME = "DateAndTime";
		private static final String VOLUME = "Volume";
		private static final String RECORD_ID = "RecordID";
		
		
		public DatabaseHelper(Context context, String app_dir) {
			super(context, app_dir + "/" + DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL("CREATE TABLE " + TABLE_NAME +
						   "(" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						   DATEANDTIME + " VARCHAR(20)," +
						   VOLUME + " INTEGER," +
						   RECORD_ID + " VARCHAR(20));");
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}
