package org.molgenis.framework.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.molgenis.MolgenisOptions;
import org.molgenis.services.SchedulingService;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class MolgenisContext
{
	private ServletConfig sc;
	private MolgenisOptions usedOptions;
	private String variant;
	private TokenFactory tokenFactory;
	private SchedulingService schedulingService;

	public MolgenisContext(ServletConfig sc, MolgenisOptions usedOptions, String variant)
	{
		this.sc = sc;
		this.usedOptions = usedOptions;
		this.variant = variant;
		this.tokenFactory = new TokenFactory();
		this.schedulingService = new SchedulingService();
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
		else throw new SchedulerException("Scheduler not started");
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

	public MolgenisOptions getUsedOptions()
	{
		return usedOptions;
	}

	public void setUsedOptions(MolgenisOptions usedOptions)
	{
		this.usedOptions = usedOptions;
	}
}
