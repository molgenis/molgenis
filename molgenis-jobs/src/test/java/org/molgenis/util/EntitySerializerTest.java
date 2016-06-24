package org.molgenis.util;

import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.util.Date;

import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.test.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@ContextConfiguration(classes = { EntitySerializerTest.Config.class })
public class EntitySerializerTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private JobExecutionMetaData jobExecutionMeta;

	private JobExecution jobExecution;

	@BeforeClass
	public void beforeClass()
	{
		jobExecution = new JobExecution(jobExecutionMeta);
	}

	@Test
	public void testEntitySerialization() throws ParseException
	{
		Date date = MolgenisDateFormat.getDateTimeFormat().parse("2016-02-19T13:33:09+0100");

		jobExecution.setProgressMessage(
				"Annotating with SnpEff, Exac, CADD, Clinvar, Dann, CGD, Fitcon, GoNL, HPO, and many more.... %p");
		jobExecution.setEndDate(date);
		jobExecution.setIdentifier("AAAABDASDFR");
		jobExecution.setUser("fdlk");
		jobExecution.setStatus(JobExecution.Status.PENDING);
		jobExecution.setType("AnnotatorJob");
		jobExecution.setSubmissionDate(date);
		jobExecution.setStartDate(date);
		jobExecution.setProgressInt(50);
		jobExecution.setProgressMax(1032);

		Gson gson = new GsonBuilder().registerTypeAdapter(JobExecution.class, new EntitySerializer())
				.setDateFormat(MolgenisDateFormat.DATEFORMAT_DATETIME).create();
		assertEquals(gson.toJson(jobExecution),
				"{\"__entityName\":\"sys_JobExecution\",\"identifier\":\"AAAABDASDFR\",\"user\":\"fdlk\",\"status\":\"PENDING\",\"type\":\"AnnotatorJob\",\"submissionDate\":\"2016-02-19T13:33:09+0100\",\"startDate\":\"2016-02-19T13:33:09+0100\",\"endDate\":\"2016-02-19T13:33:09+0100\",\"progressInt\":50,\"progressMax\":1032,\"progressMessage\":\"Annotating with SnpEff, Exac, CADD, Clinvar, Dann, CGD, Fitcon, GoNL, HPO, and many more.... %p\"}");
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.jobs.model" })
	public static class Config
	{

	}
}
