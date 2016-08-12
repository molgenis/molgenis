package org.molgenis.pathways.model;

import com.google.auto.value.AutoValue;
import org.molgenis.wikipathways.client.WSPathwayInfo;
import org.molgenis.wikipathways.client.WSSearchResult;

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