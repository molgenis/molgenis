package org.molgenis.data.convert;

import org.molgenis.data.util.MolgenisDateFormat;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;

public class StringToDateConverter implements Converter<String, LocalDate>
{

	@Override
	public LocalDate convert(String source)
	{
		return MolgenisDateFormat.parseLocalDate(source);
	}
}
