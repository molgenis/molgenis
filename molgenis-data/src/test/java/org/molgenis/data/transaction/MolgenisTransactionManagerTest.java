package org.molgenis.data.transaction;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.sql.DataSource;

import org.molgenis.data.support.UuidGenerator;
import org.testng.annotations.Test;

public class MolgenisTransactionManagerTest
{
	private MolgenisTransactionManager molgenisTransactionManager = new MolgenisTransactionManager(new UuidGenerator(),
			mock(DataSource.class));

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
