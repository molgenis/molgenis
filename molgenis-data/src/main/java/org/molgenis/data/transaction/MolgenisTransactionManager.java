package org.molgenis.data.transaction;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.support.UuidGenerator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * TransactionManager used by Molgenis.
 * 
 * Extends the JpaTransactionManager because that is needed for JPA to work.
 * 
 * TransactionListeners can be registered and will be notified on transaction begin, commit and rollback of transactions
 * that are not readonly.
 * 
 * Each transaction is given a unique transaction id.
 * 
 */
public class MolgenisTransactionManager extends JpaTransactionManager
{
	private static final long serialVersionUID = 1L;
	private UuidGenerator idGenerator = new UuidGenerator();
	private List<MolgenisTransactionListener> transactionListeners = new ArrayList<>();

	public MolgenisTransactionManager()
	{
		super();
		setNestedTransactionAllowed(false);
	}

	public void addTransactionListener(MolgenisTransactionListener transactionListener)
	{
		transactionListeners.add(transactionListener);
	}

	@Override
	protected Object doGetTransaction() throws TransactionException
	{
		Object jpaTransaction = super.doGetTransaction();
		String id = idGenerator.generateId().toLowerCase();

		return new MolgenisTransaction(id, jpaTransaction);
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException
	{
		MolgenisTransaction molgenisTransaction = (MolgenisTransaction) transaction;
		super.doBegin(molgenisTransaction.getJpaTransaction(), definition);

		if (!definition.isReadOnly())
		{
			transactionListeners.forEach(j -> j.transactionStarted(molgenisTransaction.getId()));
		}
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) throws TransactionException
	{
		MolgenisTransaction transaction = (MolgenisTransaction) status.getTransaction();

		DefaultTransactionStatus jpaTransactionStatus = new DefaultTransactionStatus(transaction.getJpaTransaction(),
				status.isNewTransaction(), status.isNewSynchronization(), status.isReadOnly(), status.isDebug(),
				status.getSuspendedResources());

		super.doCommit(jpaTransactionStatus);

		if (!status.isReadOnly())
		{
			transactionListeners.forEach(j -> j.commitTransaction(transaction.getId()));
		}
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) throws TransactionException
	{
		MolgenisTransaction transaction = (MolgenisTransaction) status.getTransaction();

		DefaultTransactionStatus jpaTransactionStatus = new DefaultTransactionStatus(transaction.getJpaTransaction(),
				status.isNewTransaction(), status.isNewSynchronization(), status.isReadOnly(), status.isDebug(),
				status.getSuspendedResources());

		super.doRollback(jpaTransactionStatus);

		if (!status.isReadOnly())
		{
			transactionListeners.forEach(j -> j.rollbackTransaction(transaction.getId()));
		}
	}

	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status)
	{
		MolgenisTransaction transaction = (MolgenisTransaction) status.getTransaction();

		DefaultTransactionStatus jpaTransactionStatus = new DefaultTransactionStatus(transaction.getJpaTransaction(),
				status.isNewTransaction(), status.isNewSynchronization(), status.isReadOnly(), status.isDebug(),
				status.getSuspendedResources());

		super.doSetRollbackOnly(jpaTransactionStatus);
	}

	@Override
	protected boolean isExistingTransaction(Object transaction)
	{
		return super.isExistingTransaction(((MolgenisTransaction) transaction).getJpaTransaction());
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction)
	{
		super.doCleanupAfterCompletion(((MolgenisTransaction) transaction).getJpaTransaction());
	}

	@Override
	protected Object doSuspend(Object transaction)
	{
		return super.doSuspend(((MolgenisTransaction) transaction).getJpaTransaction());
	}

	@Override
	protected void doResume(Object transaction, Object suspendedResources)
	{
		super.doResume(((MolgenisTransaction) transaction).getJpaTransaction(), suspendedResources);
	}

}
