package org.molgenis.compute.db.commandline;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.executor.Scheduler;
import org.molgenis.compute.runtime.ComputeHost;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 13/09/2012 Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class RunPilotsOnLocalhost
{
	private static final String BACKEND_NAME_LOCALHOST = "localhost";
	private static final Logger LOG = Logger.getLogger(RunPilotsOnLocalhost.class);

	public static void main(String[] args) throws BeansException, DatabaseException
	{
		LOG.info("execute with pilots on localhost");

		String hostName = BACKEND_NAME_LOCALHOST;
		if (args.length > 0)
		{
			hostName = args[0];
		}

		@SuppressWarnings("resource")
		ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
		ComputeHost computeHost = ComputeHost.findByName(ctx.getBean("unathorizedDatabase", Database.class), hostName);
		if (computeHost == null)
		{
			System.out.println("Unknown ComputeHost name [" + hostName
					+ "] please add it to the ComputeHost table. You can do that in the gui on the 'ComputeHosts tab'");
			System.exit(1);
		}

		Scheduler scheduler = ctx.getBean(Scheduler.class);
		scheduler.schedule(computeHost, null);
	}
}
