package org.molgenis.data.postgresql.transaction;

import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.transaction.MolgenisTransaction;
import org.molgenis.data.transaction.TransactionExceptionTranslatorRegistry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class PostgreSqlTransactionManagerTest
{
	private PostgreSqlTransactionManager molgenisTransactionManager;
	private IdGenerator idGenerator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		idGenerator = mock(IdGenerator.class);
		DataSource dataSource = mock(DataSource.class);
		TransactionExceptionTranslatorRegistry transactionExceptionTranslatorRegistry = mock(
				TransactionExceptionTranslatorRegistry.class);
		molgenisTransactionManager = new PostgreSqlTransactionManager(idGenerator, dataSource,
				transactionExceptionTranslatorRegistry);
	}

	@Test
	public void doGetTransaction()
	{
		String id = "unique_id";
		when(idGenerator.generateId()).thenReturn(id);
		Object trans = molgenisTransactionManager.doGetTransaction();
		assertNotNull(trans);
		assertTrue(trans instanceof MolgenisTransaction);

		MolgenisTransaction molgenisTransaction = (MolgenisTransaction) trans;
		assertEquals(molgenisTransaction.getId(), id);
	}
}
