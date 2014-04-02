package org.molgenis.data.model.registry;

import java.util.List;

public class SearchForm
{
	private String searchTerm;
	private List<String> entityClassTypes = ModelRegistryController.ENTITY_CLASS_TYPES;

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
}
