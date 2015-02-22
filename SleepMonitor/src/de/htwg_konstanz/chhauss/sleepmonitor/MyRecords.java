package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;
import java.io.FileFilter;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;


public class MyRecords extends ListActivity{
	
	private File MEDIA_DIR;
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        MEDIA_DIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
        			  	 	 getString(R.string.app_directory) +
        			  	 	 getString(R.string.record_directory));
        
        getListView().setOnItemClickListener(new OnItemClickListener() {
        	
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				File record = (File) parent.getItemAtPosition(position);
				Intent intent = new Intent(parent.getContext(), RecordDetails.class);
				intent.putExtra("recordPath", record.getAbsolutePath());
				intent.putExtra("recordID", removeFileExtension(record.getName()));
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Record[] records = getAllRecords();
		
		ArrayAdapter<Record> adapter = new ArrayAdapter<Record>(this, android.R.layout.simple_list_item_1, records);
		setListAdapter(adapter);
	}
	
	private Record[] getAllRecords() {
		FileFilter recordFilter = new FileFilter() {
		    @Override
		    public boolean accept(File file) {
		        return file.getAbsolutePath().matches(".*\\" + getString(R.string.record_file_ending));
		    }
		};
		
		File[] files = (File[]) MEDIA_DIR.listFiles(recordFilter);
		Record[] records = new Record[files.length];
		for (int i = 0; i < files.length; i++) {
			records[i] = new Record(files[i].getAbsolutePath());
		}
		
		return records;
	}
	
	private String removeFileExtension(String filename) {
        if (filename == null) return null;
        int pos = filename.lastIndexOf(".");
        
        if (pos == -1) {
        	return filename;
        }
        return filename.substring(0, pos);
    }

}
