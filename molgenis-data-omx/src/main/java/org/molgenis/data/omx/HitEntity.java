package org.molgenis.data.omx;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.omx.dataset.DataSetMatrixRepository;
import org.molgenis.search.Hit;
import org.molgenis.util.MolgenisDateFormat;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class HitEntity extends AbstractEntity
{
	private static final long serialVersionUID = 1L;

	private static final String HIT_KEY_PREFIX = "key-";

	private final Map<String, Object> columnValueMap;
	private final Set<String> attributeNames;
	private final EntityMetaData entityMetaData;
	private final DataService dataService;

	private Map<String, Object> caseInsensitiveColumnValueMap;
	private Set<String> caseInsensitiveAttributeNames;

	public HitEntity(Hit hit, Set<String> attributeNames, EntityMetaData entityMetaData, DataService dataService)
	{
		if (hit == null) throw new IllegalArgumentException("Hit is null");
		if (attributeNames == null) throw new IllegalArgumentException("attributeNames is null");
		if (entityMetaData == null) throw new IllegalArgumentException("entityMetaData is null");
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.columnValueMap = hit.getColumnValueMap();
		this.attributeNames = attributeNames;
		this.entityMetaData = entityMetaData;
		this.dataService = dataService;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return attributeNames;
	}

	@Override
	public Object get(final String attributeName)
	{
		if (caseInsensitiveAttributeNames == null) initCaseInsensitiveAttributeNames();
		if (caseInsensitiveColumnValueMap == null) initCaseInsensitiveColumnValueMap();

		String caseInsensitiveAttributeName = attributeName.toLowerCase();
		if (caseInsensitiveAttributeNames.contains(caseInsensitiveAttributeName))
		{
			AttributeMetaData attribute = entityMetaData.getAttribute(caseInsensitiveAttributeName);
			FieldTypeEnum dataType = attribute.getDataType().getEnumType();
			switch (dataType)
			{
				case BOOL:
				{
					Object attributeValue = caseInsensitiveColumnValueMap.get(caseInsensitiveAttributeName);
					return attributeValue;
				}
				case CATEGORICAL:
				{
					Object attributeValue = caseInsensitiveColumnValueMap.get(HIT_KEY_PREFIX
							+ caseInsensitiveAttributeName);
					if (attributeValue != null)
					{
						String xrefName = attribute.getRefEntity().getName();
						String xrefIdentifier = (String) attributeValue;
						return new HitRefEntity(columnValueMap, xrefName, xrefIdentifier, attributeName, null,
								dataService);
					}
					else return null;
				}
				case DATE:
				{
					Object attributeValue = caseInsensitiveColumnValueMap.get(caseInsensitiveAttributeName);
					if (attributeValue != null)
					{
						try
						{
							return MolgenisDateFormat.getDateFormat().parse(attributeValue.toString());
						}
						catch (ParseException eDate)
						{
							throw new RuntimeException(eDate);
						}
					}
					else return null;
				}
				case DATE_TIME:
				{
					Object attributeValue = caseInsensitiveColumnValueMap.get(caseInsensitiveAttributeName);
					if (attributeValue != null)
					{
						try
						{
							return MolgenisDateFormat.getDateTimeFormat().parse(attributeValue.toString());
						}
						catch (ParseException eDateTime)
						{
							throw new RuntimeException(eDateTime);
						}
					}
					else return null;
				}
				case DECIMAL:
				{
					Object attributeValue = caseInsensitiveColumnValueMap.get(caseInsensitiveAttributeName);
					return attributeValue != null ? Double.valueOf(attributeValue.toString()) : null;
				}
				case EMAIL:
				{
					Object attributeValue = caseInsensitiveColumnValueMap.get(caseInsensitiveAttributeName);
					return attributeValue;
				}
				case COMPOUND:
				{
					AttributeMetaData compoundAttribute = entityMetaData.getAttribute(caseInsensitiveAttributeName);
					return compoundAttribute.getAttributeParts();
				}
				case HYPERLINK:
				{
					Object attributeValue = caseInsensitiveColumnValueMap.get(caseInsensitiveAttributeName);
					return attributeValue;
				}
				case INT:
				{
					Object attributeValue = caseInsensitiveColumnValueMap.get(caseInsensitiveAttributeName);
					return attributeValue != null ? Integer.valueOf(attributeValue.toString()) : null;
				}
				case LONG:
				{
					Object attributeValue = caseInsensitiveColumnValueMap.get(caseInsensitiveAttributeName);
					return attributeValue != null ? Long.valueOf(attributeValue.toString()) : null;
				}
				case MREF:
				{
					final Object attributeValue = caseInsensitiveColumnValueMap.get(HIT_KEY_PREFIX
							+ caseInsensitiveAttributeName);
					@SuppressWarnings("unchecked")
					List<String> mrefIdentifiers = (List<String>) attributeValue;
					if (mrefIdentifiers != null && !mrefIdentifiers.isEmpty())
					{
						final AtomicInteger count = new AtomicInteger();
						final String xrefName = attribute.getRefEntity().getName();
						// TODO use iterable, currently gives problems with RestController
						return Lists.transform(mrefIdentifiers, new Function<String, Entity>()
						{
							@Override
							public Entity apply(String mrefIdentifier)
							{
								return new HitRefEntity(columnValueMap, xrefName, mrefIdentifier, attributeName, count
										.getAndIncrement(), dataService);
							}
						});
					}
					else return Collections.emptyList();
				}
				case STRING:
				{
					Object attributeValue = caseInsensitiveColumnValueMap.get(caseInsensitiveAttributeName);
					return attributeValue;
				}
				case TEXT:
				{
					Object attributeValue = caseInsensitiveColumnValueMap.get(caseInsensitiveAttributeName);
					return attributeValue;
				}
				case XREF:
				{
					Object attributeValue = caseInsensitiveColumnValueMap.get(HIT_KEY_PREFIX
							+ caseInsensitiveAttributeName);
					if (attributeValue != null)
					{
						String xrefName = attribute.getRefEntity().getName();
						String xrefIdentifier = (String) attributeValue;
						return new HitRefEntity(columnValueMap, xrefName, xrefIdentifier, attributeName, null,
								dataService);
					}
					else return null;
				}
				default:
					throw new IllegalArgumentException("unsupported field type [" + dataType + "]");
			}
		}
		else return null;
	}

	private void initCaseInsensitiveAttributeNames()
	{
		caseInsensitiveAttributeNames = Sets.newHashSetWithExpectedSize(attributeNames.size());
		for (String attributeName : attributeNames)
			caseInsensitiveAttributeNames.add(attributeName.toLowerCase());
	}

	private void initCaseInsensitiveColumnValueMap()
	{
		caseInsensitiveColumnValueMap = Maps.newHashMapWithExpectedSize(attributeNames.size());
		for (Map.Entry<String, Object> entry : columnValueMap.entrySet())
			caseInsensitiveColumnValueMap.put(entry.getKey().toLowerCase(), entry.getValue());
	}

	@Override
	public void set(String attributeName, Object value)
	{
		if (caseInsensitiveAttributeNames == null) initCaseInsensitiveAttributeNames();
		if (caseInsensitiveColumnValueMap == null) initCaseInsensitiveColumnValueMap();

		caseInsensitiveColumnValueMap.put(attributeName.toLowerCase(), value);
	}

	@Override
	public void set(Entity values)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getIdValue()
	{
		return Integer.valueOf(columnValueMap.get(DataSetMatrixRepository.ENTITY_ID_COLUMN_NAME).toString());
	}

	@Override
	public String getLabelValue()
	{
		AttributeMetaData attribute = entityMetaData.getLabelAttribute();
		return attribute != null ? getString(attribute.getName()) : null;
	}

	@Override
	public List<String> getLabelAttributeNames()
	{
		return Collections.singletonList(entityMetaData.getLabelAttribute().getName());
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public void set(Entity entity, boolean strict)
	{
		throw new UnsupportedOperationException();
	}
}
