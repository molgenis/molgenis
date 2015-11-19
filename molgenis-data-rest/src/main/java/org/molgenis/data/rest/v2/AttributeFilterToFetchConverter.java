package org.molgenis.data.rest.v2;

import static java.lang.String.format;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.FILE;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.file.FileMeta;

/**
 * Converts {@link AttributeFilter} to {@link Fetch}.
 */
public class AttributeFilterToFetchConverter
{
	/**
	 * Converts {@link AttributeFilter} to {@link Fetch} based on {@link EntityMetaData}.
	 * 
	 * @param attrFilter
	 * @param entityMeta
	 * @return {@link Fetch} or null for 'all attributes' {@link AttributeFilter}
	 * @throws UnknownAttributeException
	 */
	public static Fetch convert(AttributeFilter attrFilter, EntityMetaData entityMeta)
	{
		if (attrFilter == null)
		{
			return createDefaultEntityFetch(entityMeta);
		}

		if (attrFilter.isIncludeAllAttrs())
		{
			return null;
		}

		Fetch fetch = new Fetch();
		createFetchContentRec(attrFilter, entityMeta, fetch);
		return fetch;
	}

	private static void createFetchContentRec(AttributeFilter attrFilter, EntityMetaData entityMeta, Fetch fetch)
	{
		if (attrFilter.isIncludeAllAttrs())
		{
			return;
		}

		if (attrFilter.isIncludeIdAttr())
		{
			fetch.field(entityMeta.getIdAttribute().getName());
		}

		if (attrFilter.isIncludeLabelAttr())
		{
			fetch.field(entityMeta.getLabelAttribute().getName());
		}

		attrFilter.forEach(entry -> {
			String attrName = entry.getKey();
			AttributeMetaData attr = getAttribute(entityMeta, attrName);
			createFetchContentRec(attrFilter, entityMeta, attr, fetch);
		});
	}

	private static void createFetchContentRec(AttributeFilter attrFilter, EntityMetaData entityMeta,
			AttributeMetaData attr, Fetch fetch)
	{
		FieldTypeEnum attrType = attr.getDataType().getEnumType();
		switch (attrType)
		{
			case COMPOUND:
			{
				AttributeFilter subAttrFilter = attrFilter != null ? attrFilter.getAttributeFilter(attr) : null;
				if (subAttrFilter != null && !subAttrFilter.isIncludeAllAttrs())
				{
					// include compound attribute parts defined by filter
					if (subAttrFilter.isIncludeIdAttr())
					{
						createFetchContentRec(subAttrFilter, entityMeta, entityMeta.getIdAttribute(), fetch);
					}
					if (subAttrFilter.isIncludeLabelAttr())
					{
						createFetchContentRec(subAttrFilter, entityMeta, entityMeta.getLabelAttribute(), fetch);
					}
					subAttrFilter.forEach(entry -> {
						String attrPartName = entry.getKey();
						AttributeMetaData attrPart = attr.getAttributePart(attrPartName);
						createFetchContentRec(attrFilter, entityMeta, attrPart, fetch);
					});
				}
				else
				{
					// include all compound attribute parts
					attr.getAttributeParts().forEach(attrPart -> {
						createFetchContentRec(subAttrFilter, entityMeta, attrPart, fetch);
					});
				}
				break;
			}
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case FILE:
			case MREF:
			case XREF:
			{
				AttributeFilter subAttrFilter = attrFilter != null ? attrFilter.getAttributeFilter(attr) : null;
				Fetch subFetch;
				if (subAttrFilter != null)
				{
					subFetch = convert(subAttrFilter, attr.getRefEntity());

				}
				else
				{
					subFetch = createDefaultAttributeFetch(attr);
				}
				fetch.field(attr.getName(), subFetch);
				break;
			}
				// $CASES-OMITTED$
			default:
				fetch.field(attr.getName());
				break;
		}
	}

	private static AttributeMetaData getAttribute(EntityMetaData entityMeta, String attrName)
	{
		AttributeMetaData attr = entityMeta.getAttribute(attrName);
		if (attr == null)
		{
			throw new UnknownAttributeException(
					format("Unknown attribute [%s] of entity [%s]", attrName, entityMeta.getName()));
		}
		return attr;
	}

	/**
	 * Create default entity fetch that fetches all attributes.
	 * 
	 * @param entityMeta
	 * @return default entity fetch or null
	 */
	public static Fetch createDefaultEntityFetch(EntityMetaData entityMeta)
	{
		boolean hasRefAttr = false;
		Fetch fetch = new Fetch();
		for (AttributeMetaData attr : entityMeta.getAtomicAttributes())
		{
			Fetch subFetch = createDefaultAttributeFetch(attr);
			if (subFetch != null)
			{
				hasRefAttr = true;
			}
			fetch.field(attr.getName(), subFetch);
		}
		return hasRefAttr ? fetch : null;
	}

	/**
	 * Create default fetch for the given attribute. For attributes referencing entities the id and label value are
	 * fetched. Additionally for file entities the URL is fetched. For other attributes the default fetch is null;
	 * 
	 * @param attr
	 * @return default attribute fetch or null
	 */
	public static Fetch createDefaultAttributeFetch(AttributeMetaData attr)
	{
		Fetch fetch;
		if (attr.getDataType() instanceof XrefField || attr.getDataType() instanceof MrefField)
		{
			fetch = new Fetch();
			EntityMetaData refEntityMeta = attr.getRefEntity();
			String idAttrName = refEntityMeta.getIdAttribute().getName();
			fetch.field(idAttrName);

			String labelAttrName = refEntityMeta.getLabelAttribute().getName();
			if (!labelAttrName.equals(idAttrName))
			{
				fetch.field(labelAttrName);
			}

			if (attr.getDataType().getEnumType() == FILE)
			{
				fetch.field(FileMeta.URL);
			}
		}
		else
		{
			fetch = null;
		}
		return fetch;
	}
}
