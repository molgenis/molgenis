package org.molgenis.data.importer;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.validation.EntityNameValidator;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

@Component
public class OptionsWizardPage extends AbstractWizardPage
{
	private static final long serialVersionUID = -2931051095557369343L;
	private static final Logger logger = Logger.getLogger(OptionsWizardPage.class);
	private final transient FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final transient ImportServiceFactory importServiceFactory;

	@Autowired
	public OptionsWizardPage(FileRepositoryCollectionFactory fileRepositoryCollectionFactory,
			ImportServiceFactory importServiceFactory)
	{
		super();
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.importServiceFactory = importServiceFactory;
	}

	@Override
	public String getTitle()
	{
		return "Options";
	}

	@Override
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{
		if (!(wizard instanceof ImportWizard))
		{
			throw new RuntimeException("Wizard must be of type '" + ImportWizard.class.getSimpleName()
					+ "' instead of '" + wizard.getClass().getSimpleName() + "'");
		}

		ImportWizard importWizard = (ImportWizard) wizard;

		String entityImportOption = request.getParameter("entity_option");
		importWizard.setEntityImportOption(entityImportOption);

		if (importWizard.getMustChangeEntityName())
		{
			String userGivenName = request.getParameter("name");
			if (StringUtils.isEmpty(userGivenName))
			{
				result.addError(new ObjectError("wizard", "Please enter an entity name"));
				return null;
			}

			if (!EntityNameValidator.isValid(userGivenName))
			{
				result.addError(new ObjectError("wizard",
						"Invalid entity name (only alphanumeric characters are allowed)"));
				return null;
			}

			File tmpFile = importWizard.getFile();
			try
			{
				String fileName = tmpFile.getName();

				int index = fileName.lastIndexOf('.');
				String extension = (index > -1) ? fileName.substring(index) : "";

				File file = new File(tmpFile.getParent(), userGivenName + extension);
				FileCopyUtils.copy(tmpFile, file);

				importWizard.setFile(file);
			}
			catch (IOException e)
			{
				result.addError(new ObjectError("wizard", "Error importing file: " + e.getMessage()));
				logger.error("Exception importing file", e);
			}
			finally
			{
				tmpFile.delete();
			}
		}

		try
		{
			return validateInput(importWizard.getFile(), importWizard, result);
		}
		catch (Exception e)
		{
			result.addError(new ObjectError("wizard", "Error validating import file: " + e.getMessage()));
			logger.error("Exception validating import file", e);
		}

		return null;
	}

	private String validateInput(File file, ImportWizard wizard, BindingResult result) throws Exception
	{

		// decide what importer to use...
		RepositoryCollection source = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, source);
		EntitiesValidationReport validationReport = importService.validateImport(file, source);

		wizard.setEntitiesImportable(validationReport.getSheetsImportable());
		wizard.setFieldsDetected(validationReport.getFieldsImportable());
		wizard.setFieldsRequired(validationReport.getFieldsRequired());
		wizard.setFieldsAvailable(validationReport.getFieldsAvailable());
		wizard.setFieldsUnknown(validationReport.getFieldsUnknown());

		String msg = null;
		if (validationReport.valid())
		{
			wizard.setFile(file);
			msg = "File is validated and can be imported.";
		}
		else
		{
			wizard.setValidationMessage("File did not pass validation see results below. Please resolve the errors and try again.");
		}

		return msg;

	}
}
