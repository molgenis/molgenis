package org.molgenis.ui.form;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

public class EntityFormMetaData implements FormMetaData
{
	private final EntityMetaData entityMetaData;

	public EntityFormMetaData(EntityMetaData entityMetaData)
	{
		this.entityMetaData = entityMetaData;
	}

	@Override
	public List<AttributeMetaData> getFields()
	{
		List<AttributeMetaData> attributes = new ArrayList<AttributeMetaData>();
		for (AttributeMetaData attr : entityMetaData.getAttributes())
		{
			if (!attr.isIdAtrribute() && !attr.getName().equals("__Type"))// TODO system fields in AttributeMetaData
			{
				attributes.add(attr);
			}
		}

		return attributes;
	}

	@Override
	public String getName()
	{
		return entityMetaData.getName();
	}

}
