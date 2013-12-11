package org.molgenis.charts.requests;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.molgenis.charts.AbstractChart;
import org.molgenis.data.QueryRule;

public abstract class ChartRequest
{
	@NotNull
	private String entity;
	
	private String title;
	private Integer width = AbstractChart.DEFAULT_WITH;
	private Integer height = AbstractChart.DEFAULT_HEIGHT;

	// The query rules to select the rows
	private List<QueryRule> queryRules;
	private String xLabel;
	private String yLabel;

	/**
	 * @return the entity
	 */
	public String getEntity()
	{
		return entity;
	}

	/**
	 * @param entity
	 *            the entity to set
	 */
	public void setEntity(String entity)
	{
		this.entity = entity;
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
	
	public String getxLabel() {
		return xLabel;
	}

	public void setxLabel(String xLabel) {
		this.xLabel = xLabel;
	}

	public String getyLabel() {
		return yLabel;
	}

	public void setyLabel(String yLabel) {
		this.yLabel = yLabel;
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
	 * @return the queryRules
	 */
	public List<QueryRule> getQueryRules()
	{
		return queryRules;
	}

	/**
	 * @param queryRules
	 *            the queryRules to set
	 */
	public void setQueryRules(List<QueryRule> queryRules)
	{
		this.queryRules = queryRules;
	}

}
