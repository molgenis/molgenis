package org.molgenis.data.mapper.data.request;

import java.util.List;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AutoTagRequest.class)
public abstract class AutoTagRequest
{
	@NotBlank
	public abstract String getEntityName();

	@NotEmpty
	public abstract List<String> getOntologyIds();
}
