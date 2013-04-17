package org.molgenis.compute.db.commandline;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.executor.ComputeExecutor;
import org.molgenis.compute.db.executor.ComputeExecutorPilotDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 13/09/2012 Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class RunPilotsOnLocalhost
{
	private static final Logger LOG = Logger.getLogger(RunPilotsOnLocalhost.class);
	private final ComputeExecutor executor;
	private static boolean ready = false;

	@Autowired
	public RunPilotsOnLocalhost(ComputeExecutor executor)
	{
		this.executor = executor;
	}

	@Scheduled(fixedDelay = 10000)
	public void executeTasks()
	{
		if (ready)
		{
			try
			{
				executor.executeTasks("localhost", ComputeExecutorPilotDB.BACK_END_LOCALHOST);
			}
			catch (Exception e)
			{
				LOG.error("Exception executingTasks", e);
			}
		}
	}

	public static void main(String[] args)
	{
		LOG.info("execute with pilots on localhost");

		@SuppressWarnings("resource")
		ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class, RunPilotsOnLocalhost.class);
		if (args.length > 0)
		{
			ComputeExecutor executor = ctx.getBean(ComputeExecutor.class);
			executor.setCommand(args[0]);
		}

		ready = true;
	}
}
