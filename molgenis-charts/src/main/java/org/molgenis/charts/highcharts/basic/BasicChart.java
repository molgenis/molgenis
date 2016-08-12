package org.molgenis.charts.highcharts.basic;

public class BasicChart
{
	private Integer height;
	private Integer width;
	private Integer marginBottom;
	private Integer marginLeft;
	private Integer marginRight;
	private Integer marginTop;
	private String type;

	/**
	 * @return the height
	 */
	public Integer getHeight()
	{
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public BasicChart setHeight(Integer height)
	{
		this.height = height;
		return this;
	}

	/**
	 * @return the width
	 */
	public Integer getWidth()
	{
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public BasicChart setWidth(Integer width)
	{
		this.width = width;
		return this;
	}

	/**
	 * @return the marginBottom
	 */
	public Integer getMarginBottom()
	{
		return marginBottom;
	}

	/**
	 * @param marginBottom the marginBottom to set
	 */
	public BasicChart setMarginBottom(Integer marginBottom)
	{
		this.marginBottom = marginBottom;
		return this;
	}

	/**
	 * @return the marginLeft
	 */
	public Integer getMarginLeft()
	{
		return marginLeft;
	}

	/**
	 * @param marginLeft the marginLeft to set
	 */
	public BasicChart setMarginLeft(Integer marginLeft)
	{
		this.marginLeft = marginLeft;
		return this;
	}

	/**
	 * @return the marginRight
	 */
	public Integer getMarginRight()
	{
		return marginRight;
	}

	/**
	 * @param marginRight the marginRight to set
	 */
	public BasicChart setMarginRight(Integer marginRight)
	{
		this.marginRight = marginRight;
		return this;
	}

	/**
	 * @return the marginTop
	 */
	public Integer getMarginTop()
	{
		return marginTop;
	}

	/**
	 * @param marginTop the marginTop to set
	 */
	public BasicChart setMarginTop(Integer marginTop)
	{
		this.marginTop = marginTop;
		return this;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public BasicChart setType(ChartType type)
	{
		this.type = type.toString();
		return this;
	}
}
