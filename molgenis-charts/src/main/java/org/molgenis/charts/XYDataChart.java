package org.molgenis.charts;

import java.util.List;

import org.molgenis.charts.data.XYDataSerie;

/**
 * A chart that uses xy data points like a LineChart or BarChart.
 */
public class XYDataChart extends Chart
{
	private final List<XYDataSerie> data;

	protected XYDataChart(ChartType type, List<XYDataSerie> data)
	{
		super(type);
		if (data == null) throw new IllegalArgumentException("data is null");
		this.data = data;
	}

	public List<XYDataSerie> getData()
	{
		return data;
	}

}
