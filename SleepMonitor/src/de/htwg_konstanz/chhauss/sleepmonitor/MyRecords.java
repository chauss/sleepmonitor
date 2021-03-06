package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;


public class MyRecords extends ListFragment {
	
	private File RECORD_DIR;
	
	private Activity thisActivity;
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		thisActivity = activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		RECORD_DIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
				getString(R.string.app_directory) +
				getString(R.string.record_directory));
		View v = inflater.inflate(R.layout.fragment_myrecords, container, false);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		getListView().setOnItemClickListener(new OnItemClickListener() {
			
				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					Record record = (Record) parent.getItemAtPosition(position);
					Intent intent = new Intent(parent.getContext(), RecordDetails.class);
					intent.putExtra("record", record);
					startActivity(intent);
				}
			});
		
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		refreshListAdapter();
		super.onResume();
	}

	public void refreshListAdapter() {
		Record[] records = getAllRecords();
		
		ArrayAdapter<Record> adapter = new ArrayAdapter<Record>(thisActivity,
																android.R.layout.simple_list_item_1,
																records);
		adapter.sort(new Comparator<Record>() {

			@Override
			public int compare(Record r1, Record r2) {
				// This changes the order of the list view: newest on top
				return r2.toString().compareTo(r1.toString());
			}
		});
		setListAdapter(adapter);
	}

	private Record[] getAllRecords() {
		File[] files = getAllRecordFiles();
		
		// Get all record data from the database
		DatabaseAdapter dba = new DatabaseAdapter(thisActivity);
		ArrayList<String> recordIDs = dba.selectAllRecordIDs();
		
		ArrayList<Record> records = mergeFilesAndVolumeData(files, recordIDs);
		
		return records.toArray(new Record[records.size()]);
	}

	private ArrayList<Record> mergeFilesAndVolumeData(File[] files, ArrayList<String> recordIDs) {
		// Put all file, volumeData(here optional) pairs in a list (as Record-Objects)
		ArrayList<Record> records = new ArrayList<Record>();
		String recordID;
		String name;
		
		for(int i = 0; i < files.length; i++) {
			name = recordID = removeFileExtension(files[i].getName());
			if(recordIDs.contains(recordID)) {
				recordIDs.remove(recordID);
			} else {
				recordID = null;
			}
			records.add(new Record(files[i].getAbsolutePath(), recordID, name));
		}
		
		// Add all remaining volumeDatas with no record file to the list
		for(int i = 0; i < recordIDs.size(); i++) {
			name = recordID = recordIDs.get(i);
			records.add(new Record(null, recordID, name));
		}
		return records;
	}

	private File[] getAllRecordFiles() {
		FileFilter recordFilter = new FileFilter() {
		    @Override
		    public boolean accept(File file) {
		    	// Filter to match only files with the record-file ending
		        return file.getName().endsWith(getString(R.string.record_file_ending));
		    }
		};
		File[] files = (File[]) RECORD_DIR.listFiles(recordFilter);

		return files;
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
