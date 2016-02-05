package org.molgenis.data.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
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
	private final List<String> addedEntities = new ArrayList<>();
	private final Map<String, Collection<AttributeMetaData>> addedAttributes = new LinkedHashMap<>();
	private final List<Entity> addedLanguages = new ArrayList<>();

	public void addEntity(String entity)
	{
		addedEntities.add(entity);
	}

	public void addAttributes(String entityName, List<AttributeMetaData> attributes)
	{
		addedAttributes.put(entityName, ImmutableList.<AttributeMetaData> copyOf(attributes));
	}

	public void addLanguage(Entity language)
	{
		addedLanguages.add(language);
	}

	public ImmutableList<String> getAddedEntities()
	{
		return ImmutableList.copyOf(addedEntities);
	}

	public ImmutableMap<String, Collection<AttributeMetaData>> getAddedAttributes()
	{
		return ImmutableMap.copyOf(addedAttributes);
	}

	public ImmutableList<Entity> getAddedLanguages()
	{
		return ImmutableList.copyOf(addedLanguages);
	}

}
