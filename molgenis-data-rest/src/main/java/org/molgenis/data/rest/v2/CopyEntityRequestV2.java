package org.molgenis.data.rest.v2;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.validation.constraints.NotNull;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CopyEntityRequestV2.class)
@SuppressWarnings("squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class CopyEntityRequestV2
{
	@NotNull
	public abstract String getNewEntityName();

	public static CopyEntityRequestV2 create(String newEntityName)
	{
		return new AutoValue_CopyEntityRequestV2(newEntityName);
	}
}