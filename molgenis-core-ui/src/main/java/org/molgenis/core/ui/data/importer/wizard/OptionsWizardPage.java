package org.molgenis.core.ui.data.importer.wizard;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.core.ui.wizard.AbstractWizardPage;
import org.molgenis.core.ui.wizard.Wizard;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.file.util.FileExtensionUtils;
import org.molgenis.data.importer.EntitiesValidationReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.validation.meta.NameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;

@Component
public class OptionsWizardPage extends AbstractWizardPage
{
	private static final long serialVersionUID = -2931051095557369343L;
	private static final Logger LOG = LoggerFactory.getLogger(OptionsWizardPage.class);

	private final transient FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final transient ImportServiceFactory importServiceFactory;
	private transient DataService dataService;

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
		ImportWizardUtil.validateImportWizard(wizard);
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
				NameValidator.validateEntityName(userGivenName);
				if (dataService.hasRepository(userGivenName))
				{
					result.addError(new ObjectError("wizard", "An entity with this name already exists."));
					return null;
				}
			}
			catch (MolgenisDataException e)
			{
				ImportWizardUtil.handleException(e, importWizard, result, LOG, entityImportOption);
				return null;
			}

			File tmpFile = importWizard.getFile();
			String fileName = tmpFile.getName();

			// FIXME: can this be done a bit cleaner?
			String extension = FileExtensionUtils.findExtensionFromPossibilities(fileName,
					fileRepositoryCollectionFactory.createFileRepositoryCollection(tmpFile).getFileNameExtensions());

			File file = new File(tmpFile.getParent(), userGivenName + "." + extension);
			if (!tmpFile.renameTo(file))
			{
				LOG.error("Failed to rename '{}' to '{}'", tmpFile.getName(), file.getName());
			}
			importWizard.setFile(file);
		}

		try
		{
			return validateInput(importWizard.getFile(), importWizard, result);
		}
		catch (Exception e)
		{
			ImportWizardUtil.handleException(e, importWizard, result, LOG, entityImportOption);
		}

		return null;
	}

	private String validateInput(File file, ImportWizard wizard, BindingResult result)
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
			allPackages.add(p.getId());
		}

		List<String> entitiesInDefaultPackage = new ArrayList<>();
		for (String entityTypeId : validationReport.getSheetsImportable().keySet())
		{
			if (validationReport.getSheetsImportable().get(entityTypeId))
			{
				if (isInDefaultPackage(entityTypeId, allPackages)) entitiesInDefaultPackage.add(entityTypeId);
			}
		}
		wizard.setEntitiesInDefaultPackage(entitiesInDefaultPackage);

		List<String> packages = new ArrayList<>(validationReport.getPackages());
		packages.add(0, PACKAGE_DEFAULT);
		wizard.setPackages(packages);

		String msg = null;
		if (validationReport.valid())
		{
			wizard.setFile(file);
			msg = "File is validated and can be imported.";
		}
		else
		{
			wizard.setValidationMessage(
					"File did not pass validation see results below. Please resolve the errors and try again.");
		}

		return msg;
	}

	private boolean isInDefaultPackage(String entityTypeId, Set<String> packages)
	{
		for (String packageName : packages)
		{
			if (entityTypeId.toLowerCase().startsWith(packageName.toLowerCase())) return false;
		}

		return true;
	}
}
