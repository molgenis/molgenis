package org.molgenis.data.convert;

import org.molgenis.util.MolgenisDateFormat;
import org.springframework.core.convert.converter.Converter;

import java.util.Date;

public class DateToStringConverter implements Converter<Date, String>
{

	@Override
	public String convert(Date source)
	{
		return MolgenisDateFormat.getDateTimeFormat().format(source);
	}

}
