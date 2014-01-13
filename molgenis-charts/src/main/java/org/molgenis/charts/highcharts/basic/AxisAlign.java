package org.molgenis.charts.highcharts.basic;

public enum AxisAlign
{
	LOW("low"), 
	MIDDLE("middle"), 
	HIGH("high");
	
	private String align;
	
	private AxisAlign(String align) {
		this.align = align;
	}
	
	public String toString(){
		return this.align;
	}
}
