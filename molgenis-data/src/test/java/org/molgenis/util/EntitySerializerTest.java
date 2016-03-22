package org.molgenis.util;

import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.util.Date;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.jobs.JobExecution;
import org.molgenis.data.support.DataServiceImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EntitySerializerTest
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
	public void testEntitySerialization() throws ParseException
	{
		Date date = MolgenisDateFormat.getDateTimeFormat().parse("2016-02-19T13:33:09+0100");

		MolgenisUser me = new MolgenisUser();
		jobExecution.setProgressMessage(
				"Annotating with SnpEff, Exac, CADD, Clinvar, Dann, CGD, Fitcon, GoNL, HPO, and many more.... %p");
		jobExecution.setEndDate(date);
		jobExecution.setIdentifier("AAAABDASDFR");
		me.setUsername("fdlk");
	    me.setId("AAAAA");
		jobExecution.setUser(me);
		jobExecution.setStatus(JobExecution.Status.PENDING);
		jobExecution.setType("AnnotatorJob");
		jobExecution.setSubmissionDate(date);
		jobExecution.setStartDate(date);
		jobExecution.setProgressInt(50);
		jobExecution.setProgressMax(1032);

		Gson gson = new GsonBuilder().registerTypeAdapter(JobExecution.class, new EntitySerializer())
				.setDateFormat(MolgenisDateFormat.DATEFORMAT_DATETIME)
				.registerTypeAdapter(MolgenisUser.class, new EntitySerializer()).create();
		assertEquals(gson.toJson(jobExecution),
				"{\"__entityName\":\"JobExecution\",\"identifier\":\"AAAABDASDFR\",\"user\":{\"__entityName\":\"MolgenisUser\",\"__idValue\":\"AAAAA\",\"__labelValue\":\"fdlk\"},\"status\":\"PENDING\",\"type\":\"AnnotatorJob\",\"submissionDate\":\"2016-02-19T13:33:09+0100\",\"startDate\":\"2016-02-19T13:33:09+0100\",\"endDate\":\"2016-02-19T13:33:09+0100\",\"progressInt\":50,\"progressMax\":1032,\"progressMessage\":\"Annotating with SnpEff, Exac, CADD, Clinvar, Dann, CGD, Fitcon, GoNL, HPO, and many more.... %p\"}");
	}
}
