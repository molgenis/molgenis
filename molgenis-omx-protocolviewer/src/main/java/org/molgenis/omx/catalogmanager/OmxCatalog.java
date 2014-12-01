package org.molgenis.omx.catalogmanager;

import java.util.Collections;
import java.util.List;

import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogFolder;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
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
	public CatalogFolder findItem(String catalogItemId)
	{
		// TODO verify that the catalog item is part of this catalog.
		// this check was removed because of bad performance (worst case the full protocol tree needs to be pruned).
		Protocol protocol = dataService.findOne(Protocol.ENTITY_NAME, new QueryImpl().eq(Protocol.ID, catalogItemId)
				.and().eq(Protocol.ACTIVE, true), Protocol.class);
		if (protocol == null) throw new IllegalArgumentException("catalogItemId does not exist");
		return new OmxCatalogFolder(protocol);
	}
}
