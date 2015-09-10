package org.molgenis.data.transaction;

import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AsyncTransactionLogTest
{
	private DataService dataService;
	private AsyncTransactionLog log;

	@BeforeMethod
	private void beforeMethod()
	{
		dataService = Mockito.mock(DataService.class);
		log = new AsyncTransactionLog(dataService);
		log.start();
	}

	@AfterMethod
	public void afterMethod()
	{
		log.stop();
	}

	@Test
	public void addLogEntry()
	{
		Entity entity = new MapEntity(new MolgenisTransactionLogEntryMetaData(new MolgenisTransactionLogMetaData(null),
				null));
		log.addLogEntry(entity);
		Mockito.verify(dataService, Mockito.timeout(1000)).add(MolgenisTransactionLogEntryMetaData.ENTITY_NAME, entity);

	}

	@Test
	public void logTransactionFinished()
	{
		Entity entity = new MapEntity(new MolgenisTransactionLogMetaData(null));
		log.logTransactionFinished(entity);
		Mockito.verify(dataService, Mockito.timeout(1000)).update(MolgenisTransactionLogMetaData.ENTITY_NAME, entity);
	}
}
