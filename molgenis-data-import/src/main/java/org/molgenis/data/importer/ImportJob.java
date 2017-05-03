package org.molgenis.data.importer;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.RepositoryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpSession;

public class ImportJob implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(ImportJob.class);

	private final ImportService importService;
	private final SecurityContext securityContext;
	private final RepositoryCollection source;
	private final DatabaseAction databaseAction;
	private final String importRunId;
	private final ImportRunService importRunService;
	private final HttpSession session;
	private final String packageName;

	public ImportJob(ImportService importService, SecurityContext securityContext, RepositoryCollection source,
			DatabaseAction databaseAction, String importRunId, ImportRunService importRunService, HttpSession session,
			String packageName)
	{
		this.importService = importService;
		this.securityContext = securityContext;
		this.source = source;
		this.databaseAction = databaseAction;
		this.importRunId = importRunId;
		this.importRunService = importRunService;
		this.session = session;
		this.packageName = packageName;
	}

	@Override
	public void run()
	{
		try
		{
			long t0 = System.currentTimeMillis();
			LOG.info("Import started");

			SecurityContextHolder.setContext(securityContext);

			EntityImportReport importReport = importService.doImport(source, databaseAction, packageName);

			session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

			try
			{
				session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
			}
			catch (IllegalStateException e)
			{
				// session invalidated
			}

			importRunService.finishImportRun(importRunId, importReport.toString(),
					StringUtils.join(importReport.getNewEntities(), ','));

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
