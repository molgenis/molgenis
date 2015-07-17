package org.molgenis.data.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Package;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.MetaValidationUtils;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.molgenis.util.FileExtensionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

@Component
public class OptionsWizardPage extends AbstractWizardPage
{
	private static final long serialVersionUID = -2931051095557369343L;
	private static final Logger LOG = LoggerFactory.getLogger(OptionsWizardPage.class);

	private final transient FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final transient ImportServiceFactory importServiceFactory;
	private transient DataService dataService;

	@Autowired
	public OptionsWizardPage(FileRepositoryCollectionFactory fileRepositoryCollectionFactory,
			ImportServiceFactory importServiceFactory, DataService dataService)
	{
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.importServiceFactory = importServiceFactory;
		this.dataService = dataService;
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

			try
			{
				MetaValidationUtils.validateName(userGivenName);
			}
			catch (MolgenisDataException e)
			{
				result.addError(new ObjectError("wizard", e.getMessage()));
				return null;
			}

			File tmpFile = importWizard.getFile();
			try
			{
				String fileName = tmpFile.getName();

				// FIXME: can this be done a bit cleaner?
				String extension = FileExtensionUtils
						.findExtensionFromPossibilities(fileName, fileRepositoryCollectionFactory
								.createFileRepositoryCollection(tmpFile).getFileNameExtensions());

				File file = new File(tmpFile.getParent(), userGivenName + "." + extension);
				FileCopyUtils.copy(tmpFile, file);

				importWizard.setFile(file);
			}
			catch (IOException e)
			{
				result.addError(new ObjectError("wizard", "Error importing file: " + e.getMessage()));
				LOG.error("Exception importing file", e);
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
			LOG.error("Exception validating import file", e);
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

		Set<String> allPackages = new HashSet<>(validationReport.getPackages());
		for (Package p : dataService.getMeta().getPackages())
		{
			allPackages.add(p.getName());
		}

		List<String> entitiesInDefaultPackage = new ArrayList<>();
		for (String entityName : validationReport.getSheetsImportable().keySet())
		{
			if (validationReport.getSheetsImportable().get(entityName))
			{
				if (isInDefaultPackage(entityName, allPackages)) entitiesInDefaultPackage.add(entityName);
			}
		}
		wizard.setEntitiesInDefaultPackage(entitiesInDefaultPackage);

		List<String> packages = new ArrayList<>(validationReport.getPackages());
		packages.add(0, Package.DEFAULT_PACKAGE_NAME);
		wizard.setPackages(packages);

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

	private boolean isInDefaultPackage(String entityName, Set<String> packages)
	{
		for (String packageName : packages)
		{
			if (entityName.toLowerCase().startsWith(packageName.toLowerCase())) return false;
		}

		return true;
	}
}
