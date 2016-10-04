package org.molgenis.charts.highcharts.basic;

import java.util.ArrayList;
import java.util.List;

public class Options
{
	private BasicChart chart;
	private ChartTitle title;
	private Legend legend;
	private List<Series> series = new ArrayList<Series>();
	private List<XAxis> xAxis = new ArrayList<XAxis>();
	private List<YAxis> yAxis = new ArrayList<YAxis>();
	private Credits credits;

	/**
	 * @return the chart
	 */
	public BasicChart getChart()
	{
		return chart;
	}

	/**
	 * @param chart the chart to set
	 */
	public void setChart(BasicChart chart)
	{
		this.chart = chart;
	}

	/**
	 * @return the title
	 */
	public ChartTitle getTitle()
	{
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(ChartTitle title)
	{
		this.title = title;
	}

	/**
	 * @return the series
	 */
	public List<Series> getSeries()
	{
		return series;
	}

	/**
	 * @param series the series to set
	 */
	public void setSeries(List<Series> series)
	{
		this.series = series;
	}

	/**
	 * @param series the series to add
	 */
	public void addSeries(List<Series> series)
	{
		for (Series serie : series)
		{
			this.series.add(serie);
		}
	}

	/**
	 * @return the xAxis
	 */
	public List<XAxis> getxAxis()
	{
		return xAxis;
	}

	/**
	 * @param xAxes the xAxis to set
	 */
	public void setxAxes(List<XAxis> xAxes)
	{
		this.xAxis = xAxes;
	}

	/**
	 * @param xAxis the xAxis to add
	 */
	public void addxAxis(XAxis xAxis)
	{
		this.xAxis.add(xAxis);
	}

	/**
	 * @return the yAxis
	 */
	public List<YAxis> getyAxis()
	{
		return yAxis;
	}

	/**
	 * @param yAxes the yAxis to set
	 */
	public void setyAxes(List<YAxis> yAxes)
	{
		this.yAxis = yAxes;
	}

	/**
	 * @param yAxis the xAxis to add
	 */
	public void addyAxis(YAxis yAxis)
	{
		this.yAxis.add(yAxis);
	}

	/**
	 * @return the credits
	 */
	public Credits getCredits()
	{
		return credits;
	}

	/**
	 * @param credits the credits to set
	 */
	public void setCredits(Credits credits)
	{
		this.credits = credits;
	}

	/**
	 * @return the legend
	 */
	public Legend getLegend()
	{
		return legend;
	}

	/**
	 * @param legend the legend to set
	 */
	public void setLegend(Legend legend)
	{
		this.legend = legend;
	}
}
