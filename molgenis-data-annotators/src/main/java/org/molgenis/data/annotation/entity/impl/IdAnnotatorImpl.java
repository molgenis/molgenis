package org.molgenis.data.annotation.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.QueryCreator;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Base class for any {@link EntityAnnotator} that uses a {@link QueryCreator} to query the {@link DataService} or
 * {@link Resources}. It leaves it up to concrete implementations how they wish to process the results by implementing
 * {@link #processQueryResults(Entity, Iterable, Entity)}.
 * 
 * See {@link AnnotatorImpl} for the most standard implementation of
 * {@link #processQueryResults(Entity, Iterable, Entity)}.
 */
public abstract class IdAnnotatorImpl implements EntityAnnotator
{
	private final QueryCreator queryCreator;
	private final Resources resources;
	private final DataService dataService;
	private final String sourceRepositoryName;
	private final AnnotatorInfo info;
	private final CmdLineAnnotatorSettingsConfigurer cmdLineAnnotatorSettingsConfigurer;
	private final AttributeMetaDataFactory attributeMetaDataFactory;
	private final EntityMetaDataFactory entityMetaDataFactory;

	public IdAnnotatorImpl(String sourceRepositoryName, AnnotatorInfo info, QueryCreator queryCreator,
			DataService dataService, Resources resources,
			CmdLineAnnotatorSettingsConfigurer cmdLineAnnotatorSettingsConfigurer, AttributeMetaDataFactory attributeMetaDataFactory, EntityMetaDataFactory entityMetaDataFactory)
	{
		this.sourceRepositoryName = sourceRepositoryName;
		this.dataService = dataService;
		this.resources = resources;
		this.queryCreator = requireNonNull(queryCreator);
		this.info = info;
		this.cmdLineAnnotatorSettingsConfigurer = cmdLineAnnotatorSettingsConfigurer;
		this.attributeMetaDataFactory = attributeMetaDataFactory;
		this.entityMetaDataFactory = entityMetaDataFactory;
	}

	@Override
	public AnnotatorInfo getInfo()
	{
		return info;
	}

	@Override
	public boolean sourceExists()
	{
		return resources.hasRepository(sourceRepositoryName) || dataService.hasRepository(sourceRepositoryName);
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
		if (updateMode == true)
		{
			throw new MolgenisDataException("This annotator/filter does not support updating of values");
		}
		Query q = queryCreator.createQuery(entity);
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
		EntityMetaData meta = entityMetaDataFactory.create(entity.getEntityMetaData());
		info.getOutputAttributes().forEach(meta::addAttribute);
		Entity resultEntity = new DynamicEntity(meta);
		resultEntity.set(entity);
		processQueryResults(entity, annotatationSourceEntities, resultEntity);
		return Collections.singletonList(resultEntity);
	}

	@Override
	public CmdLineAnnotatorSettingsConfigurer getCmdLineAnnotatorSettingsConfigurer()
	{
		return cmdLineAnnotatorSettingsConfigurer;
	}

	/**
	 * Processes the query results.
	 * 
	 * @param inputEntity
	 *            the input entity that is being annotated
	 * @param annotationSourceEntities
	 *            the entities resulting from the query on the annotation source
	 * @param resultEntity
	 *            the result entity to write the annotation attributes to
	 */
	protected abstract void processQueryResults(Entity inputEntity, Iterable<Entity> annotationSourceEntities,
			Entity resultEntity);

}
