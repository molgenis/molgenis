package org.molgenis.data.convert;

import java.util.Date;

import org.molgenis.util.MolgenisDateFormat;
import org.springframework.core.convert.converter.Converter;

public class DateToStringConverter implements Converter<Date, String>
{

	@Override
	public String convert(Date source)
	{
		return MolgenisDateFormat.getDateFormat().format(source);
	}

}
