package org.molgenis.web.converter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.molgenis.util.AutoGson;

public class AutoValueTypeAdapterFactory implements TypeAdapterFactory
{
	@SuppressWarnings("unchecked")
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type)
	{
		Class<? super T> rawType = type.getRawType();

		AutoGson annotation = rawType.getAnnotation(AutoGson.class);
		// Only deserialize classes decorated with @AutoGson.
		if (annotation == null)
		{
			return null;
		}

		return (TypeAdapter<T>) gson.getAdapter(annotation.autoValueClass());
	}
}
