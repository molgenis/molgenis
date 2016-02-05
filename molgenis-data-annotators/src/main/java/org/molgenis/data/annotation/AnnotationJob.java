package org.molgenis.data.annotation;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.AnnotatorDependencyOrderResolver;
import org.molgenis.data.validation.EntityValidator;
import org.molgenis.file.FileStore;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
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
	AnnotatorRunService annotatorRunService;
	@Autowired
	CrudRepositoryAnnotator crudRepositoryAnnotator;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
	{
		String repositoryName = jobExecutionContext.getMergedJobDataMap().getString(REPOSITORY_NAME);
		String username = jobExecutionContext.getMergedJobDataMap().getString(USERNAME);
		String annotationRunId = jobExecutionContext.getMergedJobDataMap().getString(ANNOTATION_RUN);
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

			while (annotatorQueue.size() != 0)
			{
				runSingleAnnotator(crudRepositoryAnnotator, annotatorQueue.poll(), annotationRunId, repository,
						username);
			}

			long t = System.currentTimeMillis();
			annotatorRunService.finishAnnotationRun(annotationRunId, "Annotations finished in " + (t - t0) + " msec.");
		}
		catch (Exception e)
		{
			LOG.info("Annotations failed.", e);
			annotatorRunService.failAnnotationRun(annotationRunId, e.getMessage());
		}
	}

	private void runSingleAnnotator(CrudRepositoryAnnotator crudRepositoryAnnotator, RepositoryAnnotator annotator,
			String annotationRunId, Repository repository, String username)
	{
		try
		{
			annotatorRunService.updateAnnotatorStarted(annotationRunId, annotator.getSimpleName());
			if (annotator != null)
			{
				try
				{
					LOG.info("Started annotating \"" + repository.getName() + "\" with the " + annotator.getSimpleName()
							+ " annotator (started by \"" + username + "\")");
					crudRepositoryAnnotator.annotate(annotator, repository);
					LOG.info("Finished annotating \"" + repository.getName() + "\" with the "
							+ annotator.getSimpleName() + " annotator (started by \"" + username + "\")");
				}
				catch (IOException e)
				{
					annotatorRunService.updateAnnotatorFailed(annotationRunId, annotator.getSimpleName());
				}
			}
			annotatorRunService.updateAnnotatorFinished(annotationRunId, annotator.getSimpleName());

		}
		catch (Exception e)
		{
			annotatorRunService.updateAnnotatorFailed(annotationRunId, annotator.getSimpleName());
		}
	}
}