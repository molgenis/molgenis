package org.molgenis.vortext;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;

@Service
public class VortexResponseParser
{
	public List<AnnotationGroup> parse(Reader reader)
	{
		Type type = new TypeToken<List<AnnotationGroup>>()
		{
		}.getType();

		return new GsonHttpMessageConverter().getGson().fromJson(reader, type);
	}
}
