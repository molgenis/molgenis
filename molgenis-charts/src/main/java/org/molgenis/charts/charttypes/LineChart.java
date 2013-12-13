package org.molgenis.charts.charttypes;

import java.util.List;

import org.molgenis.charts.MolgenisAxisType;
import org.molgenis.charts.XYDataChart;
import org.molgenis.charts.data.XYDataSerie;

public class LineChart extends XYDataChart
{
	public LineChart(List<XYDataSerie> data, String xAxisLabel, String yAxisLabel, MolgenisAxisType xAxisType, MolgenisAxisType yAxisType)
	{
		super(ChartType.LINE_CHART,
				data,
				xAxisType, 
				yAxisType);
	}

	@Override
	public String toString()
	{
		return "LineChart [xYDataSeries=" + getData() + 
				", width=" + getWidth() + 
				", height=" + getHeight() + 
				", xAxisLabel=" + getxAxisLabel() +
				", yAxisLabel=" + getyAxisLabel() +
				", xAxisType=" + getxAxisType() +
				", yAxisType=" + getyAxisType() +
				"]";
	}

}
