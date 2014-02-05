package org.molgenis.omx.importer;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntitySource;
import org.molgenis.data.Repository;
import org.molgenis.framework.db.EntityImportReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

@Service
public class DataSetImporterServiceImpl implements DataSetImporterService
{
	public static final String DATASET_SHEET_PREFIX = "dataset_";
	private final DataService dataService;

	@Autowired
	public DataSetImporterServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.importer.DataSetImporter#importDataSet(java.io.File, java.util.List)
	 */
	@Override
	@Transactional
	public EntityImportReport importDataSet(EntitySource entitySource, List<String> dataSetEntityNames,
			DatabaseAction databaseAction)
	{
		// TODO use databaseAction (see http://www.molgenis.org/ticket/1933)

		EntityImportReport importReport = new EntityImportReport();
		try
		{
			for (String entityName : entitySource.getEntityNames())
			{
				if (dataSetEntityNames.contains(entityName))
				{
					Repository repo = entitySource.getRepositoryByEntityName(entityName);
					try
					{
						EntityImportReport sheetImportReport = importSheet(repo, entityName);
						importReport.addEntityImportReport(sheetImportReport);
					}
					finally
					{
						IOUtils.closeQuietly(repo);
					}
				}
			}
		}
		finally
		{
			IOUtils.closeQuietly(entitySource);
		}

		return importReport;
	}

	@Override
	public EntityImportReport importSheet(Repository repo, String sheetName)
	{
		EntityImportReport importReport = new EntityImportReport();
		String identifier = sheetName.substring(DATASET_SHEET_PREFIX.length());
		dataService.add(identifier, repo);
		importReport.addEntityCount(identifier, Iterables.size(repo));

		return importReport;
	}
}
