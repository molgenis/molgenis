package org.molgenis.data.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.WritableFactory;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.exporter.EmxExporterService;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.molgenis.util.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

@Component
public class UploadWizardPage extends AbstractWizardPage
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(UploadWizardPage.class);

	private final ImportServiceFactory importServiceFactory;
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

	@Autowired
	private EmxExporterService emxExporterService;

	@Autowired
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
		WritableFactory xlsWritableFactory = null;
		try
		{
			xlsWritableFactory = new ExcelWriter(new File("D:\\tmp\\dump.xls"));
			emxExporterService.export(xlsWritableFactory);
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if (xlsWritableFactory != null)
			{
				try
				{
					xlsWritableFactory.close();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		if (!(wizard instanceof ImportWizard))
		{
			throw new RuntimeException("Wizard must be of type '" + ImportWizard.class.getSimpleName()
					+ "' instead of '" + wizard.getClass().getSimpleName() + "'");
		}

		ImportWizard importWizard = (ImportWizard) wizard;

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
				RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
						.createFileRepositoryCollection(file);
				ImportService importService = importServiceFactory.getImportService(file, repositoryCollection);

				importWizard.setFile(file);
				importWizard.setSupportedDatabaseActions(importService.getSupportedDatabaseActions());
				importWizard.setMustChangeEntityName(importService.getMustChangeEntityName());
			}

		}
		catch (Exception e)
		{
			result.addError(new ObjectError("wizard", "Error uploading file: " + e.getMessage()));
			LOG.error("Exception uploading file", e);
		}

		return null;
	}

}
