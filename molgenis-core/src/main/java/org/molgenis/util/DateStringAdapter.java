package org.molgenis.util;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateStringAdapter extends XmlAdapter<String, Date>
{

	/**
	 * String format used for Dates: {@value} .
	 */
	public static final String DATEFORMAT = "MMMM d, yyyy";

	/**
	 * String format used for Dates: {@value} .
	 */
	public static final String DATEFORMAT2 = "yyyy-MM-dd";

	/**
	 * String format used for Timestamps: {@value} .
	 */
	public static final String DATETIMEFORMAT = "MMMM d, yyyy, HH:mm:ss";
	/**
	 * Alternative String format used for Dates: {@value} .
	 */
	public static final String DATETIMEFORMAT2 = "yyyy-MM-dd HH:mm:ss";

	@Override
	public String marshal(Date v) throws Exception
	{
		return null;
	}

	@Override
	public Date unmarshal(String v) throws Exception
	{
		return null;
	}
}
