package org.molgenis.omx.importer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.importer.EmxImportServiceImpl;
import org.molgenis.data.importer.EmxImporterService;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.RepositoryDecorator;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntitiesValidator;
import org.molgenis.omx.observ.DataSet;
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
	private static final String DATASET_PREFIX = DataSet.class.getSimpleName().toLowerCase();
	private final transient DataService dataService;
	private final transient EntitiesValidator entitiesValidator;
	private final transient FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

	@Autowired
	public UploadWizardPage(DataService dataService, EntitiesValidator entitiesValidator,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory)
	{
		this.dataService = dataService;
		this.entitiesValidator = entitiesValidator;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (entitiesValidator == null) throw new IllegalArgumentException("EntitiesValidator is null");
		if (fileRepositoryCollectionFactory == null) throw new IllegalArgumentException(
				"FileRepositoryCollectionFactory is null");
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
		if (source.getRepositoryByEntityName("attributes") != null)
		{
			return validateEMXInput(file, wizard, source);
		}
		else
		{
			// if any of the entities is EMX than use the EMX importer, else assume JPA
			// we do not support "mixed import" of JPA and EMX at the moment
			for (String name : source.getEntityNames())
			{
				try {
                    Repository repository = dataService.getRepositoryByEntityName(name);

                    String repositoryClassName;
                    if (repository instanceof RepositoryDecorator) {
                        repositoryClassName = ((RepositoryDecorator) repository).getRepositoryClass();
                    } else {
                        repositoryClassName = repository.getClass().getName();
                    }
                    if (repositoryClassName.equals(MysqlRepository.class.getSimpleName())) {
                        return validateEMXInput(file, wizard, source);
                    }
                }catch(UnknownEntityException e){
                    //Entity not yet known
                }
			}
			// validate entity sheets
			EntitiesValidationReport validationReport = entitiesValidator.validate(file);

			// remove data sheets
			Map<String, Boolean> entitiesImportable = validationReport.getSheetsImportable();
			if (entitiesImportable != null)
			{
				for (Iterator<Entry<String, Boolean>> it = entitiesImportable.entrySet().iterator(); it.hasNext();)
				{
					if (it.next().getKey().toLowerCase().startsWith("dataset_"))
					{
						it.remove();
					}
				}
			}

			Map<String, Boolean> dataSetsImportable = validateDataSetInstances(fileRepositoryCollectionFactory, file);

			// determine if validation succeeded
			boolean ok = true;
			if (entitiesImportable != null)
			{
				for (Boolean b : entitiesImportable.values())
				{
					ok = ok & b;
				}

				for (Collection<String> fields : validationReport.getFieldsRequired().values())
				{
					ok = ok & (fields == null || fields.isEmpty());
				}
			}

			if (dataSetsImportable != null)
			{
				for (Boolean b : dataSetsImportable.values())
				{
					ok = ok & b;
				}
			}

			String msg = null;
			if (ok)
			{
				wizard.setFile(file);
				msg = "File is validated and can be imported.";
			}
			else
			{
				wizard.setValidationMessage("File did not pass validation see results below. Please resolve the errors and try again.");
			}

			// if no error, set prognosis, set file, and continue
			wizard.setEntitiesImportable(entitiesImportable);
			wizard.setDataImportable(dataSetsImportable);
			wizard.setFieldsDetected(validationReport.getFieldsImportable());
			wizard.setFieldsRequired(validationReport.getFieldsRequired());
			wizard.setFieldsAvailable(validationReport.getFieldsAvailable());
			wizard.setFieldsUnknown(validationReport.getFieldsUnknown());

			return msg;
		}
	}

	private String validateEMXInput(File file, ImportWizard wizard, RepositoryCollection source)
	{
		EmxImporterService importer = new EmxImportServiceImpl(dataService);
		EntitiesValidationReport validationReport = importer.validateImport(source);

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

	private Map<String, Boolean> validateDataSetInstances(
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, File file) throws IOException
	{
		RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
				.createFileRepositoryCollection(file);

		// get dataset identifiers (case insensitive)
		Set<String> datasetIdentifiers = new HashSet<String>();

		Repository repo = null;
		try
		{
			repo = repositoryCollection.getRepositoryByEntityName(DATASET_PREFIX);
			if (repo != null)
			{
				for (Entity entity : repo)
				{
					String identifier = entity.getString(DataSet.IDENTIFIER.toLowerCase());
					if (identifier != null) datasetIdentifiers.add(identifier);
				}
			}
		}
		catch (UnknownEntityException e)
		{
			// Ok, no dataset sheet
		}
		finally
		{
			if (repo != null)
			{
				repo.close();
			}
		}

		// validate dataset matrices
		Map<String, Boolean> dataSetValidationMap = new LinkedHashMap<String, Boolean>();

		// determine if dataset matrices can be imported
		for (String name : repositoryCollection.getEntityNames())
		{
			Repository repository = repositoryCollection.getRepositoryByEntityName(name);

			if (repository.getName().toLowerCase().startsWith(DATASET_PREFIX + "_"))
			{
				String identifier = repository.getName().substring((DATASET_PREFIX + "_").length());

				// Check if dataset is present in the excel or in the database
				boolean canImport = datasetIdentifiers.contains(identifier)
						|| (dataService
								.findOne(DataSet.ENTITY_NAME, new QueryImpl().eq(DataSet.IDENTIFIER, identifier)) != null);

				dataSetValidationMap.put(identifier, canImport);
			}
		}

		return dataSetValidationMap;

	}

}
