package org.molgenis.data.annotation.entity.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.QueryCreator;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.common.base.Preconditions;

/**
 * Base class for any {@link EntityAnnotator} that uses a {@link QueryCreator} to query the {@link DataService} or
 * {@link Resources}. It leaves it up to concrete implementations how they wish to process the results by implementing
 * {@link #processQueryResults(Entity, Iterable, Entity)}.
 * 
 * See {@link AnnotatorImpl} for the most standard implementation of
 * {@link #processQueryResults(Entity, Iterable, Entity)}.
 */
public abstract class QueryAnnotatorImpl implements EntityAnnotator
{
	private final QueryCreator queryCreator;
	private final Resources resources;
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
		this.queryCreator = Preconditions.checkNotNull(queryCreator);
		this.info = info;
		this.cmdLineAnnotatorSettingsConfigurer = cmdLineAnnotatorSettingsConfigurer;
	}

	@Override
	public AnnotatorInfo getInfo()
	{
		return info;
	}

	@Override
	public AttributeMetaData getAnnotationAttributeMetaData()
	{
		DefaultAttributeMetaData result = new DefaultAttributeMetaData(ANNOTATORPREFIX + info.getCode(),
				FieldTypeEnum.COMPOUND).setLabel(info.getCode());
		getInfo().getOutputAttributes().forEach(result::addAttributePart);

		return result;
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
	public List<Entity> annotateEntity(Entity entity)
	{
		Query q = queryCreator.createQuery(entity);
		Iterable<Entity> annotatationSourceEntities;
		if (resources.hasRepository(sourceRepositoryName))
		{
			annotatationSourceEntities = resources.findAll(sourceRepositoryName, q);
		}
		else
		{
			annotatationSourceEntities = dataService.findAll(sourceRepositoryName, q);
		}
		DefaultEntityMetaData meta = new DefaultEntityMetaData(entity.getEntityMetaData());
		info.getOutputAttributes().forEach(meta::addAttributeMetaData);
		Entity resultEntity = new MapEntity(entity, meta);
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
