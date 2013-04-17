package org.molgenis.compute.db.pilot;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LogFileParserTest
{
	private static final String LOGFILE_CONTENT = "TASKID:imputation_run01_6131292775895701\n"
			+ "Tue Apr 16 14:29:02 CEST 2013\n\n#LogInfo\nxxx\n#EndLogInfo\nkhdsdhskj#LogInfoqqq#EndLogInfo";

	private LogFileParser logFileParser;

	@BeforeMethod
	public void beforeMethod()
	{
		logFileParser = new LogFileParser(LOGFILE_CONTENT);
	}

	@Test
	public void getLogBlocks()
	{
		List<String> logBlocks = logFileParser.getLogBlocks();
		assertEquals(logBlocks.size(), 2);
		assertEquals(logBlocks.get(0), "xxx");
		assertEquals(logBlocks.get(1), "qqq");
	}

	@Test
	public void getTaskID()
	{
		assertEquals(logFileParser.getTaskID(), "imputation_run01_6131292775895701");
	}
}
