package org.molgenis.data.mapper;

import java.util.List;

import org.molgenis.gson.AutoGson;
import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AddTagRequest.class)
public abstract class AddTagRequest
{
	public abstract String getEntityName();

	public abstract String getAttributeName();

	public abstract String getRelationIRI();

	public abstract List<String> getOntologyTermIRIs();

}
