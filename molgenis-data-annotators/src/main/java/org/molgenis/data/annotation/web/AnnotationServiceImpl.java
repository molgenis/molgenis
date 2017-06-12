package org.molgenis.data.annotation.web;

import com.google.common.collect.Lists;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	public List<RepositoryAnnotator> getAnnotatorsByMetaData(EntityType metaData)
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
			Map<String, RepositoryAnnotator> configuredAnnotators = applicationContext.getBeansOfType(
					RepositoryAnnotator.class); // FIXME use repository annotator registry
			annotators.addAll(configuredAnnotators.values());
		}
		return annotators;
	}
}
