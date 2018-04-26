package org.molgenis.data.annotation.core.entity.impl.framework;

import com.google.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.AbstractRepositoryEntityAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.resources.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;

import java.io.IOException;
import java.util.List;

public class RepositoryAnnotatorImpl extends AbstractRepositoryEntityAnnotator
{
	@SuppressWarnings("unused")
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
	public List<Attribute> getOutputAttributes()
	{
		return entityAnnotator.getAnnotatorAttributes();
	}

	@Override
	public List<Attribute> getRequiredAttributes()
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

	@Override
	public List<Attribute> createAnnotatorAttributes(AttributeFactory attributeFactory)
	{
		return entityAnnotator.createAnnotatorAttributes(attributeFactory);
	}
}
