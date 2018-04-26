package org.molgenis.core.ui.data.importer.wizard;

import org.molgenis.core.ui.wizard.Wizard;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.security.auth.Group;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ImportWizard extends Wizard
{
	private static final long serialVersionUID = -7985644531791952523L;
	private File file;
	private EntityImportReport importResult;
	private Map<String, Boolean> entitiesImportable;
	private Map<String, Collection<String>> fieldsDetected;
	private Map<String, Collection<String>> fieldsUnknown;
	private Map<String, Collection<String>> fieldsRequired;
	private Map<String, Collection<String>> fieldsAvailable;
	private String entityImportOption;
	private String validationMessage;
	private String importRunId;
	private List<DatabaseAction> supportedDatabaseActions;
	private boolean mustChangeEntityName;
	private Iterable<Group> groups;
	private List<String> entityTypeIds;
	private boolean allowPermissions;
	private List<String> packages;
	private List<String> entitiesInDefaultPackage;
	private String selectedPackage;

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	public EntityImportReport getImportResult()
	{
		return importResult;
	}

	public void setImportResult(EntityImportReport importResult)
	{
		this.importResult = importResult;
	}

	public Map<String, Boolean> getEntitiesImportable()
	{
		return entitiesImportable;
	}

	public void setEntitiesImportable(Map<String, Boolean> entitiesImportable)
	{
		this.entitiesImportable = entitiesImportable;
	}

	public Map<String, Collection<String>> getFieldsDetected()
	{
		return fieldsDetected;
	}

	public ImportWizard setFieldsDetected(Map<String, Collection<String>> fieldsDetected)
	{
		this.fieldsDetected = fieldsDetected;
		return this;
	}

	public Map<String, Collection<String>> getFieldsUnknown()
	{
		return fieldsUnknown;
	}

	public ImportWizard setFieldsUnknown(Map<String, Collection<String>> fieldsUnknown)
	{
		this.fieldsUnknown = fieldsUnknown;
		return this;
	}

	public Map<String, Collection<String>> getFieldsRequired()
	{
		return fieldsRequired;
	}

	public ImportWizard setFieldsRequired(Map<String, Collection<String>> fieldsRequired)
	{
		this.fieldsRequired = fieldsRequired;
		return this;
	}

	public Map<String, Collection<String>> getFieldsAvailable()
	{
		return fieldsAvailable;
	}

	public ImportWizard setFieldsAvailable(Map<String, Collection<String>> fieldsAvailable)
	{
		this.fieldsAvailable = fieldsAvailable;
		return this;
	}

	public String getEntityImportOption()
	{
		return entityImportOption;
	}

	public void setEntityImportOption(String entityImportOption)
	{
		this.entityImportOption = entityImportOption;
	}

	public String getValidationMessage()
	{
		return validationMessage;
	}

	public void setValidationMessage(String validationMessage)
	{
		this.validationMessage = validationMessage;
	}

	public String getImportRunId()
	{
		return importRunId;
	}

	public void setImportRunId(String importRunId)
	{
		this.importRunId = importRunId;
	}

	public List<DatabaseAction> getSupportedDatabaseActions()
	{
		return supportedDatabaseActions;
	}

	public void setSupportedDatabaseActions(List<DatabaseAction> supportedDatabaseActions)
	{
		this.supportedDatabaseActions = supportedDatabaseActions;
	}

	public boolean getMustChangeEntityName()
	{
		return mustChangeEntityName;
	}

	public void setMustChangeEntityName(boolean mustChangeEntityName)
	{
		this.mustChangeEntityName = mustChangeEntityName;
	}

	public void setGroups(Iterable<Group> groups)
	{
		this.groups = groups;
	}

	public Iterable<Group> getGroups()
	{
		return groups;
	}

	public void setImportedEntities(List<String> entityTypeIds)
	{
		this.entityTypeIds = entityTypeIds;
	}

	public List<String> getImportedEntities()
	{
		return this.entityTypeIds;
	}

	public List<String> getPackages()
	{
		return packages;
	}

	public void setPackages(List<String> packages)
	{
		this.packages = packages;
	}

	public List<String> getEntitiesInDefaultPackage()
	{
		return entitiesInDefaultPackage;
	}

	public void setEntitiesInDefaultPackage(List<String> entitiesInDefaultPackage)
	{
		this.entitiesInDefaultPackage = entitiesInDefaultPackage;
	}

	public String getSelectedPackage()
	{
		return selectedPackage;
	}

	public void setSelectedPackage(String selectedPackage)
	{
		this.selectedPackage = selectedPackage;
	}
}
