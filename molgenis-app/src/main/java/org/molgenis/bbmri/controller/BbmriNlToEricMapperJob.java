package org.molgenis.bbmri.controller;

import org.molgenis.bbmri.service.BbmriNlToEricMapperService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@DisallowConcurrentExecution
public class BbmriNlToEricMapperJob implements Job
{
	private static final Logger LOG = LoggerFactory.getLogger(BbmriNlToEricMapperJob.class);

	// Autowire by constructor not possible for Job classes
	@Autowired
	private BbmriNlToEricMapperService nlToEricConverter;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
	{
		try
		{
			LOG.info("Executing scheduled BBMRI-NL to BBMRI-ERIC mapping job ...");
			nlToEricConverter.convertNlToEric();
			LOG.info("Executed scheduled BBMRI-NL to BBMRI-ERIC mapping job");
		}
		catch (Throwable t)
		{
			LOG.error("An error occured rebuilding index", t);
		}
	}
}