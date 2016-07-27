package org.molgenis.gavin.controller;

import com.google.common.collect.ImmutableMap;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.EffectsAnnotator;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.web.CrudRepositoryAnnotator;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.reindex.meta.IndexPackage;
import org.molgenis.file.FileStore;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.gavin.job.GavinJob;
import org.molgenis.gavin.job.GavinJobExecution;
import org.molgenis.gavin.job.GavinJobFactory;
import org.molgenis.gavin.job.meta.GavinJobExecutionMetaData;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import static java.io.File.separator;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.GAVIN_JOB_EXECUTION;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { GavinControllerTest.Config.class, GavinController.class })
public class GavinControllerTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private GavinController gavinController;

	@Autowired
	private DataService dataService;

	@Autowired
	private ExecutorService executorService;

	@Autowired
	private GavinJobFactory gavinJobFactory;

	@Autowired
	private FileStore fileStore;

	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	GavinJobExecutionMetaData gavinJobExecutionMetaData;

	@BeforeClass
	public void init()
	{
		gavinJobExecutionMetaData.init();
	}

	@Test
	public void testInitResourcesPresent() throws Exception
	{
		Model model = new ExtendedModelMap();
		when(gavinJobFactory.getAnnotatorsWithMissingResources()).thenReturn(emptyList());

		gavinController.init(model);

		assertEquals(model.asMap(), Collections.emptyMap());
	}

	@Test
	public void testInitMissingResources() throws Exception
	{
		Model model = new ExtendedModelMap();
		when(gavinJobFactory.getAnnotatorsWithMissingResources()).thenReturn(singletonList("cadd"));

		gavinController.init(model);

		assertEquals(model.asMap(), ImmutableMap.of("annotatorsWithMissingResources", singletonList("cadd")));
	}

	@Test
	public void testAnnotateFile() throws Exception
	{
		MultipartFile vcf = mock(MultipartFile.class);
		GavinJob job = mock(GavinJob.class);
		File inputFile = mock(File.class);
		File parentDir = mock(File.class);
		MolgenisUser user = mock(MolgenisUser.class);
		when(user.getUsername()).thenReturn("tommy");

		// Job Factory sets the Identifier in the JobExecution object.
		ArgumentCaptor<GavinJobExecution> captor = ArgumentCaptor.forClass(GavinJobExecution.class);
		when(userAccountService.getCurrentUser()).thenReturn(user);
		doAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			((GavinJobExecution) args[0]).setIdentifier("ABCDE");
			return job;
		}).when(gavinJobFactory).createJob(captor.capture());

		when(inputFile.getParentFile()).thenReturn(parentDir);

		assertEquals(gavinController.annotateFile(vcf, "annotate-file"), "/api/v2/GavinJobExecution/ABCDE");

		verify(fileStore).createDirectory("gavin-app");
		verify(fileStore).createDirectory("gavin-app" + separator + "ABCDE");
		verify(fileStore).writeToFile(Mockito.any(InputStream.class),
				Mockito.eq("gavin-app" + separator + "ABCDE" + separator + "input.vcf"));

		verify(executorService).submit(job);
		GavinJobExecution jobExecution = captor.getValue();
		assertEquals(jobExecution.getFilename(), "annotate-file-gavin.vcf");
		assertEquals(jobExecution.getUser(), "tommy");
	}

	@Test
	public void testResult() throws Exception
	{
		GavinJobExecution gavinJobExecution = mock(GavinJobExecution.class);
		File resultFile = File
				.createTempFile("gavin", ".vcf", new File(ResourceUtils.getFile(getClass(), "/").getPath()));
		resultFile.deleteOnExit();

		when(gavinJobExecution.getFilename()).thenReturn("annotate-file-gavin.vcf");
		when(dataService.findOneById(GAVIN_JOB_EXECUTION, "ABCDE", GavinJobExecution.class))
				.thenReturn(gavinJobExecution);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "gavin-result.vcf"))
				.thenReturn(resultFile);

		HttpServletResponse response = mock(HttpServletResponse.class);

		assertEquals(gavinController.result(response, "ABCDE"), new FileSystemResource(resultFile));

		verify(response).setHeader("Content-Disposition", "inline; filename=\"annotate-file-gavin.vcf\"");
	}

	@Test
	public void testResultNotFound() throws Exception
	{
		GavinJobExecution gavinJobExecution = mock(GavinJobExecution.class);
		File file = mock(File.class);

		when(gavinJobExecution.getFilename()).thenReturn("annotate-file-gavin.vcf");
		when(dataService.findOneById(GAVIN_JOB_EXECUTION, "ABCDE", GavinJobExecution.class))
				.thenReturn(gavinJobExecution);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "gavin-result.vcf")).thenReturn(file);
		when(file.exists()).thenReturn(false);

		try
		{
			gavinController.result(mock(HttpServletResponse.class), "ABCDE");
			Assert.fail("Should throw exception cause file doesn't exist.");
		}
		catch (MolgenisDataException expected)
		{
			assertEquals(expected.getMessage(), "No output file found for this job.");
		}
	}

	@Test
	public void testCleanUp() throws Exception
	{
		gavinController.cleanUp();
		verify(fileStore).deleteDirectory("gavin-app");
	}

	@Configuration
	@ComponentScan({ "org.molgenis.gavin.job.meta", "org.molgenis.data.jobs.model" })
	public static class Config
	{
		@Bean
		IndexPackage indexPackage()
		{
			return mock(IndexPackage.class);
		}

		@Bean
		ExecutorService executorService()
		{
			return mock(ExecutorService.class);
		}

		@Bean
		GavinJobFactory gavinJobFactory()
		{
			return mock(GavinJobFactory.class);
		}

		@Bean
		UserAccountService userAccountService()
		{
			return mock(UserAccountService.class);
		}

		@Bean
		MolgenisPluginRegistry pluginRegistry()
		{
			return mock(MolgenisPluginRegistry.class);
		}

		@Bean
		PlatformTransactionManager platformTransactionManager()
		{
			return mock(PlatformTransactionManager.class);
		}

		@Bean
		CrudRepositoryAnnotator crudRepositoryAnnotator()
		{
			return mock(CrudRepositoryAnnotator.class);
		}

		@Bean
		DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		PlatformTransactionManager transactionManager()
		{
			return mock(PlatformTransactionManager.class);
		}

		@Bean
		UserDetailsService userDetailsService()
		{
			return mock(UserDetailsService.class);
		}

		@Bean
		JobExecutionUpdater jobExecutionUpdater()
		{
			return mock(JobExecutionUpdater.class);
		}

		@Bean
		MailSender mailSender()
		{
			return mock(MailSender.class);
		}

		@Bean
		FileStore fileStore()
		{
			return mock(FileStore.class);
		}

		@Bean
		RepositoryAnnotator cadd()
		{
			return mock(RepositoryAnnotator.class);
		}

		@Bean
		RepositoryAnnotator exac()
		{
			return mock(RepositoryAnnotator.class);
		}

		@Bean
		RepositoryAnnotator snpEff()
		{
			return mock(RepositoryAnnotator.class);
		}

		@Bean
		EffectsAnnotator gavin()
		{
			return mock(EffectsAnnotator.class);
		}

		@Bean
		MenuReaderService menuReaderService()
		{
			return mock(MenuReaderService.class);
		}
	}
}