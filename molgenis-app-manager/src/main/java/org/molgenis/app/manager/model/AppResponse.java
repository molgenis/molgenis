package org.molgenis.app.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AppResponse.class)
public abstract class AppResponse
{
	public abstract String getId();

	public abstract String getLabel();

	public abstract String getDescription();

	public abstract boolean getIsActive();

	public static AppResponse create(String id, String label, String description, boolean isActive)
	{
		return new AutoValue_AppResponse(id, label, description, isActive);
	}
}
