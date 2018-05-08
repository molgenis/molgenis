package org.molgenis.semanticmapper.data.request;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AddTagRequest.class)
public abstract class AddTagRequest
{
	@NotNull
	public abstract String getEntityTypeId();

	@NotNull
	public abstract String getAttributeName();

	@NotNull
	public abstract String getRelationIRI();

	@NotEmpty
	public abstract List<String> getOntologyTermIRIs();

}
