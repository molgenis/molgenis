package org.molgenis.data.annotation.mini.impl;

import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.mini.AnnotatorInfo;
import org.molgenis.data.annotation.mini.EntityAnnotator;
import org.molgenis.data.annotation.mini.QueryCreator;
import org.molgenis.data.annotation.mini.ResultFilter;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Optional;

public class AnnotatorImpl implements EntityAnnotator
{
	@Autowired
	private DataService dataService;

	private String sourceRepositoryName;

	private AnnotatorInfo info;

	private QueryCreator queryCreator;

	private ResultFilter resultFilter;

	public AnnotatorImpl(String sourceRepositoryName, AnnotatorInfo info, QueryCreator queryCreator,
			ResultFilter resultFilter)
	{
		this.sourceRepositoryName = sourceRepositoryName;
		this.info = info;
		this.queryCreator = queryCreator;
		this.resultFilter = resultFilter;
	}

	@Override
	public AnnotatorInfo getInfo()
	{
		return info;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity)
	{
		Iterable<Entity> results = dataService.findAll(sourceRepositoryName, queryCreator.createQuery(entity));
		Optional<Entity> filteredResults = resultFilter.filterResults(results, entity);
		return Lists.newArrayList(filteredResults.asSet());
	}

	@Override
	public AttributeMetaData getAnnotationAttributeMetaData()
	{
		DefaultAttributeMetaData result = new DefaultAttributeMetaData(info.getCode(), FieldTypeEnum.COMPOUND);
		dataService.getEntityMetaData(sourceRepositoryName).getAtomicAttributes().forEach(result::addAttributePart);
		return result;
	}

	@Override
	public boolean sourceExists()
	{
		return dataService.hasRepository(sourceRepositoryName);
	}

	@Override
	public EntityMetaData getRequiredEntityMetaData()
	{
		DefaultEntityMetaData sourceMetaData = new DefaultEntityMetaData(info.getCode());
		sourceMetaData.addAllAttributeMetaData(queryCreator.getRequiredAttributes());
		sourceMetaData.addAllAttributeMetaData(resultFilter.getRequiredAttributes());
		return sourceMetaData;
	}

}
