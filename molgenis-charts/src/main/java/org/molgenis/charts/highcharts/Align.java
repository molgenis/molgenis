package org.molgenis.charts.highcharts;

public enum Align
{
	LEFT("left"), 
	CENTER("center"), 
	RIGHT("right");
	
	private String align;
	
	private Align(String align) {
		this.align = align;
	}
	
	public String toString(){
		return this.align;
	}
}
