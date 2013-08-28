package org.molgenis.ui.form;

import java.util.List;

import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Field;

public class EntityFormMetaData implements FormMetaData
{
	private final Entity entity;

	public EntityFormMetaData(Entity entity)
	{
		this.entity = entity;
	}

	@Override
	public List<Field> getFields()
	{
		try
		{
			List<Field> fields = entity.getNonSystemFields(true);

			// Remove pk field
			fields.remove(entity.getPrimaryKey());

			return fields;
		}
		catch (MolgenisModelException e)
		{
			throw new UnknownEntityException("Exception getting fields for entity [" + entity.getName() + "]", e);
		}
	}

	@Override
	public String getName()
	{
		return entity.getName();
	}

}
