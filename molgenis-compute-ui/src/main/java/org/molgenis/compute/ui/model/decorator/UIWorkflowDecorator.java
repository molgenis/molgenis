package org.molgenis.compute.ui.model.decorator;

import static org.molgenis.MolgenisFieldTypes.MREF;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.molgenis.compute.ui.analysis.event.AnalysisHandlerRegistratorService;
import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.compute.ui.model.UIWorkflow;
import org.molgenis.compute.ui.workflow.event.WorkflowHandlerRegistratorService;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

/**
 * Compute UIWorkflow decorator that adds an analysis column to each entity referred to by a UIWorkflow and registers
 * the workflow
 */
public class UIWorkflowDecorator implements Repository
{
	public static final AttributeMetaData ANALYSIS_ATTRIBUTE;

	static
	{
		ANALYSIS_ATTRIBUTE = new DefaultAttributeMetaData("analysis").setLabel("Analysis").setDataType(MREF)
				.setRefEntity(AnalysisMetaData.INSTANCE);
	}

	private final Repository decoratedRepository;
	private final DataService dataService;
	private final WorkflowHandlerRegistratorService workflowHandlerRegistratorService;
	private final AnalysisHandlerRegistratorService analysisHandlerRegistratorService;

	// TODO use WritableMetaDataService instead of ManageableCrudRepositoryCollection
	public UIWorkflowDecorator(Repository decoratedRepository, DataService dataService,
			WorkflowHandlerRegistratorService workflowHandlerRegistratorService,
			AnalysisHandlerRegistratorService analysisHandlerRegistratorService)
	{

		this.decoratedRepository = decoratedRepository;
		this.dataService = dataService;
		this.workflowHandlerRegistratorService = workflowHandlerRegistratorService;
		this.analysisHandlerRegistratorService = analysisHandlerRegistratorService;
	}

	@Override
	public void add(Entity entity)
	{
		decoratedRepository.add(entity);
		addTargetTypeAnalysis(entity);
		registerWorkflowHandler(entity);
	}

	@Override
	public void update(Entity entity)
	{
		decoratedRepository.update(entity);
		addTargetTypeAnalysis(entity);
		registerWorkflowHandler(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		Integer count = decoratedRepository.add(entities);
		addTargetTypeAnalysis(entities);
		registerWorkflowHandler(entities);
		return count;
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		decoratedRepository.update(records);
		addTargetTypeAnalysis(records);
		registerWorkflowHandler(records);
	}

	// FIXME decorate delete methods and map to unregisterWorkflowHandler

	private void registerWorkflowHandler(Entity entity)
	{
		UIWorkflow uiWorkflow = new UIWorkflow();
		uiWorkflow.set(entity);

		// publish data explorer action event
		workflowHandlerRegistratorService.registerWorkflowHandler(uiWorkflow);
	}

	private void registerWorkflowHandler(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
			registerWorkflowHandler(entity);
	}

	private void addTargetTypeAnalysis(Entity entity)
	{
		String targetType = entity.getString(UIWorkflowMetaData.TARGET_TYPE);
		if (targetType != null && !targetType.isEmpty())
		{
			Repository repository = dataService.getRepository(targetType);
			EntityMetaData entityMetaData = repository.getEntityMetaData();
			if (entityMetaData == null) throw new UnknownEntityException("Entity [" + targetType + "] does not exist");

			if (!(entityMetaData instanceof EditableEntityMetaData))
			{
				throw new RuntimeException("Entity [" + targetType + "] meta data not editable");
			}

			AttributeMetaData analysisAttribute = entityMetaData.getAttribute(ANALYSIS_ATTRIBUTE.getName());
			if (analysisAttribute == null)
			{
				EditableEntityMetaData updatedEntityMetaData = new DefaultEntityMetaData(entityMetaData);
				updatedEntityMetaData.addAttributeMetaData(ANALYSIS_ATTRIBUTE);
				dataService.getMeta().updateEntityMeta(updatedEntityMetaData);

				analysisHandlerRegistratorService.registerAnalysisHandler(targetType, ANALYSIS_ATTRIBUTE.getName());
			}
		}
	}

	private void addTargetTypeAnalysis(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
			addTargetTypeAnalysis(entity);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return this.decoratedRepository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		this.decoratedRepository.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return this.decoratedRepository.getCapabilities();
	}

	@Override
	public String getName()
	{
		return this.decoratedRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return this.decoratedRepository.getEntityMetaData();
	}

	@Override
	public long count()
	{
		return this.decoratedRepository.count();
	}

	@Override
	public Query query()
	{
		return this.decoratedRepository.query();
	}

	@Override
	public long count(Query q)
	{
		return this.decoratedRepository.count(q);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		return this.decoratedRepository.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		return this.decoratedRepository.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return this.decoratedRepository.findOne(id);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return this.decoratedRepository.findAll(ids);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return this.decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public void delete(Entity entity)
	{
		this.decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		this.decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		this.decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		this.decoratedRepository.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		this.decoratedRepository.deleteAll();
	}

	@Override
	public void flush()
	{
		this.decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		this.decoratedRepository.clearCache();
	}
}
