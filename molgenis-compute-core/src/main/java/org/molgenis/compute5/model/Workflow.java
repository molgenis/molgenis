package org.molgenis.compute5.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

public class Workflow
{
	private List<Step> steps = new ArrayList<Step>();

	public Set<String> getUserParameters()
	{
		Set<String> result = new HashSet<String>();
		for (Step s : steps)
		{
			for (String value : s.getParametersMapping().values())
			{
				if (value.startsWith(Parameters.USER_PREFIX))
				{
					result.add(value);
				}
			}
		}
		return result;
	}

	public String toString()
	{
		String result = new Gson().toJson(this);
		return "workflow=" + result;
	}

	public List<Step> getSteps()
	{
		return steps;
	}

	public void addStep(Step step)
	{
		this.steps.add(step);
	}

	public Step getStep(String previousStepName)
	{
		for (Step step : this.steps)
		{
			if (previousStepName.equals(step.getName())) return step;
		}
		return null;
	}

	public boolean parameterHasStepPrefix(String parameter)
	{
		for(Step step : steps)
		{
			if(parameter.contains(step.getName()+ Parameters.STEP_PARAM_SEP_SCRIPT))
				return true;
		}
		return false;
	}
}
