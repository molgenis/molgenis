package org.molgenis.omx.catalogmanager;

import java.util.Collection;

import org.molgenis.catalog.CatalogFolder;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;

public class OmxCatalogItem extends AbstractOmxCatalogItem
{
	private final ObservableFeature observableFeature;

	public OmxCatalogItem(ObservableFeature observableFeature)
	{
		if (observableFeature == null) throw new IllegalArgumentException("Observable feature is null");
		this.observableFeature = observableFeature;
	}

	@Override
	public String getId()
	{
		return observableFeature.getId().toString();
	}

	@Override
	public String getName()
	{
		return observableFeature.getName();
	}

	@Override
	public String getDescription()
	{
		return observableFeature.getDescription();
	}

	@Override
	public String getCode()
	{
		return null;
	}

	@Override
	public String getCodeSystem()
	{
		return null;
	}

	@Override
	public Iterable<CatalogFolder> getPath()
	{
		Collection<Protocol> protocols = observableFeature.getFeaturesProtocolCollection();
		if (protocols == null || protocols.size() != 1)
		{
			throw new RuntimeException("ObservableFeature must belong to exactly one protocol");
		}
		return getPath(protocols.iterator().next());
	}
}
