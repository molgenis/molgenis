package org.molgenis.data.rest.v2;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.validation.constraints.NotNull;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ResourcesResponseV2.class)
public abstract class ResourcesResponseV2
{
	@NotNull
	public abstract String getHref();

	public static ResourcesResponseV2 create(String href)
	{
		return new AutoValue_ResourcesResponseV2(href);
	}
}
