package org.molgenis.catalog;

import java.util.List;

public interface Catalog extends CatalogFolder
{
	String getVersion();

	List<String> getAuthors();

	String getAuthorEmail();

	CatalogFolder findItem(String catalogItemId);
}
