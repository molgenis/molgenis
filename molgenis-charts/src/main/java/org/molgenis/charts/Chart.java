package org.molgenis.charts;

/**
 * Base class for the different Chart types
 */
public abstract class Chart
{
	public enum ChartType
	{
		LINE_CHART, HEAT_MAP
	}

	public static final int DEFAULT_WITH = 200;
	public static final int DEFAULT_HEIGHT = 200;

	private final ChartType type;
	private int width = DEFAULT_WITH;
	private int height = DEFAULT_HEIGHT;

	private String title = "";

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = (title == null) ? "" : title;
	}

	protected Chart(ChartType type)
	{
		this.type = type;
	}

	public ChartType getType()
	{
		return type;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

}
