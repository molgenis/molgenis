package org.molgenis.pathways.service;

import com.google.auto.value.AutoValue;
import com.google.common.cache.LoadingCache;

/**
 * Parameter object used as key for the {@link LoadingCache} with Pathways per gene
 */
@AutoValue
abstract class PathwaysPerGeneParameters
{
	public abstract String getSpecies();

	public abstract String getGene();

	public String[] getGeneArray()
	{
		return new String[] { getGene() };
	}

	public static PathwaysPerGeneParameters create(String species, String gene)
	{
		return new AutoValue_PathwaysPerGeneParameters(species, gene);
	}
}