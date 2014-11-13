package org.molgenis.data.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class MetaDataChanges
{
	private List<String> addedEntities = new ArrayList<String>();
	private Map<String, Collection<AttributeMetaData>> addedAttributes = new LinkedHashMap<String, Collection<AttributeMetaData>>();

	public void addEntity(String entity)
	{
		addedEntities.add(entity);
	}

	public void addAttributes(String entityName, List<AttributeMetaData> attributes)
	{
		addedAttributes.put(entityName, ImmutableList.<AttributeMetaData> copyOf(attributes));
	}

	public ImmutableList<String> getAddedEntities()
	{
		return ImmutableList.<String> copyOf(addedEntities);
	}

	public ImmutableMap<String, Collection<AttributeMetaData>> getAddedAttributes()
	{
		return ImmutableMap.<String, Collection<AttributeMetaData>> copyOf(addedAttributes);
	}

}
