package org.molgenis.gavin.job;

import com.google.common.collect.Multiset;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.jobs.TransactionalJob;
import org.molgenis.file.FileStore;
import org.molgenis.gavin.job.input.Parser;
import org.molgenis.gavin.job.input.model.LineType;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.util.Arrays;

import static java.io.File.separator;
import static java.text.MessageFormat.format;
import static org.molgenis.gavin.controller.GavinController.GAVIN_APP;
import static org.molgenis.gavin.job.input.model.LineType.*;

public class GavinJob extends TransactionalJob<Void>
{
	private final String jobIdentifier;
	private final MenuReaderService menuReaderService;
	private final FileStore fileStore;

	private final RepositoryAnnotator cadd;
	private final RepositoryAnnotator exac;
	private final RepositoryAnnotator snpeff;
	private final RepositoryAnnotator gavin;

	private final File inputFile;
	private final File processedInputFile;
	private final File errorFile;
	private final File caddOutputFile;
	private final File exacOutputFile;
	private final File snpeffOutputFile;
	private final File gavinOutputFile;

	private final Parser parser;
	private final AnnotatorRunner annotatorRunner;
	private final GavinJobExecution gavinJobExecution;

	public GavinJob(Progress progress, TransactionTemplate transactionTemplate, Authentication authentication,
			String jobIdentifier, FileStore fileStore, MenuReaderService menuReaderService, RepositoryAnnotator cadd,
			RepositoryAnnotator exac, RepositoryAnnotator snpeff, RepositoryAnnotator gavin, Parser parser,
			AnnotatorRunner annotatorRunner, GavinJobExecution gavinJobExecution)
	{
		super(progress, transactionTemplate, authentication);
		this.fileStore = fileStore;
		this.jobIdentifier = jobIdentifier;
		this.menuReaderService = menuReaderService;
		this.cadd = cadd;
		this.exac = exac;
		this.snpeff = snpeff;
		this.gavin = gavin;
		this.annotatorRunner = annotatorRunner;
		this.parser = parser;
		this.gavinJobExecution = gavinJobExecution;

		inputFile = getFile("input", gavinJobExecution.getInputFileExtension());
		processedInputFile = getFile("temp-processed-input");
		errorFile = getFile("error", "txt");
		caddOutputFile = getFile("temp-cadd");
		exacOutputFile = getFile("temp-exac");
		snpeffOutputFile = getFile("temp-snpeff");
		gavinOutputFile = getFile("gavin-result");
	}

	private File getFile(String name)
	{
		return getFile(name, "vcf");
	}

	private File getFile(String name, String extension)
	{
		return fileStore.getFile(
				format("{0}{1}{2}{3}{4}.{5}", GAVIN_APP, separator, jobIdentifier, separator, name, extension));
	}

	@Override
	public Void call(Progress progress) throws Exception
	{
		progress.setProgressMax(5);

		progress.progress(0, "Preprocessing input file...");
		Multiset<LineType> lineTypes = parser.tryTransform(inputFile, processedInputFile, errorFile);
		progress.status(format("Parsed input file. Found {0} lines ({1} comments, {2} valid VCF, {3} valid CADD, "
						+ "{4} errors, {5} indels without CADD score, {6} skipped)", lineTypes.size(), lineTypes.count(COMMENT),
				lineTypes.count(VCF), lineTypes.count(CADD), lineTypes.count(ERROR), lineTypes.count(INDEL_NOCADD),
				lineTypes.count(SKIPPED)));
		gavinJobExecution.setLineTypes(lineTypes);
		if (lineTypes.contains(SKIPPED))
		{
			throw new MolgenisDataException(
					format("Input file contains too many lines. Maximum is {0}.", Parser.MAX_LINES));
		}
		if (lineTypes.containsAll(Arrays.asList(CADD, VCF)))
		{
			throw new MolgenisDataException(
					"Input file contains mixed line types. Please use one type only, either VCF or CADD.");
		}

		if (!lineTypes.contains(CADD) && !lineTypes.contains(VCF))
		{
			throw new MolgenisDataException("Not a single valid variant line found.");
		}

		File exacInputFile = processedInputFile;
		if (!lineTypes.contains(CADD))
		{
			progress.progress(1, "Annotating with cadd...");
			annotatorRunner.runAnnotator(cadd, processedInputFile, caddOutputFile, true);
			exacInputFile = caddOutputFile;
		}
		else
		{
			progress.progress(1, "File already annotated by cadd, skipping cadd annotation.");
		}

		progress.progress(2, "Annotating with exac...");
		annotatorRunner.runAnnotator(exac, exacInputFile, exacOutputFile, true);

		progress.progress(3, "Annotating with snpEff...");
		annotatorRunner.runAnnotator(snpeff, exacOutputFile, snpeffOutputFile, false);

		progress.progress(4, "Annotating with gavin...");
		annotatorRunner.runAnnotator(gavin, snpeffOutputFile, gavinOutputFile, false);

		progress.progress(5, "Result is ready for download.");
		String path = menuReaderService.getMenu().findMenuItemPath(GAVIN_APP);
		//TODO Filter
		//TODO write to database
		//TODO result -> GeneNetwork
		//TODO VCF pipe aware import
		progress.setResultUrl(format("{0}/result/{1}", path, jobIdentifier));

		return null;
	}

}
