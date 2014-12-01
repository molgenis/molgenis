package org.molgenis.catalog;

import java.util.List;

public interface CatalogFolder extends CatalogItem
{
	String getExternalId(); // FIXME continue here

	List<CatalogFolder> getChildren();

	List<CatalogItem> getItems();
}
