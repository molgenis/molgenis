package org.molgenis.data.view.response;

import java.util.List;

public class EntityViewCollectionResponse
{
	private String identifier;
	private String viewName;
	private String masterEntityName;
	private List<String> joinedEntityNames;

	public EntityViewCollectionResponse(String identifier, String viewName, String masterEntityName,
			List<String> joinedEntityNames)
	{
		this.identifier = identifier;
		this.viewName = viewName;
		this.masterEntityName = masterEntityName;
		this.joinedEntityNames = joinedEntityNames;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String getViewName()
	{
		return viewName;
	}

	public String getMasterEntityName()
	{
		return masterEntityName;
	}

	public List<String> getJoinedEntityNames()
	{
		return joinedEntityNames;
	}
}