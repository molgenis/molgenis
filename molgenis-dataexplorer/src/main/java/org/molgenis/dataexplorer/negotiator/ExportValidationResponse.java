package org.molgenis.dataexplorer.negotiator;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ExportValidationResponse.class)
public abstract class ExportValidationResponse
{
	public abstract boolean isValid();

	public abstract String message();

	public static ExportValidationResponse create(boolean success, String message)
	{
		return new AutoValue_ExportValidationResponse(success, message);
	}
}
