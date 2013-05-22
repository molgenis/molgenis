package org.molgenis.lifelines.catalogue;

import java.util.Arrays;
import java.util.List;

/**
 * Dummy implementation of the CatalogueLoaderService we can us it without the
 * need to use a real LL backend
 * 
 * @author erwin
 * 
 */
public class MockCatalogueLoaderService implements CatalogLoaderService
{
	private final List<CatalogInfo> catalogs;

	public MockCatalogueLoaderService()
	{
		this(Arrays.asList(new CatalogInfo("1", "RELEASE_1"), new CatalogInfo("2", "RELEASE_2")));
	}

	public MockCatalogueLoaderService(List<CatalogInfo> catalogs)
	{
		super();
		this.catalogs = catalogs;
	}

	@Override
	public List<CatalogInfo> findCatalogs()
	{
		return catalogs;
	}

	@Override
	public void loadCatalog(String id) throws UnknownCatalogException
	{
		for (CatalogInfo catalog : catalogs)
		{
			if (catalog.getId().equals(id))
			{
				System.out.println("Catalog [" + id + "] loaded");
				return;
			}
		}

		throw new UnknownCatalogException("Unknown catalog id [" + id + "]");
	}

}
