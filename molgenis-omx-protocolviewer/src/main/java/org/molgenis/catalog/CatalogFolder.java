package org.molgenis.catalog;

import java.util.List;

public interface CatalogFolder
{
	String getId();

	String getName();

	String getDescription();

	List<CatalogFolder> getChildren();

	List<CatalogItem> getItems();
}
