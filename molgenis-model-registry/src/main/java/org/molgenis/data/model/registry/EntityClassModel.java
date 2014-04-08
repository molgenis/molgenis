package org.molgenis.data.model.registry;

public class EntityClassModel
{
	private final EntityClass entityClass;
	private final String explorerUri;
	private final String formUri;

	public EntityClassModel(EntityClass entityClass, String explorerUri, String formUri)
	{
		this.entityClass = entityClass;
		this.explorerUri = explorerUri;
		this.formUri = formUri;
	}

	public EntityClass getEntityClass()
	{
		return entityClass;
	}

	public String getExplorerUri()
	{
		return explorerUri;
	}

	public String getFormUri()
	{
		return formUri;
	}

}
