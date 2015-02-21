package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import android.app.ListActivity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class MyRecords extends ListActivity{
	
	private MediaPlayer mediaPlayer;
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
				Toast.makeText(v.getContext(),
							   String.format("%d", parent.getPositionForView(v)),
							   Toast.LENGTH_SHORT).show();
				File record = (File) parent.getItemAtPosition(position);
				try {
					playRecord(record.getAbsolutePath());
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
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

	private void playRecord(String record_path) throws IllegalStateException, IOException {
		if(mediaPlayer != null) {
			stopPlayback();
		}
		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				stopPlayback();
			}
		});
		
		mediaPlayer.setDataSource(record_path);
		mediaPlayer.prepare();
		mediaPlayer.start();
	}
	
	private void stopPlayback() {
		mediaPlayer.stop();
		mediaPlayer.reset();
		mediaPlayer.release();
		mediaPlayer = null;
	}
}
