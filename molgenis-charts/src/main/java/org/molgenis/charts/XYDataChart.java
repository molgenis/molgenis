package org.molgenis.charts;

import java.util.List;

import org.molgenis.charts.data.XYDataSerie;

/**
 * A chart that uses xy data points like a LineChart or BarChart.
 */
public class XYDataChart extends AbstractChart
{
	private final List<XYDataSerie> data;

	protected XYDataChart(AbstractChartType type, List<XYDataSerie> data)
	{
		super(type);
		if (data == null) throw new IllegalArgumentException("data is null");
		this.data = data;
	}

	public List<XYDataSerie> getXYDataSeries()
	{
		return data;
	}

}
