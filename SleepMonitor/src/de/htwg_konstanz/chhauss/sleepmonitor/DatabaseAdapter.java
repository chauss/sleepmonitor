package de.htwg_konstanz.chhauss.sleepmonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseAdapter {

	private static final int databaseCloseTimeout = 10;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	public DatabaseAdapter(Context context) {
		dbHelper = new DatabaseHelper(context);
	}
	
	public long insertVolume(String dateAndTime, int volume) {
		if(db == null || !db.isOpen()){
			db = dbHelper.getWritableDatabase();
		}
		ContentValues cv = new ContentValues();
		cv.put(DatabaseHelper.DATEANDTIME, dateAndTime);
		cv.put(DatabaseHelper.VOLUME, volume);
		long id = db.insert(DatabaseHelper.TABLE_NAME, null, cv);
		
		return id;
	}
	
	public void selectAllByDate(String date) {
		if(db == null || !db.isOpen()){
			db = dbHelper.getWritableDatabase();
		}
		String[] columns = {DatabaseHelper.DATEANDTIME, DatabaseHelper.VOLUME};
		
		Cursor cursor = db.query(DatabaseHelper.TABLE_NAME,
								 columns,
								 DatabaseHelper.DATEANDTIME + " LIKE '" + date + "_%'",
							 	 null, null, null, null);
		int dateTimeColumnIdx = cursor.getColumnIndex(DatabaseHelper.DATEANDTIME);
		int volumeColumnIdx = cursor.getColumnIndex(DatabaseHelper.VOLUME);
		String dateAndTime;
		int volume;
		while(cursor.moveToNext()) {
			dateAndTime = cursor.getString(dateTimeColumnIdx);
			volume = cursor.getInt(volumeColumnIdx);
			
			System.out.printf("Um %s war die Lautstärke: %d\n", dateAndTime, volume);
		}
		db.close();
	}
	
	static class DatabaseHelper extends SQLiteOpenHelper{
		private static final String DB_NAME = "sleepmonitor.db";
		private static final int DB_VERSION = 5;
		
		private static final String TABLE_NAME = "SLEEPVOLUME";
		private static final String UID = "_id";
		private static final String DATEANDTIME = "DateAndTime";
		private static final String VOLUME = "Volume";
		
		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL("CREATE TABLE " + TABLE_NAME +
						   "(" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
						   DATEANDTIME + " VARCHAR(20)," +
						   VOLUME + " INTEGER);");
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
