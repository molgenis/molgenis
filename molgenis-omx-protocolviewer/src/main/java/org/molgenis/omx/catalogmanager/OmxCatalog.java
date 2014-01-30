package org.molgenis.omx.catalogmanager;

import java.util.Collections;
import java.util.List;

import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogItem;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;

public class OmxCatalog extends OmxCatalogFolder implements Catalog
{
	private final DataService dataService;

	public OmxCatalog(Protocol protocol, DataService dataService)
	{
		super(protocol);
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	@Override
	public String getVersion()
	{
		return "Unknown";
	}

	@Override
	public List<String> getAuthors()
	{
		return Collections.emptyList();
	}

	@Override
	public String getAuthorEmail()
	{
		return null;
	}

	@Override
	public CatalogItem findItem(String catalogItemId)
	{
		// TODO verify that the catalog item is part of this catalog.
		// this check was removed because of bad performance (worst case the full protocol tree needs to be pruned).
		ObservableFeature observableFeature = dataService.findOne(ObservableFeature.ENTITY_NAME,
				new QueryImpl().eq(ObservableFeature.ID, catalogItemId), ObservableFeature.class);
		if (observableFeature == null) throw new IllegalArgumentException("catalogItemId does not exist");
		return new OmxCatalogItem(observableFeature);
	}
}
