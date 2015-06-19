package de.htwg_konstanz.chhauss.sleepmonitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseAdapter {

    private static final String DATE_TIME_FORMAT = "dd_MM_yyyy_HH_mm_ss";
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private SimpleDateFormat dateFormatter;
	
	public DatabaseAdapter(Context context) {
	    dbHelper = new DatabaseHelper(context);
		dateFormatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
	}
	
	public long insertDataValues(String dateAndTime, double x, double y, double z, int volume, String recordID) {
		openDatabase();
		
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.DATEANDTIME, dateAndTime);
        cv.put(DatabaseHelper.ACC_X, x);
        cv.put(DatabaseHelper.ACC_Y, y);
        cv.put(DatabaseHelper.ACC_Z, z);
        cv.put(DatabaseHelper.VOLUME, volume);
        cv.put(DatabaseHelper.RECORD_ID, recordID);
        long id = db.insert(DatabaseHelper.TABLE_DATA, null, cv);
        
        return id;
    }
	
	public int deleteRecordDataByID(String recordID) {
		openDatabase();
		int count = db.delete(DatabaseHelper.TABLE_DATA, DatabaseHelper.RECORD_ID + "=?", new String[] {recordID});
		closeDatabase();
		return count;
	}
	
	public HashMap<Date, Integer> selectAllVolumeByRecordID(String recordID) {
		openDatabase();
		
		String[] columns = {DatabaseHelper.DATEANDTIME, DatabaseHelper.VOLUME, DatabaseHelper.RECORD_ID};
		Cursor cursor = db.query(DatabaseHelper.TABLE_DATA,
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
		closeDatabase();
		
		return result;
	}
	
	public HashMap<Date, Double> selectAllAccValuesByRecordID(String recordID) {
	    openDatabase();
	    
	    String[] columns = {DatabaseHelper.DATEANDTIME,
	                        DatabaseHelper.ACC_X,
	                        DatabaseHelper.ACC_Y,
	                        DatabaseHelper.ACC_Z,
	                        DatabaseHelper.RECORD_ID};
        Cursor cursor = db.query(DatabaseHelper.TABLE_DATA,
                                 columns,
                                 DatabaseHelper.RECORD_ID + " = '" + recordID + "'",
                                 null, null, null, null);
        if(cursor.getColumnCount() == 0) {
            return null;
        }
        
        int dateTimeColumnIdx = cursor.getColumnIndex(DatabaseHelper.DATEANDTIME);
        int acc_xIdx = cursor.getColumnIndex(DatabaseHelper.ACC_X);
        int acc_yIdx = cursor.getColumnIndex(DatabaseHelper.ACC_Y);
        int acc_zIdx = cursor.getColumnIndex(DatabaseHelper.ACC_Z);
        String dateAndTime;
        Double xyz;
        
        HashMap<Date, Double> result = new HashMap<Date, Double>();
        
        while(cursor.moveToNext()) {
            dateAndTime = cursor.getString(dateTimeColumnIdx);
            xyz = cursor.getDouble(acc_xIdx) + 
                  cursor.getDouble(acc_yIdx) +
                  cursor.getDouble(acc_zIdx);
            try {
                result.put(dateFormatter.parse(dateAndTime), xyz);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
	    
	    closeDatabase();
        return result;
	}
	
	public ArrayList<String> selectAllRecordIDs() {
		openDatabase();
		
		String[] columns = {DatabaseHelper.RECORD_ID};
		Cursor cursor = db.query(true, DatabaseHelper.TABLE_DATA,
								 columns,
								 null, null, null, null, null, null);
		if(cursor.getColumnCount() == 0) {
		    return null;
		}
		
		int recordIDColumIdx = cursor.getColumnIndex(DatabaseHelper.RECORD_ID);
		String recordID;
		ArrayList<String> result = new ArrayList<String>();
		
		while(cursor.moveToNext()) {
			recordID = cursor.getString(recordIDColumIdx);
			result.add(recordID);
		}
		closeDatabase();
		
		return result;
	}
	
	private void openDatabase() {
		if(db == null || !db.isOpen()){
			db = dbHelper.getWritableDatabase();
		}
	}
	
	public void closeDatabase() {
	    if(db != null && db.isOpen()) {
	        db.close();
	    }
	}
	
	public void resetDatabase() {
		openDatabase();
		db.delete(DatabaseHelper.TABLE_DATA, null, null);
	}
	
	static class DatabaseHelper extends SQLiteOpenHelper{
		private static final String DB_NAME = "sleepmonitor.db";
		private static final int DB_VERSION = 1;
		
		private static final String TABLE_DATA = "DATA";
		private static final String UID = "_id";
		private static final String DATEANDTIME = "DateAndTime";
		private static final String ACC_X = "Acc_x";
		private static final String ACC_Y = "Acc_y";
		private static final String ACC_Z = "Acc_z";
		private static final String VOLUME = "Volume";
		private static final String RECORD_ID = "RecordID";
		
		
		
		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL("CREATE TABLE " + TABLE_DATA +
						   "(" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						   DATEANDTIME + " VARCHAR(20)," +
						   ACC_X + " REAL," +
						   ACC_Y + " REAL," +
						   ACC_Z + " REAL," +
						   VOLUME + " INTEGER," +
						   RECORD_ID + " VARCHAR(20));");
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
			onCreate(db);
		}
	}
}
