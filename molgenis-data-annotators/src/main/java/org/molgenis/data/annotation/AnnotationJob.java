package org.molgenis.data.annotation;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.support.AnnotatorDependencyOrderResolver;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class AnnotationJob implements org.quartz.Job
{
	private static final Logger LOG = LoggerFactory.getLogger(AnnotationJob.class);
	public static final String REPOSITORY_NAME = "REPOSITORY_NAME";
	public static final String ANNOTATORS = "ANNOTATORS";
	public static final String USERNAME = "USERNAME";
	public static final String ANNOTATION_RUN = "ANNOTATION_RUN";

	@Autowired
	DataService dataService;
	@Autowired
	FileStore fileStore;
	@Autowired
	SearchService searchService;
	@Autowired
	AnnotationService annotationService;
	@Autowired
	EntityValidator entityValidator;
	@Autowired
	CrudRepositoryAnnotator crudRepositoryAnnotator;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
	{
		String repositoryName = jobExecutionContext.getMergedJobDataMap().getString(REPOSITORY_NAME);
		String username = jobExecutionContext.getMergedJobDataMap().getString(USERNAME);
		Job annotationRun = (Job) jobExecutionContext.getMergedJobDataMap().get(ANNOTATION_RUN);
		try
		{
			long t0 = System.currentTimeMillis();
			LOG.info("Annotations started");

			List<RepositoryAnnotator> annotators = (List<RepositoryAnnotator>) jobExecutionContext.getMergedJobDataMap()
					.get(ANNOTATORS);
			Repository repository = dataService.getRepository(repositoryName);

			List<RepositoryAnnotator> availableAnnotators = annotationService.getAllAnnotators().stream()
					.filter(annotator -> annotator.annotationDataExists()).collect(Collectors.toList());
			AnnotatorDependencyOrderResolver resolver = new AnnotatorDependencyOrderResolver();
			annotate(username, annotationRun, annotators, repository, availableAnnotators, resolver);

			long t = System.currentTimeMillis();
			logAndUpdateProgress(annotationRun, Job.Status.SUCCESS, "Annotations finished in " + (t - t0) + " msec.");
		}
		catch (Exception e)
		{
			logAndUpdateProgress(annotationRun, Job.Status.FAILED, e.getMessage());
		}
	}

	private void annotate(String username, Job annotationRun, List<RepositoryAnnotator> annotators,
			Repository repository, List<RepositoryAnnotator> availableAnnotators,
			AnnotatorDependencyOrderResolver resolver) throws IOException
	{
		int totalAnnotators;
		Queue<RepositoryAnnotator> annotatorQueue = resolver.getAnnotatorSelectionDependencyList(availableAnnotators,
				annotators, repository);
		totalAnnotators = annotatorQueue.size();
		while (annotatorQueue.size() != 0)
		{
			RepositoryAnnotator annotator = annotatorQueue.poll();
			String message = "Annotating \"" + repository.getEntityMetaData().getLabel() + "\" with "
					+ annotator.getSimpleName() + " (annotator " + (totalAnnotators - annotatorQueue.size()) + " of "
					+ totalAnnotators + ", started by \"" + username + "\")";
			logAndUpdateProgress(annotationRun, Job.Status.RUNNING, message);
			runSingleAnnotator(crudRepositoryAnnotator, annotator, repository);
		}
	}

	private void logAndUpdateProgress(Job annotationRun, Job.Status status, String message)
	{
		LOG.info(message);
		annotationRun.setProgressMessage(message);
		annotationRun.setStatus(status);
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.update(Job.ENTITY_NAME, annotationRun);
		});
	}

	private void runSingleAnnotator(CrudRepositoryAnnotator crudRepositoryAnnotator, RepositoryAnnotator annotator,
			Repository repository) throws IOException
	{
		if (annotator != null)
		{
			crudRepositoryAnnotator.annotate(annotator, repository);
		}
	}
}