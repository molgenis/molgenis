package org.molgenis.charts.requests;

import javax.validation.constraints.NotNull;

public class LineChartRequest extends ChartRequest
{
	@NotNull
	private String x;

	@NotNull
	private String y;

	/**
	 * @return the x
	 */
	public String getX()
	{
		return x;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(String x)
	{
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public String getY()
	{
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(String y)
	{
		this.y = y;
	}

}