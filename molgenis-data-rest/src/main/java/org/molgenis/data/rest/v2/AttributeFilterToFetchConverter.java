package org.molgenis.data.rest.v2;

import static java.lang.String.format;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.FILE;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.UnknownAttributeException;
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
			return null;
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
				if (subAttrFilter != null)
				{
					Fetch subFetch = convert(subAttrFilter, attr.getRefEntity());
					fetch.field(attr.getName(), subFetch);
				}
				else
				{
					// In case of no sub attribute filter return id and label. For FILE type also include the
					// URL.
					Fetch subFetch = new Fetch();

					String idAttrName = attr.getRefEntity().getIdAttribute().getName();
					subFetch.field(idAttrName);

					String labelAttrName = attr.getRefEntity().getLabelAttribute().getName();
					if (!labelAttrName.equals(idAttrName))
					{
						subFetch.field(labelAttrName);
					}

					if (attrType == FILE)
					{
						subFetch.field(FileMeta.URL);
					}
					fetch.field(attr.getName(), subFetch);
				}
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
}
