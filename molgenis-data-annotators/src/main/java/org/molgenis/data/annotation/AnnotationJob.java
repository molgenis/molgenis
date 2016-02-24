package org.molgenis.data.annotation;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.taskdefs.SQLExec;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.jta.TransactionFactory;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
	public static final String CONTEXT = "CONTEXT";

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
		TransactionTemplate template = new TransactionTemplate();
		template.execute(new TransactionCallbackWithoutResult()
		{
			@Override
			public void doInTransactionWithoutResult(TransactionStatus transactionStatus)
			{
				SecurityContext securityContext = (SecurityContext) jobExecutionContext.getMergedJobDataMap()
						.get(CONTEXT);
				SecurityContextHolder.setContext(securityContext);

				String repositoryName = jobExecutionContext.getMergedJobDataMap().getString(REPOSITORY_NAME);
				String username = jobExecutionContext.getMergedJobDataMap().getString(USERNAME);
				AnnotationJobMetaData annotationJobMetaData = new AnnotationJobMetaData(dataService);
				annotationJobMetaData.setIdentifier(UUID.randomUUID().toString());

				try
				{
					long t0 = System.currentTimeMillis();
					LOG.info("Annotations started");

					List<RepositoryAnnotator> annotators = (List<RepositoryAnnotator>) jobExecutionContext
							.getMergedJobDataMap().get(ANNOTATORS);
					Repository repository = dataService.getRepository(repositoryName);

					List<RepositoryAnnotator> availableAnnotators = annotationService.getAllAnnotators().stream()
							.filter(annotator -> annotator.annotationDataExists()).collect(Collectors.toList());
					AnnotatorDependencyOrderResolver resolver = new AnnotatorDependencyOrderResolver();
					Queue<RepositoryAnnotator> annotatorQueue = resolver
							.getAnnotatorSelectionDependencyList(availableAnnotators, annotators, repository);

					annotationJobMetaData.setAnnotators(StringUtils.join(
							annotatorQueue.stream().map(a -> a.getSimpleName()).collect(Collectors.toList()), ','));
					annotationJobMetaData.setProgressMessage("Started annotation run. (started by " + username + ")");
					annotationJobMetaData.setLog("Started annotation run. (started by " + username + ")");
					annotationJobMetaData.setProgressMax(annotatorQueue.size());
					annotationJobMetaData.setStatus(JobMetaData.Status.RUNNING);
					annotationJobMetaData.setTarget(repositoryName);
					annotationJobMetaData.setUser(userAccountService.getCurrentUser());
					annotationJobMetaData.setSubmissionDate(new Date());
					annotationJobMetaData.setStartDate(new Date());
					annotationJobMetaData.setType("Annotators");
					Runnable task = () -> {
						RunAsSystemProxy.runAsSystem(() -> {
							dataService.add(AnnotationJobMetaData.ENTITY_NAME, annotationJobMetaData);
						});
					};
					Thread thread = new Thread(task);
					thread.start();
					thread.join();// otherwise the update of the JobMeta "overtakes" the creation

					annotate(username, annotationJobMetaData, repository, annotatorQueue);
					// FIXME: This a workaround for:Github #4485 If an annotator finishes within a second the user is
					// not sent
					// to the dataexplorer data tab
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						throw new RuntimeException(e);
					}

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
						try
						{
							logAndUpdateProgress(annotationJobMetaData, JobMetaData.Status.FAILED, e.getMessage(),
									annotationJobMetaData.getProgressMax());
						}
						catch (InterruptedException ex)
						{
							throw new RuntimeException(ex);
						}
					}
				}
			}
		});
	}

	private void annotate(String username, AnnotationJobMetaData annotationJobMetaData, Repository repository,
			Queue<RepositoryAnnotator> annotatorQueue) throws IOException, InterruptedException
	{

		int totalAnnotators = annotatorQueue.size();
		while (annotatorQueue.size() != 0)
		{
			RepositoryAnnotator annotator = annotatorQueue.poll();
			if (annotator.getSimpleName().equals("clinvar")) throw new RuntimeException("HAHA");
			String message = "Annotating \"" + repository.getEntityMetaData().getLabel() + "\" with "
					+ annotator.getSimpleName() + " (annotator " + (totalAnnotators - annotatorQueue.size()) + " of "
					+ totalAnnotators + ", started by \"" + username + "\")";
			logAndUpdateProgress(annotationJobMetaData, JobMetaData.Status.RUNNING, message,
					(totalAnnotators - annotatorQueue.size()));
			runSingleAnnotator(crudRepositoryAnnotator, annotator, repository);
		}
	}

	private void logAndUpdateProgress(AnnotationJobMetaData annotationJobMetaData, JobMetaData.Status status,
			String message, int progress) throws InterruptedException
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

		Runnable task = () -> {
			RunAsSystemProxy.runAsSystem(() -> {
				dataService.update(AnnotationJobMetaData.ENTITY_NAME, annotationJobMetaData);
			});
		};
		Thread thread = new Thread(task);
		thread.start();
		thread.join();
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