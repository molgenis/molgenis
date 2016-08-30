package org.molgenis.data.support;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;

import static java.lang.String.format;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

public class EntityMetaDataUtils
{
	private EntityMetaDataUtils()
	{
	}

	/**
	 * Returns whether the attribute type references single entities (e.g. is 'XREF').
	 *
	 * @param attr attribute
	 * @return true if an attribute references a single entity
	 */
	public static boolean isSingleReferenceType(AttributeMetaData attr)
	{
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case CATEGORICAL:
			case FILE:
			case XREF:
			case MANY_TO_ONE:
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
	public static boolean isMultipleReferenceType(AttributeMetaData attr)
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
			case MANY_TO_ONE:
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
	public static boolean isReferenceType(AttributeMetaData attr)
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
			case MANY_TO_ONE:
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
	public static boolean isStringType(AttributeMetaData attr)
	{
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case EMAIL:
			case HYPERLINK:
			case STRING:
				return true;
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
	 * Returns whether the attribute type is a string type.
	 *
	 * @param attr attribute
	 * @return true if the attribute is a string type.
	 */
	public static boolean isTextType(AttributeMetaData attr)
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
	 * Returns attribute names for the given attributes
	 *
	 * @return attribute names
	 */
	public static Iterable<String> getAttributeNames(Iterable<AttributeMetaData> attrs)
	{
		return () -> stream(attrs.spliterator(), false).map(AttributeMetaData::getName).iterator();
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
		if (package_ != null && !PACKAGE_DEFAULT.equals(package_.getName()))
		{
			StringBuilder sb = new StringBuilder();
			sb.append(package_.getName());
			sb.append(PACKAGE_SEPARATOR);
			sb.append(simpleName);
			return sb.toString();
		}
		else
		{
			return simpleName;
		}
	}

	public static Fetch createFetchForReindexing(EntityMetaData refEntityMetaData)
	{
		Fetch fetch = new Fetch();
		for (AttributeMetaData attr : refEntityMetaData.getAtomicAttributes())
		{
			if (attr.getRefEntity() != null)
			{
				Fetch attributeFetch = new Fetch();
				for (AttributeMetaData refAttr : attr.getRefEntity().getAtomicAttributes())
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
}
