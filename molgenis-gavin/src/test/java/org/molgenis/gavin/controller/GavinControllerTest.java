package org.molgenis.gavin.controller;

import com.google.common.collect.ImmutableMap;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.file.FileStore;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.gavin.job.GavinJob;
import org.molgenis.gavin.job.GavinJobExecution;
import org.molgenis.gavin.job.GavinJobFactory;
import org.molgenis.security.user.UserAccountService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import static java.io.File.separator;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.gavin.job.GavinJobExecutionMetaData.GAVIN_JOB_EXECUTION;
import static org.testng.Assert.assertEquals;

public class GavinControllerTest
{
	@InjectMocks
	private GavinController gavinController;

	@Mock
	private DataService dataService;

	@Mock
	private ExecutorService executorService;

	@Mock
	private GavinJobFactory gavinJobFactory;

	@Mock
	private FileStore fileStore;

	@Mock
	private UserAccountService userAccountService;

	@Mock
	private MolgenisPluginRegistry pluginRegistry;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);
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

		// Job Factory sets the Identifier in the JobExecution object.
		ArgumentCaptor<GavinJobExecution> captor = ArgumentCaptor.forClass(GavinJobExecution.class);
		when(userAccountService.getCurrentUser()).thenReturn(user);
		doAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			((GavinJobExecution) args[0]).setIdentifier("ABCDE");
			return job;
		}).when(gavinJobFactory).createJob(captor.capture());

		when(inputFile.getParentFile()).thenReturn(parentDir);
		when(fileStore.getFile("gavin-app" + separator + "ABCDE" + separator + "input.vcf")).thenReturn(inputFile);

		assertEquals(gavinController.annotateFile(vcf, "annotate-file"), "/api/v2/GavinJobExecution/ABCDE");

		verify(vcf).transferTo(inputFile);
		verify(fileStore).createDirectory("gavin-app");
		verify(fileStore).createDirectory("gavin-app" + separator + "ABCDE");
		verify(executorService).submit(job);
		GavinJobExecution jobExecution = captor.getValue();
		assertEquals(jobExecution.getFilename(), "annotate-file-gavin.vcf");
		assertEquals(jobExecution.getUser(), user);
	}

	@Test
	public void testResult() throws Exception
	{
		GavinJobExecution gavinJobExecution = mock(GavinJobExecution.class);
		File resultFile = File.createTempFile("gavin", ".vcf", new File("target"));
		resultFile.deleteOnExit();

		when(gavinJobExecution.getFilename()).thenReturn("annotate-file-gavin.vcf");
		when(dataService.findOne(GAVIN_JOB_EXECUTION, "ABCDE", GavinJobExecution.class)).thenReturn(gavinJobExecution);
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
		when(dataService.findOne(GAVIN_JOB_EXECUTION, "ABCDE", GavinJobExecution.class)).thenReturn(gavinJobExecution);
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

}