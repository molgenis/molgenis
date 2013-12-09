package org.molgenis.charts.highcharts;

public enum AxisType
{
	LINEAR("linear"), 
	LOGARITHMIC("logarithmic"), 
	DATETIME("datetime"),
	CATEGORY("category");
	
	String type;
	
	private AxisType(String type) {
		this.type = type; 
	}
	
	public String toString() {
		return this.type;
	}
}
