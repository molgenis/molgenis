package org.molgenis.data.omx;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.AbstractEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.Characteristic;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Lazy entity for xref/mref/categorical attributes in a Hit to ensure high performance operations. Avoids using the
 * data service in favor of data stores in the Hit.
 */
public class HitRefEntity extends AbstractEntity
{
	private static final long serialVersionUID = 1L;

	private static final String HIT_ID_PREFIX = "id-";

	private final Map<String, Object> columnValueMap;
	private final String entityName;
	private final String entityIdentifier;
	private final String caseInsensitiveAttributeName;
	private final Integer columnValueIndex;
	private final DataService dataService;

	private transient EntityMetaData cachedEntityMetaData;
	private transient String cachedIdAttributeName;
	private transient String cachedLabelAttributeName;
	private transient Entity cachedEntity;

	public HitRefEntity(Map<String, Object> columnValueMap, String entityName, String entityIdentifier,
			String caseInsensitiveAttributeName, Integer columnValueIndex, DataService dataService)
	{
		if (columnValueMap == null) throw new IllegalArgumentException("columnValueMap is null");
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.columnValueMap = columnValueMap;
		this.entityName = entityName;
		this.entityIdentifier = entityIdentifier;
		this.caseInsensitiveAttributeName = caseInsensitiveAttributeName;
		this.columnValueIndex = columnValueIndex;
		this.dataService = dataService;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (cachedEntityMetaData == null) cachedEntityMetaData = dataService.getEntityMetaData(entityName);
		return cachedEntityMetaData;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return Iterables.transform(getEntityMetaData().getAttributes(), new Function<AttributeMetaData, String>()
		{
			@Override
			public String apply(AttributeMetaData attributeMetaData)
			{
				return attributeMetaData.getName();
			}
		});
	}

	@Override
	public Integer getIdValue()
	{
		return getInt(getEntityMetaData().getIdAttribute().getName());
	}

	@Override
	public List<String> getLabelAttributeNames()
	{

		return Collections.singletonList(getLabelAttributeName());
	}

	@Override
	public Object get(String attributeName)
	{
		if (attributeName.equals(getIdAttributeName()))
		{
			Object val = columnValueMap.get(HIT_ID_PREFIX + caseInsensitiveAttributeName);
			if (columnValueIndex == null || val == null) return val;
			else return ((List<?>) val).get(columnValueIndex);
		}
		else if (attributeName.equals(getLabelAttributeName()))
		{
			Object val = columnValueMap.get(caseInsensitiveAttributeName);
			if (columnValueIndex == null || val == null) return val;
			else return val.toString().split(",")[columnValueIndex];
		}
		else
		{
			return getEntityFromDataService().get(attributeName);
		}
	}

	@Override
	public void set(String attributeName, Object value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(Entity entity, boolean strict)
	{
		throw new UnsupportedOperationException();
	}

	private Entity getEntityFromDataService()
	{
		if (cachedEntity == null)
		{
			Query xrefQ = new QueryImpl().eq(Characteristic.IDENTIFIER, entityIdentifier);
			cachedEntity = dataService.findOne(entityName, xrefQ);
		}
		return cachedEntity;
	}

	private String getIdAttributeName()
	{
		if (cachedIdAttributeName == null)
		{
			cachedIdAttributeName = getEntityMetaData().getIdAttribute().getName();
		}
		return cachedIdAttributeName;
	}

	private String getLabelAttributeName()
	{
		if (cachedLabelAttributeName == null)
		{
			cachedLabelAttributeName = getEntityMetaData().getLabelAttribute().getName();
		}
		return cachedLabelAttributeName;
	}
}
