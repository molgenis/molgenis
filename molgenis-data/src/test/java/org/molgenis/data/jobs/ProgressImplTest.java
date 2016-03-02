package org.molgenis.data.jobs;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import junit.framework.Assert;

public class ProgressImplTest
{
	private ProgressImpl progress;
	@Mock
	private JobExecutionUpdater updater;
	private JobExecution jobExecution;

	@BeforeClass
	public void beforeClass()
	{
		MockitoAnnotations.initMocks(this);
		jobExecution = new JobExecution(null);
		jobExecution.setIdentifier("ABCDE");
		progress = new ProgressImpl(jobExecution, updater);
	}

	@Test
	public void testLog()
	{
		progress.start();
		progress.status("Working....");
		progress.success();
		System.out.println(jobExecution.getLog());
		Assert.assertTrue(jobExecution.getLog().contains("INFO  - start ()\n"));
		Assert.assertTrue(jobExecution.getLog().contains("INFO  - Working....\n"));
		Assert.assertTrue(jobExecution.getLog().contains("INFO  - Execution successful. Time spent: "));
	}
}
