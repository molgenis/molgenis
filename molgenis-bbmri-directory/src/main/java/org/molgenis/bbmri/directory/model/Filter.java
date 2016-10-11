package org.molgenis.bbmri.directory.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Filter.class)
public abstract class Filter
{
	public abstract String getHumanReadable();

	public static Filter createFilter(String humanReadable)
	{
		return new AutoValue_Filter(humanReadable);
	}
	// TODO get structured data from jonathan
}
