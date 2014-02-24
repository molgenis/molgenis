package org.molgenis.data.support;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public abstract class AbstractMetaDataEntity extends AbstractEntity
{
	private static final long serialVersionUID = 1L;
	private final EntityMetaData metaData;

	public AbstractMetaDataEntity(EntityMetaData metaData)
	{
		if (metaData == null) throw new IllegalArgumentException("EntityMetaData cannot be null");
		this.metaData = metaData;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return metaData;
	}

	@Override
	public void set(Entity entity, boolean strict)
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
		List<String> labels = Lists.transform(metaData.getLabelAttributes(), new Function<AttributeMetaData, String>()
		{
			@Override
			public String apply(AttributeMetaData attr)
			{
				Object label = get(attr.getName());
				return DataConverter.convert(label, String.class);
			}
		});

		return labels.isEmpty() ? null : StringUtils.join(labels, ':');
	}

	@Override
	public List<String> getLabelAttributeNames()
	{
		return Arrays.asList(new String[]
		{ getLabelValue() });
	}

}
