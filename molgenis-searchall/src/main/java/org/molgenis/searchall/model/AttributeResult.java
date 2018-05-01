package org.molgenis.searchall.model;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AttributeResult.class)
public abstract class AttributeResult implements Described
{
	public abstract String getDataType();

	public static AttributeResult create(String label, String description, String datatype)
	{
		return new AutoValue_AttributeResult(label, description, datatype);
	}

	public static AttributeResult create(Attribute attr, String languageCode)
	{
		return create(attr.getLabel(languageCode), attr.getDescription(languageCode), attr.getDataType().toString());
	}
}
