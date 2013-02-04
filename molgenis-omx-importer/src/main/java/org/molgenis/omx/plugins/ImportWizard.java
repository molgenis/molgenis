package org.molgenis.omx.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.molgenis.framework.db.EntityImportReport;

/**
 * Import wizard model
 */
public class ImportWizard
{
	private List<WizardPage> pages = new ArrayList<WizardPage>();
	private int currentPageIndex = 0;
	private String errorMessage;// Error messages are shown in red and if
								// present the wizard will not go to the next
								// page
	private String validationMessage;// Validation message are shown in red but
										// wizard will move to the next page
	private String successMessage;// Success message are shown in green (on the
									// next page)
	private File file;
	private EntityImportReport importResult;

	private Map<String, Boolean> entitiesImportable;
	private Map<String, Boolean> dataImportable;
	private Map<String, Collection<String>> fieldsDetected;
	private Map<String, Collection<String>> fieldsUnknown;
	private Map<String, Collection<String>> fieldsRequired;
	private Map<String, Collection<String>> fieldsAvailable;

	public ImportWizard()
	{
		super();
		addPage(new UploadWizardPage());
		addPage(new ValidationResultWizardPage());
		addPage(new ImportFileWizardPage());
		addPage(new ImportResultsWizardPage());
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public String getValidationMessage()
	{
		return validationMessage;
	}

	public void setValidationMessage(String validationMessage)
	{
		this.validationMessage = validationMessage;
	}

	public String getSuccessMessage()
	{
		return successMessage;
	}

	public void setSuccessMessage(String successMessage)
	{
		this.successMessage = successMessage;
	}

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

	public void addPage(WizardPage page)
	{
		page.setWizard(this);
		pages.add(page);
	}

	public WizardPage getCurrentPage()
	{
		return pages.get(currentPageIndex);
	}

	public int getCurrentPageIndex()
	{
		return currentPageIndex;
	}

	public List<WizardPage> getPages()
	{
		return Collections.unmodifiableList(pages);
	}

	public boolean isLastPage()
	{
		return getCurrentPageIndex() == getPages().size() - 1;
	}

	public boolean isFirstPage()
	{
		return getCurrentPageIndex() == 0;
	}

	public void next()
	{
		if (currentPageIndex < pages.size() - 1)
		{
			currentPageIndex++;
		}

	}

	public void previous()
	{
		if (currentPageIndex > 0)
		{
			currentPageIndex--;
		}
	}

	public Map<String, Boolean> getEntitiesImportable()
	{
		return entitiesImportable;
	}

	public void setEntitiesImportable(Map<String, Boolean> entitiesImportable)
	{
		this.entitiesImportable = entitiesImportable;
	}

	public Map<String, Boolean> getDataImportable()
	{
		return dataImportable;
	}

	public void setDataImportable(Map<String, Boolean> dataImportable)
	{
		this.dataImportable = dataImportable;
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

	public void setCurrentPageIndex(int currentPageIndex)
	{
		this.currentPageIndex = currentPageIndex;
	}

	public void setPages(List<WizardPage> pages)
	{
		this.pages = pages;
	}

}
