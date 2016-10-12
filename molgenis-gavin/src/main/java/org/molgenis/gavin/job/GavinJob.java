package org.molgenis.gavin.job;

import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.file.FileStore;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.IOException;

import static java.io.File.separator;
import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;
import static org.molgenis.gavin.controller.GavinController.GAVIN_APP;

public class GavinJob extends Job<Void>
{
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

	private final VcfAttributes vcfAttributes;
	private final VcfUtils vcfUtils;
	private final EntityMetaDataFactory entityMetaDataFactory;
	private final AttributeFactory attributeFactory;

	public GavinJob(Progress progress, TransactionTemplate transactionTemplate, Authentication authentication,
			String jobIdentifier, FileStore fileStore, MenuReaderService menuReaderService, RepositoryAnnotator cadd,
			RepositoryAnnotator exac, RepositoryAnnotator snpeff, RepositoryAnnotator gavin,
			VcfAttributes vcfAttributes, VcfUtils vcfUtils, EntityMetaDataFactory entityMetaDataFactory,
			AttributeFactory attributeFactory)
	{
		super(progress, transactionTemplate, authentication);
		this.jobIdentifier = jobIdentifier;
		this.menuReaderService = menuReaderService;
		this.cadd = cadd;
		this.exac = exac;
		this.snpeff = snpeff;
		this.gavin = gavin;
		this.vcfAttributes = vcfAttributes;
		this.vcfUtils = vcfUtils;
		this.entityMetaDataFactory = entityMetaDataFactory;
		this.attributeFactory = attributeFactory;

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
		runAnnotator(cadd, inputFile, caddOutputFile, true);

		progress.progress(1, "Annotating with exac...");
		runAnnotator(exac, caddOutputFile, exacOutputFile, true);

		progress.progress(2, "Annotating with snpEff...");
		runAnnotator(snpeff, exacOutputFile, snpeffOutputFile, false);

		progress.progress(3, "Annotating with gavin...");
		runAnnotator(gavin, snpeffOutputFile, gavinOutputFile, false);

		progress.progress(4, "Result is ready for download.");
		String path = menuReaderService.getMenu().findMenuItemPath(GAVIN_APP);
		progress.setResultUrl(format("{0}/result/{1}", path, jobIdentifier));

		return null;
	}

	public void runAnnotator(RepositoryAnnotator annotator, File inputFile, File outputFile, boolean update)
			throws IOException, MolgenisInvalidFormatException
	{
		AnnotatorUtils.annotate(annotator, vcfAttributes, entityMetaDataFactory, attributeFactory, vcfUtils,
				inputFile, outputFile, emptyList(), update);
	}
}
