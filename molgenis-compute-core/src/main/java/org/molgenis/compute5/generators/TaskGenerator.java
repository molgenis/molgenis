package org.molgenis.compute5.generators;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.molgenis.compute5.model.Input;
import org.molgenis.compute5.model.Output;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.model.Step;
import org.molgenis.compute5.model.Task;
import org.molgenis.compute5.model.Workflow;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class TaskGenerator
{
	public static List<Task> generate(Workflow workflow, Parameters parameters) throws IOException
	{

		List<Task> result = new ArrayList<Task>();

		final List<WritableTuple> globalParameters = parameters.getValues();
		for (Step step : workflow.getSteps())
		{

			// map global to local parameters
			List<WritableTuple> localParameters = mapGlobalToLocalParameters(globalParameters, step);

			// System.out.println(">> localParameters: \n" + localParameters);
			WritableTuple wt = localParameters.get(0);

			// collapse parameter values
			localParameters = collapseOnTargets(localParameters, step);

			// System.out.println(">> localParameters 'collapseOnTargets': \n" +
			// localParameters);

			// add the output templates/values + generate step ids
			localParameters = addOutputValues(step, localParameters);

			// add step ids as
			// (i) taskId = name_id
			// (ii) taskIndex = id
			localParameters = addStepIds(localParameters, step);

			// generate the tasks from template, add step id
			result.addAll(generateTasks(step, localParameters));

			// uncollapse
			localParameters = TupleUtils.uncollapse(localParameters, Parameters.ID_COLUMN);

			// add local input/output parameters to the global parameters
			addLocalToGlobalParameters(step, globalParameters, localParameters);

		}

		return result;

	}

	private static Collection<? extends Task> generateTasks(Step step, List<WritableTuple> localParameters)
			throws IOException
	{
		List<Task> tasks = new ArrayList<Task>();

		// No freemarker anymore:
		//Configuration conf = new Configuration();
		//Template template = new Template(step.getName(), new StringReader(step.getProtocol().getTemplate()), conf);
		StringWriter out;

		for (WritableTuple target : localParameters)
		{
			Task task = new Task(target.getString(Task.TASKID_COLUMN));

			// add data dependencies
			for (String previousStep : step.getPreviousSteps())
			{
				if (!target.isNull(previousStep + Parameters.STEP_PARAM_SEP + Task.TASKID_COLUMN))
				{
					task.getPreviousTasks().addAll(
							target.getList(previousStep + Parameters.STEP_PARAM_SEP + Task.TASKID_COLUMN));
				}
			}

			// generate script from template
			try
			{
				out = new StringWriter();
				Map<String, Object> map = TupleUtils.toMap(target);

				// add parameters for resource management:
//				map.put(Parameters.WALLTIME, step.getProtocol().getWalltime());
				
				// remember parameter values
				task.setParameters(map);

				//  prepend header that loads Molgenis_Functions.sh
				String parameterHeader = "\n#\n##\n### Load Molgenis functions for e.g. error handling\n##\n#\nsource " + Parameters.MOLGENIS_FUNCTION_FILE + "\n\n";
				
				// now source the task's parameters from environment.txt
				parameterHeader += "\n#\n##\n### Load parameters from previous steps\n##\n#\nsource environment.txt\n\n";
				parameterHeader += "\n#\n##\n### Map parameters to environment\n##\n#\n";

				// now couple input parameters to parameters in sourced
				// environment
				for (Input input : step.getProtocol().getInputs())
				{
					String p = input.getName();
					// TODO IF target...() returns a list then (CHECK!) all
					// members are equal and return a member!
					// parameterHeader += p + "=${" +
					// step.getParameters().get(p) + "["
					// + target.getString(Task.TASKID_INDEX_COLUMN) + "]}\n";

					List<String> rowIndex = target.getList(Parameters.ID_COLUMN);
					for (int i = 0; i < rowIndex.size(); i++)
					{
						Object rowIndexObject = rowIndex.get(i);
						String rowIndexString = (String) rowIndexObject.toString();
//						System.out.println(">> " + rowIndexString);
						parameterHeader += p + "[" + i + "]=${" + step.getParameters().get(p) + "[" + rowIndexString
								+ "]}\n";
					}
					// parameterHeader += p + "=${" +
					// step.getParameters().get(p) +
					// target.getList(Parameters.ID_COLUMN) + "}\n";

				}

				parameterHeader = parameterHeader
						+ "\n#\n##\n### Validate that each 'value' parameter has only identical values in its list\n"
						+ "### We do that to protect you against parameter values that might not be correctly set at runtime.\n"
						+ "##\n#\n";
				for (Input input : step.getProtocol().getInputs())
				{
					boolean isList = Parameters.LIST.equals(input.getType());
					if (!isList)
					{
						String p = input.getName();

						parameterHeader += "if [[ ! $(IFS=$'\\n' sort -u <<< \"${"
								+ p
								+ "[*]}\" | wc -l | sed -e 's/^[[:space:]]*//') = 1 ]]; then echo \"Error in Step '" + step.getName() + "': input parameter '" + p + "' is an array with different values.\" >&2; exit 1; fi\n";
					}
				}

				parameterHeader += "\n# Start of your protocol template\n";

				String script = step.getProtocol().getTemplate();// out.toString();
				script = parameterHeader + script;

				// append footer that appends the task's parameters to
				// environment.txt

				script = script + "\n# End of your protocol template\n";
				script = script + "\n#\n##\n### Update '" + Parameters.ENVIRONMENT
						+ "' with the output vars of this step\n##\n#";
				script = appendToEnv(script, "#");
				script = appendToEnv(script, "## " + task.getName());
				script = appendToEnv(script, "#");
				script += "\n";

				Iterator<String> itParam = map.keySet().iterator();
				while (itParam.hasNext())
				{
					String p = itParam.next();

					// add to environment only if this is an output
					// iterate through outputs to check that
					Iterator<Output> itOutput = step.getProtocol().getOutputs().iterator();
					while (itOutput.hasNext())
					{
						Output o = itOutput.next();
						if (o.getName().equals(p))
						{
							// we've found a match

							// if ($p is set) write "p=$p" to env.txt
							// else write "p=o.getValue()"

							// If parameter not set then ERROR
							String line = "if [[ -z \"$" + p + "\" ]]; then echo \"In step '" + step.getName()
									+ "', parameter '" + p + "' has no value! Please assign a value to parameter '" + p
									+ "'." + "\" >&2; exit 1; fi\n";

							// Else set parameters at right indexes.
							// Explanation: if param file is collapsed in this
							// template, then we should not output a single
							// value but a list of values because next step may
							// be run in uncollapsed fashion

							List<String> rowIndex = target.getList(Parameters.ID_COLUMN);
							for (int i = 0; i < rowIndex.size(); i++)
							{
								Object rowIndexObject = rowIndex.get(i);
								String rowIndexString = (String) rowIndexObject.toString();
								//System.out.println(">> " + rowIndexString);
								line += "echo \"" + step.getName() + Parameters.STEP_PARAM_SEP + p + "[" + rowIndexString + "]=${" + p + "[" + i + "]}\" >> " + Parameters.ENVIRONMENT + "\n";
							}

							// // // taking into account that output parameter
							// can be set in header of protocol: #output p 1
							// String line = "if [[ -z \"$" + p +
							// "\"]]; then echo \"" + lefthand + "=" +
							// o.getValue()
							// + "\" >> " + Parameters.ENVIRONMENT + ";";
							// line += " else echo \"" + lefthand + "=$" + p +
							// "\" >> " + Parameters.ENVIRONMENT + ";";

							script += line;
						}
					}
				}
				script = appendToEnv(script, ""); // empty line

				script += "\n";

				task.setScript(script);

			}
			catch (Exception e)
			{
				// String params =
				// guessParametersNeeded(step.getProtocol().getTemplate());
				throw new IOException("Generation of protocol '" + step.getProtocol().getName() + "' failed: "
						+ e.getMessage() + ".\nParameters used: " + target);
			}

			tasks.add(task);
		}
		return tasks;
	}

	private static String appendToEnv(String script, String string)
	{
		String appendString = "echo \"" + string + "\" >> " + Parameters.ENVIRONMENT;

		return script + "\n" + appendString;
	}

	private static String guessParametersNeeded(String ftl)
	{
		Set<String> params = new HashSet<String>();
		Pattern pattern = Pattern.compile("\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(ftl);
		while (matcher.find())
		{
			params.add(matcher.group(0).replace("{", "").replace("}", "")); // prints
																			// /{item}/
		}

		String result = "\nParameters ${x} refered include:";
		for (String p : params)
			result += "\n" + p;
		return result;
	}

	private static List<WritableTuple> addStepIds(List<WritableTuple> localParameters, Step step)
	{
		int stepId = 0;
		for (WritableTuple target : localParameters)
		{
			String name = step.getName() + "_" + stepId;
			target.set(Task.TASKID_COLUMN, name);
			target.set(Task.TASKID_INDEX_COLUMN, stepId++);
		}
		return localParameters;
	}

	private static void addLocalToGlobalParameters(Step step, final List<WritableTuple> globalParameters,
			List<WritableTuple> localParameters)
	{
		for (int i = 0; i < localParameters.size(); i++)
		{
			WritableTuple local = localParameters.get(i);

			for (String localName : local.getColNames())
			{
				if (!localName.contains(Parameters.STEP_PARAM_SEP))
				{
					globalParameters.get(i).set(step.getName() + Parameters.STEP_PARAM_SEP + localName,
							local.get(localName));
				}
			}
		}
	}

	private static List<WritableTuple> addOutputValues(Step step, List<WritableTuple> localParameters)
	{
		// try
		// {
		for (WritableTuple target : localParameters)
		{
			// add parameters for resource management:
			target.set(Parameters.QUEUE, step.getProtocol().getQueue());
			target.set(Parameters.NODES, step.getProtocol().getNodes());
			target.set(Parameters.PPN, step.getProtocol().getPpn());
			target.set(Parameters.WALLTIME, step.getProtocol().getWalltime());
			target.set(Parameters.MEMORY, step.getProtocol().getMemory());
			
			// add protocol parameters
			for (Output o : step.getProtocol().getOutputs())
			{
				target.set(o.getName(), o.getValue());
			}
		}

		// DON'T solve the output templates (if any)
		// TupleUtils.solve(localParameters);

		return localParameters;
		// }
		// catch (IOException e)
		// {
		// throw new IOException("Solving of outputs for step '" +
		// step.getName() + "' failed: " + e.getMessage());
		// }
	}

	private static List<WritableTuple> collapseOnTargets(List<WritableTuple> localParameters, Step step)
	{

		List<String> targets = new ArrayList<String>();

		for (Input i : step.getProtocol().getInputs())
		{
			String origin = step.getParameters().get(i.getName());
			boolean initialized = origin.startsWith(Parameters.USER_PREFIX);

			boolean isList = Parameters.LIST.equals(i.getType());

			if (!isList && initialized) targets.add(i.getName());
		}

		// System.out.println(">> targets   >> " + targets);
		// System.out.println(">> original  >> " + localParameters);
		// System.out.println(">> collapsed >> " +
		// TupleUtils.collapse(localParameters, targets));

		if (0 == targets.size()) // no values from user_*, so do not collapse
		{
			return localParameters;
		}
		else
		{
			return TupleUtils.collapse(localParameters, targets);
		}
	}

	private static List<WritableTuple> mapGlobalToLocalParameters(List<WritableTuple> globalParameters, Step step)
			throws IOException
	{
		List<WritableTuple> localParameters = new ArrayList<WritableTuple>();

		for (Tuple global : globalParameters)
		{
			WritableTuple local = new KeyValueTuple();

			// add previous steps
			// local.set(global);

			// include row number for later to enable uncollapse
			local.set(Parameters.ID_COLUMN, global.get(Parameters.ID_COLUMN));

			// check and map
			for (Input i : step.getProtocol().getInputs())
			{
				// check the mapping, give error if missing
				String localName = i.getName();
				String globalName = step.getLocalGlobalParameterMap().get(localName);
				if (globalName == null) throw new IOException("Generation of step '" + step.getName()
						+ "' failed: mapping of input '" + localName
						+ "' is missing from workflow file.\nProvided mappings: " + step.getLocalGlobalParameterMap());

				// check if the parameter name exists in parameters (todo: also
				// check for null??)
				boolean found = false;
				for (String col : global.getColNames())
				{
					if (globalName.equals(col)) found = true;
				}
				if (!found)
				{
					throw new IOException("Generation of step '" + step.getName() + "' failed: mapped input '"
							+ globalName + "' is missing from parameter file(s).\nProvided parameters: "
							+ globalParameters);
				}

				// set
				local.set(localName, global.get(globalName));
			}

			localParameters.add(local);
		}

		return localParameters;
	}

	/** Convert all parameters to lists, except the once marked as target */

}
