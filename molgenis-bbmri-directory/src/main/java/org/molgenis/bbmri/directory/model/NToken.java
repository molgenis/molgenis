package org.molgenis.bbmri.directory.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_NToken.class)
public abstract class NToken
{
	@Nullable
	public abstract String getNToken();

	public static NToken createNToken(String nToken)
	{
		return new AutoValue_NToken(nToken);
	}
}
