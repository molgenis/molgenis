package org.molgenis.gavin.job;

import org.mockito.Mock;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.file.FileStore;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static java.io.File.separator;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.gavin.controller.GavinController.GAVIN_APP;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

public class GavinJobTest
{
	private GavinJob job;

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
	private Menu menu;
	@Mock
	private File inputFile;
	@Mock
	private File caddResult;
	@Mock
	private File exacResult;
	@Mock
	private File snpEffResult;
	@Mock
	private File gavinResult;
	@Mock
	private VcfAttributes vcfAttributes;
	@Mock
	private VcfUtils vcfUtils;
	@Mock
	private EntityMetaDataFactory entityMetaDataFactory;
	@Mock
	private AttributeMetaDataFactory attributeMetaDataFactory;
	@Mock
	private AnnotatorUtils annotatorUtils;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(menu.findMenuItemPath(GAVIN_APP)).thenReturn("/menu/plugins/gavin-app");

		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "input.vcf")).thenReturn(inputFile);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "temp-cadd.vcf")).thenReturn(caddResult);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "temp-exac.vcf")).thenReturn(exacResult);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "temp-snpeff.vcf"))
				.thenReturn(snpEffResult);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "gavin-result.vcf"))
				.thenReturn(gavinResult);

		job = new GavinJob(progress, transactionTemplate, authentication, "ABCDE", fileStore, menuReaderService, cadd,
				exac, snpeff, gavin, vcfAttributes, vcfUtils, entityMetaDataFactory, attributeMetaDataFactory,
				annotatorUtils);
	}

	@Test
	public void testRunHappyPath() throws Exception
	{
		job.call(progress);

		verify(progress).setProgressMax(4);
		verify(progress).progress(0, "Annotating with cadd...");
		verify(annotatorUtils)
				.annotate(cadd, vcfAttributes, entityMetaDataFactory, attributeMetaDataFactory, vcfUtils, inputFile,
						caddResult, emptyList(), true);
		verify(progress).progress(1, "Annotating with exac...");
		verify(annotatorUtils)
				.annotate(exac, vcfAttributes, entityMetaDataFactory, attributeMetaDataFactory, vcfUtils, caddResult,
						exacResult, emptyList(), true);
		verify(progress).progress(2, "Annotating with snpEff...");
		verify(annotatorUtils)
				.annotate(snpeff, vcfAttributes, entityMetaDataFactory, attributeMetaDataFactory, vcfUtils, exacResult,
						snpEffResult, emptyList(), false);
		verify(progress).progress(3, "Annotating with gavin...");
		verify(annotatorUtils)
				.annotate(gavin, vcfAttributes, entityMetaDataFactory, attributeMetaDataFactory, vcfUtils, snpEffResult,
						gavinResult, emptyList(), false);
		verify(progress).progress(4, "Result is ready for download.");
		verify(progress).setResultUrl("/menu/plugins/gavin-app/result/ABCDE");

	}

	@Test
	public void testRunCaddFails() throws Exception
	{
		RuntimeException ex = new RuntimeException();
		doThrow(ex).when(annotatorUtils)
				.annotate(cadd, vcfAttributes, entityMetaDataFactory, attributeMetaDataFactory, vcfUtils, inputFile,
						caddResult, emptyList(), true);
		try
		{
			job.call(progress);
			fail("Should throw exception if one of the annotators fails");
		}
		catch (Exception expected)
		{
			assertSame(ex, expected);
		}

		verify(progress).setProgressMax(4);
		verify(progress).progress(0, "Annotating with cadd...");

		verify(progress, never()).setResultUrl(any());
	}
}
