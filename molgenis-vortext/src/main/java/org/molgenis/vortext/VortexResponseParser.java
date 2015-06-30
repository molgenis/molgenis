package org.molgenis.vortext;

import java.io.Reader;

import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.stereotype.Service;

@Service
public class VortexResponseParser
{
	public Marginalia parse(Reader reader)
	{
		return new GsonHttpMessageConverter().getGson().fromJson(reader, Marginalia.class);
	}
}
