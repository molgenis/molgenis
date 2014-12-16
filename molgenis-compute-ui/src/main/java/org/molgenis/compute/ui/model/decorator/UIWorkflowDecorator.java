package org.molgenis.compute.ui.model.decorator;

import static org.molgenis.MolgenisFieldTypes.MREF;

import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.compute.ui.meta.UIWorkflowMetaData;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

/**
 * Compute UIWorkflow decorator that adds an analysis column to each entity refered to by a UIWorkflow
 */
public class UIWorkflowDecorator extends CrudRepositoryDecorator
{
	public static final AttributeMetaData ANALYSIS_ATTRIBUTE;

	static
	{
		ANALYSIS_ATTRIBUTE = new DefaultAttributeMetaData("analysis").setLabel("Analysis").setDataType(MREF)
				.setRefEntity(AnalysisMetaData.INSTANCE);
	}

	private final CrudRepository decoratedRepository;
	private final ManageableCrudRepositoryCollection repositoryCollection;

	// TODO use WritableMetaDataService instead of ManageableCrudRepositoryCollection
	public UIWorkflowDecorator(CrudRepository decoratedRepository,
			ManageableCrudRepositoryCollection repositoryCollection)
	{
		super(decoratedRepository);
		this.decoratedRepository = decoratedRepository;
		this.repositoryCollection = repositoryCollection;
	}

	@Override
	public void add(Entity entity)
	{
		decoratedRepository.add(entity);
		addTargetTypeAnalysis(entity);
	}

	@Override
	public void update(Entity entity)
	{
		decoratedRepository.update(entity);
		addTargetTypeAnalysis(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		Integer count = decoratedRepository.add(entities);
		addTargetTypeAnalysis(entities);
		return count;
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		decoratedRepository.update(records);
		addTargetTypeAnalysis(records);
	}

	private void addTargetTypeAnalysis(Entity entity)
	{
		String targetType = entity.getString(UIWorkflowMetaData.TARGET_TYPE);
		if (targetType != null && !targetType.isEmpty())
		{
			EntityMetaData entityMetaData = repositoryCollection.getRepositoryByEntityName(targetType)
					.getEntityMetaData();
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
				repositoryCollection.update(updatedEntityMetaData);
			}
		}
	}

	private void addTargetTypeAnalysis(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
			addTargetTypeAnalysis(entity);
	}
}
