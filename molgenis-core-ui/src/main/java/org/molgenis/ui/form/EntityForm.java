package org.molgenis.ui.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

public class EntityForm implements Form
{
	private final EntityMetaData entityMetaData;
	private Entity entity;
	private Integer primaryKey;
	private boolean unique;
	private final boolean hasWritePermission;
	private final List<SubEntityForm> subForms = new ArrayList<SubEntityForm>();

	public EntityForm(EntityMetaData entityMetaData, boolean hasWritePermission)
	{
		this.entityMetaData = entityMetaData;
		this.hasWritePermission = hasWritePermission;
	}

	public EntityForm(EntityMetaData entityMetaData, boolean hasWritePermission, Entity entity)
	{
		this.entityMetaData = entityMetaData;
		this.hasWritePermission = hasWritePermission;
		this.entity = entity;
	}

	public EntityForm(EntityMetaData entityMetaData, Entity entity, Integer primaryKey, boolean hasWritePermission)
	{
		this.entityMetaData = entityMetaData;
		this.primaryKey = primaryKey;
		this.hasWritePermission = hasWritePermission;
		this.entity = entity;
	}

	@Override
	public String getTitle()
	{
		return entityMetaData.getName();
	}

	@Override
	public FormMetaData getMetaData()
	{
		return new EntityFormMetaData(entityMetaData);
	}

	@Override
	public Integer getPrimaryKey()
	{
		return primaryKey;
	}

	@Override
	public boolean getHasWritePermission()
	{
		return hasWritePermission;
	}

	public List<SubEntityForm> getSubForms()
	{
		return Collections.unmodifiableList(subForms);
	}

	public void addSubForm(SubEntityForm entityForm)
	{
		subForms.add(entityForm);
	}

	@Override
	public Entity getEntity()
	{
		return entity;
	}

	public boolean isUnique()
	{
		return unique;
	}

	@Override
	public String getBaseUri(String contextUrl)
	{
		return contextUrl.substring(0, contextUrl.lastIndexOf('/')) + "/"
				+ MolgenisEntityFormPluginController.PLUGIN_NAME_PREFIX + getMetaData().getName();
	}

}
