package org.molgenis.data.annotation.entity.impl;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AbstractRepositoryEntityAnnotator;
import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RepositoryAnnotatorImpl extends AbstractRepositoryEntityAnnotator
{
	private final EntityAnnotator entityAnnotator;

	public RepositoryAnnotatorImpl(EntityAnnotator entityAnnotator)
	{
		this.entityAnnotator = entityAnnotator;
	}

	@Override
	public List<AttributeMetaData> getOutputMetaData()
	{
		List<AttributeMetaData> result = new ArrayList<>();
		result.add(entityAnnotator.getAnnotationAttributeMetaData());
		return result;
	}

	@Override
	public List<AttributeMetaData> getRequiredAttributes()
	{
		return entityAnnotator.getRequiredAttributes();
	}

	@Override
	public String getSimpleName()
	{
		return entityAnnotator.getInfo().getCode();
	}

	@Override
	public boolean annotationDataExists()
	{
		return entityAnnotator.sourceExists();
	}

	@Override
	public List<Entity> annotateEntity(Entity entity, boolean updateMode) throws IOException, InterruptedException
	{
		return Lists.newArrayList(entityAnnotator.annotateEntity(entity, updateMode));
	}

	@Override
	public AnnotatorInfo getInfo()
	{
		return entityAnnotator.getInfo();
	}

	@Override
	public CmdLineAnnotatorSettingsConfigurer getCmdLineAnnotatorSettingsConfigurer()
	{
		return entityAnnotator.getCmdLineAnnotatorSettingsConfigurer();
	}
}
