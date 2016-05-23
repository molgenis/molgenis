package org.molgenis.data.support;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.meta.AnnotationJobExecution;
import org.molgenis.data.jobs.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Created with IntelliJ IDEA. User: charbonb Date: 19/02/14 Time: 12:50 To change this template use File | Settings |
 * File Templates.
 */

@Component
public class AnnotationServiceImpl implements AnnotationService, ApplicationListener<ContextRefreshedEvent>
{
	private List<RepositoryAnnotator> annotators = null;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private DataService dataService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		runAsSystem(() -> {
			// check if there are annotators with a status running, this can only occur because of a shutdown of the
			// server during an annotation run.
			if (dataService.hasRepository(AnnotationJobExecution.ENTITY_NAME))
			{
				Stream<Entity> runningAnnotations = dataService.findAll(AnnotationJobExecution.ENTITY_NAME,
						new QueryImpl().eq(AnnotationJobExecution.STATUS, JobExecution.Status.RUNNING));
				runningAnnotations.forEach(entity -> failRunningAnnotation(entity));
			}
		});
	}

	private void failRunningAnnotation(Entity entity)
	{
		entity.set(AnnotationJobExecution.STATUS, JobExecution.Status.FAILED);
		entity.set(AnnotationJobExecution.PROGRESS_MESSAGE, "Annotation failed because MOLGENIS was restarted.");
		dataService.update(AnnotationJobExecution.ENTITY_NAME, entity);
	}

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
					.getBeansOfType(RepositoryAnnotator.class);
			annotators.addAll(configuredAnnotators.values());
		}
		return annotators;
	}
}
