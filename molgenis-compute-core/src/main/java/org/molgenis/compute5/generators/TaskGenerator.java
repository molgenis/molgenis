package org.molgenis.compute5.generators;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.model.*;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

public class TaskGenerator
{
	private List<WritableTuple> globalParameters = null;
	private HashMap<String, String> environment = null;
	private Workflow workflow = null;

	public List<Task> generate(Compute compute) throws IOException
	{
		workflow = compute.getWorkflow();
		Parameters parameters = compute.getParameters();
		ComputeProperties computeProperties = compute.getComputeProperties();
		environment = compute.getMapUserEnvironment();

		List<Task> result = new ArrayList<Task>();

		globalParameters = parameters.getValues();
		for (Step step : workflow.getSteps())
		{

			// map global to local parameters
			List<WritableTuple> localParameters = mapGlobalToLocalParameters(globalParameters, step);

			// collapse parameter values
			localParameters = collapseOnTargets(localParameters, step);

			// System.out.println(">> localParameters 'collapseOnTargets': \n" +
			// localParameters);

			// add the output templates/values + generate step ids
			localParameters = addResourceValues(step, localParameters);

			// add step ids as
			// (i) taskId = name_id
			// (ii) taskIndex = id
			localParameters = addStepIds(localParameters, step);

			List<Task> tasks = (List<Task>) generateTasks(step, localParameters, workflow, computeProperties);
			// generate the tasks from template, add step id
			result.addAll(tasks);

			// uncollapse
			localParameters = TupleUtils.uncollapse(localParameters, Parameters.ID_COLUMN);
			// add local input/output parameters to the global parameters
			addLocalToGlobalParameters(step,  localParameters);

		}

		return result;

	}

	private Collection<? extends Task> generateTasks(Step step, List<WritableTuple> localParameters,
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


				parameterHeader += "\n\n# Connect parameters to environment\n";

				// now couple input parameters to parameters in sourced
				// environment
				for (Input input : step.getProtocol().getInputs())
				{
					String parameterName = input.getName();

					List<String> rowIndex = target.getList(Parameters.ID_COLUMN);
					for (int i = 0; i < rowIndex.size(); i++)
					{
						Object rowIndexObject = rowIndex.get(i);
						String rowIndexString = rowIndexObject.toString();

						String value = null;
						String parameterMapping = step.getParametersMapping().get(parameterName);
						if(parameterMapping != null)
						{
							//parameter is mapped locally
							value = parameterMapping;
						}
						else
						{
							if(step.hasParameter(parameterName))
								value = parameterName;
						}

						parameterHeader += parameterName + "[" + i + "]=${" + value + "[" + rowIndexString
								+ "]}\n";
					}

				}

				parameterHeader = parameterHeader
						+ "\n# Validate that each 'value' parameter has only identical values in its list\n"
						+ "# We do that to protect you against parameter values that might not be correctly set at runtime.\n";
//				for (Input input : step.getProtocol().getInputs())
//				{
//					boolean isList = Parameters.LIST_INPUT.equals(input.getType());
//					if (!isList)
//					{
//						String p = input.getName();
//
//						parameterHeader += "if [[ ! $(IFS=$'\\n' sort -u <<< \"${"
//								+ p
//								+ "[*]}\" | wc -l | sed -e 's/^[[:space:]]*//') = 1 ]]; then echo \"Error in Step '"
//								+ step.getName()
//								+ "': input parameter '"
//								+ p
//								+ "' is an array with different values. Maybe '"
//								+ p
//								+ "' is a runtime parameter with 'more variable' values than what was folded on generation-time?\" >&2; exit 1; fi\n";
//					}
//				}

				parameterHeader += "\n#\n## Start of your protocol template\n#\n\n";

				String script = step.getProtocol().getTemplate();

				//weave actual values into script here
				String weavedScript = weaveProtocol(step.getProtocol(), environment, target);

				script = parameterHeader + weavedScript;

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
				script = appendToEnv(script, "", myEnvironmentFile);
				script += "\n";

				task.setScript(script);

			}
			catch (Exception e)
			{
					throw new IOException("Generation of protocol '" + step.getProtocol().getName() + "' failed: "
						+ e.getMessage() + ".\nParameters used: " + target);
			}

			tasks.add(task);
		}
		return tasks;
	}

	private String weaveProtocol(Protocol protocol, HashMap<String, String> environment, WritableTuple target)
	{
		String template = protocol.getTemplate();
		Hashtable<String, String> values = new Hashtable<String, String>();

		for(Input input : protocol.getInputs())
		{
			if(input.getType().equalsIgnoreCase(Parameters.STRING))
			{
				String name = input.getName();
				String value = (String) target.get(name);
				if(value.equalsIgnoreCase(Parameters.NOTAVAILABLE))
				{
					//run time value and to prevent weaving
					value = formFreemarker(name);
				}
				name = formFreemarker(name);
				values.put(name, value);
			}
			else if(input.getType().equalsIgnoreCase(Parameters.LIST_INPUT))
			{
				String name = input.getName();
				ArrayList<String> arrayList = (ArrayList<String>) target.get(name);

				name += FreemarkerUtils.LIST_SIGN;

				if(checkIfAllAvailable(arrayList))
				{
					String strList = "";
					for(String s : arrayList)
					{
						s = addQuotes(s);
						strList += s + " ";
					}
					strList = strList.trim();
					name = formFreemarker(name);
					name = addQuotes(name);
					values.put(name , strList);
				}
				else
				{
					String value = formFreemarker(name);
					name = formFreemarker(name);
					name = addQuotes(name);
					value = addQuotes(value);
					values.put(name, value);
				}
			}
		}

		String result = FreemarkerUtils.weaveWithoutFreemarker(template, values);
		return result;
	}

	private String addQuotes(String str)
	{
		return "\"" + str + "\"";
	}

	private String formFreemarker(String str)
	{
		return "${" + str + "}";
	}

	private boolean checkIfAllAvailable(ArrayList<String> arrayList)
	{
		for(String s : arrayList)
		{
			if(s.equalsIgnoreCase(Parameters.NOTAVAILABLE))
				return false;
		}
		return true;
	}

	private String appendToEnv(String script, String string, String thisFile)
	{
		String appendString = "echo \"" + string + "\" >> " + thisFile;

		return script + "\n" + appendString;
	}

	private List<WritableTuple> addStepIds(List<WritableTuple> localParameters, Step step)
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

	private void addLocalToGlobalParameters(Step step, List<WritableTuple> localParameters)
	{
		for (int i = 0; i < localParameters.size(); i++)
		{
			WritableTuple local = localParameters.get(i);

			for (String localName : local.getColNames())
			{
				if (!localName.contains(Parameters.STEP_PARAM_SEP))
				{
					WritableTuple tuple = globalParameters.get(i);
					tuple.set(step.getName() + Parameters.STEP_PARAM_SEP + localName, local.get(localName));
				}
			}
		}
	}

	private List<WritableTuple> addResourceValues(Step step, List<WritableTuple> localParameters)
	{
		// try
		// {
		for (WritableTuple target : localParameters)
		{
			// add parameters for resource management:
			Tuple defaultResousesMap = globalParameters.get(0);

			//choices to get value for resources
			//1. get from protocol
			//2. get default from parameters file
			//3. get default from protocol file

			if(step.getProtocol().getQueue() == null)
			{
				String queue = (String) defaultResousesMap.get("user_"+ Parameters.QUEUE);
				if(queue != null)
					target.set(Parameters.QUEUE, queue);
				else
					target.set(Parameters.QUEUE, step.getProtocol().getDefaultQueue());
			}
			else
				target.set(Parameters.QUEUE, step.getProtocol().getQueue());

			if(step.getProtocol().getNodes() == null)
			{
				String nodes = (String) defaultResousesMap.get("user_"+ Parameters.NODES);
				if(nodes != null)
					target.set(Parameters.NODES, nodes);
				else
					target.set(Parameters.NODES, step.getProtocol().getDefaultNodes());
			}
			else
				target.set(Parameters.NODES, step.getProtocol().getNodes());

			if(step.getProtocol().getPpn() == null)
			{
				String ppn = (String) defaultResousesMap.get("user_"+ Parameters.PPN);
				if(ppn != null)
					target.set(Parameters.PPN, ppn);
				else
					target.set(Parameters.PPN, step.getProtocol().getDefaultPpn());
			}
			else
				target.set(Parameters.PPN, step.getProtocol().getPpn());

			if(step.getProtocol().getWalltime() == null)
			{
				String walltime = (String) defaultResousesMap.get("user_"+ Parameters.WALLTIME);
				if(walltime != null)
					target.set(Parameters.WALLTIME, walltime);
				else
					target.set(Parameters.WALLTIME, step.getProtocol().getDefaultWalltime());
			}
			else
				target.set(Parameters.WALLTIME, step.getProtocol().getWalltime());

			if(step.getProtocol().getMemory() == null)
			{
				String memory = (String) defaultResousesMap.get("user_"+ Parameters.MEMORY);
				if(memory != null)
					target.set(Parameters.MEMORY, memory);
				else
					target.set(Parameters.MEMORY, step.getProtocol().getDefaultMemory());
			}
			else
				target.set(Parameters.MEMORY, step.getProtocol().getMemory());

			// add protocol parameters
			for (Output o : step.getProtocol().getOutputs())
			{
				target.set(o.getName(), o.getValue());
			}
		}

		return localParameters;
	}

	private List<WritableTuple> collapseOnTargets(List<WritableTuple> localParameters, Step step)
	{

		List<String> targets = new ArrayList<String>();

		for (Input i : step.getProtocol().getInputs())
		{
//			String origin = step.getParametersMapping().get(i.getName());
			boolean initialized = true; //origin.startsWith(Parameters.USER_PREFIX);

			boolean isList = Parameters.LIST_INPUT.equals(i.getType());

			if (!isList && initialized)
				targets.add(i.getName());
		}

		if (0 == targets.size()) // no values from user_*, so do not collapse
		{
			return localParameters;
		}
		else
		{
			List<WritableTuple> collapsed = TupleUtils.collapse(localParameters, targets);
			return collapsed;
		}
	}

	private List<WritableTuple> mapGlobalToLocalParameters(List<WritableTuple> globalParameters, Step step)
			throws IOException
	{
		List<WritableTuple> localParameters = new ArrayList<WritableTuple>();

		for (Tuple global : globalParameters)
		{
			WritableTuple local = new KeyValueTuple();

			// include row number for later to enable uncollapse
			local.set(Parameters.ID_COLUMN, global.get(Parameters.ID_COLUMN));

			// check and map
			for (Input i : step.getProtocol().getInputs())
			{
				// check the mapping, give error if missing
				String localName = i.getName();
				String globalName = step.getLocalGlobalParameterMap().get(localName);

				//appending "user_" if needed
				String parameterNameWithPrefix = null;
				if (globalName == null)
				{
					//automapping
					globalName = localName;
				}

				boolean found = false;
				for (String col : global.getColNames())
				{
					if(!workflow.parameterHasStepPrefix(globalName))
						parameterNameWithPrefix = Parameters.USER_PREFIX + globalName;
					else
						parameterNameWithPrefix = globalName;

					if (col.equals(parameterNameWithPrefix))
					{
						found = true;
						break;
					}
				}

				if (!found)
				{
					throw new IOException("Generation of step '" + step.getName() + "' failed: mapped input '"
							+ globalName + "' is missing from parameter file(s).\nProvided parameters: "
							+ globalParameters);
				}

				local.set(localName, global.get(parameterNameWithPrefix));
			}

			localParameters.add(local);
		}

		return localParameters;
	}



	/** Convert all parameters to lists, except the once marked as target */

}
