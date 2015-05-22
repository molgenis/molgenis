package org.molgenis.data.rest;

import org.springframework.core.convert.converter.Converter;

public class AttributesConverter implements Converter<String, Attributes>
{
	@Override
	public Attributes convert(String source)
	{
		return new Attributes(source);
	}
}
