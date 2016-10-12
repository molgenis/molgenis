package org.molgenis.bbmri.directory.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_URL.class)
public abstract class URL
{
	public abstract String getURL();

	public static URL createURL(String url)
	{
		return new AutoValue_URL(url);
	}
}
