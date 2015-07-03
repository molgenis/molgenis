package org.molgenis.data.annotation.entity.impl;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.collect.Lists;
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
	private Resources resources;
	private DataService dataService;
	private String sourceRepositoryName;
	private AnnotatorInfo info;
	private QueryCreator queryCreator;
	private ResultFilter resultFilter;

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
		List<Entity> results = new ArrayList<>();
		Iterable<Entity> annotatationSourceEntities;

		Query q = queryCreator.createQuery(entity);
		if (resources.hasRepository(sourceRepositoryName))
		{
			annotatationSourceEntities = resources.findAll(sourceRepositoryName, q);
		}
		else
		{
			annotatationSourceEntities = dataService.findAll(sourceRepositoryName, q);
		}
		Optional<Entity> filteredResults = resultFilter.filterResults(annotatationSourceEntities, entity);
		annotatationSourceEntities = Lists.newArrayList(filteredResults.asSet());
		for (Entity anntotationSourceEntity : annotatationSourceEntities)
		{
			Entity resultEntity = new MapEntity(entity, entity.getEntityMetaData());
			for (AttributeMetaData attr : info.getOutputAttributes())
			{
				resultEntity.set(attr.getName(), anntotationSourceEntity.get(getResourceAttributeName(attr)));
			}
			results.add(resultEntity);
		}
		// no data added? add original entity
		if (results.size() == 0) results.add(entity);

		return results;
	}

	/**
	 * Get the resource attribute name for one of this annotator's output attributes.
	 * @param attr the name of the output attribute
	 * @return the name of the attribute to copy from the resource entity
	 */
	protected String getResourceAttributeName(AttributeMetaData attr)
	{
		return attr.getName();
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
