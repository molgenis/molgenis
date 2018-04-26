package org.molgenis.data.annotation.core.utils;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JarRunnerImpl implements JarRunner
{

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

		// Error logging to standard logging.
		pb.redirectError(ProcessBuilder.Redirect.INHERIT);

		Process p = pb.start();
		p.waitFor();
		return outputVcf;
	}
}
