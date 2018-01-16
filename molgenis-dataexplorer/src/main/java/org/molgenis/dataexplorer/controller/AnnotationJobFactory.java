package org.molgenis.dataexplorer.controller;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorDependencyOrderResolver;
import org.molgenis.data.annotation.web.AnnotationService;
import org.molgenis.data.annotation.web.CrudRepositoryAnnotator;
import org.molgenis.data.annotation.web.meta.AnnotationJobExecution;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.jobs.JobExecutionUpdater;
import org.molgenis.jobs.ProgressImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.molgenis.data.annotation.web.meta.AnnotationJobExecutionMetaData.ANNOTATION_JOB_EXECUTION;

/**
 * Creates AnnotationJob based on its {@link AnnotationJobExecution}. Is a bean so that it can use {@link Autowired}
 * services needed to rehydrate the primitive data types. Runs at execution time.
 */
@Component
public class AnnotationJobFactory
{
	@Autowired
	CrudRepositoryAnnotator crudRepositoryAnnotator;

	@Autowired
	DataService dataService;

	@Autowired
	private AnnotationService annotationService;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JobExecutionUpdater jobExecutionUpdater;

	@Autowired
	private MailSender mailSender;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@RunAsSystem
	public AnnotationJob createJob(AnnotationJobExecution metaData)
	{
		dataService.add(ANNOTATION_JOB_EXECUTION, metaData);
		String annotatorNames = metaData.getAnnotators();
		String targetName = metaData.getTargetName();
		String username = metaData.getUser();

		// create an authentication to run as the user that is listed as the owner of the job
		RunAsUserToken runAsAuthentication = new RunAsUserToken("Job Execution", username, null,
				userDetailsService.loadUserByUsername(username).getAuthorities(), null);

		Repository<Entity> repository = dataService.getRepository(targetName);
		List<RepositoryAnnotator> availableAnnotators = annotationService.getAllAnnotators()
																		 .stream()
																		 .filter(RepositoryAnnotator::annotationDataExists)
																		 .collect(toList());
		List<RepositoryAnnotator> requestedAnnotators = Arrays.stream(annotatorNames.split(","))
															  .map(annotationService::getAnnotatorByName)
															  .collect(toList());
		AnnotatorDependencyOrderResolver resolver = new AnnotatorDependencyOrderResolver();
		List<RepositoryAnnotator> annotators = Lists.newArrayList(
				resolver.getAnnotatorSelectionDependencyList(availableAnnotators, requestedAnnotators, repository,
						entityTypeFactory));
		return new AnnotationJob(crudRepositoryAnnotator, username, annotators, repository,
				new ProgressImpl(metaData, jobExecutionUpdater, mailSender), runAsAuthentication,
				new TransactionTemplate(transactionManager));
	}
}
