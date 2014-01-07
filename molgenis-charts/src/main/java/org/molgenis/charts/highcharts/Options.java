package org.molgenis.charts.highcharts;

import java.util.ArrayList;
import java.util.List;

public class Options
{
	private Chart chart;
	private ChartTitle title;
	private List<Series> series = new ArrayList<Series>();
	private List<XAxis> xAxis = new ArrayList<XAxis>();
	private List<YAxis> yAxis = new ArrayList<YAxis>();
	private Credits credits;

	/**
	 * @return the chart
	 */
	public Chart getChart()
	{
		return chart;
	}

	/**
	 * @param chart
	 *            the chart to set
	 */
	public void setChart(Chart chart)
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
	 * @param title
	 *            the title to set
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
	 * @param series
	 *            the series to set
	 */
	public void setSeries(List<Series> series)
	{
		this.series = series;
	}

	/**
	 * @return the xAxis
	 */
	public List<XAxis> getxAxis()
	{
		return xAxis;
	}

	/**
	 * @param xAxis
	 *            the xAxis to set
	 */
	public void setxAxes(List<XAxis> xAxes)
	{
		this.xAxis = xAxes;
	}
	
	/**
	 * @param xAxis
	 *            the xAxis to add
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
	 * @param yAxis
	 *            the yAxis to set
	 */
	public void setyAxes(List<YAxis> yAxes)
	{
		this.yAxis = yAxes;
	}
	
	/**
	 * @param xAxis
	 *            the xAxis to add
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
}
