package org.molgenis.data.support;

import org.molgenis.data.Fetch;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.util.EntityUtils;

import static java.lang.String.format;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

public class EntityTypeUtils
{
	private EntityTypeUtils()
	{
	}

	/**
	 * Returns whether the attribute type references single entities (e.g. is 'XREF').
	 *
	 * @param attr attribute
	 * @return true if an attribute references a single entity
	 */
	public static boolean isSingleReferenceType(Attribute attr)
	{
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case CATEGORICAL:
			case FILE:
			case XREF:
				return true;
			case BOOL:
			case CATEGORICAL_MREF:
			case COMPOUND:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case INT:
			case LONG:
			case MREF:
			case ONE_TO_MANY:
			case SCRIPT:
			case STRING:
			case TEXT:
				return false;
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	/**
	 * Returns whether the attribute type references multiple entities (e.g. is 'MREF').
	 *
	 * @param attr attribute
	 * @return true if an attribute references multiple entities
	 */
	public static boolean isMultipleReferenceType(Attribute attr)
	{
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				return true;
			case BOOL:
			case CATEGORICAL:
			case COMPOUND:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case EMAIL:
			case ENUM:
			case FILE:
			case HTML:
			case HYPERLINK:
			case INT:
			case LONG:
			case SCRIPT:
			case STRING:
			case TEXT:
			case XREF:
				return false;
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	/**
	 * Returns whether the attribute references other entities (e.g. is 'XREF' or 'MREF').
	 *
	 * @param attr attribute
	 * @return true if the attribute references other entities
	 */
	public static boolean isReferenceType(Attribute attr)
	{
		return isReferenceType(attr.getDataType());
	}

	/**
	 * Returns whether the attribute type references other entities (e.g. is 'XREF' or 'MREF').
	 *
	 * @param attrType attribute type
	 * @return true if the attribute type references other entities
	 */
	public static boolean isReferenceType(AttributeType attrType)
	{
		switch (attrType)
		{
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case FILE:
			case MREF:
			case ONE_TO_MANY:
			case XREF:
				return true;
			case BOOL:
			case COMPOUND:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case INT:
			case LONG:
			case SCRIPT:
			case STRING:
			case TEXT:
				return false;
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	/**
	 * Returns whether the attribute type is a string type.
	 *
	 * @param attr attribute
	 * @return true if the attribute is a string type.
	 */
	public static boolean isStringType(Attribute attr)
	{
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case EMAIL:
			case HYPERLINK:
			case STRING:
				return true;
			case ONE_TO_MANY:
			case BOOL:
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case COMPOUND:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case ENUM:
			case FILE:
			case HTML: // text type is not a string type
			case INT:
			case LONG:
			case MREF:
			case SCRIPT: // text type is not a string type
			case TEXT: // text type is not a string type
			case XREF:
				return false;
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	/**
	 * Returns whether the attribute type is a text type.
	 *
	 * @param attr attribute
	 * @return true if the attribute is a text type.
	 */
	public static boolean isTextType(Attribute attr)
	{
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case HTML:
			case SCRIPT:
			case TEXT:
				return true;
			case BOOL:
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case ONE_TO_MANY:
			case COMPOUND:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case EMAIL:
			case ENUM:
			case FILE:
			case HYPERLINK:
			case INT:
			case LONG:
			case MREF:
			case STRING:
			case XREF:
				return false;
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	/**
	 * Returns whether the attribute is an integer type.
	 *
	 * @param attrType attribute type
	 * @return true if the attribute is an integer type.
	 */
	public static boolean isIntegerType(AttributeType attrType)
	{
		switch (attrType)
		{
			case INT:
			case LONG:
				return true;
			case HTML:
			case SCRIPT:
			case TEXT:
			case BOOL:
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case COMPOUND:
			case ONE_TO_MANY:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case EMAIL:
			case ENUM:
			case FILE:
			case HYPERLINK:
			case MREF:
			case STRING:
			case XREF:
				return false;
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	/**
	 * Returns whether the attribute is a date type.
	 *
	 * @param attrType attribute type
	 * @return true if the attribute is a date type.
	 */
	public static boolean isDateType(AttributeType attrType)
	{
		switch (attrType)
		{
			case DATE:
			case DATE_TIME:
				return true;
			case LONG:
			case INT:
			case HTML:
			case SCRIPT:
			case TEXT:
			case BOOL:
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case COMPOUND:
			case ONE_TO_MANY:
			case DECIMAL:
			case EMAIL:
			case ENUM:
			case FILE:
			case HYPERLINK:
			case MREF:
			case STRING:
			case XREF:
				return false;
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	/**
	 * Returns attribute names for the given attributes
	 *
	 * @return attribute names
	 */
	public static Iterable<String> getAttributeNames(Iterable<Attribute> attrs)
	{
		return () -> stream(attrs.spliterator(), false).map(Attribute::getName).iterator();
	}

	/**
	 * Builds and returns an entity full name based on a package and a simpleName
	 *
	 * @param package_
	 * @param simpleName
	 * @return String entity full name
	 */
	public static String buildFullName(Package package_, String simpleName)
	{
		if (package_ != null)
		{
			String sb = package_.getId() + PACKAGE_SEPARATOR + simpleName;
			return sb;
		}
		else
		{
			return simpleName;
		}
	}

	public static Fetch createFetchForReindexing(EntityType refEntityType)
	{
		Fetch fetch = new Fetch();
		for (Attribute attr : refEntityType.getAtomicAttributes())
		{
			if (attr.getRefEntity() != null)
			{
				Fetch attributeFetch = new Fetch();
				for (Attribute refAttr : attr.getRefEntity().getAtomicAttributes())
				{
					attributeFetch.field(refAttr.getName());
				}
				fetch.field(attr.getName(), attributeFetch);
			}
			else
			{
				fetch.field(attr.getName());
			}
		}
		return fetch;
	}

	public static boolean hasSelfReferences(EntityType entityType)
	{
		for (Attribute attr : entityType.getAtomicAttributes())
		{
			if (attr.getRefEntity() != null)
			{
				if (EntityUtils.equals(attr.getRefEntity(), entityType))
				{
					return true;
				}
			}
		}
		return false;
	}
}
