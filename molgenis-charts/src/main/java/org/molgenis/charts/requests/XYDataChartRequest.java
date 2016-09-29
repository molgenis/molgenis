package org.molgenis.charts.requests;

import javax.validation.constraints.NotNull;

public class XYDataChartRequest extends ChartRequest
{
	@NotNull
	private String x;

	@NotNull
	private String xAxisLabel;

	@NotNull
	private String y;

	@NotNull
	private String yAxisLabel;

	private String split;

	/**
	 * @return the x
	 */
	public String getX()
	{
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(String x)
	{
		this.x = x;
	}

	/**
	 * @return the xAxisLabel
	 */
	public String getxAxisLabel()
	{
		return xAxisLabel;
	}

	/**
	 * @param xAxisLabel the xAxisLabel to set
	 */
	public void setxAxisLabel(String xAxisLabel)
	{
		this.xAxisLabel = xAxisLabel;
	}

	/**
	 * @return the y
	 */
	public String getY()
	{
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(String y)
	{
		this.y = y;
	}

	/**
	 * @return the yAxisLabel
	 */
	public String getyAxisLabel()
	{
		return yAxisLabel;
	}

	/**
	 * @param yAxisLabel the yAxisLabel to set
	 */
	public void setyAxisLabel(String yAxisLabel)
	{
		this.yAxisLabel = yAxisLabel;
	}

	/**
	 * @return the split
	 */
	public String getSplit()
	{
		return this.split;
	}

	/**
	 * @param split the split to set
	 */
	public void setSplit(String split)
	{
		this.split = split;
	}

}