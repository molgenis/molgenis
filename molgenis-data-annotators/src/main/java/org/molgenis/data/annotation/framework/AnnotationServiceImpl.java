package org.molgenis.data.annotation.framework;

import com.google.common.collect.Lists;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.meta.model.EntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: charbonb Date: 19/02/14 Time: 12:50 To change this template use File | Settings |
 * File Templates.
 */

@Component
public class AnnotationServiceImpl implements AnnotationService
{
	private List<RepositoryAnnotator> annotators = null;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public RepositoryAnnotator getAnnotatorByName(String annotatorName)
	{
		getAllAnnotators();
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
		getAllAnnotators();
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
		if (annotators == null)
		{
			annotators = new ArrayList<>();
			Map<String, RepositoryAnnotator> configuredAnnotators = applicationContext
					.getBeansOfType(RepositoryAnnotator.class); // FIXME use repository annotator registry
			annotators.addAll(configuredAnnotators.values());
		}
		return annotators;
	}
}
