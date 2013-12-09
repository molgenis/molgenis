package org.molgenis.charts.charttypes;

import org.molgenis.charts.data.DataMatrix;

public class HeatMapChart extends DataMatrixChart
{
	public HeatMapChart(DataMatrix data)
	{
		super(AbstractChartType.HEAT_MAP, data);
	}

	@Override
	public String toString()
	{
		return "HeatMapChart [data=" + getData() + ", width=" + getWidth() + ", height=" + getHeight() + "]";
	}

}
