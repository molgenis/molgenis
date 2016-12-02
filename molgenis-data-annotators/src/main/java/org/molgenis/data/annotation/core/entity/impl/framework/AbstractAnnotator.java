package org.molgenis.data.annotation.core.entity.impl.framework;

import com.google.common.base.Optional;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.QueryCreator;
import org.molgenis.data.annotation.core.entity.ResultFilter;
import org.molgenis.data.annotation.core.resources.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.meta.model.Attribute;

import java.util.List;

/**
 * The most standard implementation of an {@link EntityAnnotator} that first creates a query, queries the resource, then
 * filters the results using a {@link ResultFilter} and then copies the resulting attributes to the annotated
 * {@link Entity}.
 */
public abstract class AbstractAnnotator extends QueryAnnotatorImpl implements EntityAnnotator
{
	private final ResultFilter resultFilter;

	public AbstractAnnotator(String sourceRepositoryName, AnnotatorInfo info, QueryCreator queryCreator,
			ResultFilter resultFilter, DataService dataService, Resources resources,
			CmdLineAnnotatorSettingsConfigurer cmdLineAnnotatorSettingsConfigurer)
	{
		super(sourceRepositoryName, info, queryCreator, dataService, resources, cmdLineAnnotatorSettingsConfigurer);
		this.resultFilter = resultFilter;
	}

	@Override
	protected void processQueryResults(Entity entity, Iterable<Entity> annotationSourceEntities, boolean updateMode)
	{
		Optional<Entity> filteredResult = resultFilter.filterResults(annotationSourceEntities, entity, updateMode);
		if (filteredResult.isPresent())
		{
			for (Attribute attr : getInfo().getOutputAttributes())
			{
				entity.set(attr.getName(), getResourceAttributeValue(attr, filteredResult.get()));
			}
		}
		else
		{
			for (Attribute attr : getInfo().getOutputAttributes())
			{
				if (!updateMode || entity.get(attr.getName()) == null)
				{
					entity.set(attr.getName(), null);
				}
			}
		}
	}

	/**
	 * Get the resource attribute value for one of this annotator's output attributes.
	 *
	 * @param attr   the name of the output attribute
	 * @param entity the current entity
	 * @return the value of the attribute to copy from the resource entity
	 */
	protected Object getResourceAttributeValue(Attribute attr, Entity entity)
	{
		return entity.get(attr.getName());
	}

	@Override
	public List<Attribute> getRequiredAttributes()
	{
		List<Attribute> sourceMetaData = super.getRequiredAttributes();
		sourceMetaData.addAll(resultFilter.getRequiredAttributes());
		return sourceMetaData;
	}
}
