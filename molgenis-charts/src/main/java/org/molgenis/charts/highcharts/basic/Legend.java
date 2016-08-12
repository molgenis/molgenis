package org.molgenis.charts.highcharts.basic;

public class Legend
{
	private Boolean enabled;
	private String align;
	private String verticalAlign;
	private String layout;

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
	public Legend setEnabled(Boolean enabled)
	{
		this.enabled = enabled;
		return this;
	}

	/**
	 * @return the align
	 */
	public String getAlign()
	{
		return align;
	}

	/**
	 * "left", "center" and "right". Defaults to "center"
	 *
	 * @param align the align to set
	 */
	public Legend setAlign(String align)
	{
		this.align = align;
		return this;
	}

	/**
	 * @return the verticalAlign
	 */
	public String getVerticalAlign()
	{
		return verticalAlign;
	}

	/**
	 * "top", "middle" or "bottom".
	 *
	 * @param verticalAlign the verticalAlign to set
	 */
	public Legend setVerticalAlign(String verticalAlign)
	{
		this.verticalAlign = verticalAlign;
		return this;
	}

	/**
	 * "horizontal" or "vertical"
	 *
	 * @return the layout
	 */
	public String getLayout()
	{
		return layout;
	}

	/**
	 * @param layout the layout to set
	 */
	public Legend setLayout(String layout)
	{
		this.layout = layout;
		return this;
	}
}
