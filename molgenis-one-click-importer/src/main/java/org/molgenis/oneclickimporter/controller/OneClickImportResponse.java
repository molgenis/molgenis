package org.molgenis.oneclickimporter.controller;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OneClickImportResponse.class)
public abstract class OneClickImportResponse
{
	public abstract String getEntityId();

	public abstract String getBaseFileName();

	public static OneClickImportResponse create(String entityId, String baseFileName)
	{
		return new AutoValue_OneClickImportResponse(entityId, baseFileName);
	}
}
