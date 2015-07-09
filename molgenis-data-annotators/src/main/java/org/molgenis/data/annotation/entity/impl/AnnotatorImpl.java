package org.molgenis.data.annotation.entity.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.QueryCreator;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.common.base.Optional;

public class AnnotatorImpl implements EntityAnnotator
{
	private final Resources resources;
	private final DataService dataService;
	private final String sourceRepositoryName;
	private final AnnotatorInfo info;
	private final QueryCreator queryCreator;
	private final ResultFilter resultFilter;

	public AnnotatorImpl(String sourceRepositoryName, AnnotatorInfo info, QueryCreator queryCreator,
			ResultFilter resultFilter, DataService dataService, Resources resources)
	{
		this.sourceRepositoryName = sourceRepositoryName;
		this.info = info;
		this.queryCreator = queryCreator;
		this.resultFilter = resultFilter;
		this.dataService = dataService;
		this.resources = resources;
	}

	@Override
	public AnnotatorInfo getInfo()
	{
		return info;
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

		Entity resultEntity = new MapEntity(entity, entity.getEntityMetaData());

		Optional<Entity> filteredResult = resultFilter.filterResults(annotatationSourceEntities, entity);
		if (filteredResult.isPresent())
		{
			for (AttributeMetaData attr : info.getOutputAttributes())
			{
				resultEntity.set(attr.getName(), getResourceAttributeValue(attr, filteredResult.get()));
			}
		}

		return Collections.singletonList(resultEntity);
	}

	/**
	 * Get the resource attribute value for one of this annotator's output attributes.
	 * 
	 * @param attr
	 *            the name of the output attribute
	 * @param the
	 *            current entity
	 * @return the value of the attribute to copy from the resource entity
	 */
	protected Object getResourceAttributeValue(AttributeMetaData attr, Entity entity)
	{
		return entity.get(attr.getName());
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
		sourceMetaData.addAll(resultFilter.getRequiredAttributes());
		return sourceMetaData;
	}

}
