package org.molgenis.data.transaction;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;


import org.molgenis.data.support.UuidGenerator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;

public class MolgenisTransactionManagerTest
{
	private MolgenisTransactionManager molgenisTransactionManager;

	@BeforeMethod
	public void beforeMethod()
	{
		molgenisTransactionManager = new MolgenisTransactionManager(new UuidGenerator(), mock(DataSource.class));
	}

	@Test
	public void doGetTransaction()
	{
		Object trans = molgenisTransactionManager.doGetTransaction();
		assertNotNull(trans);
		assertTrue(trans instanceof MolgenisTransaction);

		MolgenisTransaction molgenisTransaction = (MolgenisTransaction) trans;
		assertNotNull(molgenisTransaction.getId());
	}

}
