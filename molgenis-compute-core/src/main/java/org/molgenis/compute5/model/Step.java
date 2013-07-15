package org.molgenis.compute5.model;

import java.util.*;

import org.molgenis.model.elements.Parameter;

import com.google.gson.Gson;

/**
 * A step describes a node in a workflow. Given actual parameters, the
 * TaskGenerator can generate Tasks from the Steps.
 */
public class Step
{
	// name of the step
	private String name;

	// map between global parameters and local inputs
	private Map<String, String> parametersMapping = new LinkedHashMap<String, String>();
	private List<String> parameterNames = new ArrayList();

	// map of previous steps, i.e. where inputs depend on values from previous
	// steps.
	private Set<String> previousSteps = new HashSet<String>();
	
	// map taskId -> jobName
	private Map<Integer, String> idJobMap = new HashMap<Integer, String>();
	
	public String getJobName(Integer id)
	{
		return this.idJobMap.get(id);
	}
	
	public void setJobName(Integer id, String name)
	{
		this.idJobMap.put(id, name);
	}
	

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
		return parametersMapping;
	}

	public void setParametersMapping(Map<String, String> parameters)
	{
		this.parametersMapping = parameters;
	}
	
	public Map<String, String> getParametersMapping()
	{
		return parametersMapping;
	}

	public Set<String> getPreviousSteps()
	{
		return previousSteps;
	}

	public String toString()
	{
		return new Gson().toJson(this);
	}

	public void setPreviousSteps(Set<String> previousSteps)
	{
		this.previousSteps = previousSteps;
	}

	public void addParameter(String name)
	{
		parameterNames.add(name);
	}

	public List<String> getAutoMappedParameters()
	{
		List autoMapped = new ArrayList<String>();

		for(String parameter : parameterNames)
		{
			boolean isMapped = lookInMapping(parameter);
			if(!isMapped)
				autoMapped.add(parameter);
		}

		return autoMapped;
	}

	private boolean lookInMapping(String parameter)
	{
		for (String key : parametersMapping.keySet())
		{
			if(key.equalsIgnoreCase(parameter))
				return true;
		}

		return false;
	}
}
