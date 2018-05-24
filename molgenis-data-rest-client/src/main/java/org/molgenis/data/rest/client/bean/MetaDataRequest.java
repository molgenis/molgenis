package org.molgenis.data.rest.client.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.annotation.Nullable;
import java.util.Collection;

@AutoValue
@AutoGson(autoValueClass = AutoValue_MetaDataRequest.class)
public abstract class MetaDataRequest
{

	public abstract @Nullable
	Collection<String> getAttributes();

	public abstract @Nullable
	Collection<String> getExpands();

	public static MetaDataRequest create()
	{
		return new AutoValue_MetaDataRequest(null, null);
	}

	public static MetaDataRequest create(Collection<String> attributes, Collection<String> expands)
	{
		return new AutoValue_MetaDataRequest(attributes, expands);
	}
}
