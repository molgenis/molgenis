package org.molgenis.compute.db.pilot;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the raw logfile and extracts logininfo blocks and taskid from it.
 * 
 * 
 * @author erwin
 * 
 */
public class LogFileParser
{
	private static String LOG_INFO_START = "#LogInfo";
	private static String LOG_INFO_END = "#EndLogInfo";

	private final String logFileContents;

	public LogFileParser(String logFileContents)
	{
		if (logFileContents == null) throw new IllegalArgumentException("logFileContents is null");
		this.logFileContents = logFileContents;
	}

	public String getTaskID()
	{
		int idPos = logFileContents.indexOf("TASKID:");
		int endPos = logFileContents.indexOf("\n");

		if ((idPos < 0) || (endPos < 0))
		{
			return null;
		}

		return logFileContents.substring(idPos + 7, endPos).trim();
	}

	public List<String> getLogBlocks()
	{
		List<String> logBlocks = new ArrayList<String>();

		int startPos = logFileContents.indexOf(LOG_INFO_START);
		while (startPos > -1)
		{
			int endPos = logFileContents.indexOf(LOG_INFO_END, startPos);
			if (endPos > -1)
			{
				String logBlock = logFileContents.substring(startPos + LOG_INFO_START.length(), endPos).trim();
				logBlocks.add(logBlock);
			}
			startPos = logFileContents.indexOf(LOG_INFO_START, startPos + LOG_INFO_START.length());
		}

		return logBlocks;
	}
}
