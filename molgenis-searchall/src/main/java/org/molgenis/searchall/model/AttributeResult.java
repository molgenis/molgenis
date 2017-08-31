package org.molgenis.searchall.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AttributeResult.class)
public abstract class AttributeResult
{
	abstract String getLabel();

	@Nullable
	abstract String getDescription();

	abstract String getDataType();

	public static AttributeResult create(String label, String description, String datatype)
	{
		return new AutoValue_AttributeResult(label, description, datatype);
	}
}
