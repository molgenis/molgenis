package org.molgenis.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public class AutoValueTypeAdapterFactory implements TypeAdapterFactory
{
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type)
	{
		Class<T> rawType = (Class<T>) type.getRawType();

		AutoGson annotation = rawType.getAnnotation(AutoGson.class);
		// Only deserialize classes decorated with @AutoGson.
		if (annotation == null)
		{
			return null;
		}

		return gson.getAdapter(annotation.autoValueClass());
	}
}
