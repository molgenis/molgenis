package org.molgenis.data.mapper.job;

import org.molgenis.data.DataService;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.data.mapper.service.MappingService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.mapper.job.MappingJobExecutionMetadata.MAPPING_JOB_EXECUTION;

/**
 * Creates MappingJob based on its {@link MappingJobExecution}. Is a bean so that it can use {@link Autowired}
 * services needed to rehydrate the primitive data types. Runs at execution time.
 */
@Component
public class MappingJobFactory
{
	private final DataService dataService;
	private final PlatformTransactionManager transactionManager;
	private final UserDetailsService userDetailsService;
	private final JobExecutionUpdater jobExecutionUpdater;
	private final MailSender mailSender;
	private final MappingService mappingService;

	@Autowired
	public MappingJobFactory(DataService dataService, PlatformTransactionManager transactionManager,
			UserDetailsService userDetailsService, JobExecutionUpdater jobExecutionUpdater, MailSender mailSender,
			MappingService mappingService)
	{
		this.dataService = requireNonNull(dataService);
		this.transactionManager = requireNonNull(transactionManager);
		this.userDetailsService = requireNonNull(userDetailsService);
		this.jobExecutionUpdater = requireNonNull(jobExecutionUpdater);
		this.mailSender = requireNonNull(mailSender);
		this.mappingService = requireNonNull(mappingService);
	}

	@RunAsSystem
	public MappingJob createJob(MappingJobExecution mappingJobExecution)
	{
		dataService.add(MAPPING_JOB_EXECUTION, mappingJobExecution);
		String username = mappingJobExecution.getUser();
		String mappingProjectId = mappingJobExecution.getMappingProjectId();
		String targetEntityTypeId = mappingJobExecution.getTargetEntityTypeId();
		Boolean addSourceAttribute = mappingJobExecution.isAddSourceAttribute();
		String packageId = mappingJobExecution.getPackageId();
		String label = mappingJobExecution.getLabel();

		// create an authentication to run as the user that is listed as the owner of the job
		RunAsUserToken runAsAuthentication = new RunAsUserToken("Job Execution", username, null,
				userDetailsService.loadUserByUsername(username).getAuthorities(), null);

		return new MappingJob(mappingProjectId, targetEntityTypeId, addSourceAttribute, packageId, label,
				new ProgressImpl(mappingJobExecution, jobExecutionUpdater, mailSender), runAsAuthentication,
				new TransactionTemplate(transactionManager), mappingService, dataService);
	}
}
