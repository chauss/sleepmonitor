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
		
		Intent intent = ChartFactory.getTimeChartIntent(context, dataset, mRenderer, "Test Title");
		
		return intent;
	}
	
}
