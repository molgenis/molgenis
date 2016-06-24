package org.molgenis.pathways.model;

import org.molgenis.wikipathways.client.WSPathwayInfo;
import org.molgenis.wikipathways.client.WSSearchResult;

import com.google.auto.value.AutoValue;

/**
 * Name and ID of a Pathway
 */
@AutoValue
public abstract class Pathway
{
	public abstract String getId();

	public abstract String getName();

	public static Pathway create(String id, String name)
	{
		return new AutoValue_Pathway(id, name + " (" + id + ")");
	}

	public static Pathway create(WSPathwayInfo p)
	{
		return Pathway.create(p.getId(), p.getName());
	}

	public static Pathway create(WSSearchResult p)
	{
		return Pathway.create(p.getId(), p.getName());
	}
}