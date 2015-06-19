package org.molgenis.catalogue;

import java.util.List;

import javax.validation.constraints.NotNull;

public class RefreshShoppingCartRequest
{
	@NotNull
	private String entityName;

	@NotNull
	private List<String> attributeNames;

	public String getEntityName()
	{
		return entityName;
	}

	public List<String> getAttributeNames()
	{
		return attributeNames;
	}

	@Override
	public String toString()
	{
		return "RefreshShoppingCartRequest [entityName=" + entityName + ", attributeNames=" + attributeNames + "]";
	}

}
