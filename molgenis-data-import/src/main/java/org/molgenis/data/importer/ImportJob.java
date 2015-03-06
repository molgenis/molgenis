package org.molgenis.data.importer;

import javax.servlet.http.HttpSession;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.framework.db.EntityImportReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class ImportJob implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(ImportJob.class);

	private final ImportService importService;
	private final SecurityContext securityContext;
	private final RepositoryCollection source;
	private final DatabaseAction databaseAction;
	private final String importRunId;
	private final ImportRunService importRunService;
	private final ImportPostProcessingService importPostProcessingService;
	private final HttpSession session;

	public ImportJob(ImportService importService, SecurityContext securityContext, RepositoryCollection source,
			DatabaseAction databaseAction, String importRunId, ImportRunService importRunService,
			ImportPostProcessingService importPostProcessingService, HttpSession session)
	{
		this.importService = importService;
		this.securityContext = securityContext;
		this.source = source;
		this.databaseAction = databaseAction;
		this.importRunId = importRunId;
		this.importRunService = importRunService;
		this.importPostProcessingService = importPostProcessingService;
		this.session = session;
	}

	@Override
	public void run()
	{
		try
		{
			long t0 = System.currentTimeMillis();
			LOG.info("Import started");

			SecurityContextHolder.setContext(securityContext);

			EntityImportReport importReport = importService.doImport(source, databaseAction);

			if (!importReport.getNewEntities().isEmpty())
			{
				// Add new entities to entities menu if that exists
				importPostProcessingService.addMenuItems(importReport.getNewEntities());
			}

			session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
			importRunService.finishImportRun(importRunId, importReport.toString());

			long t = System.currentTimeMillis();
			LOG.info("Import finished in " + (t - t0) + " msec.");
		}
		catch (Exception e)
		{
			LOG.info("Import failed.", e);
			importRunService.failImportRun(importRunId, e.getMessage());
		}
	}
}
