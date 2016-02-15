package org.molgenis.data.annotation;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
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
import org.quartz.Job;
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

public class AnnotationJob implements Job
{
	private static final Logger LOG = LoggerFactory.getLogger(AnnotationJob.class);
	public static final String REPOSITORY_NAME = "REPOSITORY_NAME";
	public static final String ANNOTATORS = "ANNOTATORS";
	public static final String USERNAME = "USERNAME";

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

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
	{
		String repositoryName = jobExecutionContext.getMergedJobDataMap().getString(REPOSITORY_NAME);
		String username = jobExecutionContext.getMergedJobDataMap().getString(USERNAME);
		AnnotationJobMetaData annotationJobMetaData = new AnnotationJobMetaData(dataService);
		annotationJobMetaData.setIdentifier(UUID.randomUUID().toString());

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

			annotationJobMetaData.setAnnotators(StringUtils
					.join(annotatorQueue.stream().map(a -> a.getSimpleName()).collect(Collectors.toList()), ','));
			annotationJobMetaData.setProgressMessage("Started annotation run. (started by " + username + ")");
			annotationJobMetaData.setLog("Started annotation run. (started by " + username + ")");
			annotationJobMetaData.setProgressMax(annotatorQueue.size());
			annotationJobMetaData.setStatus(JobMetaData.Status.RUNNING);
			annotationJobMetaData.setTarget(repositoryName);
			annotationJobMetaData.setUser(userAccountService.getCurrentUser());
			annotationJobMetaData.setSubmissionDate(new Date());
			annotationJobMetaData.setStartDate(new Date());
			annotationJobMetaData.setType("Annotators");
			RunAsSystemProxy.runAsSystem(() -> {
				dataService.add(AnnotationJobMetaData.ENTITY_NAME, annotationJobMetaData);
			});
			annotate(username, annotationJobMetaData, repository, annotatorQueue);

			long t = System.currentTimeMillis();
			logAndUpdateProgress(annotationJobMetaData, JobMetaData.Status.SUCCESS,
					"Annotations (started by " + username + ") finished in " + (t - t0) + " msec.",
					annotationJobMetaData.getProgressMax());
		}
		catch (Exception e)
		{
			LOG.error("An error occured during annotation. ", e);
			if (annotationJobMetaData != null)
			{
				logAndUpdateProgress(annotationJobMetaData, JobMetaData.Status.FAILED, e.getMessage(),
						annotationJobMetaData.getProgressMax());
			}
		}
	}

	private void annotate(String username, AnnotationJobMetaData annotationJobMetaData, Repository repository,
			Queue<RepositoryAnnotator> annotatorQueue) throws IOException
	{
		int totalAnnotators = annotatorQueue.size();
		while (annotatorQueue.size() != 0)
		{
			RepositoryAnnotator annotator = annotatorQueue.poll();
			String message = "Annotating \"" + repository.getEntityMetaData().getLabel() + "\" with "
					+ annotator.getSimpleName() + " (annotator " + (totalAnnotators - annotatorQueue.size()) + " of "
					+ totalAnnotators + ", started by \"" + username + "\")";
			logAndUpdateProgress(annotationJobMetaData, JobMetaData.Status.RUNNING, message,
					(totalAnnotators - annotatorQueue.size()));
			runSingleAnnotator(crudRepositoryAnnotator, annotator, repository);
		}
	}

	private void logAndUpdateProgress(AnnotationJobMetaData annotationJobMetaData, JobMetaData.Status status,
			String message, int progress)
	{
		LOG.info(message);
		annotationJobMetaData.setProgressMessage(message);
		annotationJobMetaData.setLog(annotationJobMetaData.getLog() + "\n" + message);
		annotationJobMetaData.setProgressInt(progress);
		annotationJobMetaData.setStatus(status);
		if (status.equals(JobMetaData.Status.SUCCESS) || status.equals(JobMetaData.Status.FAILED))
		{
			annotationJobMetaData.setEndDate(new Date());
		}
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.update(AnnotationJobMetaData.ENTITY_NAME, annotationJobMetaData);
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