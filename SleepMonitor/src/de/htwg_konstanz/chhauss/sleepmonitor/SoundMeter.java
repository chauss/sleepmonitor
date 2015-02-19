package de.htwg_konstanz.chhauss.sleepmonitor;

import java.io.IOException;

import android.media.MediaRecorder;

public class SoundMeter {
	private MediaRecorder mRecorder;
	private String OUTPUT_FILE;
	
	public SoundMeter(String output_file) {
		mRecorder = null;
		this.OUTPUT_FILE = output_file;
	}
	
	public void start() throws IllegalStateException, IOException {
		if (mRecorder == null) {
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
}