package org.molgenis.data.mapper.data.request;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GenerateAlgorithmRequest.class)
public abstract class GenerateAlgorithmRequest
{
	@NotNull
	public abstract String getTargetEntityName();

	@NotNull
	public abstract String getTargetAttributeName();

	@NotNull
	public abstract String getSourceEntityName();

	@NotEmpty
	public abstract List<String> getSourceAttributes();
}
