package org.molgenis.charts.highcharts;

public class Chart
{
	// Default values
	private static final Integer DEFAULT_MARGIN_BOTTOM = 100;
	private static final Integer DEFAULT_MARGIN_LEFT = 100;
	private static final Integer DEFAULT_MARGIN_RIGHT = 100;
	private static final Integer DEFAULT_MARGIN_TOP = 100;

	Integer height;
	Integer width;
	Integer marginBottom = DEFAULT_MARGIN_BOTTOM;
	Integer marginLeft = DEFAULT_MARGIN_LEFT;
	Integer marginRight = DEFAULT_MARGIN_RIGHT;
	Integer marginTop = DEFAULT_MARGIN_TOP;
	String type;

	/**
	 * @return the height
	 */
	public Integer getHeight()
	{
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public Chart setHeight(Integer height)
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
	 * @param width
	 *            the width to set
	 */
	public Chart setWidth(Integer width)
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
	 * @param marginBottom
	 *            the marginBottom to set
	 */
	public Chart setMarginBottom(Integer marginBottom)
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
	 * @param marginLeft
	 *            the marginLeft to set
	 */
	public Chart setMarginLeft(Integer marginLeft)
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
	 * @param marginRight
	 *            the marginRight to set
	 */
	public Chart setMarginRight(Integer marginRight)
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
	 * @param marginTop
	 *            the marginTop to set
	 */
	public Chart setMarginTop(Integer marginTop)
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
	 * @param type
	 *            the type to set
	 */
	public Chart setType(ChartType type)
	{
		this.type = type.toString();
		return this;
	}
}
