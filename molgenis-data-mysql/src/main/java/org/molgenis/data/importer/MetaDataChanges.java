package org.molgenis.data.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.framework.db.EntityImportReport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * MetaDataChanges keeps track of which entities and attributes have been added by the {@link ImportWriter}. Used to
 * manually rollback DDL changes.
 * 
 * TODO this has fairly high overlap with {@link EntityImportReport}
 */
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
