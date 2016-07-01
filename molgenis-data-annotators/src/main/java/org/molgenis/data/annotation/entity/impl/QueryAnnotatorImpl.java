package org.molgenis.data.annotation.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.QueryCreator;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Base class for any {@link EntityAnnotator} that uses a {@link QueryCreator} to query the {@link DataService} or
 * {@link Resources}. It leaves it up to concrete implementations how they wish to process the results by implementing
 * {@link #processQueryResults(Entity, Iterable, boolean)}.
 * <p>
 * See {@link AnnotatorImpl} for the most standard implementation of
 * {@link #processQueryResults(Entity, Iterable, boolean)}.
 */
public abstract class QueryAnnotatorImpl implements EntityAnnotator
{
	private final QueryCreator queryCreator;
	private Resources resources;
	private final DataService dataService;
	private final String sourceRepositoryName;
	private final AnnotatorInfo info;
	private final CmdLineAnnotatorSettingsConfigurer cmdLineAnnotatorSettingsConfigurer;

	public QueryAnnotatorImpl(String sourceRepositoryName, AnnotatorInfo info, QueryCreator queryCreator,
			DataService dataService, Resources resources,
			CmdLineAnnotatorSettingsConfigurer cmdLineAnnotatorSettingsConfigurer)
	{
		this.sourceRepositoryName = sourceRepositoryName;
		this.dataService = dataService;
		this.resources = resources;
		this.queryCreator = requireNonNull(queryCreator);
		this.info = info;
		this.cmdLineAnnotatorSettingsConfigurer = cmdLineAnnotatorSettingsConfigurer;
	}

	@Override
	public AnnotatorInfo getInfo()
	{
		return info;
	}

	@Override
	public List<AttributeMetaData> getAnnotationAttributeMetaDatas()
	{
		return getInfo().getOutputAttributes();
	}

	@Override
	public boolean sourceExists()
	{
		getResources();
		return resources.hasRepository(sourceRepositoryName) || dataService.hasRepository(sourceRepositoryName);
	}

	private void getResources()
	{
		if (resources == null)
		{
			ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
			resources = applicationContext.getBean(Resources.class);
		}
	}

	@Override
	public List<AttributeMetaData> getRequiredAttributes()
	{
		List<AttributeMetaData> sourceMetaData = new ArrayList<>();
		sourceMetaData.addAll(queryCreator.getRequiredAttributes());
		return sourceMetaData;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity, boolean updateMode)
	{
		getResources();
		Query<Entity> q = queryCreator.createQuery(entity);
		Iterable<Entity> annotatationSourceEntities;
		if (resources.hasRepository(sourceRepositoryName))
		{
			annotatationSourceEntities = resources.findAll(sourceRepositoryName, q);
		}
		else
		{
			annotatationSourceEntities = new Iterable<Entity>()
			{
				@Override
				public Iterator<Entity> iterator()
				{
					return dataService.findAll(sourceRepositoryName, q).iterator();
				}
			};
		}
		processQueryResults(entity, annotatationSourceEntities, updateMode);
		return Collections.singletonList(entity);
	}

	@Override
	public CmdLineAnnotatorSettingsConfigurer getCmdLineAnnotatorSettingsConfigurer()
	{
		return cmdLineAnnotatorSettingsConfigurer;
	}

	/**
	 * Processes the query results.
	 *
	 * @param inputEntity              the input entity that is being annotated
	 * @param annotationSourceEntities the entities resulting from the query on the annotation source
	 */
	protected abstract void processQueryResults(Entity inputEntity, Iterable<Entity> annotationSourceEntities,
			boolean updateMode);

}
