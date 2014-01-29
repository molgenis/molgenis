package org.molgenis.omx.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntitySource;
import org.molgenis.framework.db.EntitiesImporter;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.omx.converters.ValueConverterException;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.DataSetImportedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OmxImporterServiceImpl implements OmxImporterService
{
	@Autowired
	private DataService dataService;
	@Autowired
	private EntitiesImporter entitiesImporter;
	@Autowired
	private DataSetImporterService dataSetImporterService;

	@Transactional(rollbackFor =
	{ IOException.class, ValueConverterException.class })
	@Override
	public EntityImportReport doImport(EntitySource entitySource, Map<String, Boolean> dataImportableMap,
			DatabaseAction databaseAction) throws IOException, ValueConverterException
	{
		// import entities
		EntityImportReport importReport = entitiesImporter.importEntities(entitySource, databaseAction);

		// import dataset instances
		if (dataImportableMap != null)
		{
			List<String> dataSetSheetNames = new ArrayList<String>();
			for (Entry<String, Boolean> entry : dataImportableMap.entrySet())
				if (entry.getValue() == true) dataSetSheetNames.add("dataset_" + entry.getKey());

			EntityImportReport dataSetImportReport = dataSetImporterService.importDataSet(entitySource,
					dataSetSheetNames, databaseAction);
			importReport.addEntityImportReport(dataSetImportReport);
		}

		// publish dataset imported event(s)

		Iterable<DataSet> dataSets = dataService.findAll(DataSet.ENTITY_NAME, DataSet.class);
		for (DataSet dataSet : dataSets)
			ApplicationContextProvider.getApplicationContext().publishEvent(
					new DataSetImportedEvent(this, dataSet.getId()));

		return importReport;

	}
}
