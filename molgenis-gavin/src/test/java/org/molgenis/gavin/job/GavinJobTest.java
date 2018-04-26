package org.molgenis.gavin.job;

import com.google.common.collect.ImmutableMultiset;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.utils.EffectStructureConverter;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.gavin.job.input.Parser;
import org.molgenis.gavin.job.input.model.LineType;
import org.molgenis.jobs.Progress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;

import static java.io.File.separator;
import static org.mockito.Mockito.*;
import static org.molgenis.gavin.controller.GavinController.GAVIN_APP;
import static org.molgenis.gavin.job.input.model.LineType.*;

@ContextConfiguration(classes = { GavinJobTest.Config.class })
public class GavinJobTest extends AbstractMolgenisSpringTest
{
	private GavinJob job;

	@Autowired
	VcfAttributes vcfAttributes;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	AttributeFactory attributeFactory;

	@Mock
	private Progress progress;
	@Mock
	private TransactionTemplate transactionTemplate;
	@Mock
	private Authentication authentication;
	@Mock
	private FileStore fileStore;
	@Mock
	private MenuReaderService menuReaderService;
	@Mock
	private RepositoryAnnotator cadd;
	@Mock
	private RepositoryAnnotator exac;
	@Mock
	private RepositoryAnnotator snpeff;
	@Mock
	private RepositoryAnnotator gavin;
	@Mock
	private Parser parser;
	@Mock
	private Menu menu;
	@Mock
	EffectStructureConverter effectStructureConverter;

	@Mock
	private File inputFile;
	@Mock
	private File processedInputFile;
	@Mock
	private File errorFile;
	@Mock
	private File caddResult;
	@Mock
	private File exacResult;
	@Mock
	private File snpEffResult;
	@Mock
	private File gavinResult;
	@Mock
	private AnnotatorRunner annotatorRunner;
	@Mock
	private GavinJobExecution gavinJobExecution;

	public GavinJobTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		reset(progress, annotatorRunner);

		when(menuReaderService.getMenu()).thenReturn(menu);
		when(menu.findMenuItemPath(GAVIN_APP)).thenReturn("/menu/plugins/gavin-app");

		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "input.tsv")).thenReturn(inputFile);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "temp-processed-input.vcf")).thenReturn(
				processedInputFile);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "error.txt")).thenReturn(errorFile);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "temp-cadd.vcf")).thenReturn(caddResult);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "temp-exac.vcf")).thenReturn(exacResult);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "temp-snpeff.vcf")).thenReturn(
				snpEffResult);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "gavin-result.vcf")).thenReturn(
				gavinResult);

		Iterator<Entity> iterator = Collections.<Entity>emptyList().iterator();
		when(cadd.annotate(any(), eq(true))).thenReturn(iterator);
		when(exac.annotate(any(), eq(true))).thenReturn(iterator);
		when(snpeff.annotate(any(), eq(false))).thenReturn(iterator);
		when(gavin.annotate(any(), eq(false))).thenReturn(iterator);
		when(gavinJobExecution.getIdentifier()).thenReturn("ABCDE");
		when(gavinJobExecution.getInputFileExtension()).thenReturn("tsv");
		when(effectStructureConverter.createVcfEntityStructure(any())).thenReturn(iterator);

		job = new GavinJob(progress, transactionTemplate, authentication, "ABCDE", fileStore, menuReaderService, cadd,
				exac, snpeff, gavin, parser, annotatorRunner, gavinJobExecution);
	}

	@Test
	public void testRunHappyPathVcf() throws Exception
	{
		final ImmutableMultiset<LineType> lineTypes = ImmutableMultiset.of(COMMENT, COMMENT, VCF, VCF);
		when(parser.tryTransform(inputFile, processedInputFile, errorFile)).thenReturn(lineTypes);

		job.call(progress);

		verify(progress).setProgressMax(5);
		verify(progress).progress(0, "Preprocessing input file...");

		verify(progress).progress(1, "Annotating with cadd...");
		verify(annotatorRunner).runAnnotator(cadd, processedInputFile, caddResult, true);

		verify(progress).progress(2, "Annotating with exac...");
		verify(annotatorRunner).runAnnotator(exac, caddResult, exacResult, true);

		verify(progress).progress(3, "Annotating with snpEff...");
		verify(annotatorRunner).runAnnotator(snpeff, exacResult, snpEffResult, false);

		verify(progress).progress(4, "Annotating with gavin...");
		verify(annotatorRunner).runAnnotator(gavin, snpEffResult, gavinResult, false);

		verify(progress).progress(5, "Result is ready for download.");
		verify(progress).setResultUrl("/menu/plugins/gavin-app/result/ABCDE");

		verify(gavinJobExecution).setLineTypes(lineTypes);
	}

	@Test
	public void testRunHappyPathCadd() throws Exception
	{
		when(parser.tryTransform(inputFile, processedInputFile, errorFile)).thenReturn(
				ImmutableMultiset.of(COMMENT, COMMENT, CADD, CADD));

		job.call(progress);

		verify(progress).setProgressMax(5);
		verify(progress).progress(0, "Preprocessing input file...");
		verify(progress).status(
				"Parsed input file. Found 4 lines (2 comments, 0 valid VCF, 2 valid CADD, 0 errors, 0 indels without CADD score, 0 skipped)");

		verify(progress).progress(1, "File already annotated by cadd, skipping cadd annotation.");

		verify(progress).progress(2, "Annotating with exac...");
		verify(annotatorRunner).runAnnotator(exac, processedInputFile, exacResult, true);

		verify(progress).progress(3, "Annotating with snpEff...");
		verify(annotatorRunner).runAnnotator(snpeff, exacResult, snpEffResult, false);

		verify(progress).progress(4, "Annotating with gavin...");
		verify(annotatorRunner).runAnnotator(gavin, snpEffResult, gavinResult, false);

		verify(progress).progress(5, "Result is ready for download.");
		verify(progress).setResultUrl("/menu/plugins/gavin-app/result/ABCDE");
	}

	@Test(expectedExceptions = {
			MolgenisDataException.class }, expectedExceptionsMessageRegExp = "Input file contains too many lines\\. Maximum is 100,000\\.")
	public void testSkippedThrowsException() throws Exception
	{
		when(parser.tryTransform(inputFile, processedInputFile, errorFile)).thenReturn(
				ImmutableMultiset.of(COMMENT, COMMENT, CADD, VCF, SKIPPED));

		job.call(progress);
	}

	@Test(expectedExceptions = {
			MolgenisDataException.class }, expectedExceptionsMessageRegExp = "Not a single valid variant line found\\.")
	public void testNoValidLinesThrowsException() throws Exception
	{
		when(parser.tryTransform(inputFile, processedInputFile, errorFile)).thenReturn(ImmutableMultiset.of());

		job.call(progress);
	}

	@Test(expectedExceptions = {
			MolgenisDataException.class }, expectedExceptionsMessageRegExp = "Input file contains mixed line types\\. Please use one type only, either VCF or CADD.")
	public void testMixedInputsThrowsException() throws Exception
	{
		when(parser.tryTransform(inputFile, processedInputFile, errorFile)).thenReturn(
				ImmutableMultiset.of(COMMENT, COMMENT, CADD, VCF));

		job.call(progress);
	}

	@Configuration
	@Import(VcfTestConfig.class)
	public static class Config
	{
	}
}
