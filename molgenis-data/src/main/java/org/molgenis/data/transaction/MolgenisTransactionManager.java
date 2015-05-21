package org.molgenis.data.transaction;

import org.molgenis.data.support.UuidGenerator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.DefaultTransactionStatus;

public class MolgenisTransactionManager extends JpaTransactionManager
{
	private static final long serialVersionUID = 1L;
	private UuidGenerator idGenerator = new UuidGenerator();

	@Override
	protected Object doGetTransaction() throws TransactionException
	{
		System.out.println("doGetTransaction");
		Object jpaTransaction = super.doGetTransaction();
		return new MolgenisTransaction(idGenerator.generateId(), jpaTransaction);
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException
	{
		System.out.println("BEGIN TRANS: " + transaction + ", readOnly=" + definition.isReadOnly());
		super.doBegin(((MolgenisTransaction) transaction).getJpaTransaction(), definition);
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) throws TransactionException
	{
		MolgenisTransaction transaction = (MolgenisTransaction) status.getTransaction();
		System.out.println("COMMIT TRANS:" + transaction + ",new=" + status.isNewTransaction());

		DefaultTransactionStatus jpaTransactionStatus = new DefaultTransactionStatus(transaction.getJpaTransaction(),
				status.isNewTransaction(), status.isNewSynchronization(), status.isReadOnly(), status.isDebug(),
				status.getSuspendedResources());

		super.doCommit(jpaTransactionStatus);
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) throws TransactionException
	{
		MolgenisTransaction transaction = (MolgenisTransaction) status.getTransaction();
		System.out.println("ROLLBACK TRANS:" + transaction);

		DefaultTransactionStatus jpaTransactionStatus = new DefaultTransactionStatus(transaction.getJpaTransaction(),
				status.isNewTransaction(), status.isNewSynchronization(), status.isReadOnly(), status.isDebug(),
				status.getSuspendedResources());

		super.doRollback(jpaTransactionStatus);
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
