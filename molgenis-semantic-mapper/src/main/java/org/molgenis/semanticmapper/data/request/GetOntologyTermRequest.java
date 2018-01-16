package org.molgenis.semanticmapper.data.request;

import com.google.auto.value.AutoValue;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.molgenis.core.gson.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GetOntologyTermRequest.class)
public abstract class GetOntologyTermRequest
{
	@NotBlank
	public abstract String getSearchTerm();

	@NotEmpty
	public abstract List<String> getOntologyIds();
}
