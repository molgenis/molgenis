package org.molgenis.data.annotation;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.meta.AnnotationJobMetaData;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.jobs.JobMetaData;
import org.molgenis.data.support.AnnotatorDependencyOrderResolver;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.user.UserAccountService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
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
	@Autowired
	UserAccountService userAccountService;
	private AnnotationJobMetaData annotationRun;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
	{
		String repositoryName = jobExecutionContext.getMergedJobDataMap().getString(REPOSITORY_NAME);
		String username = jobExecutionContext.getMergedJobDataMap().getString(USERNAME);
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
			Queue<RepositoryAnnotator> annotatorQueue = resolver
					.getAnnotatorSelectionDependencyList(availableAnnotators, annotators, repository);
			annotationRun = new AnnotationJobMetaData(dataService);
			annotationRun.setIdentifier(UUID.randomUUID().toString());
			annotationRun.setProgressMessage("Started annotation run. (started by " + username + ")");
			annotationRun.setProgressMax(annotatorQueue.size());
			annotationRun.setStatus(JobMetaData.Status.RUNNING);
			annotationRun.setTarget(repositoryName);
			annotationRun.setUser(userAccountService.getCurrentUser());
			annotationRun.setSubmissionDate(new Date());
			annotationRun.setType("Annotators");
			RunAsSystemProxy.runAsSystem(() -> {
				dataService.add(AnnotationJobMetaData.ENTITY_NAME, annotationRun);
			});
			annotate(username, annotationRun, repository, annotatorQueue);

			long t = System.currentTimeMillis();
			logAndUpdateProgress(annotationRun, JobMetaData.Status.SUCCESS,
					"Annotations (started by " + username + ") finished in " + (t - t0) + " msec.",
					annotationRun.getProgressMax());
		}
		catch (Exception e)
		{
			LOG.error("An error occured during annotation. ",e);
			if(annotationRun != null) {
				logAndUpdateProgress(annotationRun, JobMetaData.Status.FAILED, e.getMessage(),
						annotationRun.getProgressMax());
			}
		}
	}

	private void annotate(String username, JobMetaData annotationRun, Repository repository,
			Queue<RepositoryAnnotator> annotatorQueue) throws IOException
	{
		int totalAnnotators = annotatorQueue.size();
		while (annotatorQueue.size() != 0)
		{
			RepositoryAnnotator annotator = annotatorQueue.poll();
			String message = "Annotating \"" + repository.getEntityMetaData().getLabel() + "\" with "
					+ annotator.getSimpleName() + " (annotator " + (totalAnnotators - annotatorQueue.size()) + " of "
					+ totalAnnotators + ", started by \"" + username + "\")";
			logAndUpdateProgress(annotationRun, JobMetaData.Status.RUNNING, message,
					(totalAnnotators - annotatorQueue.size()));
			runSingleAnnotator(crudRepositoryAnnotator, annotator, repository);
		}
	}

	private void logAndUpdateProgress(JobMetaData annotationRun, JobMetaData.Status status, String message,
			int progress)
	{
		LOG.info(message);
		annotationRun.setProgressMessage(message);
		annotationRun.setProgressInt(progress);
		annotationRun.setStatus(status);
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.update(AnnotationJobMetaData.ENTITY_NAME, annotationRun);
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