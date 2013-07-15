package org.molgenis.compute5.generators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

import org.molgenis.compute5.model.*;
import org.molgenis.util.tuple.WritableTuple;

public class EnvironmentGenerator
{
	/**
	 * Returns initial environment with all user params that are used somewhere in this workflow
	 * @param compute
	 * @return
	 */

	private HashMap<String, String> environment = new HashMap<String, String>();
	private List<Step> steps = null;
	private Workflow workflow = null;

	public String getEnvironmentAsString(Compute compute) throws Exception
	{
		workflow = compute.getWorkflow();

		// put header 'adding user params' in environment
		String output = "#\n## User parameters\n#\n";

		// user parameters that we want to put in environment
		HashSet<String> userInputParamSet = new HashSet<String>();

		steps = compute.getWorkflow().getSteps();

		// first collect all user parameters that are used in this workflow
		for (Step step : steps)
		{
			Map<String, String> pmap = step.getParametersMapping();

			Iterator<String> itValue = pmap.values().iterator();
			while (itValue.hasNext())
			{
				String value = itValue.next();
				
				// if value matches 'user_*' then add * to environment
//				Integer prefLength = Parameters.USER_PREFIX.length();
//				if (value.substring(0, prefLength).equals(Parameters.USER_PREFIX))
				if(!workflow.parameterHasStepPrefix(value))
				{
					userInputParamSet.add(value);
				}
			}

			List<String> autoMappedParameters = step.getAutoMappedParameters();
			userInputParamSet.addAll(autoMappedParameters);

		}

		// get all parameters from parameters.csv
		for (String parameter: userInputParamSet)
		{
			String userParameter = Parameters.USER_PREFIX + parameter;

			for (WritableTuple wt : compute.getParameters().getValues())
			{
				// retrieve index and value for that index
				Integer index = null;
				String value = null;
				for (String col : wt.getColNames())
				{
					if (col.equals(userParameter))
						value = wt.getString(col);
					if (col.equals(Parameters.USER_PREFIX + Task.TASKID_COLUMN))
						index = wt.getInt(col);
				}

				if (index == null || value == null)
					throw new Exception("Parameter '" + parameter +
							"' does not value in the parameters (.csv, .properties) files ");

				String assignment = parameter + "[" + index + "]=\"" + value + "\"\n";

				environment.put(parameter + "[" + index + "]", value);
				output += assignment;
			}
		}
		
		return output;
	}



	public HashMap<String, String> generate(Compute compute, String workDir) throws Exception
	{
		Parameters.ENVIRONMENT_FULLPATH = workDir + File.separator + Parameters.ENVIRONMENT;
		
		File env = new File(Parameters.ENVIRONMENT_FULLPATH);
		env.delete();

			// give user environment to compute
			String strUserEnvironment = getEnvironmentAsString(compute);
			compute.setUserEnvironment(strUserEnvironment);
			
			// create new environment file
			env.createNewFile();

			BufferedWriter output = new BufferedWriter(new FileWriter(env, true));
			output.write(strUserEnvironment);
			output.close();

		return environment;
	}
}
