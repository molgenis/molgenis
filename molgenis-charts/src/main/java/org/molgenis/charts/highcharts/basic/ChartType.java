package org.molgenis.charts.highcharts.basic;

import org.molgenis.charts.AbstractChart.MolgenisChartType;

public enum ChartType
{
	SCATTER("scatter"), BOXPLOT("boxplot"), LINE("line");

	private String type;

	ChartType(String type)
	{
		this.type = type;
	}

	public String toString()
	{
		return this.type;
	}

	public static ChartType getChartType(MolgenisChartType molgenisChartType)
	{
		if (MolgenisChartType.SCATTER_CHART.equals(molgenisChartType))
		{
			return ChartType.SCATTER;
		}
		else if (MolgenisChartType.BOXPLOT_CHART.equals(molgenisChartType))
		{
			return ChartType.BOXPLOT;
		}
		else if (MolgenisChartType.LINE_CHART.equals(molgenisChartType))
		{
			return ChartType.LINE;
		}
		else
		{
			return null;
		}
	}
}
