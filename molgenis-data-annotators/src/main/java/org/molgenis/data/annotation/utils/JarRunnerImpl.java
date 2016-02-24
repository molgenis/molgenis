package org.molgenis.data.annotation.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.MolgenisDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarRunnerImpl implements JarRunner
{
	private static final Logger LOG = LoggerFactory.getLogger(JarRunnerImpl.class);

	@Override
	public File runJar(String outputFile, List<String> params, File inputVcf) throws IOException, InterruptedException
	{
		File outputVcf = File.createTempFile(outputFile, ".vcf");

		List<String> command = new ArrayList<>();
		command.add("java");
		command.add("-jar");
		command.addAll(params);
		command.add(inputVcf.getAbsolutePath());
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectOutput(outputVcf);

		Process p = pb.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		StringBuilder msgSB = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null)
		{
			msgSB.append(line);
		}
		
		String msg = msgSB.toString();
		if(!msg.isEmpty()){
			// Error logging to standard logging.
			LOG.error(msg);

			// Throw exception
			throw new MolgenisDataException(msg);
		}
	
		p.waitFor();
		return outputVcf;
	}
}
