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
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.io.processor.LowerCaseProcessor;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.util.tuple.Tuple;

import app.ImportWizardExcelPrognosis;

public class UploadWizardPage extends WizardPage
{
	public UploadWizardPage()
	{
		super("Upload file");
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		File file = request.getFile("upload");

		if (file == null)
		{
			getWizard().setErrorMessage("No file selected");
		}
		else if (!file.getName().endsWith(".xls"))
		{
			getWizard().setErrorMessage("File does not end with '.xls', other formats are not supported.");
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
		ImportWizardExcelPrognosis xlsValidator = new ImportWizardExcelPrognosis(db, file);

		// remove data sheets
		Map<String, Boolean> entitiesImportable = xlsValidator.getSheetsImportable();
		for (Iterator<Entry<String, Boolean>> it = entitiesImportable.entrySet().iterator(); it.hasNext();)
		{
			if (it.next().getKey().toLowerCase().startsWith("dataset_"))
			{
				it.remove();
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

			for (Collection<String> fields : xlsValidator.getFieldsRequired().values())
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
		getWizard().setFieldsDetected(xlsValidator.getFieldsImportable());
		getWizard().setFieldsRequired(xlsValidator.getFieldsRequired());
		getWizard().setFieldsAvailable(xlsValidator.getFieldsAvailable());
		getWizard().setFieldsUnknown(xlsValidator.getFieldsUnknown());
	}

	private Map<String, Boolean> validateDataSetInstances(Database db, File file) throws IOException, DatabaseException
	{
		Map<String, Boolean> dataSetValidationMap = new LinkedHashMap<String, Boolean>();

		ExcelReader excelReader = new ExcelReader(file);
		excelReader.addCellProcessor(new LowerCaseProcessor(true, false));
		try
		{
			ExcelSheetReader dataSetReader = excelReader.getSheet("dataset");
			if (dataSetReader != null)
			{
				// get dataset identifiers
				Set<String> datasetIdentifiers = new HashSet<String>();
				for (Tuple tuple : dataSetReader)
				{
					String identifier = tuple.getString("identifier");
					if (identifier != null) datasetIdentifiers.add(identifier);
				}

				// check the matrix sheets
				final int nrSheets = excelReader.getNumberOfSheets();
				for (int i = 0; i < nrSheets; i++)
				{
					String sheetName = excelReader.getSheetName(i);
					if (sheetName.toLowerCase().startsWith("dataset_"))
					{
						String identifier = sheetName.substring("dataset_".length());
						boolean canImport;
						if (datasetIdentifiers.contains(identifier)) canImport = true;
						else if (!db
								.find(DataSet.class, new QueryRule(DataSet.IDENTIFIER, Operator.EQUALS, identifier))
								.isEmpty()) canImport = true;
						else
							canImport = false;
						dataSetValidationMap.put(identifier, canImport);
					}
				}
			}
		}
		finally
		{
			excelReader.close();
		}

		return dataSetValidationMap;
	}
}
