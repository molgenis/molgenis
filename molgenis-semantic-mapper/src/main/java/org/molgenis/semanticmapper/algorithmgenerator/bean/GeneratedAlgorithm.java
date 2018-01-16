package org.molgenis.semanticmapper.algorithmgenerator.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState;

import javax.annotation.Nullable;
import java.util.Set;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GeneratedAlgorithm.class)
public abstract class GeneratedAlgorithm
{
	public abstract String getAlgorithm();

	@Nullable
	public abstract Set<Attribute> getSourceAttributes();

	@Nullable
	public abstract AlgorithmState getAlgorithmState();

	public static GeneratedAlgorithm create(String algorithm, Set<Attribute> sourceAttributes,
			AlgorithmState algorithmState)
	{
		return new AutoValue_GeneratedAlgorithm(algorithm, sourceAttributes, algorithmState);
	}
}
