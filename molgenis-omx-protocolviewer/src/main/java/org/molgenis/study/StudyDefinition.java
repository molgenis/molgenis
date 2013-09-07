package org.molgenis.study;

import java.util.List;

import org.molgenis.catalog.CatalogItem;

public interface StudyDefinition
{
	String getId();

	void setId(String id);

	String getName();

	String getDescription();

	String getCatalogVersion();

	List<CatalogItem> getItems();

	boolean containsItem(CatalogItem item);

	List<String> getAuthors();

	String getAuthorEmail();
}
