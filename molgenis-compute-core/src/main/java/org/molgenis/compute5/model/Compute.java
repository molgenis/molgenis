package org.molgenis.compute5.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.generators.BackendGenerator;
import org.molgenis.compute5.generators.TaskGenerator;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

public class Compute
{
	Workflow workflow;
	Parameters parameters;
	List<Task> tasks;
	BackendGenerator backend;
	ComputeProperties computeProperties;
	String userEnvironment;

	public Compute(ComputeProperties computeProperties)
	{
		this.setComputeProperties(computeProperties);
	}

	public List<Task> generateTasks() throws IOException
	{
		tasks = TaskGenerator.generate(workflow, parameters, new ComputeProperties("."));
		return tasks;
	}

	public List<Task> getTasks()
	{
		return tasks;
	}

	public void setTasks(List<Task> tasks)
	{
		this.tasks = tasks;
	}

	public Workflow getWorkflow()
	{
		return workflow;
	}

	public void setWorkflow(Workflow workflow)
	{
		this.workflow = workflow;
	}

	public Parameters getParameters()
	{
		return parameters;
	}

	public void setParameters(Parameters parameters)
	{
		this.parameters = parameters;
	}

	public BackendGenerator getBackend()
	{
		return backend;
	}

	public void setBackend(BackendGenerator backend)
	{
		this.backend = backend;
	}

	public ComputeProperties getComputeProperties()
	{
		return computeProperties;
	}

	public void setComputeProperties(ComputeProperties computeProperties)
	{
		this.computeProperties = computeProperties;
	}

	public void setUserEnvironment(String environment)
	{
		this.userEnvironment = environment;		
	}

	public String getUserEnvironment()
	{
		return this.userEnvironment;
	}
}

//
