package org.molgenis.data.mapper.data.request;

import com.google.auto.value.AutoValue;
import org.hibernate.validator.constraints.NotEmpty;
import org.molgenis.gson.AutoGson;

import javax.validation.constraints.NotNull;

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
