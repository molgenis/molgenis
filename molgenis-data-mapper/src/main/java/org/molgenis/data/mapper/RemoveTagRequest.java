package org.molgenis.data.mapper;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_RemoveTagRequest.class)
public abstract class RemoveTagRequest
{
	@NotNull
	public abstract String getEntityName();

	@NotNull
	public abstract String getAttributeName();

	@NotNull
	public abstract String getRelationIRI();

	@NotEmpty
	public abstract String getOntologyTermIRI();
}
