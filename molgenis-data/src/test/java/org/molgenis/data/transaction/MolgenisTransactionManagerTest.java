package org.molgenis.data.transaction;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.persistence.EntityManagerFactory;

import org.molgenis.data.support.UuidGenerator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisTransactionManagerTest
{
	private MolgenisTransactionManager molgenisTransactionManager;
	private EntityManagerFactory mockEntityManagerFactory;

	@BeforeMethod
	public void beforeMethod()
	{
		molgenisTransactionManager = new MolgenisTransactionManager(new UuidGenerator());

		mockEntityManagerFactory = mock(EntityManagerFactory.class);
		molgenisTransactionManager.setEntityManagerFactory(mockEntityManagerFactory);
	}

	@Test
	public void doGetTransaction()
	{
		Object trans = molgenisTransactionManager.doGetTransaction();
		assertNotNull(trans);
		assertTrue(trans instanceof MolgenisTransaction);

		MolgenisTransaction molgenisTransaction = (MolgenisTransaction) trans;
		assertNotNull(molgenisTransaction.getId());
		assertNotNull(molgenisTransaction.getJpaTransaction());
	}

}
