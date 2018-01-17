package org.molgenis.dataexplorer.negotiator;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

import java.util.List;

import static java.util.Collections.emptyList;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ExportValidationResponse.class)
public abstract class ExportValidationResponse
{
	public abstract boolean isValid();

	public abstract String message();

	public abstract List<String> enabledCollections();

	public abstract List<String> disabledCollections();

	public static ExportValidationResponse create(boolean success, String message)
	{
		return new AutoValue_ExportValidationResponse(success, message, emptyList(), emptyList());
	}

	public static ExportValidationResponse create(boolean success, String message, List<String> enabledCollections,
			List<String> disabledCollections)
	{
		return new AutoValue_ExportValidationResponse(success, message, enabledCollections, disabledCollections);
	}
}
