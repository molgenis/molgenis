package org.molgenis.core.ui.data.importer.wizard;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.molgenis.core.ui.wizard.Wizard;
import org.molgenis.data.DataAction;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.MetadataAction;

public class ImportWizard extends Wizard {
  private static final long serialVersionUID = -7985644531791952523L;
  private File file;
  private EntityImportReport importResult;
  private Map<String, Boolean> entitiesImportable;
  private Map<String, Collection<String>> fieldsDetected;
  private Map<String, Collection<String>> fieldsUnknown;
  private Map<String, Collection<String>> fieldsRequired;
  private Map<String, Collection<String>> fieldsAvailable;
  private String entityTypeImportOption;
  private String dataImportOption;
  private String validationMessage;
  private String importRunId;
  private List<MetadataAction> supportedMetadataActions;
  private List<DataAction> supportedDataActions;
  private boolean mustChangeEntityName;
  private List<String> entityTypeIds;
  private Map<String, String> packageSelection;
  private List<String> entitiesInDefaultPackage;
  private String selectedPackage;

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public EntityImportReport getImportResult() {
    return importResult;
  }

  public void setImportResult(EntityImportReport importResult) {
    this.importResult = importResult;
  }

  public Map<String, Boolean> getEntitiesImportable() {
    return entitiesImportable;
  }

  public void setEntitiesImportable(Map<String, Boolean> entitiesImportable) {
    this.entitiesImportable = entitiesImportable;
  }

  public Map<String, Collection<String>> getFieldsDetected() {
    return fieldsDetected;
  }

  public ImportWizard setFieldsDetected(Map<String, Collection<String>> fieldsDetected) {
    this.fieldsDetected = fieldsDetected;
    return this;
  }

  public Map<String, Collection<String>> getFieldsUnknown() {
    return fieldsUnknown;
  }

  public ImportWizard setFieldsUnknown(Map<String, Collection<String>> fieldsUnknown) {
    this.fieldsUnknown = fieldsUnknown;
    return this;
  }

  public Map<String, Collection<String>> getFieldsRequired() {
    return fieldsRequired;
  }

  public ImportWizard setFieldsRequired(Map<String, Collection<String>> fieldsRequired) {
    this.fieldsRequired = fieldsRequired;
    return this;
  }

  public Map<String, Collection<String>> getFieldsAvailable() {
    return fieldsAvailable;
  }

  public ImportWizard setFieldsAvailable(Map<String, Collection<String>> fieldsAvailable) {
    this.fieldsAvailable = fieldsAvailable;
    return this;
  }

  public String getMetadataImportOption() {
    return entityTypeImportOption;
  }

  public void setMetadataImportOption(String entityTypeImportOption) {
    this.entityTypeImportOption = entityTypeImportOption;
  }

  public String getDataImportOption() {
    return dataImportOption;
  }

  public void setDataImportOption(String dataImportOption) {
    this.dataImportOption = dataImportOption;
  }

  public String getValidationMessage() {
    return validationMessage;
  }

  public void setValidationMessage(String validationMessage) {
    this.validationMessage = validationMessage;
  }

  public String getImportRunId() {
    return importRunId;
  }

  public void setImportRunId(String importRunId) {
    this.importRunId = importRunId;
  }

  public List<MetadataAction> getSupportedMetadataActions() {
    return supportedMetadataActions;
  }

  public void setSupportedMetadataActions(List<MetadataAction> supportedMetadataActions) {
    this.supportedMetadataActions = supportedMetadataActions;
  }

  public List<DataAction> getSupportedDataActions() {
    return supportedDataActions;
  }

  public void setSupportedDataActions(List<DataAction> supportedDataActions) {
    this.supportedDataActions = supportedDataActions;
  }

  public boolean getMustChangeEntityName() {
    return mustChangeEntityName;
  }

  public void setMustChangeEntityName(boolean mustChangeEntityName) {
    this.mustChangeEntityName = mustChangeEntityName;
  }

  public void setImportedEntities(List<String> entityTypeIds) {
    this.entityTypeIds = entityTypeIds;
  }

  public List<String> getImportedEntities() {
    return this.entityTypeIds;
  }

  public Map<String, String> getPackages() {
    return packageSelection;
  }

  public void setPackages(Map<String, String> packageSelection) {
    this.packageSelection = packageSelection;
  }

  public List<String> getEntitiesInDefaultPackage() {
    return entitiesInDefaultPackage;
  }

  public void setEntitiesInDefaultPackage(List<String> entitiesInDefaultPackage) {
    this.entitiesInDefaultPackage = entitiesInDefaultPackage;
  }

  public String getSelectedPackage() {
    return selectedPackage;
  }

  public void setSelectedPackage(String selectedPackage) {
    this.selectedPackage = selectedPackage;
  }
}
