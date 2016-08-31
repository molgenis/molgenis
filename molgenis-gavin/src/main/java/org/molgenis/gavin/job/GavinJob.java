package org.molgenis.gavin.job;

import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
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
	private final AnnotatorUtils annotatorUtils;

	VcfAttributes vcfAttributes;
	VcfUtils vcfUtils;
	EntityMetaDataFactory entityMetaDataFactory;
	AttributeMetaDataFactory attributeMetaDataFactory;

	public GavinJob(Progress progress, TransactionTemplate transactionTemplate, Authentication authentication,
			String jobIdentifier, FileStore fileStore, MenuReaderService menuReaderService, RepositoryAnnotator cadd,
			RepositoryAnnotator exac, RepositoryAnnotator snpeff, RepositoryAnnotator gavin,
			VcfAttributes vcfAttributes, VcfUtils vcfUtils, EntityMetaDataFactory entityMetaDataFactory,
			AttributeMetaDataFactory attributeMetaDataFactory, AnnotatorUtils annotatorUtils)
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
		this.attributeMetaDataFactory = attributeMetaDataFactory;
		this.annotatorUtils = annotatorUtils;

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
		annotatorUtils
				.annotate(cadd, vcfAttributes, entityMetaDataFactory, attributeMetaDataFactory, vcfUtils, inputFile,
						caddOutputFile, emptyList(), true);

		progress.progress(1, "Annotating with exac...");
		annotatorUtils.annotate(exac, vcfAttributes, entityMetaDataFactory, attributeMetaDataFactory, vcfUtils,
				caddOutputFile, exacOutputFile, emptyList(), true);

		progress.progress(2, "Annotating with snpEff...");
		annotatorUtils.annotate(snpeff, vcfAttributes, entityMetaDataFactory, attributeMetaDataFactory, vcfUtils,
				exacOutputFile, snpeffOutputFile, emptyList(), false);

		progress.progress(3, "Annotating with gavin...");
		annotatorUtils.annotate(gavin, vcfAttributes, entityMetaDataFactory, attributeMetaDataFactory, vcfUtils,
				snpeffOutputFile, gavinOutputFile, emptyList(), false);

		progress.progress(4, "Result is ready for download.");
		String path = menuReaderService.getMenu().findMenuItemPath(GAVIN_APP);
		progress.setResultUrl(format("{0}/result/{1}", path, jobIdentifier));

		return null;
	}
}
