package org.molgenis.semanticmapper.algorithmgenerator.rules.quality.impl;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;
import org.molgenis.semanticmapper.algorithmgenerator.rules.quality.Quality;

@AutoValue
@AutoGson(autoValueClass = AutoValue_NumericQuality.class)
public abstract class NumericQuality extends Quality<Double>
{
	public abstract Double getQualityIndicator();

	public static NumericQuality create(double qualityIndicator)
	{
		return new AutoValue_NumericQuality(qualityIndicator);
	}

	@Override
	public int compareTo(Quality<Double> quality)
	{
		return Double.compare(quality.getQualityIndicator(), getQualityIndicator());
	}
}