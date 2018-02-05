package org.molgenis.semanticmapper.data.request;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@AutoValue
@AutoGson(autoValueClass = AutoValue_RemoveTagRequest.class)
public abstract class RemoveTagRequest
{
	@NotNull
	public abstract String getEntityTypeId();

	@NotNull
	public abstract String getAttributeName();

	@NotNull
	public abstract String getRelationIRI();

	@NotEmpty
	public abstract String getOntologyTermIRI();
}
