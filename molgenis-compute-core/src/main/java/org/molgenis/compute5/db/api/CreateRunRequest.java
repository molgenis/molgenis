package org.molgenis.compute5.db.api;

import java.util.Collections;
import java.util.List;

import org.molgenis.compute5.model.Task;

public class CreateRunRequest
{
	private final String runName;
	private final String backendName;
	private final Long pollDelay;
	private final List<Task> tasks;
	private final String environment;
	private final String userName;

	public CreateRunRequest(String runName, String backendName, Long pollDelay, List<Task> tasks, String environment, String userName)
	{
		this.runName = runName;
		this.backendName = backendName;
		this.pollDelay = pollDelay;
		this.tasks = tasks;
		this.environment = environment;
		this.userName = userName;
	}

	public String getRunName()
	{
		return runName;
	}

	public String getBackendName()
	{
		return backendName;
	}

	public Long getPollDelay()
	{
		return pollDelay;
	}

	public List<Task> getTasks()
	{
		return Collections.unmodifiableList(tasks);
	}

	public String getEnvironment()
	{
		return environment;
	}

    public String getUserName()
    {
        return userName;
    }
}
