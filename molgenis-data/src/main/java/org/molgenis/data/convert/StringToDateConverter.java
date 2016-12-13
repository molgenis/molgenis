package org.molgenis.data.convert;

import org.molgenis.util.MolgenisDateFormat;
import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.util.Date;

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
							+ MolgenisDateFormat.getDateTimeFormat().toPattern() + " OR " + MolgenisDateFormat.getDateFormat().toPattern()
							+ " OR " + MolgenisDateFormat.getDateTimeFormatSimple().toPattern());
				}
			}
		}
	}
}
