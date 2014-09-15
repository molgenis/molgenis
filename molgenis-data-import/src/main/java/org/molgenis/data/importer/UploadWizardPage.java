package org.molgenis.data.importer;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.molgenis.util.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

@Component
public class UploadWizardPage extends AbstractWizardPage
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(UploadWizardPage.class);
	private final transient FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final transient ImportServiceFactory importServiceFactory;

	@Autowired
	public UploadWizardPage(FileRepositoryCollectionFactory fileRepositoryCollectionFactory,
			ImportServiceFactory importServiceFactory)
	{
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.importServiceFactory = importServiceFactory;
	}

	@Override
	public String getTitle()
	{
		return "Upload file";
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

		try
		{
			File file = null;
			Part part = request.getPart("upload");
			if (part != null)
			{
				file = FileUploadUtils.saveToTempFolder(part);
			}

			if (file == null)
			{
				result.addError(new ObjectError("wizard", "No file selected"));
			}
			else
			{
				return validateInput(file, importWizard, result);
			}
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
		// wizard.setDataImportable(validationReport.getSheetsImportable());
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
