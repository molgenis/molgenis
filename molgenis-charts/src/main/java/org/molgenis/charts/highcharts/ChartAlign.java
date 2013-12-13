package org.molgenis.charts.highcharts;

public enum ChartAlign
{
	LEFT("left"), 
	CENTER("center"), 
	RIGHT("right");
	
	private String align;
	
	private ChartAlign(String align) {
		this.align = align;
	}
	
	public String toString(){
		return this.align;
	}
}
