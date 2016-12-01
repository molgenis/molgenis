package org.molgenis.gavin.job;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.resources.CmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;

import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptyList;

public class FailingAnnotator implements RepositoryAnnotator
{
	private final RuntimeException ex;

	public FailingAnnotator(RuntimeException ex)
	{
		this.ex = ex;
	}

	@Override
	public AnnotatorInfo getInfo()
	{
		return new AnnotatorInfo()
		{
			@Override
			public Status getStatus()
			{
				return Status.BETA;
			}

			@Override
			public Type getType()
			{
				return Type.UNKNOWN;
			}

			@Override
			public String getCode()
			{
				return "FAIL";
			}

			@Override
			public String getDescription()
			{
				return "Failing annotator for unit test purposes";
			}

			@Override
			public List<Attribute> getOutputAttributes()
			{
				return emptyList();
			}
		};
	}

	@Override
	public Iterator<Entity> annotate(Iterable<Entity> source, boolean updateMode)
	{
		return this.annotate(source);
	}

	@Override
	public Iterator<Entity> annotate(Iterable<Entity> source)
	{
		throw ex;
	}

	@Override
	public boolean annotationDataExists()
	{
		return true;
	}

	@Override
	public Iterator<Entity> annotate(Iterator<Entity> source)
	{
		throw ex;
	}

	@Override
	public List<Attribute> getOutputAttributes()
	{
		return emptyList();
	}

	@Override
	public List<Attribute> getRequiredAttributes()
	{
		return emptyList();
	}

	@Override
	public String canAnnotate(EntityType inputMetaData)
	{
		return null;
	}

	@Override
	public String getSimpleName()
	{
		return "FAIL";
	}

	@Override
	public String getFullName()
	{
		return "FAIL";
	}

	@Override
	public CmdLineAnnotatorSettingsConfigurer getCmdLineAnnotatorSettingsConfigurer()
	{
		return annotationSourceFileName ->
		{
		};
	}

	@Override
	public List<Attribute> createAnnotatorAttributes(AttributeFactory attributeFactory)
	{
		return emptyList();
	}
}
