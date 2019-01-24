package org.molgenis.data.importer;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataAction;
import org.molgenis.data.RepositoryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class ImportJob implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(ImportJob.class);

  private final ImportService importService;
  private final SecurityContext securityContext;
  private final RepositoryCollection source;
  private final MetadataAction metadataAction;
  private final DataAction databaseAction;
  private final String importRunId;
  private final ImportRunService importRunService;
  private final HttpSession session;
  private final String packageId;

  public ImportJob(
      ImportService importService,
      SecurityContext securityContext,
      RepositoryCollection source,
      MetadataAction metadataAction,
      DataAction databaseAction,
      String importRunId,
      ImportRunService importRunService,
      HttpSession session,
      String packageId) {
    this.importService = importService;
    this.securityContext = securityContext;
    this.source = source;
    this.metadataAction = requireNonNull(metadataAction);
    this.databaseAction = databaseAction;
    this.importRunId = importRunId;
    this.importRunService = importRunService;
    this.session = session;
    this.packageId = packageId;
  }

  @Override
  public void run() {
    try {
      long t0 = System.currentTimeMillis();
      LOG.info("Import started");

      SecurityContextHolder.setContext(securityContext);

      EntityImportReport importReport =
          importService.doImport(source, metadataAction, databaseAction, packageId);

      setSessionSecurityContext();

      importRunService.finishImportRun(
          importRunId,
          importReport.toString(),
          StringUtils.join(importReport.getNewEntities(), ','));

      long t = System.currentTimeMillis();
      LOG.info("Import finished in {} msec.", t - t0);
    } catch (Exception e) {
      LOG.info("Import failed.", e);
      importRunService.failImportRun(importRunId, e.getLocalizedMessage());
    }
  }

  private void setSessionSecurityContext() {
    try {
      session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
    } catch (IllegalStateException e) {
      // session invalidated
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ImportJob)) {
      return false;
    }
    ImportJob importJob = (ImportJob) o;
    return Objects.equals(importService, importJob.importService)
        && Objects.equals(securityContext, importJob.securityContext)
        && Objects.equals(source, importJob.source)
        && metadataAction == importJob.metadataAction
        && databaseAction == importJob.databaseAction
        && Objects.equals(importRunId, importJob.importRunId)
        && Objects.equals(importRunService, importJob.importRunService)
        && Objects.equals(session, importJob.session)
        && Objects.equals(packageId, importJob.packageId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        importService,
        securityContext,
        source,
        metadataAction,
        databaseAction,
        importRunId,
        importRunService,
        session,
        packageId);
  }
}
