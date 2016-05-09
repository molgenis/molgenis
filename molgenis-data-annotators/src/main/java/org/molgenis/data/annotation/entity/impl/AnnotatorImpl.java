package org.molgenis.data.annotation.entity.impl;

import com.google.common.base.Optional;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.QueryCreator;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.annotation.resources.Resources;

import java.util.List;

/**
 * The most standard implementation of an {@link EntityAnnotator} that first creates a query, queries the resource, then
 * filters the results using a {@link ResultFilter} and then copies the resulting attributes to the annotated
 * {@link Entity}.
 */
public class AnnotatorImpl extends QueryAnnotatorImpl implements EntityAnnotator
{
	private final ResultFilter resultFilter;

	public AnnotatorImpl(String sourceRepositoryName, AnnotatorInfo info, QueryCreator queryCreator,
			ResultFilter resultFilter, DataService dataService, Resources resources,
			CmdLineAnnotatorSettingsConfigurer cmdLineAnnotatorSettingsConfigurer)
	{
		super(sourceRepositoryName, info, queryCreator, dataService, resources, cmdLineAnnotatorSettingsConfigurer);
		this.resultFilter = resultFilter;
	}

	@Override
	protected void processQueryResults(Entity entity, Iterable<Entity> annotationSourceEntities, Entity resultEntity,
			boolean updateMode)
	{
		Optional<Entity> filteredResult = resultFilter.filterResults(annotationSourceEntities, entity, updateMode);
		if (filteredResult.isPresent())
		{
			for (AttributeMetaData attr : getInfo().getOutputAttributes())
			{
				resultEntity.set(attr.getName(), getResourceAttributeValue(attr, filteredResult.get()));
			}
		}
		else
		{
			for (AttributeMetaData attr : getInfo().getOutputAttributes())
			{
				resultEntity.set(attr.getName(), null);
			}
		}
	}

	/**
	 * Get the resource attribute value for one of this annotator's output attributes.
	 * 
	 * @param attr the name of the output attribute
	 * @param entity the current entity
	 * @return the value of the attribute to copy from the resource entity
	 */
	protected Object getResourceAttributeValue(AttributeMetaData attr, Entity entity)
	{
		return entity.get(attr.getName());
	}

	@Override
	public List<AttributeMetaData> getRequiredAttributes()
	{
		List<AttributeMetaData> sourceMetaData = super.getRequiredAttributes();
		sourceMetaData.addAll(resultFilter.getRequiredAttributes());
		return sourceMetaData;
	}

}
