package org.molgenis.core.ui.data.importer.wizard;

import static java.util.Objects.requireNonNull;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import org.molgenis.core.ui.wizard.AbstractWizardPage;
import org.molgenis.core.ui.wizard.Wizard;
import org.molgenis.data.DataAction;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.importer.ImportJob;
import org.molgenis.data.importer.ImportRun;
import org.molgenis.data.importer.ImportRunService;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.MetadataAction;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.ExecutorServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class ValidationResultWizardPage extends AbstractWizardPage {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(ValidationResultWizardPage.class);

  private transient ImportServiceFactory transientImportServiceFactory;
  private transient FileRepositoryCollectionFactory transientFileRepositoryCollectionFactory;
  private transient ImportRunService transientImportRunService;
  private transient ExecutorService transientExecutorService;

  ValidationResultWizardPage(
      ImportServiceFactory importServiceFactory,
      FileRepositoryCollectionFactory fileRepositoryCollectionFactory,
      ImportRunService importRunService) {
    this.transientImportServiceFactory = requireNonNull(importServiceFactory);
    this.transientFileRepositoryCollectionFactory = requireNonNull(fileRepositoryCollectionFactory);
    this.transientImportRunService = requireNonNull(importRunService);

    this.transientExecutorService = createExecutorService();
  }

  @PreDestroy
  void preDestroy() {
    if (transientExecutorService != null) {
      ExecutorServiceUtils.shutdownAndAwaitTermination(transientExecutorService);
    }
  }

  @Override
  public String getTitle() {
    return "Validation";
  }

  @Override
  public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard) {
    ImportWizardUtil.validateImportWizard(wizard);
    ImportWizard importWizard = (ImportWizard) wizard;
    String metadataImportOption = importWizard.getMetadataImportOption();
    String dataImportOption = importWizard.getDataImportOption();

    if (dataImportOption != null) {
      try {
        // convert input to database action
        MetadataAction metadataAction = ImportWizardUtil.toMetadataAction(metadataImportOption);
        if (metadataAction == null) {
          throw new IOException("unknown metadata action: " + metadataImportOption);
        }
        DataAction dataAction = ImportWizardUtil.toDataAction(dataImportOption);
        if (dataAction == null) {
          throw new IOException("unknown data action: " + dataImportOption);
        }

        RepositoryCollection repositoryCollection =
            getFileRepositoryCollectionFactory()
                .createFileRepositoryCollection(importWizard.getFile());
        ImportService importService =
            getImportServiceFactory()
                .getImportService(importWizard.getFile(), repositoryCollection);

        synchronized (this) {
          ImportRun importRun =
              getImportRunService().addImportRun(SecurityUtils.getCurrentUsername(), false);
          ((ImportWizard) wizard).setImportRunId(importRun.getId());

          long callingThreadId = Thread.currentThread().getId();
          getExecutorService()
              .execute(
                  new ImportJob(
                      importService,
                      SecurityContextHolder.getContext(),
                      repositoryCollection,
                      metadataAction,
                      dataAction,
                      importRun.getId(),
                      getImportRunService(),
                      request.getSession(),
                      importWizard.getSelectedPackage(),
                      callingThreadId));
        }

      } catch (RuntimeException | IOException e) {
        ImportWizardUtil.handleException(e, importWizard, result, LOG, dataImportOption);
      }
    }

    return null;
  }

  private synchronized FileRepositoryCollectionFactory getFileRepositoryCollectionFactory() {
    if (transientFileRepositoryCollectionFactory == null) {
      transientFileRepositoryCollectionFactory =
          getApplicationContext().getBean(FileRepositoryCollectionFactory.class);
    }
    return transientFileRepositoryCollectionFactory;
  }

  private synchronized ImportServiceFactory getImportServiceFactory() {
    if (transientImportServiceFactory == null) {
      transientImportServiceFactory = getApplicationContext().getBean(ImportServiceFactory.class);
    }
    return transientImportServiceFactory;
  }

  private synchronized ImportRunService getImportRunService() {
    if (transientImportRunService == null) {
      transientImportRunService = getApplicationContext().getBean(ImportRunService.class);
    }
    return transientImportRunService;
  }

  private synchronized ExecutorService getExecutorService() {
    if (transientExecutorService == null) {
      transientExecutorService = createExecutorService();
    }
    return transientExecutorService;
  }

  private ExecutorService createExecutorService() {
    return Executors.newCachedThreadPool();
  }
}
