package org.molgenis.data.support;

import java.util.Collections;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Package;
import org.molgenis.data.Range;
import org.molgenis.fieldtypes.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeTraverser;

public abstract class AbstractEntityMetaData implements EntityMetaData
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractEntityMetaData.class);

	private String labelAttribute;
	private String idAttribute;

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
					Iterable<AttributeMetaData> parts = attributeMetaData.getAttributeParts();
					return parts != null ? parts : Collections.<AttributeMetaData> emptyList();
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
			public String getExpression()
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

			@Override
			public List<String> getEnumOptions()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isSameAs(AttributeMetaData attributeMetaData)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String getVisibleExpression()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String getValidationExpression()
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

	/**
	 * Gets the fully qualified name of this EntityMetaData.
	 */
	@Override
	public String getName()
	{
		StringBuilder sb = new StringBuilder();

		Package p = getPackage();
		if (p != null && !Package.DEFAULT_PACKAGE_NAME.equals(p.getName()))
		{
			sb.append(p.getName());
			sb.append(Package.PACKAGE_SEPARATOR);
		}
		sb.append(getSimpleName());

		return sb.toString();
	}

	@Override
	public AttributeMetaData getIdAttribute()
	{
		AttributeMetaData idAttributeMetaData = null;
		if (idAttribute != null)
		{
			AttributeMetaData att = getAttribute(idAttribute);
			if (att == null) throw new RuntimeException(getName() + ".getIdAttribute() failed: '" + idAttribute
					+ "' unknown");
			idAttributeMetaData = att;
		}
		if (idAttributeMetaData == null)
		{
			for (AttributeMetaData attribute : getAttributesTraverser())
			{
				if (attribute.isIdAtrribute())
				{
					idAttributeMetaData = attribute;
					break;
				}
			}

			if (getExtends() != null)
			{
				idAttributeMetaData = getExtends().getIdAttribute();
			}
			if (idAttributeMetaData == null && !isAbstract())
			{
				LOG.error("No idAttribute specified for entity{}, this attribute is required", getName());
				// FIXME enable exception when https://github.com/molgenis/molgenis/issues/1400 is fixed
				// throw new RuntimeException("No idAttribute specified, this attribute is required");
			}
		}
		return idAttributeMetaData;
	}

	public void setIdAttribute(String name)
	{
		this.idAttribute = name;
	}

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		if (labelAttribute != null)
		{
			AttributeMetaData att = getAttribute(labelAttribute);
			if (att == null) throw new MolgenisDataException("getLabelAttribute() failed: '" + labelAttribute
					+ "' unknown");

			return att;
		}
		else
		{
			for (AttributeMetaData attribute : getAttributesTraverser())
			{
				if (attribute.isLabelAttribute())
				{
					return attribute;
				}
			}
		}

		return getIdAttribute();
	}

	@Override
	public Iterable<AttributeMetaData> getLookupAttributes()
	{
		return Iterables.filter(getAttributesTraverser(), new Predicate<AttributeMetaData>()
		{
			@Override
			public boolean apply(AttributeMetaData attribute)
			{
				return attribute.isLookupAttribute();
			}
		});
	}

	public void setLabelAttribute(String name)
	{
		this.labelAttribute = name;
	}

	@Override
	public AttributeMetaData getAttribute(String attributeName)
	{
		AttributeMetaData attr = null;

		for (AttributeMetaData attribute : getAttributesTraverser())
		{
			if (attribute.getName().equalsIgnoreCase(attributeName))
			{
				attr = attribute;
				break;
			}
		}

		if ((attr == null) && (getExtends() != null)) attr = getExtends().getAttribute(attributeName);

		return attr;
	}
}
