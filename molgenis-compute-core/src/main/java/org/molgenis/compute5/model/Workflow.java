package org.molgenis.compute5.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

public class Workflow
{
	List<Step> steps = new ArrayList<Step>();

	public Set<String> getUserParameters()
	{
		Set<String> result = new HashSet<String>();
		for (Step s : steps)
		{
			for (String value : s.parameters.values())
			{
				if (value.startsWith("user."))
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

	public void setSteps(List<Step> steps)
	{
		this.steps = steps;
	}
}
