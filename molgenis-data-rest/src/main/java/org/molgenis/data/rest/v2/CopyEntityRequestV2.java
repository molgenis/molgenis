package org.molgenis.data.rest.v2;

import javax.validation.constraints.NotNull;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CopyEntityRequestV2.class)
public abstract class CopyEntityRequestV2
{
	@NotNull
	public abstract String getNewEntityName();

	public static CopyEntityRequestV2 create(String newEntityName)
	{
		return new AutoValue_CopyEntityRequestV2(newEntityName);
	}
}