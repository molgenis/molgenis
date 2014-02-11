package org.molgenis.charts.requests;

import java.util.List;

import org.molgenis.charts.charttypes.HeatMapScale;

public class HeatMapRequest extends ChartRequest
{
	// The column attribute names
	private List<String> x;

	// The row label attribute name
	private String y;
	
	// Option to set scaling of rows or columns (or none)
	private HeatMapScale scale;

	public HeatMapScale getScale() {
		return scale;
	}

	public void setScale(HeatMapScale scale) {
		this.scale = scale;
	}

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