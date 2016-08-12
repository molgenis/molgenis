package org.molgenis.charts.highcharts.basic;

public class ChartTitle
{
	ChartAlign align;
	Integer margin;
	String text;

	public ChartTitle()
	{
		this.align = ChartAlign.CENTER;
		this.margin = 15;
		this.text = "Chart title";
	}

	public static ChartTitle getDefault()
	{
		return new ChartTitle().setAlign(ChartAlign.CENTER).setMargin(15).setText("Chart title");
	}

	public ChartAlign getAlign()
	{
		return align;
	}

	public ChartTitle setAlign(ChartAlign align)
	{
		this.align = align;
		return this;
	}

	public Integer getMargin()
	{
		return margin;
	}

	public ChartTitle setMargin(Integer margin)
	{
		this.margin = margin;
		return this;
	}

	public String getText()
	{
		return text;
	}

	public ChartTitle setText(String text)
	{
		this.text = text;
		return this;
	}
}
