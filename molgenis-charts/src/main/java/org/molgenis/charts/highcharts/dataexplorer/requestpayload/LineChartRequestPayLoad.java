package org.molgenis.charts.highcharts.dataexplorer.requestpayload;

/**
 * 
 * @author jonathanjetten
 *
 */
public class LineChartRequestPayLoad
{
	public LineChartRequestPayLoad()
	{
	}

	private String attributeNameXaxis;
	private String attributeNameYaxis;
	private Integer height;
	private Integer width;
	private String title;
	private String entityName;

	// private String featureFilters;
	// private String searchQuery;
	// private String searchRequest;
	// private List<QueryRule> queryRules;
	/**
	 * @return the attributeNameXaxis
	 */
	public String getAttributeNameXaxis()
	{
		return attributeNameXaxis;
	}

	/**
	 * @param attributeNameXaxis
	 *            the attributeNameXaxis to set
	 */
	public void setAttributeNameXaxis(String attributeNameXaxis)
	{
		this.attributeNameXaxis = attributeNameXaxis;
	}

	/**
	 * @return the attributeNameYaxis
	 */
	public String getAttributeNameYaxis()
	{
		return attributeNameYaxis;
	}

	/**
	 * @param attributeNameYaxis
	 *            the attributeNameYaxis to set
	 */
	public void setAttributeNameYaxis(String attributeNameYaxis)
	{
		this.attributeNameYaxis = attributeNameYaxis;
	}

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
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @return the entityName
	 */
	public String getEntityName()
	{
		return entityName;
	}

	/**
	 * @param entityName
	 *            the entityName to set
	 */
	public void setEntityName(String entityName)
	{
		this.entityName = entityName;
	}
}