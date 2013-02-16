package org.molgenis.compute5.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

/**
 * A step describes a node in a workflow. Given actual parameters, the
 * TaskGenerator can generate Tasks from the Steps.
 */
public class Step
{
	// name of the step
	String name;

	// map between global parameters and local inputs
	Map<String, String> parameters = new LinkedHashMap<String, String>();

	// map of previous steps, i.e. where inputs depend on values from previous
	// steps.
	Set<String> previousSteps = new HashSet<String>();

	// reference to the protocol that should be applied on this step
	Protocol protocol;

	public Step(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Protocol getProtocol()
	{
		return protocol;
	}

	public void setProtocol(Protocol protocol)
	{
		this.protocol = protocol;
	}

	public Map<String, String> getLocalGlobalParameterMap()
	{
		return parameters;
	}

	public void setParameters(Map<String, String> parameters)
	{
		this.parameters = parameters;
		this.previousSteps.clear();
		for (String parameter : getParameters().values())
		{
			if (!parameter.startsWith("user."))
			{
				previousSteps.add(parameter.substring(0, parameter.indexOf(".")));
			}
		}
	}
	
	public Map<String, String> getParameters()
	{
		return parameters;
	}

	public Set<String> getPreviousSteps()
	{
		return previousSteps;
	}

	public String toString()
	{
		return new Gson().toJson(this);
	}


}
