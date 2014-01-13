package org.molgenis.charts.highcharts;

import org.molgenis.charts.AbstractChart.MolgenisChartType;

public enum ChartType
{
	SCATTER("scatter"),
	BOXPLOT("boxplot");
	
	private String type;
	
	private ChartType(String type){
		this.type = type;
	}
	
	public String toString(){
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
		else
		{
			return null;
		}
	}
}
