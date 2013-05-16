package org.molgenis.compute5.generators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.compute5.model.Compute;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.model.Step;
import org.molgenis.compute5.model.Task;
import org.molgenis.util.tuple.WritableTuple;

public class EnvironmentGenerator
{
	/**
	 * Returns initial environment with all user params that are used somewhere in this workflow
	 * @param compute
	 * @return
	 */
	public String getEnvironment(Compute compute)
	{
		// put header 'adding user params' in environment
		String output = "#\n## User parameters\n#\n";

		// user parameters that we want to put in environment
		HashSet<String> userInputParamSet = new HashSet<String>();

		// first collect all user parameters that are used in this workflow
		Iterator<Step> itStep = compute.getWorkflow().getSteps().iterator();
		while (itStep.hasNext())
		{
			Map<String, String> pmap = itStep.next().getParameters();
			
			Iterator<String> itValue = pmap.values().iterator();
			while (itValue.hasNext())
			{
				String value = itValue.next();
				
				// if value matches 'user_*' then add * to environment
				Integer prefLength = Parameters.USER_PREFIX.length();
				if (value.substring(0, prefLength).equals(Parameters.USER_PREFIX))
				{
					userInputParamSet.add(value);
				}
			}
		}
		
		// for each userParam, also get its value, and put them into environment
		
		// get all parameters from parameters.csv

		Iterator<String> itInputParam = userInputParamSet.iterator();
		while (itInputParam.hasNext())
		{
			String p = itInputParam.next(); // user param that is bound to an input param				
			for (WritableTuple wt : compute.getParameters().getValues())
			{
				// retrieve index and value for that index
				Integer index = null;
				String value = null;
				for (String col : wt.getColNames())
				{
					if (col.equals(p)) value = wt.getString(col);
					if (col.equals(Parameters.USER_PREFIX + Task.TASKID_COLUMN)) index = wt.getInt(col);
				}
				
				String assignment = p + "[" + index + "]=" + value + "\n";
				if (index == null || value == null) try
				{
					throw new Exception("Cannot add the following assignment to " + Parameters.ENVIRONMENT_FULLPATH + ":\n" + assignment);
				}
				catch (Exception e)
				{
					System.err.println("In your workflow.csv you maybe refer to a parameter '" + p + "' to which you did not assign a value in your parameters.csv. If so: please add that the parameter to your parameters.csv");
					e.printStackTrace();
					System.err.println("Exit with code 1.");
					System.exit(1);
				} 
				output += p + "[" + index + "]=" + value + "\n";
			}
		}
		
		return output;
	}
	
	public void generate(Compute compute, String workDir)
	{
		Parameters.ENVIRONMENT_FULLPATH = workDir + File.separator + Parameters.ENVIRONMENT;
		
		File env = new File(Parameters.ENVIRONMENT_FULLPATH);
		env.delete();

		try
		{
			// create new environment file
			env.createNewFile();

			BufferedWriter output = new BufferedWriter(new FileWriter(env, true));
			output.write(this.getEnvironment(compute));
			output.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
