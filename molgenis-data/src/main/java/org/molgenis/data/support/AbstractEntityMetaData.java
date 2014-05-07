package org.molgenis.data.support;

import java.util.Collections;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Range;
import org.molgenis.fieldtypes.FieldType;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeTraverser;

public abstract class AbstractEntityMetaData implements EntityMetaData
{
	/**
	 * Returns an iterable over all attributes including the attribute parts of compound attributes
	 * 
	 * @return
	 */
	protected Iterable<AttributeMetaData> getAttributesTraverser()
	{
		return new TreeTraverser<AttributeMetaData>()
		{
			@Override
			public Iterable<AttributeMetaData> children(AttributeMetaData attributeMetaData)
			{
				FieldType dataType = attributeMetaData.getDataType();
				if (dataType.equals(MolgenisFieldTypes.COMPOUND))
				{
					return attributeMetaData.getAttributeParts();
				}
				else return Collections.<AttributeMetaData> emptyList();
			}
		}.preOrderTraversal(new AttributeMetaData()
		{
			// traverse in same order as attributes are inserted

			@Override
			public boolean isVisible()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isUnique()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isReadonly()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isNillable()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isLabelAttribute()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isIdAtrribute()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public EntityMetaData getRefEntity()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String getName()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String getLabel()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String getDescription()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Object getDefaultValue()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public FieldType getDataType()
			{
				return MolgenisFieldTypes.getType(FieldTypeEnum.COMPOUND.toString().toLowerCase());
			}

			@Override
			public Iterable<AttributeMetaData> getAttributeParts()
			{
				return getAttributes();
			}

			@Override
			public boolean isAuto()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isLookupAttribute()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isAggregateable()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Range getRange()
			{
				throw new UnsupportedOperationException();
			}
		}).skip(1);
	}

	@Override
	public Iterable<AttributeMetaData> getAtomicAttributes()
	{
		return Iterables.filter(getAttributesTraverser(), new Predicate<AttributeMetaData>()
		{
			@Override
			public boolean apply(AttributeMetaData attributeMetaData)
			{
				return attributeMetaData.getDataType().getEnumType() != FieldTypeEnum.COMPOUND;
			}
		});
	}

	@Override
	public AttributeMetaData getIdAttribute()
	{
		for (AttributeMetaData attribute : getAttributesTraverser())
		{
			if (attribute.isIdAtrribute())
			{
				return attribute;
			}
		}

		return null;
	}

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		for (AttributeMetaData attribute : getAttributesTraverser())
		{
			if (attribute.isLabelAttribute())
			{
				return attribute;
			}
		}

		return null;
	}

	@Override
	public AttributeMetaData getAttribute(String attributeName)
	{
		for (AttributeMetaData attribute : getAttributesTraverser())
		{
			if (attribute.getName().equalsIgnoreCase(attributeName))
			{
				return attribute;
			}
		}

		return null;
	}
}
