package org.molgenis.gavin.job;

import static java.io.File.separator;
import static java.util.Collections.emptyList;

import java.io.File;

import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.CmdLineAnnotator;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.file.FileStore;
import org.molgenis.gavin.controller.GavinController;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

public class GavinJob extends Job<Void>
{
	private String jobIdentifier;
	private FileStore fileStore;
	private MenuReaderService menuReaderService;

	private RepositoryAnnotator cadd;
	private RepositoryAnnotator exac;
	private RepositoryAnnotator snpeff;
	private RepositoryAnnotator gavin;

	private File caddOutputFile;
	private File exacOutputFile;
	private File snpeffOutputFile;
	private File gavinOutputFile;

	public GavinJob(Progress progress, TransactionTemplate transactionTemplate, Authentication authentication,
			String jobIdentifier, FileStore fileStore, MenuReaderService menuReaderService, RepositoryAnnotator cadd,
			RepositoryAnnotator exac, RepositoryAnnotator snpeff, RepositoryAnnotator gavin)
	{
		super(progress, transactionTemplate, authentication);
		this.jobIdentifier = jobIdentifier;
		this.fileStore = fileStore;
		this.menuReaderService = menuReaderService;
		this.cadd = cadd;
		this.exac = exac;
		this.snpeff = snpeff;
		this.gavin = gavin;

		this.caddOutputFile = fileStore.getFile("gavin" + separator + jobIdentifier + separator + "temp-cadd.vcf");
		this.exacOutputFile = fileStore.getFile("gavin" + separator + jobIdentifier + separator + "temp-exac.vcf");
		this.snpeffOutputFile = fileStore.getFile("gavin" + separator + jobIdentifier + separator + "temp-snpeff.vcf");
		this.gavinOutputFile = fileStore.getFile("gavin" + separator + jobIdentifier + separator + "gavin-result.vcf");
	}

	@Override
	public Void call(Progress progress) throws Exception
	{
		progress.setProgressMax(4);
		File inputFile = fileStore.getFile("gavin" + separator + jobIdentifier + separator + "input.vcf");
		CmdLineAnnotator cmdLineAnnotator = new CmdLineAnnotator();

		progress.progress(0, "Annotating with cadd...");
		cmdLineAnnotator.annotate(cadd, inputFile, caddOutputFile, emptyList(), false);

		progress.progress(1, "Annotating with exac...");
		cmdLineAnnotator.annotate(exac, caddOutputFile, exacOutputFile, emptyList(), false);

		progress.progress(2, "Annotating with snpEff...");
		cmdLineAnnotator.annotate(snpeff, exacOutputFile, snpeffOutputFile, emptyList(), false);

		// TODO maybe use vcf validator on the result vcf
		progress.progress(3, "Annotating with gavin...");
		cmdLineAnnotator.annotate(gavin, snpeffOutputFile, gavinOutputFile, emptyList(), false);

		progress.progress(4, "Result is ready for download.");
		progress.setResultUrl("/gavin/result/" + jobIdentifier);

		return null;
	}
}
