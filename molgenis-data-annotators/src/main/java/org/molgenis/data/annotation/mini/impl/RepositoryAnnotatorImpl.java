package org.molgenis.data.annotation.mini.impl;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.mini.AnnotatorInfo;
import org.molgenis.data.annotation.mini.EntityAnnotator;

public class RepositoryAnnotatorImpl extends AbstractRepositoryAnnotator
{
	private EntityAnnotator entityAnnotator;
	private EntityMetaData metaData;

	public RepositoryAnnotatorImpl(EntityAnnotator entityAnnotator, EntityMetaData metaData)
	{
		this.entityAnnotator = entityAnnotator;
		this.metaData = metaData;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		// DefaultEntityMetaData result = new DefaultEntityMetaData(entityAnnotator.getInfo().getCode());
		// result.addAttributeMetaData(entityAnnotator.getAnnotationAttributeMetaData());
		// return result;
		return metaData;
	}

	@Override
	public EntityMetaData getInputMetaData()
	{
		return entityAnnotator.getRequiredEntityMetaData();
	}

	@Override
	public String getSimpleName()
	{
		return entityAnnotator.getInfo().getCode();
	}

	@Override
	protected boolean annotationDataExists()
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

}
