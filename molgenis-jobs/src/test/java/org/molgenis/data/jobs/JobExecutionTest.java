package org.molgenis.data.jobs;

import static org.testng.Assert.assertEquals;

import java.util.Date;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.support.DataServiceImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JobExecutionTest
{
	private DataService dataService;
	private JobExecution jobExecution;

	@BeforeClass
	public void beforeClass()
	{
		dataService = new DataServiceImpl();
		jobExecution = new JobExecution(dataService);
	}

	@Test
	public void testGetAndSetIdentifier()
	{
		jobExecution.setIdentifier("AAAABDASDFR");
		assertEquals(jobExecution.getIdentifier(), "AAAABDASDFR");
	}

	@Test
	public void testGetAndSetUser()
	{
		MolgenisUser me = new MolgenisUser();
		me.setUsername("fdlk");
		jobExecution.setUser(me);
		assertEquals(jobExecution.getUser(), me);
	}

	@Test
	public void testGetAndSetStatus()
	{
		jobExecution.setStatus(JobExecution.Status.PENDING);
		assertEquals(jobExecution.getStatus(), JobExecution.Status.PENDING);
	}

	@Test
	public void testGetAndSetType()
	{
		jobExecution.setType("AnnotatorJob");
		assertEquals(jobExecution.getType(), "AnnotatorJob");
	}

	@Test
	public void testGetAndSetSubmissionDate()
	{
		Date date = new Date();
		jobExecution.setSubmissionDate(date);
		assertEquals(jobExecution.getSubmissionDate(), date);
	}

	@Test
	public void testGetAndSetStartDate()
	{
		Date date = new Date();
		jobExecution.setStartDate(date);
		assertEquals(jobExecution.getStartDate(), date);
	}

	@Test
	public void testGetAndSetEndDate()
	{
		Date date = new Date();
		jobExecution.setEndDate(date);
		assertEquals(jobExecution.getEndDate(), date);
	}

	@Test
	public void testGetAndSetProgressInt()
	{
		jobExecution.setProgressInt(50);
		assertEquals(jobExecution.getProgressInt(), new Integer(50));
	}

	@Test
	public void testGetAndSetProgressMessage()
	{
		jobExecution.setProgressMessage(
				"Annotating with SnpEff, Exac, CADD, Clinvar, Dann, CGD, Fitcon, GoNL, HPO, and many more.... %p");
		assertEquals(jobExecution.getProgressMessage(),
				"Annotating with SnpEff, Exac, CADD, Clinvar, Dann, CGD, Fitcon, GoNL, HPO, and many more.... %p");
	}

	@Test
	public void testGetAndSetProgressMax()
	{
		jobExecution.setProgressMax(1032);
		assertEquals(jobExecution.getProgressMax(), new Integer(1032));
	}
	
	@Test
	public void testGetAndSetLog()
	{
		jobExecution.setLog("Log");
		assertEquals(jobExecution.getLog(), "Log");
	}
}
