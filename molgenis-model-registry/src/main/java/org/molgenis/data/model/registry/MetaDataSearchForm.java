package org.molgenis.data.model.registry;

import java.util.List;

public class MetaDataSearchForm
{
	private String searchTerm;
	private List<String> entityClassTypes = MetaDataExplorerController.ENTITY_CLASS_TYPES;
	private int page = 1;

	public String getSearchTerm()
	{
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm)
	{
		this.searchTerm = searchTerm;
	}

	public List<String> getEntityClassTypes()
	{
		return entityClassTypes;
	}

	public void setEntityClassTypes(List<String> entityClassTypes)
	{
		this.entityClassTypes = entityClassTypes;
	}

	public void setPage(int page)
	{
		this.page = page;
	}

	public int getPage()
	{
		return page;
	}

}
