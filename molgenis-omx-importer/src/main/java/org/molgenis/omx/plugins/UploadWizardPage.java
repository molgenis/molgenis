package org.molgenis.omx.plugins;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.io.TableReader;
import org.molgenis.io.TableReaderFactory;
import org.molgenis.io.TupleReader;
import org.molgenis.io.processor.LowerCaseProcessor;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.util.ApplicationUtil;
import org.molgenis.util.tuple.Tuple;

public class UploadWizardPage extends WizardPage
{
	private static final String DATASET_PREFIX = DataSet.class.getSimpleName().toLowerCase();

	public UploadWizardPage()
	{
		super("Upload file");
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		String entityImportOption = request.getString("entity_option");
		ImportWizard importWizard = getWizard();
		importWizard.setEntityImportOption(entityImportOption);

		File file = request.getFile("upload");

		if (file == null)
		{
			getWizard().setErrorMessage("No file selected");
		}
		else
		{
			try
			{
				validateInput(db, file);
			}
			catch (Exception e)
			{
				getWizard().setErrorMessage("Error validating import file: " + e.getMessage());
				logger.error("Exception validating import file", e);
			}
		}
	}

	private void validateInput(Database db, File file) throws Exception
	{
		// validate entity sheets
		EntitiesValidationReport validationReport = ApplicationUtil.getEntitiesValidator().validate(file);

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

		Map<String, Boolean> dataSetsImportable = validateDataSetInstances(db, file);

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

		if (ok)
		{
			getWizard().setFile(file);
			getWizard().setSuccessMessage("File is validated and can be imported.");
		}
		else
		{
			getWizard().setValidationMessage(
					"File did not pass validation see results below. Please resolve the errors and try again.");
		}

		// if no error, set prognosis, set file, and continue
		getWizard().setEntitiesImportable(entitiesImportable);
		getWizard().setDataImportable(dataSetsImportable);
		getWizard().setFieldsDetected(validationReport.getFieldsImportable());
		getWizard().setFieldsRequired(validationReport.getFieldsRequired());
		getWizard().setFieldsAvailable(validationReport.getFieldsAvailable());
		getWizard().setFieldsUnknown(validationReport.getFieldsUnknown());
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
