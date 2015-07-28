package org.molgenis.ui.form;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Field;

public class EntityFormMetaData implements FormMetaData
{
	private final Entity entityMetaData;

	public EntityFormMetaData(Entity entityMetaData)
	{
		this.entityMetaData = entityMetaData;
	}

	@Override
	public List<Field> getFields()
	{
		try
		{
			List<Field> fields = new ArrayList<Field>(entityMetaData.getNonSystemFields(true));

			// Remove hidden fields
			ListIterator<Field> it = fields.listIterator();
			while (it.hasNext())
			{
				Field field = it.next();
				if (field.isHidden())
				{
					it.remove();
				}
			}

			// Workaround for bug that if an entity implements an abstract entity in the model xml and that has a hidden
			// field it is not set to hidden in the implementing entity
			fields.remove(entityMetaData.getPrimaryKey());

			return fields;
		}
		catch (MolgenisModelException e)
		{
			throw new UnknownEntityException("Exception getting fields for entity [" + entityMetaData.getName() + "]",
					e);
		}
	}

	@Override
	public String getName()
	{
		return entityMetaData.getName();
	}

}
