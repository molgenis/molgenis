package org.molgenis.gavin.job;

import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.DataService;
import org.molgenis.data.annotation.core.EffectBasedAnnotator;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.file.FileStore;
import org.molgenis.gavin.job.input.Parser;
import org.molgenis.jobs.JobExecutionUpdater;
import org.molgenis.jobs.ProgressImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.mail.MailSender;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.GAVIN_JOB_EXECUTION;

@Component
public class GavinJobFactory
{
	private final Parser parser;
	private DataService dataService;
	private PlatformTransactionManager transactionManager;
	private UserDetailsService userDetailsService;
	private JobExecutionUpdater jobExecutionUpdater;
	private MailSender mailSender;
	private FileStore fileStore;
	private RepositoryAnnotator cadd;
	private RepositoryAnnotator exac;
	private RepositoryAnnotator snpEff;
	private EffectBasedAnnotator gavin;
	private MenuReaderService menuReaderService;
	private AnnotatorRunner annotatorRunner;

	public GavinJobFactory(DataService dataService, PlatformTransactionManager transactionManager,
			UserDetailsService userDetailsService, JobExecutionUpdater jobExecutionUpdater, MailSender mailSender,
			FileStore fileStore, RepositoryAnnotator cadd, RepositoryAnnotator exac, RepositoryAnnotator snpEff,
			EffectBasedAnnotator gavin, MenuReaderService menuReaderService, Parser parser,
			AnnotatorRunner annotatorRunner)
	{
		this.dataService = requireNonNull(dataService);
		this.transactionManager = requireNonNull(transactionManager);
		this.userDetailsService = requireNonNull(userDetailsService);
		this.jobExecutionUpdater = requireNonNull(jobExecutionUpdater);
		this.mailSender = requireNonNull(mailSender);
		this.fileStore = requireNonNull(fileStore);
		this.cadd = requireNonNull(cadd);
		this.exac = requireNonNull(exac);
		this.snpEff = requireNonNull(snpEff);
		this.gavin = requireNonNull(gavin);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.parser = requireNonNull(parser);
		this.annotatorRunner = requireNonNull(annotatorRunner);
	}

	@RunAsSystem
	public GavinJob createJob(GavinJobExecution gavinJobExecution)
	{
		dataService.add(gavinJobExecution.getEntityType().getId(), gavinJobExecution);
		String username = gavinJobExecution.getUser();
		// create an authentication to run as the user that is listed as the owner of the job
		RunAsUserToken runAsAuthentication = new RunAsUserToken("Job Execution", username, null,
				userDetailsService.loadUserByUsername(username).getAuthorities(), null);

		return new GavinJob(new ProgressImpl(gavinJobExecution, jobExecutionUpdater, mailSender),
				new TransactionTemplate(transactionManager), runAsAuthentication, gavinJobExecution.getIdentifier(),
				fileStore, menuReaderService, cadd, exac, snpEff, gavin, parser, annotatorRunner, gavinJobExecution);
	}

	public List<String> getAnnotatorsWithMissingResources()
	{
		return of(cadd, exac, snpEff, gavin).filter(annotator -> !annotator.annotationDataExists())
											.map(RepositoryAnnotator::getSimpleName)
											.collect(toList());
	}

	/**
	 * Retrieves a {@link GavinJobExecution} for anyone who has the identifier, without checking their permissions.
	 *
	 * @param jobIdentifier the identifier of the {@link GavinJobExecution}
	 * @return GavinJobExecution with the specified identifier, if it exists
	 * @throws JobNotFoundException if no GavinJobExecution with the specified identifier exists.
	 */
	@RunAsSystem
	public GavinJobExecution findGavinJobExecution(String jobIdentifier) throws JobNotFoundException
	{
		GavinJobExecution result = dataService.findOneById(GAVIN_JOB_EXECUTION, jobIdentifier, GavinJobExecution.class);
		if (result == null)
		{

			throw new JobNotFoundException("Job not found.");
		}
		return result;
	}

}
