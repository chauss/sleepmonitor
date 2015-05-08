package de.htwg_konstanz.chhauss.sleepmonitor;

import static java.lang.Math.max;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class LineChart {

	private static final int LABEL_SIZE = 30;
	private static final int AXIS_TITLE_SIZE = 60;
	private static final String X_AXIS_TITLE = "Time";
	private static final String Y_AXIS_TITLE = "Volume & Movement";
	
	private HashMap<Date, Integer> volume_data;
	private HashMap<Date, Double> acc_data;
	
	public LineChart(HashMap<Date, Integer> volume_data, HashMap<Date, Double> acc_data) {
		this.volume_data = volume_data;
		this.acc_data = acc_data;
	}
	
	public Intent getIntent(Context context) {
	    // Series for volume_data
		TimeSeries volume_series = new TimeSeries("Volume");
		for (Map.Entry<Date, Integer> entry : volume_data.entrySet()) {
		    volume_series.add(entry.getKey(), entry.getValue());
		}

		// Series for acc data
		TimeSeries acc_series = new TimeSeries("Accelerometer");
        for (Map.Entry<Date, Double> entry : acc_data.entrySet()) {
            acc_series.add(entry.getKey(), max(0, ((entry.getValue() - 9.81 ) * 1000)));
        }
		
	    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(volume_series);
		dataset.addSeries(acc_series);
		
		XYSeriesRenderer volume_renderer = new XYSeriesRenderer();
		volume_renderer.setColor(Color.GREEN);
		volume_renderer.setLineWidth(2);
		volume_renderer.setPointStyle(PointStyle.SQUARE);
		volume_renderer.setFillPoints(true);
		
		XYSeriesRenderer acc_renderer = new XYSeriesRenderer();
		acc_renderer.setColor(Color.RED);
		acc_renderer.setLineWidth(2);
		acc_renderer.setPointStyle(PointStyle.DIAMOND);
		acc_renderer.setFillPoints(true);
		
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.addSeriesRenderer(volume_renderer);
		mRenderer.addSeriesRenderer(acc_renderer);
		mRenderer.setShowLegend(false);
		mRenderer.setLabelsTextSize(LABEL_SIZE);
		mRenderer.setXTitle(X_AXIS_TITLE);
		mRenderer.setYTitle(Y_AXIS_TITLE);
		mRenderer.setAxisTitleTextSize(AXIS_TITLE_SIZE);
		mRenderer.setMargins(new int[] {0, 2 * AXIS_TITLE_SIZE, AXIS_TITLE_SIZE, 0});
		
		return ChartFactory.getTimeChartIntent(context, dataset, mRenderer, "Noise Chart");
	}
	
}
