package org.molgenis.compute5.generators;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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

			// collapse parameter values
			localParameters = collapseOnTargets(localParameters, step);

			// add the output templates/values + generate step ids
			localParameters = addOutputValues(step, localParameters);

			// add step ids
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

		Configuration conf = new Configuration();
		Template template = new Template(step.getName(), new StringReader(step.getProtocol().getTemplate()), conf);
		StringWriter out;

		for (WritableTuple target : localParameters)
		{
			Task task = new Task(target.getString(Task.TASKID_COLUMN));

			// add data dependencies
			for (String previousStep : step.getPreviousSteps())
			{
				if (!target.isNull(previousStep + "." + Task.TASKID_COLUMN))
				{
					task.getPreviousTasks().addAll(target.getList(previousStep + "." + Task.TASKID_COLUMN));
				}
			}

			// generate script from template
			try
			{
				out = new StringWriter();
				Map<String, Object> map = TupleUtils.toMap(target);
				template.process(map, out);

				// remember paramter values
				task.setParameters(map);

				task.setScript(out.toString());
			}
			catch (Exception e)
			{
				String params = guessParametersNeeded(step.getProtocol().getTemplate());
				throw new IOException("Generation of protocol '" + step.getProtocol().getName() + "' failed: "
						+ e.getMessage() + ".\nParameters used: " + target);
			}

			tasks.add(task);
		}
		return tasks;
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
		int stepId = 1;
		for (WritableTuple target : localParameters)
		{
			String name = step.getName() + "_" + stepId++;
			target.set(Task.TASKID_COLUMN, name);
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
				if (!localName.contains("."))
				{
					globalParameters.get(i).set(step.getName() + "." + localName, local.get(localName));
				}
			}
		}
	}

	private static List<WritableTuple> addOutputValues(Step step, List<WritableTuple> localParameters)
			throws IOException
	{
		try
		{
			for (WritableTuple target : localParameters)
			{
				// add protocol parameters
				// FIXME complete with mem, etc
				target.set("cores", step.getProtocol().getCores());

				for (Output o : step.getProtocol().getOutputs())
				{
					target.set(o.getName(), o.getValue());
				}
			}

			// solve the output templates (if any)
			TupleUtils.solve(localParameters);

			return localParameters;
		}
		catch (IOException e)
		{
			throw new IOException("Solving of outputs for step '" + step.getName() + "' failed: " + e.getMessage());
		}
	}

	private static List<WritableTuple> collapseOnTargets(List<WritableTuple> localParameters, Step step)
	{

		List<String> targets = new ArrayList<String>();

		for (Input i : step.getProtocol().getInputs())
		{
			if (!"list".equals(i.getType())) targets.add(i.getName());
		}
		return TupleUtils.collapse(localParameters, targets);
	}

	private static List<WritableTuple> mapGlobalToLocalParameters(List<WritableTuple> globalParameters, Step step)
			throws IOException
	{
		List<WritableTuple> localParameters = new ArrayList<WritableTuple>();

		for (Tuple global : globalParameters)
		{
			WritableTuple local = new KeyValueTuple();

			// add previous steps
			local.set(global);

			// include row number for later ;-)
			// local.set(Parameters.ID_COLUMN,
			// global.get(Parameters.ID_COLUMN));

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
