package org.molgenis.data.convert;

import org.molgenis.data.util.MolgenisDateFormat;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;

public class StringToDateTimeConverter implements Converter<String, Instant>
{

	@Override
	public Instant convert(String source)
	{
		return MolgenisDateFormat.parseInstant(source);
	}
}
