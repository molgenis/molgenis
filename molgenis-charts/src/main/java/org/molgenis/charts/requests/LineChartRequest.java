package org.molgenis.charts.requests;

import java.util.List;

import javax.validation.constraints.NotNull;

public class LineChartRequest extends ChartRequest
{
	@NotNull
	private String x;

	@NotNull
	private List<String> y;

	public String getX()
	{
		return x;
	}

	public void setX(String x)
	{
		this.x = x;
	}

	public List<String> getY()
	{
		return y;
	}

	public void setY(List<String> y)
	{
		this.y = y;
	}

}