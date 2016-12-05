package org.molgenis.data.importer.emx;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.Entity;
import org.molgenis.data.importer.EntityImportReport;

import java.util.ArrayList;
import java.util.List;

/**
 * MetaDataChanges keeps track of which entities and attributes have been added by the {@link ImportWriter}.
 * <p>
 * TODO this has fairly high overlap with {@link EntityImportReport}
 */
public class MetaDataChanges
{
	private final List<String> addedEntities = new ArrayList<>();
	private final List<Entity> addedLanguages = new ArrayList<>();

	public void addEntity(String entity)
	{
		addedEntities.add(entity);
	}

	public void addLanguage(Entity language)
	{
		addedLanguages.add(language);
	}

	public ImmutableList<String> getAddedEntities()
	{
		return ImmutableList.copyOf(addedEntities);
	}
}
