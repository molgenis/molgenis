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

public class MolgenisFunctionFileGenerator
{
	public void generate(Compute compute, String workDir)
	{
		// Set error log file name
		String errorLogFile = Parameters.ERROR_FILE_DEFAULT;

		if (!compute.getParameters().getValues().get(0).isNull(Parameters.ERROR_LOG_COLUMN)) errorLogFile = compute
				.getParameters().getValues().get(0).getString(Parameters.ERROR_LOG_COLUMN);

		// In 'workdir': create file with function that stores errors and
		// potentially other functions
		File mc_functionsFile = new File(workDir + File.separator + Parameters.MOLGENIS_FUNCTION_FILE);
		mc_functionsFile.delete();

		try
		{
			mc_functionsFile.createNewFile();

			// put header 'adding user params' in environment file
			BufferedWriter output = new BufferedWriter(new FileWriter(mc_functionsFile, true));
			output.write("#\n## Error function\n#\n");

			// maybe we should store the 'source code' in resources/ instead of
			// src/
			output.write("saveError(){\n" + "\terrorCode=$1\n" + "\terrorMessage=$2\n"
					+ "\techo \"$errorCode: $errorMessage\" >> " + errorLogFile + "\n" + "}\n");
			output.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
