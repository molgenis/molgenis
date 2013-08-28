package org.molgenis.ui.form;

import org.molgenis.model.elements.Entity;

public class EntityForm implements Form
{
	private final Entity entity;

	public EntityForm(Entity entity)
	{
		this.entity = entity;
	}

	@Override
	public String getTitle()
	{
		return entity.getLabel();
	}

	@Override
	public FormMetaData getMetaData()
	{
		return new EntityFormMetaData(entity);
	}

}
