package org.molgenis.data.importer;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.molgenis.data.DatabaseAction;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.ui.wizard.Wizard;

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
	private Integer importRunId;
	private List<DatabaseAction> supportedDatabaseActions;
	private boolean mustChangeEntityName;

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

	public void setFieldsDetected(Map<String, Collection<String>> fieldsDetected)
	{
		this.fieldsDetected = fieldsDetected;
	}

	public Map<String, Collection<String>> getFieldsUnknown()
	{
		return fieldsUnknown;
	}

	public void setFieldsUnknown(Map<String, Collection<String>> fieldsUnknown)
	{
		this.fieldsUnknown = fieldsUnknown;
	}

	public Map<String, Collection<String>> getFieldsRequired()
	{
		return fieldsRequired;
	}

	public void setFieldsRequired(Map<String, Collection<String>> fieldsRequired)
	{
		this.fieldsRequired = fieldsRequired;
	}

	public Map<String, Collection<String>> getFieldsAvailable()
	{
		return fieldsAvailable;
	}

	public void setFieldsAvailable(Map<String, Collection<String>> fieldsAvailable)
	{
		this.fieldsAvailable = fieldsAvailable;
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

	public Integer getImportRunId()
	{
		return importRunId;
	}

	public void setImportRunId(Integer importRunId)
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

}
