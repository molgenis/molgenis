package org.molgenis.data.mapper.algorithmgenerator.bean;

import java.util.Set;

import javax.annotation.Nullable;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GeneratedAlgorithm.class)
public abstract class GeneratedAlgorithm
{
	public abstract String getAlgorithm();

	@Nullable
	public abstract Set<AttributeMetaData> getSourceAttributes();

	@Nullable
	public abstract AlgorithmState getAlgorithmState();

	public static GeneratedAlgorithm create(String algorithm, Set<AttributeMetaData> sourceAttributes,
			AlgorithmState algorithmState)
	{
		return new AutoValue_GeneratedAlgorithm(algorithm, sourceAttributes, algorithmState);
	}
}
