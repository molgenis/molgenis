package org.molgenis.charts.highcharts;

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
}
