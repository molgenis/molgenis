package org.molgenis.charts.requests;

import java.util.List;

public class HeatMapRequest extends ChartRequest
{
	// The column attribute names
	private List<String> x;

	// The row label attribute name
	private String y;

	
	public List<String> getX()
	{
		return x;
	}

	public void setX(List<String> x)
	{
		this.x = x;
	}

	public String getY()
	{
		return y;
	}

	public void setY(String y)
	{
		this.y = y;
	}

}