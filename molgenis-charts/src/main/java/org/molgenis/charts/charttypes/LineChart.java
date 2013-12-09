package org.molgenis.charts.charttypes;

import java.util.List;

import org.molgenis.charts.XYDataChart;
import org.molgenis.charts.data.XYDataSerie;

public class LineChart extends XYDataChart
{
	public LineChart(List<XYDataSerie> data)
	{
		super(AbstractChartType.LINE_CHART, data);
	}

	@Override
	public String toString()
	{
		return "LineChart [xYDataSeries=" + getXYDataSeries() + ", width=" + getWidth() + ", height=" + getHeight() + "]";
	}

}
