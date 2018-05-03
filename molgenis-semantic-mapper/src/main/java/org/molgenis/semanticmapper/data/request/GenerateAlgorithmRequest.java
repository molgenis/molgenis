package org.molgenis.semanticmapper.data.request;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GenerateAlgorithmRequest.class)
public abstract class GenerateAlgorithmRequest
{
	@NotNull
	public abstract String getTargetEntityTypeId();

	@NotNull
	public abstract String getTargetAttributeName();

	@NotNull
	public abstract String getSourceEntityTypeId();

	@NotEmpty
	public abstract List<String> getSourceAttributes();
}
