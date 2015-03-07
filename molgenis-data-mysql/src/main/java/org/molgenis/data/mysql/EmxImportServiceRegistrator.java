package org.molgenis.data.mysql;

import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

/**
 * Register the EmxImportService
 */
public class EmxImportServiceRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private static final Logger LOG = LoggerFactory.getLogger(EmxImportServiceRegistrator.class);

	private final ImportServiceFactory importServiceFactory;
	private final ImportService emxImportService;

	public EmxImportServiceRegistrator(ImportServiceFactory importServiceFactory, ImportService emxImportService)
	{
		this.importServiceFactory = importServiceFactory;
		this.emxImportService = emxImportService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		importServiceFactory.addImportService(emxImportService);
		LOG.info("Registered EMX import service");
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
