package org.molgenis.data.index.transaction;

import org.mockito.Mock;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class IndexTransactionListenerTest
{
	private IndexTransactionListener indexTransactionListener;
	@Mock
	private IndexJobScheduler indexJobScheduler;
	@Mock
	private IndexActionRegisterService indexActionRegisterService;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
		indexTransactionListener = new IndexTransactionListener(indexJobScheduler, indexActionRegisterService);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		reset(indexJobScheduler, indexActionRegisterService);
	}

	@Test
	public void testAfterCommitTransactionStoresIndexActions()
	{
		indexTransactionListener.afterCommitTransaction("ABCDE");
		verify(indexActionRegisterService).storeIndexActions("ABCDE");
		verifyNoMoreInteractions(indexActionRegisterService);
		verifyZeroInteractions(indexJobScheduler);
	}

	@Test
	public void testAfterCommitTransactionCatchesException()
	{
		doThrow(new MolgenisDataException()).when(indexActionRegisterService).storeIndexActions("ABCDE");
		indexTransactionListener.afterCommitTransaction("ABCDE");
		verify(indexActionRegisterService).storeIndexActions("ABCDE");
		verifyNoMoreInteractions(indexActionRegisterService);
		verifyZeroInteractions(indexJobScheduler);
	}

	@Test
	public void testCommitTransactionDoesNothing()
	{
		indexTransactionListener.commitTransaction("ABCDE");
		verifyZeroInteractions(indexJobScheduler, indexActionRegisterService);
	}

	@Test
	public void testRollbackTransactionForgetsChanges()
	{
		indexTransactionListener.rollbackTransaction("ABCDE");
		verify(indexActionRegisterService).forgetIndexActions("ABCDE");
		verifyNoMoreInteractions(indexActionRegisterService);
		verifyZeroInteractions(indexJobScheduler);
	}

	@Test
	public void testDoCleanupAfterCompletionSchedulesJobIfWorkNeeded()
	{
		when(indexActionRegisterService.forgetIndexActions("ABCDE")).thenReturn(true);
		indexTransactionListener.doCleanupAfterCompletion("ABCDE");
		verify(indexActionRegisterService).forgetIndexActions("ABCDE");
		verify(indexJobScheduler).scheduleIndexJob("ABCDE");
		verifyNoMoreInteractions(indexActionRegisterService, indexJobScheduler);
	}

	@Test
	public void testDoCleanupAfterCompletionDoesNothingIfNoWorkNeeded()
	{
		when(indexActionRegisterService.forgetIndexActions("ABCDE")).thenReturn(false);
		indexTransactionListener.doCleanupAfterCompletion("ABCDE");
		verify(indexActionRegisterService).forgetIndexActions("ABCDE");
		verifyNoMoreInteractions(indexActionRegisterService);
		verifyZeroInteractions(indexActionRegisterService);
	}
}