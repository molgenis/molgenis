package org.molgenis.compute5.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.molgenis.model.elements.Parameter;

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
	
	// map taskId -> jobName
	Map<Integer, String> idJobMap = new HashMap<Integer, String>();
	
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
		return parameters;
	}

	public void setParameters(Map<String, String> parameters)
	{
		this.parameters = parameters;
		this.previousSteps.clear();
		for (String parameter : getParameters().values())
		{
			if (!parameter.startsWith(Parameters.USER_PREFIX))
			{
				int ps = parameter.indexOf(Parameters.STEP_PARAM_SEP);
				if (-1 == ps)
				{
					System.err.println(">> ERROR >> In your workflow in step '" + this.getName() + "', it is unclear from which step your parameter value '=" + parameter + "' originates. Please prepend user_ or step1_ or similar." );
					System.err.println(">> Exit with exit status 1.");
					System.exit(1);
				}
				previousSteps.add(parameter.substring(0, ps));
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
