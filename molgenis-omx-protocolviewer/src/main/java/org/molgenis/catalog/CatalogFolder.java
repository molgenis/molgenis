package org.molgenis.catalog;

import java.util.List;

public interface CatalogFolder extends CatalogItem
{
	String getExternalId();

	List<CatalogFolder> getChildren();

	List<CatalogItem> getItems();
}
