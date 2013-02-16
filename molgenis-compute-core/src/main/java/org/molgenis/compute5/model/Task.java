package org.molgenis.compute5.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

/**
 * Generated tasks from steps, with the inputs/outpus prefilled. Includes data
 * dependency graph via previousTasks.
 */
public class Task
{
	public static final String WORKDIR_COLUMN = "workdir";

	public static String TASKID_COLUMN = "taskId";

	// unique name of the task
	String name;

	// reference to previousTasks (i.e. outputs from previous tasks this task
	// depends on)
	Set<String> previousTasks = new HashSet<String>();

	// copy of the local input/outputs used
	Map<String, Object> parameters = new LinkedHashMap<String, Object>();

	// the body of the script (backend independent)
	String script;

	// working directory (i.e. the directory on the shared storage for this
	// workflow run)
	String workdir;

	public Task(String name)
	{
		this.setName(name);
	}

	public String toString()
	{
		return new Gson().toJson(this);
	}

	// List<TaskDependency> dependencies;
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	public Set<String> getPreviousTasks()
	{
		return previousTasks;
	}

	public void setPreviousTasks(Set<String> previousTasks)
	{
		this.previousTasks = previousTasks;
	}

	public Map<String, Object> getParameters()
	{
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters)
	{
		this.parameters = parameters;
	}

	public String getWorkdir()
	{
		return workdir;
	}

	public void setWorkdir(String workdir)
	{
		this.workdir = workdir;
	}

}
