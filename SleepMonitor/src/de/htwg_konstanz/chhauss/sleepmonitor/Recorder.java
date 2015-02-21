package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;

public class Recorder {
	private MediaRecorder mRecorder;
	private String OUTPUT_FILE;
	
	public Recorder(String output_file) {
		mRecorder = null;
		this.OUTPUT_FILE = output_file;
	}
	
	public void start() throws IllegalStateException, IOException {
		if (mRecorder == null) {
			createRecordFile();
		    mRecorder = new MediaRecorder();
		    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile(OUTPUT_FILE); 
			mRecorder.prepare();
			mRecorder.start();
        }
    }
        
    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }
        
    public double getAmplitude() {
        if (mRecorder != null)
            return  (mRecorder.getMaxAmplitude());
        else
            return 0;
    }
    
    private void createRecordFile() throws IOException {
    	File record = new File(OUTPUT_FILE);
    	if(!record.exists()) {
			record.createNewFile();
    	}
    }
}