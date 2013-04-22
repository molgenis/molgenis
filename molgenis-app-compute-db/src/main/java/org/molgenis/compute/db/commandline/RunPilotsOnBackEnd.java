package org.molgenis.compute.db.commandline;

import java.io.IOException;

import org.molgenis.compute.db.executor.ComputeExecutorTask;
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

public class RunPilotsOnBackEnd
{
	public static void main(String[] args) throws IOException, BeansException, DatabaseException
	{
		if (args.length != 2)
		{
			System.out.println("please specify hostname and password");
			System.exit(1);
		}

		String hostName = args[0];
		String password = args[1];

		@SuppressWarnings("resource")
		ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
		ComputeHost computeHost = ComputeHost.findByName(ctx.getBean(Database.class), hostName);
		if (computeHost == null)
		{
			System.out.println("Unknown ComputeHost name [" + hostName
					+ "] please add it to the ComputeHost table. You can do that in the gui on the 'ComputeHosts tab'");
			System.exit(1);
		}

		ComputeExecutorTask task = ctx.getBean(ComputeExecutorTask.class);
		task.start(computeHost, password);
	}
}
