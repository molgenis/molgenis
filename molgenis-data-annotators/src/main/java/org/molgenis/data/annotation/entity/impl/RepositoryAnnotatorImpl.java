package org.molgenis.data.annotation.entity.impl;

import java.io.IOException;
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
	private final String name;//unused... however debug time you have no indication which annotator this is if bootstrapping is not done yet...
	private EntityAnnotator entityAnnotator;

	public RepositoryAnnotatorImpl(String name)
	{
		this.name = name;
	}

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
