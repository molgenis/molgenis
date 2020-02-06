package org.molgenis.core.ui.data.importer.wizard;

import static java.util.Objects.requireNonNull;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

import java.io.File;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import org.molgenis.core.ui.wizard.AbstractWizardPage;
import org.molgenis.core.ui.wizard.Wizard;
import org.molgenis.core.util.FileUploadUtils;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

@Component
public class UploadWizardPage extends AbstractWizardPage {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(UploadWizardPage.class);

  private transient ImportServiceFactory transientImportServiceFactory;
  private transient FileRepositoryCollectionFactory transientFileRepositoryCollectionFactory;

  public UploadWizardPage(
      ImportServiceFactory importServiceFactory,
      FileRepositoryCollectionFactory fileRepositoryCollectionFactory) {
    super();
    this.transientImportServiceFactory = requireNonNull(importServiceFactory);
    this.transientFileRepositoryCollectionFactory = requireNonNull(fileRepositoryCollectionFactory);
  }

  @Override
  public String getTitle() {
    return "Upload file";
  }

  @Override
  public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard) {
    ImportWizardUtil.validateImportWizard(wizard);
    ImportWizard importWizard = (ImportWizard) wizard;
    String dataImportOption = request.getParameter("data-option");

    try {
      File file = null;
      Part part = request.getPart("upload");
      if (part != null) {
        file = FileUploadUtils.saveToTempFolder(part);
      }

      if (file == null) {
        result.addError(new ObjectError("wizard", "No file selected"));
      } else {
        importWizard.setFile(file);

        RepositoryCollection repositoryCollection =
            getFileRepositoryCollectionFactory().createFileRepositoryCollection(file);
        ImportService importService =
            getImportServiceFactory().getImportService(file, repositoryCollection);

        importWizard.setSupportedMetadataActions(importService.getSupportedMetadataActions());
        importWizard.setSupportedDataActions(importService.getSupportedDataActions());
        importWizard.setMustChangeEntityName(importService.getMustChangeEntityName());
      }

    } catch (Exception e) {
      ImportWizardUtil.handleException(e, importWizard, result, LOG, dataImportOption);
    }

    return null;
  }

  private synchronized ImportServiceFactory getImportServiceFactory() {
    if (transientImportServiceFactory == null) {
      transientImportServiceFactory = getApplicationContext().getBean(ImportServiceFactory.class);
    }
    return transientImportServiceFactory;
  }

  private synchronized FileRepositoryCollectionFactory getFileRepositoryCollectionFactory() {
    if (transientFileRepositoryCollectionFactory == null) {
      transientFileRepositoryCollectionFactory =
          getApplicationContext().getBean(FileRepositoryCollectionFactory.class);
    }
    return transientFileRepositoryCollectionFactory;
  }
}
