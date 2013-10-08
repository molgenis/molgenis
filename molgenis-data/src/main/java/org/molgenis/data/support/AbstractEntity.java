package org.molgenis.data.support;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;

public abstract class AbstractEntity implements Entity
{
	private static final long serialVersionUID = 1L;
	private final EntityMetaData metaData;

	public AbstractEntity(EntityMetaData metaData)
	{
		if (metaData == null) throw new IllegalArgumentException("EntityMetaData cannot be null");
		this.metaData = metaData;
	}

	@Override
	public void set(Entity entity)
	{
		for (AttributeMetaData attribute : metaData.getAttributes())
		{
			Object value = entity.get(attribute.getName());
			if (value != null)
			{
				set(attribute.getName(), value);
			}
		}
	}

	@Override
	public Integer getIdValue()
	{
		AttributeMetaData idAttribute = metaData.getIdAttribute();
		if (idAttribute == null)
		{
			return null;
		}

		Object id = get(idAttribute.getName());
		if (!id.getClass().isAssignableFrom(Integer.class))
		{
			throw new MolgenisDataException("Id attribute should be of type Integer but is of type [" + id.getClass()
					+ "]");
		}

		return (Integer) id;
	}

	@Override
	public String getLabelValue()
	{
		AttributeMetaData labelAttribute = metaData.getLabelAttribute();
		if (labelAttribute == null)
		{
			return null;
		}

		Object label = get(labelAttribute.getName());

		return DataConverter.convert(label, String.class);
	}
}
