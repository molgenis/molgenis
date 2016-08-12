package org.molgenis.charts.highcharts.basic;

public class Point
{
	private String id;
	private String name;
	private String category;
	private Number x;
	private Number y;

	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the category
	 */
	public String getCategory()
	{
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category)
	{
		this.category = category;
	}

	/**
	 * @return the x
	 */
	public Number getX()
	{
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(Number x)
	{
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public Number getY()
	{
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(Number y)
	{
		this.y = y;
	}
}
