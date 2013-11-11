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
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntitiesValidator;
import org.molgenis.io.TableReader;
import org.molgenis.io.TableReaderFactory;
import org.molgenis.io.TupleReader;
import org.molgenis.io.processor.LowerCaseProcessor;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.molgenis.util.FileUploadUtils;
import org.molgenis.util.tuple.Tuple;
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
	private final transient Database database;
	private final transient EntitiesValidator entitiesValidator;

	@Autowired
	public UploadWizardPage(Database database, EntitiesValidator entitiesValidator)
	{
		this.database = database;
		this.entitiesValidator = entitiesValidator;
		if (database == null) throw new IllegalArgumentException("Database is null");
		if (entitiesValidator == null) throw new IllegalArgumentException("EntitiesValidator is null");
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

		Map<String, Boolean> dataSetsImportable = validateDataSetInstances(database, file);

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

	private Map<String, Boolean> validateDataSetInstances(Database db, File file) throws IOException, DatabaseException
	{
		TableReader tableReader = TableReaderFactory.create(file);
		try
		{
			TupleReader dataSetReader = tableReader.getTupleReader(DATASET_PREFIX);

			// get dataset identifiers (case insensitive)
			Set<String> datasetIdentifiers = new HashSet<String>();

			if (dataSetReader != null)
			{
				try
				{
					dataSetReader.addCellProcessor(new LowerCaseProcessor(true, false));
					for (Tuple tuple : dataSetReader)
					{
						String identifier = tuple.getString(DataSet.IDENTIFIER.toLowerCase());
						if (identifier != null) datasetIdentifiers.add(identifier);
					}
				}
				finally
				{

					dataSetReader.close();
				}
			}

			// validate dataset matrices
			Map<String, Boolean> dataSetValidationMap = new LinkedHashMap<String, Boolean>();

			// determine if dataset matrices can be imported
			for (String tableName : tableReader.getTableNames())
			{
				if (tableName.toLowerCase().startsWith(DATASET_PREFIX + "_"))
				{
					String identifier = tableName.substring((DATASET_PREFIX + "_").length());

					// Check if dataset is present in the excel or in the database
					boolean canImport = datasetIdentifiers.contains(identifier)
							|| (DataSet.findByIdentifier(db, identifier) != null);

					dataSetValidationMap.put(identifier, canImport);
				}
			}

			return dataSetValidationMap;
		}
		finally
		{
			tableReader.close();
		}
	}

}
