package org.molgenis.data.annotation.core.entity.impl.gavin;

import com.google.auto.value.AutoValue;
import org.molgenis.data.Entity;

import javax.annotation.Nullable;

@AutoValue
public abstract class GavinThresholds
{
	public static final String PATHO_MAF_THRESHOLD = "PathoMAFThreshold";
	public static final String MEAN_PATHOGENIC_CADD_SCORE = "MeanPathogenicCADDScore";
	public static final String MEAN_POPULATION_CADD_SCORE = "MeanPopulationCADDScore";
	public static final String SPEC_95TH_PER_CADD_THRESHOLD = "Spec95thPerCADDThreshold";
	public static final String SENS_95TH_PER_CADD_THRESHOLD = "Sens95thPerCADDThreshold";
	private static final String CATEGORY = "Category";

	@Nullable
	abstract public Double getPathoMAFThreshold();

	@Nullable
	abstract public Double getMeanPathogenicCADDScore();

	@Nullable
	abstract public Double getMeanPopulationCADDScore();

	@Nullable
	abstract public Double getSpec95thPerCADDThreshold();

	@Nullable
	abstract public Double getSens95thPerCADDThreshold();

	abstract public Category getCategory();

	public GavinThresholds withExtraSensitivity(int extraSensitivityFactor)
	{
		Double pathoMAFThreshold = getPathoMAFThreshold();
		Double meanPathogenicCADDScore = getMeanPathogenicCADDScore();
		Double meanPopulationCADDScore = getMeanPopulationCADDScore();
		Double spec95thPerCADDThreshold = getSpec95thPerCADDThreshold();
		Double sens95thPerCADDThreshold = getSens95thPerCADDThreshold();
		if (pathoMAFThreshold != null)
		{
			pathoMAFThreshold = pathoMAFThreshold * extraSensitivityFactor * 2;
		}
		if (meanPathogenicCADDScore != null)
		{
			meanPathogenicCADDScore = meanPathogenicCADDScore - extraSensitivityFactor;
		}
		if (meanPopulationCADDScore != null)
		{
			meanPopulationCADDScore = meanPopulationCADDScore - extraSensitivityFactor;
		}
		if (spec95thPerCADDThreshold != null)
		{
			spec95thPerCADDThreshold = spec95thPerCADDThreshold - extraSensitivityFactor;
		}
		if (sens95thPerCADDThreshold != null)
		{
			sens95thPerCADDThreshold = sens95thPerCADDThreshold - extraSensitivityFactor;
		}
		return new AutoValue_GavinThresholds(pathoMAFThreshold, meanPathogenicCADDScore, meanPopulationCADDScore,
				spec95thPerCADDThreshold, sens95thPerCADDThreshold, getCategory());
	}

	public static GavinThresholds create(Double pathoMAFThreshold, Double meanPathogenicCADDScore,
			Double meanPopulationCADDScore, Double spec95thPerCADDThreshold, Double sens95thPerCADDThreshold,
			Category category)
	{
		return new AutoValue_GavinThresholds(pathoMAFThreshold, meanPathogenicCADDScore, meanPopulationCADDScore,
				spec95thPerCADDThreshold, sens95thPerCADDThreshold, category);
	}

	public static GavinThresholds fromEntity(Entity annotationSourceEntity)
	{
		Double pathoMAFThreshold, meanPathogenicCADDScore, meanPopulationCADDScore, spec95thPerCADDThreshold, sens95thPerCADDThreshold;
		//get data from entity for the annotator
		pathoMAFThreshold = annotationSourceEntity.getString(PATHO_MAF_THRESHOLD) != null ? Double.valueOf(
				annotationSourceEntity.getString(PATHO_MAF_THRESHOLD)) : null;
		meanPathogenicCADDScore = annotationSourceEntity.getString(MEAN_PATHOGENIC_CADD_SCORE) != null ? Double.valueOf(
				annotationSourceEntity.getString(MEAN_PATHOGENIC_CADD_SCORE)) : null;
		meanPopulationCADDScore = annotationSourceEntity.getString(MEAN_POPULATION_CADD_SCORE) != null ? Double.valueOf(
				annotationSourceEntity.getString(MEAN_POPULATION_CADD_SCORE)) : null;
		spec95thPerCADDThreshold =
				annotationSourceEntity.getString(SPEC_95TH_PER_CADD_THRESHOLD) != null ? Double.valueOf(
						annotationSourceEntity.getString(SPEC_95TH_PER_CADD_THRESHOLD)) : null;
		sens95thPerCADDThreshold =
				annotationSourceEntity.getString(SENS_95TH_PER_CADD_THRESHOLD) != null ? Double.valueOf(
						annotationSourceEntity.getString(SENS_95TH_PER_CADD_THRESHOLD)) : null;
		return new AutoValue_GavinThresholds(pathoMAFThreshold, meanPathogenicCADDScore, meanPopulationCADDScore,
				spec95thPerCADDThreshold, sens95thPerCADDThreshold,
				Category.valueOf(annotationSourceEntity.getString(CATEGORY)));
	}

	public boolean isAboveMeanPathogenicCADDScore(double caddScaled)
	{
		return getMeanPathogenicCADDScore() != null && caddScaled > getMeanPathogenicCADDScore();
	}

	public boolean isBelowMeanPopulationCADDScore(double caddScaled)
	{
		return getMeanPopulationCADDScore() != null && caddScaled < getMeanPopulationCADDScore();
	}

	public boolean isAbovePathoMAFThreshold(Double exacMAF)
	{
		return exacMAF != null && getPathoMAFThreshold() != null && exacMAF > getPathoMAFThreshold();
	}

	public boolean isAboveSpec95thPerCADDThreshold(double caddScaled)
	{
		return getSpec95thPerCADDThreshold() != null && caddScaled > getSpec95thPerCADDThreshold();
	}

	public boolean isBelowSens95PerCADDThreshold(double caddScaled)
	{
		return getSens95thPerCADDThreshold() != null && caddScaled < getSens95thPerCADDThreshold();
	}
}
