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
	private final String packageId;

	public ImportJob(ImportService importService, SecurityContext securityContext, RepositoryCollection source,
			DatabaseAction databaseAction, String importRunId, ImportRunService importRunService, HttpSession session,
			String packageId)
	{
		this.importService = importService;
		this.securityContext = securityContext;
		this.source = source;
		this.databaseAction = databaseAction;
		this.importRunId = importRunId;
		this.importRunService = importRunService;
		this.session = session;
		this.packageId = packageId;
	}

	@Override
	public void run()
	{
		try
		{
			long t0 = System.currentTimeMillis();
			LOG.info("Import started");

			SecurityContextHolder.setContext(securityContext);

			EntityImportReport importReport = importService.doImport(source, databaseAction, packageId);

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

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ImportJob importJob = (ImportJob) o;

		if (importService != null ? !importService.equals(importJob.importService) : importJob.importService != null)
			return false;
		if (securityContext != null ? !securityContext.equals(importJob.securityContext) :
				importJob.securityContext != null) return false;
		if (source != null ? !source.equals(importJob.source) : importJob.source != null) return false;
		if (databaseAction != importJob.databaseAction) return false;
		if (importRunId != null ? !importRunId.equals(importJob.importRunId) : importJob.importRunId != null)
			return false;
		if (importRunService != null ? !importRunService.equals(importJob.importRunService) :
				importJob.importRunService != null) return false;
		if (session != null ? !session.equals(importJob.session) : importJob.session != null) return false;
		return packageId != null ? packageId.equals(importJob.packageId) : importJob.packageId == null;
	}

	@Override
	public int hashCode()
	{
		int result = importService != null ? importService.hashCode() : 0;
		result = 31 * result + (securityContext != null ? securityContext.hashCode() : 0);
		result = 31 * result + (source != null ? source.hashCode() : 0);
		result = 31 * result + (databaseAction != null ? databaseAction.hashCode() : 0);
		result = 31 * result + (importRunId != null ? importRunId.hashCode() : 0);
		result = 31 * result + (importRunService != null ? importRunService.hashCode() : 0);
		result = 31 * result + (session != null ? session.hashCode() : 0);
		result = 31 * result + (packageId != null ? packageId.hashCode() : 0);
		return result;
	}
}
