package de.htwg_konstanz.chhauss.sleepmonitor;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DialRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.DialRenderer.Type;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;


public class NoiseMeter {
  
  public Intent getIntent(Context context) {
    CategorySeries category = new CategorySeries("Weight indic");
    category.add("Current", 75);
    category.add("Minimum", 65);
    category.add("Maximum", 90);
    DialRenderer renderer = new DialRenderer();
    renderer.setChartTitleTextSize(20);
    renderer.setLabelsTextSize(15);
    renderer.setLegendTextSize(15);
    renderer.setMargins(new int[] {20, 30, 15, 0});
    SimpleSeriesRenderer r = new SimpleSeriesRenderer();
    r.setColor(Color.BLUE);
    renderer.addSeriesRenderer(r);
    r = new SimpleSeriesRenderer();
    r.setColor(Color.rgb(0, 150, 0));
    renderer.addSeriesRenderer(r);
    r = new SimpleSeriesRenderer();
    r.setColor(Color.GREEN);
    renderer.addSeriesRenderer(r);
    renderer.setLabelsTextSize(10);
    renderer.setLabelsColor(Color.WHITE);
    renderer.setShowLabels(true);
    renderer.setVisualTypes(new DialRenderer.Type[] {Type.ARROW, Type.NEEDLE, Type.NEEDLE});
    renderer.setMinValue(0);
    renderer.setMaxValue(150);
    return ChartFactory.getDialChartIntent(context, category, renderer, "Noise Meter");
  }

}
