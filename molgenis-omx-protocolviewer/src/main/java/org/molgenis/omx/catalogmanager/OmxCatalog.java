package org.molgenis.omx.catalogmanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogFolder;
import org.molgenis.catalog.CatalogItem;
import org.molgenis.omx.observ.DataSet;

public class OmxCatalog implements Catalog
{
	private final DataSet dataSet;
	private transient Map<String, CatalogItem> catalogItemIndex;

	public OmxCatalog(DataSet dataSet)
	{
		if (dataSet == null) throw new IllegalArgumentException("Data set is null");
		this.dataSet = dataSet;
	}

	@Override
	public String getId()
	{
		return dataSet.getIdentifier();
	}

	@Override
	public String getName()
	{
		return dataSet.getName();
	}

	@Override
	public String getDescription()
	{
		return dataSet.getDescription();
	}

	@Override
	public List<CatalogFolder> getChildren()
	{
		return Collections.<CatalogFolder> singletonList(new OmxCatalogFolder(dataSet.getProtocolUsed()));
	}

	@Override
	public List<CatalogItem> getItems()
	{
		return Collections.emptyList();
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
		if (catalogItemIndex == null) createCatalogItemIndex();
		return catalogItemIndex.get(catalogItemId);
	}

	private void createCatalogItemIndex()
	{
		catalogItemIndex = new HashMap<String, CatalogItem>();
		for (CatalogFolder childNode : getChildren())
			createCatalogItemIndexRec(childNode);
	}

	private void createCatalogItemIndexRec(CatalogFolder node)
	{
		List<CatalogItem> items = node.getItems();
		if (items != null)
		{
			for (CatalogItem item : items)
				catalogItemIndex.put(item.getId(), item);
		}
		List<CatalogFolder> children = node.getChildren();
		if (children != null)
		{
			for (CatalogFolder childNode : children)
				createCatalogItemIndexRec(childNode);
		}
	}
}
