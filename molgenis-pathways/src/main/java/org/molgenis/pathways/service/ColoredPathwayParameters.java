package org.molgenis.pathways.service;

import com.google.auto.value.AutoValue;
import com.google.common.cache.LoadingCache;
import org.molgenis.pathways.model.Impact;

import java.util.Map;

/**
 * Parameter object used as key for the {@link LoadingCache} with the colored pathways.
 */
@AutoValue
abstract class ColoredPathwayParameters
{
	public abstract String getPathwayId();

	public abstract Map<String, Impact> getImpactPerGraphId();

	public String[] getGraphIdArray()
	{
		return getImpactPerGraphId().keySet().toArray(new String[0]);
	}

	public String[] getColorArray()
	{
		return getImpactPerGraphId().values().stream().map(Impact::getColor).toArray(String[]::new);
	}

	public static ColoredPathwayParameters create(String pathwayId, Map<String, Impact> impactPerGraphId)
	{
		return new AutoValue_ColoredPathwayParameters(pathwayId, impactPerGraphId);
	}
}