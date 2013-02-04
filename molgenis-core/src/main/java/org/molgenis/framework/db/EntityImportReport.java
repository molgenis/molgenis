package org.molgenis.framework.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityImportReport
{
	private List<String> progressLog;
	private Map<String, String> messages;
	private String errorItem;
	private int nrImported;

	public EntityImportReport()
	{
		progressLog = new ArrayList<String>();
		messages = new HashMap<String, String>();
		errorItem = "no error found";
	}

	public List<String> getProgressLog()
	{
		return progressLog;
	}

	public void setProgressLog(List<String> progressLog)
	{
		this.progressLog = progressLog;
	}

	public Map<String, String> getMessages()
	{
		return messages;
	}

	public void setMessages(Map<String, String> messages)
	{
		this.messages = messages;
	}

	public String getErrorItem()
	{
		return errorItem;
	}

	public void setErrorItem(String errorItem)
	{
		this.errorItem = errorItem;
	}

	public int getNrImported()
	{
		return nrImported;
	}

	public void addNrImported(int nrImported)
	{
		this.nrImported += nrImported;
	}
}
