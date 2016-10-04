package org.molgenis.gavin.job;

import org.molgenis.data.DataService;
import org.molgenis.data.annotation.core.EffectsAnnotator;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.web.CrudRepositoryAnnotator;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.ProgressImpl;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class GavinJobFactory
{
	CrudRepositoryAnnotator crudRepositoryAnnotator;
	DataService dataService;
	private PlatformTransactionManager transactionManager;
	private UserDetailsService userDetailsService;
	private JobExecutionUpdater jobExecutionUpdater;
	private MailSender mailSender;
	FileStore fileStore;
	private RepositoryAnnotator cadd;
	private RepositoryAnnotator exac;
	private RepositoryAnnotator snpEff;
	private EffectsAnnotator gavin;
	private MenuReaderService menuReaderService;
	private VcfAttributes vcfAttributes;
	private VcfUtils vcfUtils;
	private AttributeFactory attributeFactory;
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	public GavinJobFactory(CrudRepositoryAnnotator crudRepositoryAnnotator, DataService dataService,
			PlatformTransactionManager transactionManager, UserDetailsService userDetailsService,
			JobExecutionUpdater jobExecutionUpdater, MailSender mailSender, FileStore fileStore,
			RepositoryAnnotator cadd, RepositoryAnnotator exac, RepositoryAnnotator snpEff, EffectsAnnotator gavin,
			MenuReaderService menuReaderService, VcfAttributes vcfAttributes, VcfUtils vcfUtils,
			AttributeFactory attributeFactory, EntityMetaDataFactory entityMetaDataFactory)
	{
		this.crudRepositoryAnnotator = requireNonNull(crudRepositoryAnnotator);
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
		this.vcfAttributes = requireNonNull(vcfAttributes);
		this.vcfUtils = requireNonNull(vcfUtils);
		this.attributeFactory = requireNonNull(attributeFactory);
		this.entityMetaDataFactory = requireNonNull(entityMetaDataFactory);
	}

	@RunAsSystem
	public GavinJob createJob(GavinJobExecution gavinJobExecution)
	{
		dataService.add(gavinJobExecution.getEntityMetaData().getName(), gavinJobExecution);
		String username = gavinJobExecution.getUser();

		// create an authentication to run as the user that is listed as the owner of the job
		RunAsUserToken runAsAuthentication = new RunAsUserToken("Job Execution", username, null,
				userDetailsService.loadUserByUsername(username).getAuthorities(), null);

		return new GavinJob(new ProgressImpl(gavinJobExecution, jobExecutionUpdater, mailSender),
				new TransactionTemplate(transactionManager), runAsAuthentication, gavinJobExecution.getIdentifier(),
				fileStore, menuReaderService, cadd, exac, snpEff, gavin, vcfAttributes, vcfUtils, entityMetaDataFactory,
				attributeFactory);
	}

	public List<String> getAnnotatorsWithMissingResources()
	{
		return of(cadd, exac, snpEff, gavin).filter(annotator -> !annotator.annotationDataExists())
				.map(RepositoryAnnotator::getSimpleName).collect(toList());
	}
}
