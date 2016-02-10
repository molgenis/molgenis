package org.molgenis.data.jobs;

import static org.testng.Assert.assertEquals;

import java.util.Date;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.support.DataServiceImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JobMetaDataTest
{
	private DataService dataService;
	private JobMetaData jobMetaData;

	@BeforeClass
	public void beforeClass()
	{
		dataService = new DataServiceImpl();
		jobMetaData = new JobMetaData(dataService);
	}

	@Test
	public void testGetAndSetIdentifier()
	{
		jobMetaData.setIdentifier("AAAABDASDFR");
		assertEquals(jobMetaData.getIdentifier(), "AAAABDASDFR");
	}

	@Test
	public void testGetAndSetUser()
	{
		MolgenisUser me = new MolgenisUser();
		me.setUsername("fdlk");
		jobMetaData.setUser(me);
		assertEquals(jobMetaData.getUser(), me);
	}

	@Test
	public void testGetAndSetStatus()
	{
		jobMetaData.setStatus(JobMetaData.Status.PENDING);
		assertEquals(jobMetaData.getStatus(), JobMetaData.Status.PENDING);
	}

	@Test
	public void testGetAndSetType()
	{
		jobMetaData.setType("AnnotatorJob");
		assertEquals(jobMetaData.getType(), "AnnotatorJob");
	}

	@Test
	public void testGetAndSetSubmissionDate()
	{
		Date date = new Date();
		jobMetaData.setSubmissionDate(date);
		assertEquals(jobMetaData.getSubmissionDate(), date);
	}

	@Test
	public void testGetAndSetStartDate()
	{
		Date date = new Date();
		jobMetaData.setStartDate(date);
		assertEquals(jobMetaData.getStartDate(), date);
	}

	@Test
	public void testGetAndSetEndDate()
	{
		Date date = new Date();
		jobMetaData.setEndDate(date);
		assertEquals(jobMetaData.getEndDate(), date);
	}

	@Test
	public void testGetAndSetProgressInt()
	{
		jobMetaData.setProgressInt(50);
		assertEquals(jobMetaData.getProgressInt(), new Integer(50));
	}

	@Test
	public void testGetAndSetProgressMessage()
	{
		jobMetaData.setProgressMessage(
				"Annotating with SnpEff, Exac, CADD, Clinvar, Dann, CGD, Fitcon, GoNL, HPO, and many more.... %p");
		assertEquals(jobMetaData.getProgressMessage(),
				"Annotating with SnpEff, Exac, CADD, Clinvar, Dann, CGD, Fitcon, GoNL, HPO, and many more.... %p");
	}

	@Test
	public void testGetAndSetProgressMax()
	{
		jobMetaData.setProgressMax(1032);
		assertEquals(jobMetaData.getProgressMax(), new Integer(1032));
	}
}
