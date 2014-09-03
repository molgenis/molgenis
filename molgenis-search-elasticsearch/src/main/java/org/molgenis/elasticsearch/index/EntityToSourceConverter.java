package org.molgenis.elasticsearch.index;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.base.Joiner;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.util.Cell;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class EntityToSourceConverter
{
	private final DataService dataService;

	@Autowired
	public EntityToSourceConverter(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	// FIXME iterate over meta data attributes instead of entity attributes
	@SuppressWarnings("unchecked")
	public Map<String, Object> convert(Entity entity, EntityMetaData entityMetaData)
	{
		Map<String, Object> doc = new HashMap<String, Object>();

		final Set<String> xrefAndMrefColumns = new HashSet<String>();
		for (AttributeMetaData attr : entityMetaData.getAtomicAttributes())
		{
			FieldTypeEnum fieldType = attr.getDataType().getEnumType();
			boolean isXrefOrMref = fieldType.equals(FieldTypeEnum.XREF) || fieldType.equals(FieldTypeEnum.MREF);
			if (isXrefOrMref) xrefAndMrefColumns.add(attr.getName());
		}

		for (String attrName : entity.getAttributeNames())
		{
			// Serialize collections to be able to sort on them,
			// elasticsearch does not support sorting on
			// list fields
			Object id = null;
			Object key = null;
			Object value = entity.get(attrName);

			if (value != null)
			{
				if (value instanceof Entity)
				{
					Entity refEntity = (Entity) value;
					EntityMetaData refEntityMetaData = entityMetaData.getAttribute(attrName).getRefEntity();
					key = refEntity.get(refEntityMetaData.getIdAttribute().getName());
					value = refEntity.get(refEntityMetaData.getLabelAttribute().getName());
				}
				if (value instanceof Cell)
				{
					Cell<?> cell = (Cell<?>) value;
					id = cell.getId();
					key = cell.getKey();
					value = cell.getValue();
				}
				if (value instanceof Collection)
				{
					Map<String, List<Object>> mrefMaps = new HashMap<String, List<Object>>();
					Collection<?> values = (Collection<?>) value;
					if (!values.isEmpty())
					{
						Object exampleValue = values.iterator().next();
						if (exampleValue instanceof Cell)
						{
							List<Integer> mrefIds = null;
							List<String> mrefKeys = null;
							for (Iterator<Cell<?>> it = ((Collection<Cell<?>>) values).iterator(); it.hasNext();)
							{
								Cell<?> cell = it.next();
								Integer cellId = cell.getId();
								if (cellId != null)
								{
									if (mrefIds == null) mrefIds = new ArrayList<Integer>();
									mrefIds.add(cellId);
								}
								String cellKey = cell.getKey();
								if (cellKey != null)
								{
									if (mrefKeys == null) mrefKeys = new ArrayList<String>();
									mrefKeys.add(cellKey);
								}
								Entity refEntityData = dataService.findOne(entityMetaData.getAttribute(attrName)
										.getRefEntity().getName(), cellId);
								createMapValue(refEntityData, entityMetaData, mrefMaps);
							}
							if (mrefIds != null) id = mrefIds;
							if (mrefKeys != null) key = mrefKeys;
						}
						else if (exampleValue instanceof Entity)
						{
							EntityMetaData refEntityMetaData = entityMetaData.getAttribute(attrName).getRefEntity();

							List<Object> mrefIds = null;
							List<String> mrefKeys = null;
							List<Object> labelValues = Lists.newArrayListWithCapacity(values.size());

							for (Iterator<Entity> it = ((Collection<Entity>) values).iterator(); it.hasNext();)
							{

								Entity cell = it.next();

								Object labelValue = cell.get(refEntityMetaData.getLabelAttribute().getName());

								if (labelValue != null)
								{
									labelValues.add(labelValue);
								}

								Object cellId = cell.getIdValue();
								if (cellId != null)
								{
									if (mrefIds == null) mrefIds = new ArrayList<Object>();
									mrefIds.add(cellId);
								}

								String cellKey = cell.getString(refEntityMetaData.getIdAttribute().getName());
								if (cellKey != null)
								{
									if (mrefKeys == null) mrefKeys = new ArrayList<String>();
									mrefKeys.add(cellKey);
								}

								createMapValue(cell, entityMetaData, mrefMaps);
							}
							if (mrefIds != null) id = mrefIds;
							if (mrefKeys != null) key = mrefKeys;
							value = labelValues;
						}
						else
						{
							throw new RuntimeException("Unsupported value type [" + exampleValue.getClass().getName()
									+ "]");
						}
					}

					value = Joiner.on(",").join((Collection<?>) value);

					doc.put(attrName, Arrays.asList(mrefMaps));
				}
				else
				{
					AttributeMetaData attr = entityMetaData.getAttribute(attrName);
					if (attr != null)
					{
						switch (attr.getDataType().getEnumType())
						{
							case DATE:
							{
								if (!(value instanceof String)) // FIXME remove temp workaround
								{
									Date date = entity.getDate(attrName);
									value = MolgenisDateFormat.getDateFormat().format(date);
								}
								break;
							}
							case DATE_TIME:
							{
								if (!(value instanceof String)) // FIXME remove temp workaround
								{
									Date date = entity.getDate(attrName);
									value = MolgenisDateFormat.getDateTimeFormat().format(date);
								}
								break;
							}
							default:
								break;
						}
					}
				}
				if (id != null) doc.put("id-" + attrName, id);
				if (key != null) doc.put("key-" + attrName, key);
			}
			// If the attribute is not MREF
			if (entityMetaData.getAttribute(attrName) == null
					|| !entityMetaData.getAttribute(attrName).getDataType().getEnumType().toString()
							.equalsIgnoreCase(MolgenisFieldTypes.MREF.toString()))
			{
				doc.put(attrName, value);
			}
		}

		Set<String> xrefAndMrefValues = new HashSet<String>();
		for (String attrName : entity.getAttributeNames())
		{
			if (xrefAndMrefColumns.contains(attrName))
			{
				Object value = entity.get(attrName);
				if (value instanceof Cell)
				{
					Cell<?> cell = (Cell<?>) value;
					if (cell.getValue() instanceof Collection<?>)
					{
						for (Cell<?> mrefCell : (Collection<Cell<?>>) cell.getValue())
						{
							xrefAndMrefValues.add(mrefCell.getKey());
						}
					}
					else
					{
						xrefAndMrefValues.add(cell.getKey());
					}
				}
			}
		}
		doc.put("_xrefvalue", xrefAndMrefValues);

		return doc;
	}

	/**
	 * Translate the entity into a map so that we can put this map in the list for Mref case, this list is indexed as a
	 * nested type in ElasticSearch
	 * 
	 * @param refEntityData
	 * @param entityMetaData
	 * @param mrefMaps
	 * @return
	 */
	private void createMapValue(Entity refEntityData, EntityMetaData entityMetaData, Map<String, List<Object>> mrefMaps)
	{
		for (String attributeName : refEntityData.getAttributeNames())
		{
			if (!mrefMaps.containsKey(attributeName))
			{
				mrefMaps.put(attributeName, new ArrayList<>());
			}
			mrefMaps.get(attributeName).add(refEntityData.get(attributeName));
		}
	}
}
