package org.molgenis.omx.harmonization.mesh;

import javax.xml.bind.annotation.XmlAttribute;

public class DateCreated
{

	Integer year;

	Integer month;

	Integer day;

	public Integer getYear()
	{
		return year;
	}

	@XmlAttribute(name = "Year")
	public void setYear(Integer year)
	{
		this.year = year;
	}

	public Integer getMonth()
	{
		return month;
	}

	@XmlAttribute(name = "Month")
	public void setMonth(Integer month)
	{
		this.month = month;
	}

	public Integer getDay()
	{
		return day;
	}

	@XmlAttribute(name = "Day")
	public void setDay(Integer day)
	{
		this.day = day;
	}
}
