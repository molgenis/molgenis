package org.molgenis.data.support;

import com.google.common.collect.Lists;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: charbonb Date: 19/02/14 Time: 12:50 To change this template use File | Settings |
 * File Templates.
 */

@Component
public class AnnotationServiceImpl implements AnnotationService
{
	private final List<RepositoryAnnotator> annotators = Lists.newArrayList();

	@Override
	public void addAnnotator(RepositoryAnnotator newAnnotator)
	{
		for (RepositoryAnnotator annotator : annotators)
		{
			if (annotator.getSimpleName().equalsIgnoreCase(newAnnotator.getSimpleName()))
			{
				throw new MolgenisDataException("Annotator [" + annotator.getSimpleName() + "] already registered.");
			}
		}

		annotators.add(newAnnotator);
	}

	@Override
	public RepositoryAnnotator getAnnotatorByName(String annotatorName)
	{
		for (RepositoryAnnotator annotator : annotators)
		{
			if (annotator.getSimpleName().equalsIgnoreCase(annotatorName))
			{
				return annotator;
			}
		}
		throw new UnknownEntityException("Unknown annotator [" + annotatorName + "]");
	}

	@Override
	public List<RepositoryAnnotator> getAnnotatorsByMetaData(EntityMetaData metaData)
	{
		List<RepositoryAnnotator> result = Lists.newArrayList();

		for (RepositoryAnnotator annotator : annotators)
		{
			if (annotator.canAnnotate(metaData).equals("true"))
			{
				result.add(annotator);
			}
		}
		return result;
	}

	@Override
	public List<RepositoryAnnotator> getAllAnnotators()
	{
		return annotators;
	}
}
