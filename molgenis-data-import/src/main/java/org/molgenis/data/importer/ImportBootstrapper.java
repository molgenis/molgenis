package org.molgenis.data.importer;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Bootstraps the import */
@Component
public class ImportBootstrapper {
  private static final Logger LOG = LoggerFactory.getLogger(ImportBootstrapper.class);

  private final DataService dataService;

  public ImportBootstrapper(DataService dataService) {
    this.dataService = requireNonNull(dataService);
  }

  public void bootstrap() {
    LOG.trace("Failing import runs that were left running...");
    dataService
        .query(ImportRunMetadata.IMPORT_RUN, ImportRun.class)
        .eq(ImportRunMetadata.STATUS, ImportStatus.RUNNING.toString())
        .findAll()
        .forEach(this::setFailed);
    LOG.debug("Failed import runs that were left running.");
  }

  private void setFailed(ImportRun importRun) {
    importRun.setStatus(ImportStatus.FAILED.toString());
    importRun.setMessage("Application terminated unexpectedly");
    dataService.update("sys_ImportRun", importRun);
  }
}
