package org.molgenis.data.importer;

import java.util.Date;

import org.molgenis.data.DataService;
import org.molgenis.data.system.ImportRun;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImportRunService
{
	private static final Logger LOG = LoggerFactory.getLogger(ImportRunService.class);

	private final DataService dataService;

	@Autowired
	public ImportRunService(DataService dataService)
	{
		this.dataService = dataService;
	}

	@RunAsSystem
	public ImportRun addImportRun(String userName)
	{
		ImportRun importRun = new ImportRun();
		importRun.setStartDate(new Date());
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setUserName(userName);
		dataService.add(ImportRun.ENTITY_NAME, importRun);

		return importRun;
	}

	@RunAsSystem
	public void finishImportRun(String importRunId, String message, String importedEntities)
	{
		try
		{
			ImportRun importRun = dataService.findOne(ImportRun.ENTITY_NAME, importRunId, ImportRun.class);
			if (importRun != null)
			{
				importRun.setStatus(ImportStatus.FINISHED.toString());
				importRun.setEndDate(new Date());
				importRun.setMessage(message);
				importRun.setImportedEntities(importedEntities);
				dataService.update(ImportRun.ENTITY_NAME, importRun);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error updating run status", e);
		}
	}

	@RunAsSystem
	public void failImportRun(String importRunId, String message)
	{
		try
		{
			ImportRun importRun = dataService.findOne(ImportRun.ENTITY_NAME, importRunId, ImportRun.class);
			if (importRun != null)
			{
				importRun.setStatus(ImportStatus.FAILED.toString());
				importRun.setEndDate(new Date());
				importRun.setMessage(message);
				dataService.update(ImportRun.ENTITY_NAME, importRun);
			}
		}
		catch (Exception e)
		{
			LOG.error("Error updating run status", e);
		}
	}
}
