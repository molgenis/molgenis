package org.molgenis.charts.highcharts;

import org.molgenis.charts.AbstractChart.MolgenisChartType;

public enum ChartType
{
	LINE("line"),
	SPLINE("spline"),
	AREA("area"),
	AREASPLINE("areaspline"),
	COLUMN("column"),
	BAR("bar"),
	PIE("pie"),
	SCATTER("scatter"),
	BOXPLOT("boxplot"),
	GAUGE("gauge"),
	AREARANGE("arearange"),
	AREASPLINERANGE("areasplinerange"),
	COLUMNRANGE("columnrange");
	
	private String type;
	
	private ChartType(String type){
		this.type = type;
	}
	
	public String toString(){
		return this.type;
	}
	

	public static ChartType getChartType(MolgenisChartType molgenisChartType)
	{
		if (MolgenisChartType.LINE_CHART.equals(molgenisChartType))
		{
			return ChartType.LINE;
		}
		else if (MolgenisChartType.SCATTER_CHART.equals(molgenisChartType))
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
