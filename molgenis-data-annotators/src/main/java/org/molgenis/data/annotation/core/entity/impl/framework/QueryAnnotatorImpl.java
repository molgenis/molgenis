package org.molgenis.data.annotation.core.entity.impl.framework;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.QueryCreator;
import org.molgenis.data.annotation.core.resources.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.meta.model.Attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Base class for any {@link EntityAnnotator} that uses a {@link QueryCreator} to query the {@link DataService} or
 * {@link Resources}. It leaves it up to concrete implementations how they wish to process the results by implementing
 * {@link #processQueryResults(Entity, Iterable, boolean)}.
 * <p>
 * See {@link AbstractAnnotator} for the most standard implementation of
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
	public List<Attribute> getAnnotatorAttributes()
	{
		return getInfo().getOutputAttributes();
	}

	@Override
	public boolean sourceExists()
	{
		return resources.hasRepository(sourceRepositoryName) || dataService.hasRepository(sourceRepositoryName);
	}

	@Override
	public List<Attribute> getRequiredAttributes()
	{
		List<Attribute> sourceMetaData = new ArrayList<>();
		sourceMetaData.addAll(queryCreator.getRequiredAttributes());
		return sourceMetaData;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity, boolean updateMode)
	{
		Query<Entity> q = queryCreator.createQuery(entity);
		Iterable<Entity> annotatationSourceEntities;
		if (resources.hasRepository(sourceRepositoryName))
		{
			annotatationSourceEntities = resources.findAll(sourceRepositoryName, q);
		}
		else
		{
			annotatationSourceEntities = () -> dataService.findAll(sourceRepositoryName, q).iterator();
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
