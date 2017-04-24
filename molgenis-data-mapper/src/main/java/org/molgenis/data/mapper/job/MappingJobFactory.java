package org.molgenis.data.mapper.job;

import org.molgenis.data.DataService;
import org.molgenis.data.annotation.web.meta.AnnotationJobExecution;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.molgenis.data.mapper.job.MappingJobExecutionMetadata.MAPPING_JOB_EXECUTION;

/**
 * Creates AnnotationJob based on its {@link AnnotationJobExecution}. Is a bean so that it can use {@link Autowired}
 * services needed to rehydrate the primitive data types. Runs at execution time.
 */
@Component
public class MappingJobFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(MappingJobFactory.class);

	final DataService dataService;
	private final PlatformTransactionManager transactionManager;
	private final UserDetailsService userDetailsService;
	private final JobExecutionUpdater jobExecutionUpdater;
	private final MailSender mailSender;
	private final EntityTypeFactory entityTypeFactory;
	private final MappingService mappingService;

	@Autowired
	public MappingJobFactory(DataService dataService, PlatformTransactionManager transactionManager,
			UserDetailsService userDetailsService, JobExecutionUpdater jobExecutionUpdater, MailSender mailSender,
			EntityTypeFactory entityTypeFactory, MappingService mappingService)
	{
		this.dataService = dataService;
		this.transactionManager = transactionManager;
		this.userDetailsService = userDetailsService;
		this.jobExecutionUpdater = jobExecutionUpdater;
		this.mailSender = mailSender;
		this.entityTypeFactory = entityTypeFactory;
		this.mappingService = mappingService;
	}

	@RunAsSystem
	public MappingJob createJob(MappingJobExecution mappingJobExecution)
	{
		dataService.add(MAPPING_JOB_EXECUTION, mappingJobExecution);
		String mappingProjectId = mappingJobExecution.getMappingProjectId();
		String targetEntityTypeId = mappingJobExecution.getTargetEntityTypeId();
		String addSourceAttribute = mappingJobExecution.isAddSourceAttribute();
		String username = mappingJobExecution.getUser();

		// create an authentication to run as the user that is listed as the owner of the job
		RunAsUserToken runAsAuthentication = new RunAsUserToken("Job Execution", username, null,
				userDetailsService.loadUserByUsername(username).getAuthorities(), null);

		return new MappingJob(username, new ProgressImpl(mappingJobExecution, jobExecutionUpdater, mailSender),
				runAsAuthentication, new TransactionTemplate(transactionManager));
	}
}
