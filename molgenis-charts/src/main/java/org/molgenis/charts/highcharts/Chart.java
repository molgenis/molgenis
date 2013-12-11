package org.molgenis.charts.highcharts;

public class Chart
{
	//Default values
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
	public void setHeight(Integer height)
	{
		this.height = height;
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
	public void setWidth(Integer width)
	{
		this.width = width;
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
	public void setMarginBottom(Integer marginBottom)
	{
		this.marginBottom = marginBottom;
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
	public void setMarginLeft(Integer marginLeft)
	{
		this.marginLeft = marginLeft;
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
	public void setMarginRight(Integer marginRight)
	{
		this.marginRight = marginRight;
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
	public void setMarginTop(Integer marginTop)
	{
		this.marginTop = marginTop;
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
	public void setType(String type)
	{
		this.type = type;
	}
}
