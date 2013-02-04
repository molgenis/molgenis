package org.molgenis.framework.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.molgenis.MolgenisOptions;
import org.molgenis.services.SchedulingService;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class MolgenisContext
{
	private ServletConfig sc;
	private DataSource ds;
	private MolgenisOptions usedOptions;
	private String variant;
	private TokenFactory tokenFactory;
	// private Scheduler scheduler;
	private SchedulingService schedulingService;

	// other "static" variables here, eg.
	// molgenis version
	// date/time of generation
	// revision number

	public MolgenisContext(ServletConfig sc, DataSource ds, MolgenisOptions usedOptions, String variant)
	{
		this.sc = sc;
		this.ds = ds;
		this.usedOptions = usedOptions;
		this.variant = variant;
		this.tokenFactory = new TokenFactory();

		// start Quartz scheduler
		// try
		// {
		// this.scheduler = StdSchedulerFactory.getDefaultScheduler();
		// this.scheduler.start();
		// System.out.println("Quartz scheduler started");
		// }
		// catch (SchedulerException e)
		// {
		// System.err.println("FATAL EXCEPTION: failure for starting scheduler in MolgenisContext.");
		// e.printStackTrace();
		// System.exit(0);
		// }

		this.schedulingService = new SchedulingService();
		// try
		// {
		// this.schedulingService.start();
		// }
		// catch (SchedulerException e)
		// {
		// System.err.println("FATAL EXCEPTION: failure for starting scheduler in MolgenisContext.");
		// e.printStackTrace();
		// }
	}

	public SchedulingService getSchedulingService()
	{
		return schedulingService;
	}

	public Scheduler getScheduler() throws SchedulerException
	{
		StdSchedulerFactory ssf = (StdSchedulerFactory) this.sc.getServletContext().getAttribute(
				"org.quartz.impl.StdSchedulerFactory.KEY");
		if (ssf != null) return ssf.getScheduler();
		else
			throw new SchedulerException("Scheduler not started");
	}

	public TokenFactory getTokenFactory()
	{
		return tokenFactory;
	}

	public String getVariant()
	{
		return variant;
	}

	public ServletConfig getServletConfig()
	{
		return sc;
	}

	public ServletContext getServletContext()
	{
		return sc.getServletContext();
	}

	public DataSource getDataSource()
	{
		return ds;
	}

	public MolgenisOptions getUsedOptions()
	{
		return usedOptions;
	}

	public void setUsedOptions(MolgenisOptions usedOptions)
	{
		this.usedOptions = usedOptions;
	}

}
