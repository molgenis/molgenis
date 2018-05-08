package org.molgenis.semanticmapper.data.request;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AutoTagRequest.class)
public abstract class AutoTagRequest
{
	@NotBlank
	public abstract String getEntityTypeId();

	@NotEmpty
	public abstract List<String> getOntologyIds();
}
