package de.htwg_konstanz.chhauss.sleepmonitor;

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
	private static final String Y_AXIS_TITLE = "Volume";
	
	private HashMap<Date, Integer> data;
	
	public LineChart(HashMap<Date, Integer> data) {
		this.data = data;
	}
	
	public Intent getIntent(Context context) {
		TimeSeries series = new TimeSeries("Volume");
		for (Map.Entry<Date, Integer> entry : data.entrySet()) {
			series.add(entry.getKey(), entry.getValue());
		}
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series);
		
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setColor(Color.GREEN);
		renderer.setLineWidth(2);
		renderer.setPointStyle(PointStyle.SQUARE);
		renderer.setFillPoints(true);
		mRenderer.addSeriesRenderer(renderer);
		mRenderer.setShowLegend(false);
		mRenderer.setLabelsTextSize(LABEL_SIZE);
		mRenderer.setXTitle(X_AXIS_TITLE);
		mRenderer.setYTitle(Y_AXIS_TITLE);
		mRenderer.setAxisTitleTextSize(AXIS_TITLE_SIZE);
		mRenderer.setMargins(new int[] {0, 2 * AXIS_TITLE_SIZE, AXIS_TITLE_SIZE, 0});
		
		Intent intent = ChartFactory.getTimeChartIntent(context, dataset, mRenderer, "Test Title");
		
		return intent;
	}
	
}
