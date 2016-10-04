package org.molgenis.charts.highcharts.basic;

import org.molgenis.charts.MolgenisSerieType;

public enum SeriesType
{
	SCATTER("scatter"), LINE("line"), BOXPLOT("boxplot");

	private String type;

	SeriesType(String type)
	{
		this.type = type;
	}

	public String toString()
	{
		return this.type;
	}

	public static SeriesType getSeriesType(MolgenisSerieType molgenisSerieType)
	{
		if (MolgenisSerieType.SCATTER.equals(molgenisSerieType))
		{
			return SeriesType.SCATTER;
		}
		else if (MolgenisSerieType.BOXPLOT.equals(molgenisSerieType))
		{
			return SeriesType.BOXPLOT;
		}
		else if (MolgenisSerieType.LINE.equals(molgenisSerieType))
		{
			return SeriesType.LINE;
		}
		else
		{
			return null;
		}
	}
}
