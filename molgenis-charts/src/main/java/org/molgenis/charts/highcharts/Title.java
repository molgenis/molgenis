package org.molgenis.charts.highcharts;

public class Title
{	
	Align align;
	Integer margin;
	String text;

	public Title()
	{
		this.align = Align.CENTER;
		this.margin = 15;
		this.text = "Chart title";
	}

	public static Title getDefault()
	{
		return new Title()
			.setAlign(Align.CENTER)
			.setMargin(15)
			.setText("Chart title");
	}

	public Align getAlign()
	{
		return align;
	}

	public Title setAlign(Align align)
	{
		this.align = align;
		return this;
	}

	public Integer getMargin()
	{
		return margin;
	}

	public Title setMargin(Integer margin)
	{
		this.margin = margin;
		return this;
	}

	public String getText()
	{
		return text;
	}

	public Title setText(String text)
	{
		this.text = text;
		return this;
	}
}
