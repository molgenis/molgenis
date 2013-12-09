package org.molgenis.charts.requests;

import java.util.List;

import javax.validation.constraints.NotNull;

public class LineChartRequest extends ChartRequest
{
	@NotNull
	private String x;

	@NotNull
	private String y;

	public String getX()
	{
		return x;
	}

	public void setX(String x)
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