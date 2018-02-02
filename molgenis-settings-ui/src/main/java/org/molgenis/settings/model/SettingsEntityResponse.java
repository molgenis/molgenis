package org.molgenis.settings.model;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SettingsEntityResponse.class)
public abstract class SettingsEntityResponse
{
	public abstract String getId();

	public abstract String getLabel();

	public static SettingsEntityResponse create(String id, String label)
	{
		return new AutoValue_SettingsEntityResponse(id, label);
	}
}
