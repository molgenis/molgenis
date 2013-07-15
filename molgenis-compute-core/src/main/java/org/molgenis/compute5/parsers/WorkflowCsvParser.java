package org.molgenis.compute5.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.molgenis.compute5.model.*;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.util.tuple.Tuple;

/** Parser for the workflow csv */
public class WorkflowCsvParser
{
	Vector<String> stepNames = new Vector();

	public Workflow parse(String workflowFile) throws IOException
	{
		try
		{
			CsvReader reader = new CsvReader(new BufferedReader(new FileReader(workflowFile)));

			Workflow wf = new Workflow();

			for (Tuple row : reader)
			{
				// check value
				if (row.isNull(Parameters.STEP_HEADING_IN_WORKFLOW))
					throw new IOException("required column '" + Parameters.STEP_HEADING_IN_WORKFLOW +
							"' is missing in row " + row);
				if (row.isNull(Parameters.PROTOCOL_HEADING_IN_WORKFLOW))
					throw new IOException("required column '" + Parameters.PROTOCOL_HEADING_IN_WORKFLOW +
							"' is missing in row " + row);

				String stepName = row.getString(Parameters.STEP_HEADING_IN_WORKFLOW);
				Step step = new Step(stepName);
				stepNames.add(stepName);
				Protocol protocol = new ProtocolParser().parse(new File(workflowFile).getParentFile(),
						row.getString(Parameters.PROTOCOL_HEADING_IN_WORKFLOW));
				step.setProtocol(protocol);
				String strParameters = row.getString(Parameters.PARAMETER_MAPPING_HEADING_IN_WORKFLOW);
				if(strParameters!=null)
				{
					HashSet<String> dependencies = parseParametersDependencies(strParameters);
					if(dependencies.size() > 0)
						step.setPreviousSteps(dependencies);

					if(resultParsing.size() > 0)
						step.setParametersMapping(resultParsing);
				}

				Set<Input> inputs = protocol.getInputs();
				for(Input input : inputs)
				{
					step.addParameter(input.getName());
				}

				wf.addStep(step);
			}

			return wf;
		}
		catch (IOException e)
		{
			throw new IOException("Parsing of workflow failed: " + e.getMessage()
					+ ".\nThe workflow csv requires columns " + Parameters.STEP_HEADING_IN_WORKFLOW + "," + Parameters.PROTOCOL_HEADING_IN_WORKFLOW + "," + Parameters.PARAMETER_MAPPING_HEADING_IN_WORKFLOW + ".");
		}
	}

	private Map<String, String> resultParsing = null;

	private HashSet<String> parseParametersDependencies(String string) throws IOException
	{
		HashSet<String> dependencies = new HashSet();
		// split per ; and then key/value pairs are split by "="
		resultParsing = new LinkedHashMap<String, String>();

		String[] pairs = string.split(";");

		for (String pair : pairs)
		{
			String[] expr = pair.split("=");
			if (expr.length > 1)
			{
				resultParsing.put(expr[0], expr[1]);
				//here find dependencies from parameters names
				String [] subExpr = expr[1].split(Parameters.STEP_PARAM_SEP);
				if(subExpr.length > 1)
					if(stepNames.contains(subExpr[0]))
						dependencies.add(subExpr[0]);

			}
			else
			{
				if(stepNames.contains(expr[0]))
					dependencies.add(expr[0]);
			}
		}

		return dependencies;
	}
}
