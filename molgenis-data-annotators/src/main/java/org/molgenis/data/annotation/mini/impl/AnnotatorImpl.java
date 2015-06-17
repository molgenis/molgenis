package org.molgenis.data.annotation.mini.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.mini.AnnotatorInfo;
import org.molgenis.data.annotation.mini.EntityAnnotator;
import org.molgenis.data.annotation.mini.QueryCreator;
import org.molgenis.data.annotation.mini.ResultFilter;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.common.base.Optional;
import org.springframework.context.ApplicationContext;

public class AnnotatorImpl implements EntityAnnotator
{
	private ApplicationContext applicationContext;

	private DataService dataService;

	private String sourceRepositoryName;

	private AnnotatorInfo info;

	private QueryCreator queryCreator;

	private ResultFilter resultFilter;

	public AnnotatorImpl(String sourceRepositoryName, AnnotatorInfo info, QueryCreator queryCreator,
			ResultFilter resultFilter, DataService dataService, ApplicationContext applicationContext)
	{
		this.sourceRepositoryName = sourceRepositoryName;
		this.info = info;
		this.queryCreator = queryCreator;
		this.resultFilter = resultFilter;
		this.dataService = dataService;
		this.applicationContext = applicationContext;
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
		Resource resource = getResource();
		if (resource != null)
		{
			annotatationSourceEntities = resource.findAll(q);
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
				resultEntity.set(attr.getName(), anntotationSourceEntity.get(attr.getName()));
			}
			results.add(resultEntity);
		}
		// no data added? add original entity
		if (results.size() == 0) results.add(entity);

		return results;
	}

	private Resource getResource()
	{
		Map<String, Resource> resources = applicationContext.getBeansOfType(Resource.class);
		Resource resource = null;
		for (Resource res : resources.values())
		{
			if (res.getName().equals(sourceRepositoryName)) resource = res;
		}
		return resource;
	}

	@Override
	public AttributeMetaData getAnnotationAttributeMetaData()
	{
		DefaultAttributeMetaData result = new DefaultAttributeMetaData(ANNOTATORPREFIX+info.getCode(), FieldTypeEnum.COMPOUND).setLabel(info.getCode());
		getInfo().getOutputAttributes().forEach(result::addAttributePart);

		return result;
	}

	@Override
	public boolean sourceExists()
	{
		return getResource() != null || dataService.hasRepository(sourceRepositoryName);
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
