package org.molgenis.charts.highcharts;

import java.util.List;

public class Options
{
	private List<Series> series;
	private List<XAxis> xAxis;
	private List<YAxis> yAxis;
	
	public List<Series> getSeries() {
		return this.series;
	}
	
	public void setSeries(List<Series> series) {
		this.series = series;
	}

	public List<XAxis> getxAxis()
	{
		return xAxis;
	}

	public void setxAxis(List<XAxis> xAxis)
	{
		this.xAxis = xAxis;
	}

	public List<YAxis> getyAxis()
	{
		return yAxis;
	}

	public void setyAxis(List<YAxis> yAxis)
	{
		this.yAxis = yAxis;
	}
}
