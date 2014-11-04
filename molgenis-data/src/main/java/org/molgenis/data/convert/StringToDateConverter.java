package org.molgenis.data.convert;

import java.text.ParseException;
import java.util.Date;

import org.molgenis.util.MolgenisDateFormat;
import org.springframework.core.convert.converter.Converter;

public class StringToDateConverter implements Converter<String, Date>
{
	@Override
	public Date convert(String source)
	{
		try
		{
			return MolgenisDateFormat.getDateTimeFormat().parse(source);
		}
		catch (ParseException pe)
		{
			try
			{
				return MolgenisDateFormat.getDateFormat().parse(source);
			}
			catch (ParseException pe2)
			{
				try
				{
					return MolgenisDateFormat.getDateTimeFormatSimple().parse(source);
				}
				catch (ParseException pe3)
				{
					throw new IllegalArgumentException("Invalid dateformat [" + source + "] should be of format "
							+ MolgenisDateFormat.DATEFORMAT_DATETIME + " OR " + MolgenisDateFormat.DATEFORMAT_DATE
							+ " OR " + MolgenisDateFormat.DATEFORMAT_DATETIME_SIMPLE);
				}
			}
		}
	}
}
