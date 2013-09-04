package org.molgenis.ui.form;

import org.molgenis.model.elements.Entity;

public class EntityForm implements Form
{
	private final Entity entityMetaData;
	private org.molgenis.util.Entity entity;
	private String primaryKey;
	private final boolean hasWritePermission;

	public EntityForm(Entity entityMetaData, boolean hasWritePermission)
	{
		this.entityMetaData = entityMetaData;
		this.hasWritePermission = hasWritePermission;
	}

	public EntityForm(Entity entityMetaData, org.molgenis.util.Entity entity, String primaryKey,
			boolean hasWritePermission)
	{
		this.entityMetaData = entityMetaData;
		this.primaryKey = primaryKey;
		this.hasWritePermission = hasWritePermission;
		this.entity = entity;
	}

	@Override
	public String getTitle()
	{
		return entityMetaData.getLabel();
	}

	@Override
	public FormMetaData getMetaData()
	{
		return new EntityFormMetaData(entityMetaData);
	}

	@Override
	public String getPrimaryKey()
	{
		return primaryKey;
	}

	@Override
	public boolean getHasWritePermission()
	{
		return hasWritePermission;
	}

	@Override
	public org.molgenis.util.Entity getEntity()
	{
		return entity;
	}

}
