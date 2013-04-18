package org.molgenis.compute.db.commandline;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.executor.ComputeExecutor;
import org.molgenis.compute.db.executor.ExecutionHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 13/09/2012 Time: 16:10
 * To change this template use File | Settings | File Templates.
 */

public class RunPilotsOnBackEnd
{
	private static final int SSH_PORT = 22;
	private static final Logger LOG = Logger.getLogger(RunPilotsOnBackEnd.class);
	private static boolean ready = false;
	private final ComputeExecutor executor;
	private String host = null;
	private String backendType = null;

	public void setHost(String host)
	{
		this.host = host;
	}

	public void setBackendType(String backendType)
	{
		this.backendType = backendType;
	}

	@Autowired
	public RunPilotsOnBackEnd(ComputeExecutor executor)
	{
		this.executor = executor;
	}

	public ComputeExecutor getExecutor()
	{
		return executor;
	}

	@Scheduled(fixedDelay = 30000)
	public void executeTasks()
	{
		if (ready)
		{
			try
			{
				executor.executeTasks(host, backendType);
			}
			catch (Exception e)
			{
				LOG.error("Exception executingTasks", e);
			}
		}
	}

	public static void main(String[] args) throws IOException
	{
		if (args.length != 4)
		{
			System.out.println("please specify backend, user, password and backend type, which can be grid or cluster");
			System.exit(1);
		}

		String host = args[0];
		String user = args[1];
		String pass = args[2];
		String backendType = args[3];

		String command = null;
		if (args.length == 5)
		{
			command = args[4];
		}

		LOG.info("execute with pilots on " + host);

		@SuppressWarnings("resource")
		ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class, RunPilotsOnBackEnd.class);
		RunPilotsOnBackEnd runner = ctx.getBean(RunPilotsOnBackEnd.class);
		runner.getExecutor().setCommand(command);
		runner.getExecutor().setExecutionHost(new ExecutionHost(host, user, pass, SSH_PORT));
		runner.setBackendType(backendType);
		runner.setHost(host);

		ready = true;
	}
}
