package org.molgenis.ui.form;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

public class EntityFormMetaData implements FormMetaData
{
	private final EntityMetaData entityMetaData;
	private final boolean forUpdate;

	public EntityFormMetaData(EntityMetaData entityMetaData, boolean forUpdate)
	{
		this.entityMetaData = entityMetaData;
		this.forUpdate = forUpdate;
	}

	@Override
	public List<AttributeMetaData> getFields()
	{
		List<AttributeMetaData> attributes = new ArrayList<AttributeMetaData>();
		for (AttributeMetaData attr : entityMetaData.getAtomicAttributes())
		{
			if (attr.isIdAtrribute())
			{
				if (!attr.isAuto() && !forUpdate)
				{
					attributes.add(attr);
				}
			}
			else if (!attr.getName().equals("__Type"))
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

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		return entityMetaData.getLabelAttribute();
	}

}
