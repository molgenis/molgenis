package org.molgenis.data.jobs;

import static org.testng.Assert.assertEquals;

import java.util.Date;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.support.DataServiceImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JobTest
{
	private DataService dataService;
	private Job job;

	@BeforeClass
	public void beforeClass()
	{
		dataService = new DataServiceImpl();
		job = new Job(dataService);
	}

	@Test
	public void testGetAndSetIdentifier()
	{
		job.setIdentifier("AAAABDASDFR");
		assertEquals(job.getIdentifier(), "AAAABDASDFR");
	}

	@Test
	public void testGetAndSetUser()
	{
		MolgenisUser me = new MolgenisUser();
		me.setUsername("fdlk");
		job.setUser(me);
		assertEquals(job.getUser(), me);
	}

	@Test
	public void testGetAndSetStatus()
	{
		job.setStatus(Job.Status.PENDING);
		assertEquals(job.getStatus(), Job.Status.PENDING);
	}

	@Test
	public void testGetAndSetType()
	{
		job.setType("AnnotatorJob");
		assertEquals(job.getType(), "AnnotatorJob");
	}

	@Test
	public void testGetAndSetSubmissionDate()
	{
		Date date = new Date();
		job.setSubmissionDate(date);
		assertEquals(job.getSubmissionDate(), date);
	}

	@Test
	public void testGetAndSetStartDate()
	{
		Date date = new Date();
		job.setStartDate(date);
		assertEquals(job.getStartDate(), date);
	}

	@Test
	public void testGetAndSetEndDate()
	{
		Date date = new Date();
		job.setEndDate(date);
		assertEquals(job.getEndDate(), date);
	}

	@Test
	public void testGetAndSetProgressInt()
	{
		job.setProgressInt(50);
		assertEquals(job.getProgressInt(), new Integer(50));
	}

	@Test
	public void testGetAndSetProgressMessage()
	{
		job.setProgressMessage(
				"Annotating with SnpEff, Exac, CADD, Clinvar, Dann, CGD, Fitcon, GoNL, HPO, and many more.... %p");
		assertEquals(job.getProgressMessage(),
				"Annotating with SnpEff, Exac, CADD, Clinvar, Dann, CGD, Fitcon, GoNL, HPO, and many more.... %p");
	}

	@Test
	public void testGetAndSetProgressMax()
	{
		job.setProgressMax(1032);
		assertEquals(job.getProgressMax(), new Integer(1032));
	}

	@Test
	public void testGetMetaData()
	{
		assertEquals(Job.META_DATA, Job.getMetaData());
	}
}
