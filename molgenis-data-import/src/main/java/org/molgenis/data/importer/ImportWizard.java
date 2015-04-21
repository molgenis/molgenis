package org.molgenis.data.importer;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.data.DatabaseAction;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.core.utils.SecurityUtils;
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
	private String importRunId;
	private List<DatabaseAction> supportedDatabaseActions;
	private boolean mustChangeEntityName;
	private Iterable<MolgenisGroup> groups;
	private List<String> entityNames;
	private boolean allowPermissions;
	private List<String> packages;
	private List<String> entitiesInDefaultPackage;
	private String defaultEntity;

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

	public void setGroups(Iterable<MolgenisGroup> groups)
	{
		this.groups = groups;
	}

	public Iterable<MolgenisGroup> getGroups()
	{
		return groups;
	}

	public void setImportedEntities(List<String> entityNames)
	{
		this.entityNames = entityNames;
	}

	public List<String> getImportedEntities()
	{
		return this.entityNames;
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

	public String getDefaultEntity()
	{
		return defaultEntity;
	}

	public void setDefaultEntity(String defaultEntity)
	{
		this.defaultEntity = defaultEntity;
	}

	public boolean getAllowPermissions()
	{
		allowPermissions = SecurityUtils.currentUserHasRole(SecurityUtils.AUTHORITY_ENTITY_WRITE_PREFIX
				+ MolgenisGroup.ENTITY_NAME.toUpperCase())
				&& SecurityUtils.currentUserHasRole(SecurityUtils.AUTHORITY_ENTITY_WRITE_PREFIX
						+ GroupAuthority.ENTITY_NAME.toUpperCase());
		return allowPermissions || SecurityUtils.currentUserIsSu();
	}
}
