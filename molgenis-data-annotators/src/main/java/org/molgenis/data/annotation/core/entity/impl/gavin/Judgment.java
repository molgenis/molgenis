package org.molgenis.data.annotation.core.entity.impl.gavin;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

/**
 * Judgment result of the gavin method
 */
@AutoValue
public abstract class Judgment
{
	public enum Classification
	{
		Benign, Pathogenic, VOUS
	}

	public enum Method
	{
		calibrated, genomewide
	}

	public abstract Classification getClassification();

	public abstract Method getConfidence();

	@Nullable
	public abstract String getGene();

	public abstract String getReason();

	public static Judgment create(Classification classification, Method method, String gene, String reason)
	{
		return new AutoValue_Judgment(classification, method, gene, reason);
	}

}