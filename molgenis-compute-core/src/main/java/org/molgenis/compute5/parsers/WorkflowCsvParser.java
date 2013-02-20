package org.molgenis.compute5.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.compute5.model.Step;
import org.molgenis.compute5.model.Workflow;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.util.tuple.Tuple;

/** Parser for the workflow csv */
public class WorkflowCsvParser
{
	public static Workflow parse(String workflowFile) throws IOException
	{
		try
		{
			CsvReader reader = new CsvReader(new BufferedReader(new FileReader(workflowFile)));

			Workflow wf = new Workflow();

			for (Tuple row : reader)
			{
				// check value
				if (row.isNull("step")) throw new IOException("required column 'step' is missing in row " + row);
				if (row.isNull("protocol")) throw new IOException("required column 'protocol' is missing in row " + row);
				if (row.isNull("parameters")) throw new IOException("required column 'parameters' is missing in row "
						+ row);

				Step s = new Step(row.getString("step"));
				s.setProtocol(ProtocolFtlParser.parse(new File(workflowFile).getParentFile(), row.getString("protocol")));
				s.setParameters(WorkflowCsvParser.parseParameters(row.getString("parameters")));

				wf.getSteps().add(s);
			}

			return wf;
		}
		catch (IOException e)
		{
			throw new IOException("Parsing of workflow failed: " + e.getMessage()
					+ ".\nThe workflow csv requires columns step,protocol,parameters.");
		}
	}

	private static Map<String, String> parseParameters(String string) throws IOException
	{
		// split per ; and then key/value pairs are split by "="
		Map<String, String> result = new LinkedHashMap<String, String>();

		String[] pairs = string.split(";");

		for (String pair : pairs)
		{
			String[] expr = pair.split("=");
			if (expr.length != 2) throw new IOException(
					"parameters should be expressions of form 'input=prevstep.output;input2=prevstep.output ...', found:"
							+ expr);

			result.put(expr[0], expr[1]);
		}

		return result;
	}
}
