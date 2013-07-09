package org.molgenis.compute5.generators;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.molgenis.compute5.ComputeProperties;
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
	public static List<Task> generate(Workflow workflow, Parameters parameters, ComputeProperties computeProperties) throws IOException
	{
		List<Task> result = new ArrayList<Task>();

		final List<WritableTuple> globalParameters = parameters.getValues();
		for (Step step : workflow.getSteps())
		{

			// map global to local parameters
			List<WritableTuple> localParameters = mapGlobalToLocalParameters(globalParameters, step);

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
			result.addAll(generateTasks(step, localParameters, workflow, computeProperties));

			// uncollapse
			localParameters = TupleUtils.uncollapse(localParameters, Parameters.ID_COLUMN);
			// add local input/output parameters to the global parameters
			addLocalToGlobalParameters(step, globalParameters, localParameters);

		}

		return result;

	}

	private static Collection<? extends Task> generateTasks(Step step, List<WritableTuple> localParameters,
			Workflow workflow, ComputeProperties computeProperties) throws IOException
	{
		List<Task> tasks = new ArrayList<Task>();

		for (WritableTuple target : localParameters)
		{
			Task task = new Task(target.getString(Task.TASKID_COLUMN));

			try
			{
				Map<String, Object> map = TupleUtils.toMap(target);

				// remember parameter values
				task.setParameters(map);

				// for this step: store which target-ids go into which job
				for (Integer id : target.getIntList(Parameters.ID_COLUMN))
				{
					step.setJobName(id, task.getName());
				}

				String parameterHeader = "\n#\n## Generated header\n#\n";
				
				// now source the task's parameters from each prevStep.env on
				// which this task depends
				parameterHeader += "\n# Load parameters from previous steps\n" + Parameters.SOURCE_COMMAND + " " + Parameters.ENVIRONMENT_DIR_VARIABLE + File.separator + Parameters.ENVIRONMENT + "\n\n";

				for (String previousStepName : step.getPreviousSteps())
				{ // we have jobs on which we depend in this prev step
					Step prevStep = workflow.getStep(previousStepName);
					for (Integer id : target.getIntList(Parameters.ID_COLUMN))
					{
						String prevJobName = prevStep.getJobName(id);
						
						// prevent duplicate work
						if (!task.getPreviousTasks().contains(prevJobName))
						{
							// for this task: add task dependencies
							task.getPreviousTasks().add(prevJobName);

							// source its environment
							parameterHeader += Parameters.SOURCE_COMMAND + " " + Parameters.ENVIRONMENT_DIR_VARIABLE + File.separator + prevJobName + Parameters.ENVIRONMENT_EXTENSION + "\n";
						}
					}
				}

				parameterHeader += "\n# Assign values to the parameters in this script\n";
				parameterHeader += "\n# Set taskId, which is the job name of this task";
				parameterHeader += "\ntaskId=\"" + task.getName() + "\"\n";
				
				parameterHeader += "\n# Make compute.properties available";
				parameterHeader += "\nrundir=\"" + computeProperties.runDir + "\"";
				parameterHeader += "\nrunid=\"" + computeProperties.runId + "\"";
				parameterHeader += "\nworkflow=\"" + computeProperties.workFlow + "\"";
				parameterHeader += "\nparameters=\"" + computeProperties.parametersString() + "\"";
				parameterHeader += "\nuser=\"" + computeProperties.molgenisuser + "\"";
				parameterHeader += "\ndatabase=\"" + computeProperties.database + "\"";
				parameterHeader += "\nbackend=\"" + computeProperties.backend + "\"";
				parameterHeader += "\nport=\"" + computeProperties.port + "\"";
				parameterHeader += "\ninterval=\"" + computeProperties.interval + "\"";
				parameterHeader += "\npath=\"" + computeProperties.path + "\"";
				
				parameterHeader += "\n\n# Connect parameters to environment\n";

				// now couple input parameters to parameters in sourced
				// environment
				for (Input input : step.getProtocol().getInputs())
				{
					String p = input.getName();

					List<String> rowIndex = target.getList(Parameters.ID_COLUMN);
					for (int i = 0; i < rowIndex.size(); i++)
					{
						Object rowIndexObject = rowIndex.get(i);
						String rowIndexString = (String) rowIndexObject.toString();

						parameterHeader += p + "[" + i + "]=${" + step.getParameters().get(p) + "[" + rowIndexString
								+ "]}\n";
					}

				}

				parameterHeader = parameterHeader
						+ "\n# Validate that each 'value' parameter has only identical values in its list\n"
						+ "# We do that to protect you against parameter values that might not be correctly set at runtime.\n";
				for (Input input : step.getProtocol().getInputs())
				{
					boolean isList = Parameters.LIST_INPUT.equals(input.getType());
					if (!isList)
					{
						String p = input.getName();

						parameterHeader += "if [[ ! $(IFS=$'\\n' sort -u <<< \"${"
								+ p
								+ "[*]}\" | wc -l | sed -e 's/^[[:space:]]*//') = 1 ]]; then echo \"Error in Step '"
								+ step.getName()
								+ "': input parameter '"
								+ p
								+ "' is an array with different values. Maybe '"
								+ p
								+ "' is a runtime parameter with 'more variable' values than what was folded on generation-time?\" >&2; exit 1; fi\n";
					}
				}

				parameterHeader += "\n#\n## Start of your protocol template\n#\n\n";

				String script = step.getProtocol().getTemplate();
				script = parameterHeader + script;

				// append footer that appends the task's parameters to
				// environment of this task
				String myEnvironmentFile = Parameters.ENVIRONMENT_DIR_VARIABLE + File.separator + task.getName() + Parameters.ENVIRONMENT_EXTENSION;
				script = script + "\n#\n## End of your protocol template\n#\n";
				script = script + "\n# Save output in environment file: '" + myEnvironmentFile
						+ "' with the output vars of this step\n";

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
								// System.out.println(">> " + rowIndexString);
								line += "echo \"" + step.getName() + Parameters.STEP_PARAM_SEP + p + "["
										+ rowIndexString + "]=${" + p + "[" + i + "]}\" >> " + myEnvironmentFile + "\n";
							}

							script += line;
						}
					}
				}
				script = appendToEnv(script, "", myEnvironmentFile); // empty
																		// line

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

	private static String appendToEnv(String script, String string, String thisFile)
	{
		String appendString = "echo \"" + string + "\" >> " + thisFile;

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
		// // determine list length so that we can replicate thisTaskId that
		// number of times
		// Integer nCollapsedTargets = 0;
		// if (0 < localParameters.size())
		// {
		// nCollapsedTargets =
		// localParameters.get(0).getList(Parameters.ID_COLUMN).size();
		// }

		// int stepId = 0;
		// for (WritableTuple target : localParameters)
		// {
		// // String thisTaskId = step.getName() + Parameters.STEP_PARAM_SEP +
		// stepId;
		// // List<String> prevTaskIds =
		// target.getList(Parameters.PREVIOUS_COLUMN);
		// // List<String> allTaskIds = new ArrayList<String>();
		// // if (null != prevTaskIds)
		// // {
		// // allTaskIds = prevTaskIds;
		// // }
		// // allTaskIds.add(thisTaskId);
		// // target.set(Parameters.PREVIOUS_COLUMN, allTaskIds); // for
		// previous
		// // // steps
		//
		// String name = step.getName() + Parameters.PREVIOUS_COLUMN + stepId;
		// target.set(Task.TASKID_COLUMN, name);
		// target.set(Task.TASKID_INDEX_COLUMN, stepId++);
		// }
		// return localParameters;

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
				// else if (localName.equals(Parameters.PREVIOUS_COLUMN))
				// {
				// // handle previous step
				// globalParameters.get(i).set(localName, local.get(localName));
				// }
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

		return localParameters;
	}

	private static List<WritableTuple> collapseOnTargets(List<WritableTuple> localParameters, Step step)
	{

		List<String> targets = new ArrayList<String>();

		for (Input i : step.getProtocol().getInputs())
		{
			String origin = step.getParameters().get(i.getName());
			boolean initialized = origin.startsWith(Parameters.USER_PREFIX);

			boolean isList = Parameters.LIST_INPUT.equals(i.getType());

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

			// // set previous step info
			// for (String col : global.getColNames())
			// {
			// System.out.println(">> In mapGlobalToLocalParams >> " + col);
			// if (Parameters.PREVIOUS_COLUMN.equals(col))
			// {
			// // found info in which jobs this target was part of
			// local.set(col, global.getString(col));
			// }
			// }

			localParameters.add(local);
		}

		return localParameters;
	}

	/** Convert all parameters to lists, except the once marked as target */

}
