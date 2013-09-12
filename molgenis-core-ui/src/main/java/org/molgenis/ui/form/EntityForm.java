package org.molgenis.ui.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.molgenis.model.elements.Entity;

public class EntityForm implements Form
{
	private final Entity entityMetaData;
	private org.molgenis.util.Entity entity;
	private String primaryKey;
	private final boolean hasWritePermission;
	private final List<SubEntityForm> subForms = new ArrayList<SubEntityForm>();

	public EntityForm(Entity entityMetaData, boolean hasWritePermission)
	{
		this.entityMetaData = entityMetaData;
		this.hasWritePermission = hasWritePermission;
	}

	public EntityForm(Entity entityMetaData, boolean hasWritePermission, org.molgenis.util.Entity entity)
	{
		this.entityMetaData = entityMetaData;
		this.hasWritePermission = hasWritePermission;
		this.entity = entity;
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

	public List<SubEntityForm> getSubForms()
	{
		return Collections.unmodifiableList(subForms);
	}

	public void addSubForm(SubEntityForm entityForm)
	{
		subForms.add(entityForm);
	}

	@Override
	public org.molgenis.util.Entity getEntity()
	{
		return entity;
	}

	@Override
	public String getBaseUri(String contextUrl)
	{
		return contextUrl.substring(0, contextUrl.lastIndexOf('/')) + "/"
				+ MolgenisEntityFormPluginController.PLUGIN_NAME_PREFIX + getMetaData().getName();
	}

}
