package org.molgenis.gavin.job;

import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.CmdLineAnnotator;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.file.FileStore;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;

import static java.io.File.separator;
import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;
import static org.molgenis.gavin.controller.GavinController.GAVIN_APP;

public class GavinJob extends Job<Void>
{
	private final CmdLineAnnotator cmdLineAnnotator;
	private final String jobIdentifier;
	private final MenuReaderService menuReaderService;

	private final RepositoryAnnotator cadd;
	private final RepositoryAnnotator exac;
	private final RepositoryAnnotator snpeff;
	private final RepositoryAnnotator gavin;

	private final File inputFile;
	private final File caddOutputFile;
	private final File exacOutputFile;
	private final File snpeffOutputFile;
	private final File gavinOutputFile;

	public GavinJob(CmdLineAnnotator cmdLineAnnotator, Progress progress, TransactionTemplate transactionTemplate,
			Authentication authentication, String jobIdentifier, FileStore fileStore,
			MenuReaderService menuReaderService, RepositoryAnnotator cadd, RepositoryAnnotator exac,
			RepositoryAnnotator snpeff, RepositoryAnnotator gavin)
	{
		super(progress, transactionTemplate, authentication);
		this.cmdLineAnnotator = cmdLineAnnotator;
		this.jobIdentifier = jobIdentifier;
		this.menuReaderService = menuReaderService;
		this.cadd = cadd;
		this.exac = exac;
		this.snpeff = snpeff;
		this.gavin = gavin;

		this.inputFile = fileStore
				.getFile(format("{0}{1}{2}{3}input.vcf", GAVIN_APP, separator, jobIdentifier, separator));
		this.caddOutputFile = fileStore
				.getFile(format("{0}{1}{2}{3}temp-cadd.vcf", GAVIN_APP, separator, jobIdentifier, separator));
		this.exacOutputFile = fileStore
				.getFile(format("{0}{1}{2}{3}temp-exac.vcf", GAVIN_APP, separator, jobIdentifier, separator));
		this.snpeffOutputFile = fileStore
				.getFile(format("{0}{1}{2}{3}temp-snpeff.vcf", GAVIN_APP, separator, jobIdentifier, separator));
		this.gavinOutputFile = fileStore
				.getFile(format("{0}{1}{2}{3}gavin-result.vcf", GAVIN_APP, separator, jobIdentifier, separator));
	}

	@Override
	public Void call(Progress progress) throws Exception
	{
		progress.setProgressMax(4);

		progress.progress(0, "Annotating with cadd...");
		cmdLineAnnotator.annotate(cadd, inputFile, caddOutputFile, emptyList(), false);

		progress.progress(1, "Annotating with exac...");
		cmdLineAnnotator.annotate(exac, caddOutputFile, exacOutputFile, emptyList(), false);

		progress.progress(2, "Annotating with snpEff...");
		cmdLineAnnotator.annotate(snpeff, exacOutputFile, snpeffOutputFile, emptyList(), false);

		progress.progress(3, "Annotating with gavin...");
		cmdLineAnnotator.annotate(gavin, snpeffOutputFile, gavinOutputFile, emptyList(), false);

		progress.progress(4, "Result is ready for download.");
		String path = menuReaderService.getMenu().findMenuItemPath(GAVIN_APP);
		progress.setResultUrl(format("{0}/result/{1}", path, jobIdentifier));

		return null;
	}
}
