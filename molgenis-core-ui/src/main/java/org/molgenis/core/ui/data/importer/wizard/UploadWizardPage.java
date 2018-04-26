package org.molgenis.core.ui.data.importer.wizard;

import org.molgenis.core.ui.wizard.AbstractWizardPage;
import org.molgenis.core.ui.wizard.Wizard;
import org.molgenis.core.util.FileUploadUtils;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;

@Component
public class UploadWizardPage extends AbstractWizardPage
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(UploadWizardPage.class);

	private final ImportServiceFactory importServiceFactory;
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

	public UploadWizardPage(ImportServiceFactory importServiceFactory,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory)
	{
		super();
		this.importServiceFactory = importServiceFactory;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
	}

	@Override
	public String getTitle()
	{
		return "Upload file";
	}

	@Override
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{
		ImportWizardUtil.validateImportWizard(wizard);
		ImportWizard importWizard = (ImportWizard) wizard;
		String entityImportOption = request.getParameter("entity_option");

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
				importWizard.setFile(file);

				RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(
						file);
				ImportService importService = importServiceFactory.getImportService(file, repositoryCollection);

				importWizard.setSupportedDatabaseActions(importService.getSupportedDatabaseActions());
				importWizard.setMustChangeEntityName(importService.getMustChangeEntityName());
			}

		}
		catch (Exception e)
		{
			ImportWizardUtil.handleException(e, importWizard, result, LOG, entityImportOption);
		}

		return null;
	}

}
