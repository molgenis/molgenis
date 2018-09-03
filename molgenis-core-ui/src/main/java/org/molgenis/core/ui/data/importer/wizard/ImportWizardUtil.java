package org.molgenis.core.ui.data.importer.wizard;

import static java.lang.String.format;

import java.io.File;
import java.util.Optional;
import javax.annotation.Nullable;
import org.molgenis.core.ui.wizard.Wizard;
import org.molgenis.data.DataAction;
import org.molgenis.data.importer.MetadataAction;
import org.slf4j.Logger;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

class ImportWizardUtil {
  private ImportWizardUtil() {}

  static @Nullable MetadataAction toMetadataAction(String actionStr) {
    // convert input to database action
    MetadataAction metadataAction;

    switch (actionStr) {
      case "add":
        metadataAction = MetadataAction.ADD;
        break;
      case "update":
        metadataAction = MetadataAction.UPDATE;
        break;
      case "upsert":
        metadataAction = MetadataAction.UPSERT;
        break;
      case "ignore":
        metadataAction = MetadataAction.IGNORE;
        break;
      default:
        metadataAction = null;
        break;
    }

    return metadataAction;
  }

  static @Nullable DataAction toDataAction(String actionStr) {
    // convert input to database action
    DataAction dataAction;

    switch (actionStr) {
      case "add":
        dataAction = DataAction.ADD;
        break;
      case "add_update":
        dataAction = DataAction.ADD_UPDATE_EXISTING;
        break;
      case "update":
        dataAction = DataAction.UPDATE;
        break;
      default:
        dataAction = null;
        break;
    }

    return dataAction;
  }

  static void handleException(
      Exception e,
      ImportWizard importWizard,
      BindingResult result,
      Logger logger,
      String dataImportOption) {
    File file = importWizard.getFile();

    if (logger.isWarnEnabled()) {
      logger.warn(
          format(
              "Import of file [%s] failed for action [%s]",
              Optional.ofNullable(file).map(File::getName).orElse("UNKNOWN"), dataImportOption),
          e);
    }

    result.addError(
        new ObjectError("wizard", "<b>Your import failed:</b><br />" + e.getLocalizedMessage()));
  }

  static void validateImportWizard(Wizard wizard) {
    if (!(wizard instanceof ImportWizard)) {
      throw new RuntimeException(
          "Wizard must be of type '"
              + ImportWizard.class.getSimpleName()
              + "' instead of '"
              + wizard.getClass().getSimpleName()
              + "'");
    }
  }
}
