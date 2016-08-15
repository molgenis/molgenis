package org.molgenis.charts.highcharts.basic;

public class AxisTitle
{
	String align;
	Integer margin;
	String text;

	public AxisTitle()
	{
		this.align = AxisAlign.MIDDLE.toString();
		this.margin = 15;
		this.text = "Chart title";
	}

	public String getAlign()
	{
		return align;
	}

	public AxisTitle setAlign(String align)
	{
		this.align = align;
		return this;
	}

	public AxisTitle setAlign(AxisAlign align)
	{
		this.align = align.toString();
		return this;
	}

	public Integer getMargin()
	{
		return margin;
	}

	public AxisTitle setMargin(Integer margin)
	{
		this.margin = margin;
		return this;
	}

	public String getText()
	{
		return text;
	}

	public AxisTitle setText(String text)
	{
		this.text = text;
		return this;
	}
}
