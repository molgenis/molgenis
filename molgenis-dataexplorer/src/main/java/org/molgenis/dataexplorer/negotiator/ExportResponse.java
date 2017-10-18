package org.molgenis.dataexplorer.negotiator;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ExportResponse.class)
public abstract class ExportResponse
{
	public abstract boolean isSuccess();

	public abstract String getWarning();

	public abstract String getRedirectUrl();

	public static AutoValue_ExportResponse create(boolean success, String warning, String redirectUrl)
	{
		return new AutoValue_ExportResponse(success, warning, redirectUrl);
	}
}
