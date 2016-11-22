package org.molgenis.gavin.job;

import org.mockito.Mock;
import org.molgenis.annotation.cmd.conversion.EffectStructureConverter;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.file.FileStore;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;

import static java.io.File.separator;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.gavin.controller.GavinController.GAVIN_APP;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

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
	private Menu menu;
	@Mock
	EffectStructureConverter effectStructureConverter;

	private File inputFile;
	private File caddResult;
	private File exacResult;
	private File snpEffResult;
	private File gavinResult;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);
		when(menuReaderService.getMenu()).thenReturn(menu);
		when(menu.findMenuItemPath(GAVIN_APP)).thenReturn("/menu/plugins/gavin-app");

		inputFile = ResourceUtils.getFile(getClass(), "/input.vcf");
		caddResult = ResourceUtils.getFile(getClass(), "/cadd.vcf");
		;
		exacResult = ResourceUtils.getFile(getClass(), "/exac.vcf");
		;
		snpEffResult = ResourceUtils.getFile(getClass(), "/snpeff.vcf");
		;
		gavinResult = ResourceUtils.getFile(getClass(), "/gavin.vcf");
		;

		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "input.vcf")).thenReturn(inputFile);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "temp-cadd.vcf")).thenReturn(caddResult);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "temp-exac.vcf")).thenReturn(exacResult);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "temp-snpeff.vcf"))
				.thenReturn(snpEffResult);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "gavin-result.vcf"))
				.thenReturn(gavinResult);
		Iterator iterator = Collections.emptyList().iterator();
		when(cadd.annotate(anyObject(), eq(true))).thenReturn(iterator);
		when(exac.annotate(anyObject(), eq(true))).thenReturn(iterator);
		when(snpeff.annotate(anyObject(), eq(false))).thenReturn(iterator);
		when(gavin.annotate(anyObject(), eq(false))).thenReturn(iterator);
		when(effectStructureConverter.createVcfEntityStructure(anyObject())).thenReturn(iterator);

		job = new GavinJob(progress, transactionTemplate, authentication, "ABCDE", fileStore, menuReaderService, cadd,
				exac, snpeff, gavin, vcfAttributes, effectStructureConverter, entityTypeFactory, attributeFactory);
	}

	@Test
	public void testRunHappyPath() throws Exception
	{
		job.call(progress);

		verify(progress).setProgressMax(4);
		verify(progress).progress(0, "Annotating with cadd...");
		verify(cadd).annotate(anyObject(), eq(true));
		verify(progress).progress(1, "Annotating with exac...");
		verify(exac).annotate(anyObject(), eq(true));
		verify(progress).progress(2, "Annotating with snpEff...");
		verify(snpeff).annotate(anyObject(), eq(false));
		verify(progress).progress(3, "Annotating with gavin...");
		verify(gavin).annotate(anyObject(), eq(false));
		verify(progress).progress(4, "Result is ready for download.");
		verify(progress).setResultUrl("/menu/plugins/gavin-app/result/ABCDE");
	}

	@Test
	public void testRunCaddFails() throws Exception
	{
		RuntimeException expected = new RuntimeException();
		try
		{
			job.runAnnotator(new FailingAnnotator(expected), inputFile, caddResult, true);
			fail("Should throw exception if the annotator fails");
		}
		catch (Exception actual)
		{
			assertSame(actual, expected);
		}
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.vcf.model", "org.molgenis.data.vcf.utils" })
	public static class Config
	{
	}
}
