package org.molgenis.data.transaction;

import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
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

		Mockito.when(dataService.getRepository(MolgenisTransactionLogEntryMetaData.ENTITY_NAME)).thenReturn(
				Mockito.mock(Repository.class));

		log.addLogEntry(entity);

		Mockito.verify(dataService, Mockito.timeout(1000)).getRepository(
				MolgenisTransactionLogEntryMetaData.ENTITY_NAME);
	}

	@Test
	public void logTransactionFinished()
	{
		Entity entity = new MapEntity(new MolgenisTransactionLogMetaData(null));

		Mockito.when(dataService.getRepository(MolgenisTransactionLogMetaData.ENTITY_NAME)).thenReturn(
				Mockito.mock(Repository.class));

		log.logTransactionFinished(entity);

		Mockito.verify(dataService, Mockito.timeout(1000)).getRepository(MolgenisTransactionLogMetaData.ENTITY_NAME);
	}
}
