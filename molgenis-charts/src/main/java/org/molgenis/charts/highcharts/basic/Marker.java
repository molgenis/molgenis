package org.molgenis.charts.highcharts.basic;

public class Marker
{
	private Boolean enabled;
	private Integer radius;

	Marker()
	{
	}

	public Marker(Boolean enabled, Integer radius)
	{
		this.enabled = enabled;
		this.radius = radius;
	}

	/**
	 * @return the enabled
	 */
	public Boolean getEnabled()
	{
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(Boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * @return the radius
	 */
	public Integer getRadius()
	{
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(Integer radius)
	{
		this.radius = radius;
	}

}
