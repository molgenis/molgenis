package org.molgenis.charts.charttypes;

import org.molgenis.charts.data.DataMatrix;

public class HeatMapChart extends DataMatrixChart
{
	private HeatMapScale scale;

	public HeatMapChart(DataMatrix data)
	{
		super(MolgenisChartType.HEAT_MAP, data);
	}

	public HeatMapScale getScale()
	{
		return scale;
	}

	public void setScale(HeatMapScale scale)
	{
		this.scale = scale;
	}

	@Override
	public String toString()
	{
		return "HeatMapChart [data=" + getData() + ", width=" + getWidth() + ", height=" + getHeight() + "]";
	}

}
