package org.molgenis.gavin.job;

import org.mockito.Mock;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.CmdLineAnnotator;
import org.molgenis.data.jobs.Progress;
import org.molgenis.file.FileStore;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

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
	private CmdLineAnnotator cmdLineAnnotator;
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

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(menu.findMenuItemPath(GAVIN_APP)).thenReturn("/menu/plugins/gavin-app");

		when(fileStore.getFile("gavin-app/ABCDE/input.vcf")).thenReturn(inputFile);
		when(fileStore.getFile("gavin-app/ABCDE/temp-cadd.vcf")).thenReturn(caddResult);
		when(fileStore.getFile("gavin-app/ABCDE/temp-exac.vcf")).thenReturn(exacResult);
		when(fileStore.getFile("gavin-app/ABCDE/temp-snpeff.vcf")).thenReturn(snpEffResult);
		when(fileStore.getFile("gavin-app/ABCDE/gavin-result.vcf")).thenReturn(gavinResult);

		job = new GavinJob(cmdLineAnnotator, progress, transactionTemplate, authentication, "ABCDE", fileStore,
				menuReaderService, cadd, exac, snpeff, gavin);
	}

	@Test
	public void testRunHappyPath() throws Exception
	{
		job.call(progress);

		verify(progress).setProgressMax(4);
		verify(progress).progress(0, "Annotating with cadd...");
		verify(cmdLineAnnotator).annotate(cadd, inputFile, caddResult, emptyList(), false);
		verify(progress).progress(1, "Annotating with exac...");
		verify(cmdLineAnnotator).annotate(exac, caddResult, exacResult, emptyList(), false);
		verify(progress).progress(2, "Annotating with snpEff...");
		verify(cmdLineAnnotator).annotate(snpeff, exacResult, snpEffResult, emptyList(), false);
		verify(progress).progress(3, "Annotating with gavin...");
		verify(cmdLineAnnotator).annotate(gavin, snpEffResult, gavinResult, emptyList(), false);
		verify(progress).progress(4, "Result is ready for download.");
		verify(progress).setResultUrl("/menu/plugins/gavin-app/result/ABCDE");

	}

	@Test
	public void testRunCaddFails() throws Exception
	{
		RuntimeException ex = new RuntimeException();
		doThrow(ex).when(cmdLineAnnotator).annotate(cadd, inputFile, caddResult, emptyList(), false);
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
