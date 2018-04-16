package org.molgenis.app.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AppEditRequest.class)
public abstract class AppEditRequest
{
	public abstract String getId();

	public abstract String getUri();

	public abstract String getLabel();

	public abstract String getDescription();

	public static AppEditRequest create(String id, String uri, String label, String description)
	{
		return new AutoValue_AppEditRequest(id, uri, label, description);
	}
}