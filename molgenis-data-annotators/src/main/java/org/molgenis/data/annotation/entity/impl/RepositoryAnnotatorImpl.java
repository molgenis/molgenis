package org.molgenis.data.annotation.entity.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AbstractRepositoryEntityAnnotator;
import org.molgenis.data.annotation.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.meta.model.AttributeMetaData;

public class RepositoryAnnotatorImpl extends AbstractRepositoryEntityAnnotator
{
	private EntityAnnotator entityAnnotator;

	public RepositoryAnnotatorImpl(){}

	public void init(EntityAnnotator entityAnnotator)
	{
		this.entityAnnotator = entityAnnotator;
	}

	@Override
	public List<AttributeMetaData> getOutputAttributes()
	{
		return entityAnnotator.getAnnotationAttributeMetaDatas();
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
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		return Lists.newArrayList(entityAnnotator.annotateEntity(entity));
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
