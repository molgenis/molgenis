package org.molgenis.compute.db.pilot;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.molgenis.compute.runtime.ComputeTask;

/**
 * Builds the bash script for a task to be used by maverick.sh
 * 
 * @author erwin
 * 
 */
public class ScriptBuilder
{
	private final ComputeTask task;
	private final String appLocation;
	private final String pilotServicePath;

	public ScriptBuilder(ComputeTask task, String appLocation, String pilotServicePath)
	{
		this.task = task;
		this.appLocation = appLocation;
		this.pilotServicePath = pilotServicePath;
	}

	public String build()
	{
		String pilotServiceUrl = appLocation + pilotServicePath;
		String computeScript = task.getComputeScript().replaceAll("\r", "");
		String runName = task.getComputeRun().getName();

		// TODO escape quotes ??
		StringBuilder sb = new StringBuilder();
		sb.append("echo TASKNAME:").append(task.getName()).append("\n");
		sb.append("echo RUNNAME:").append(runName).append("\n");

		// Download environments so it can be sourced in the computescript
		sb.append("curl -s -S -o user.env ").append(appLocation).append("/environment/")
				.append(urlEncode(task.getComputeRun().getName())).append("/user.env\n");

		for (ComputeTask prev : task.getPrevSteps())
		{
			String fileName = prev.getName() + ".env";
			sb.append("curl -s -S -o ").append(fileName).append(" ").append(appLocation).append("/environment/")
					.append(urlEncode(task.getComputeRun().getName())).append("/").append(fileName).append("\n");
		}

		sb.append(computeScript).append("\n");
		sb.append("cp log.log done.log\n");

		// Upload log_file and if present the output env file
		sb.append("if [ -f ").append(task.getName()).append(".env ]; then\n");
		sb.append("curl -s -S -F status=done -F log_file=@done.log ");
		sb.append("-F output_file=@").append(task.getName()).append(".env ");
		sb.append(pilotServiceUrl);
		sb.append("\nelse\n");
		sb.append("curl -s -S -F status=done -F log_file=@done.log ");
		sb.append(pilotServiceUrl);
		sb.append("\nfi\n");

		// Remove created files

		// Remove created input env files
		sb.append("rm user.env\n");
		for (ComputeTask prev : task.getPrevSteps())
		{
			sb.append("rm ").append(prev.getName()).append(".env\n");
		}
		// Remove output env
		sb.append("rm ").append(task.getName()).append(".env\n");

		return sb.toString();
	}

	private String urlEncode(String s)
	{
		try
		{
			return URLEncoder.encode(s, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			return s;
		}
	}
}
