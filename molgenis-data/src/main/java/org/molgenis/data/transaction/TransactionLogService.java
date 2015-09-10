package org.molgenis.data.transaction;

import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.util.EntityUtils.getReferencingEntityMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.PessimisticLockingException;
import org.molgenis.data.Query;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.Pair;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionLogService implements MolgenisTransactionListener, ApplicationListener<ContextRefreshedEvent>,
		Ordered
{
	public static final List<String> EXCLUDED_ENTITIES = Arrays.asList(MolgenisTransactionLogEntryMetaData.ENTITY_NAME,
			MolgenisTransactionLogMetaData.ENTITY_NAME, LockMetaData.ENTITY_NAME);

	private final DataService dataService;
	private final MolgenisTransactionLogMetaData molgenisTransactionLogMetaData;
	private final MolgenisTransactionLogEntryMetaData molgenisTransactionLogEntryMetaData;
	private final LockMetaData lockMetaData;
	private final AsyncTransactionLog asyncTransactionLog;

	public TransactionLogService(DataService dataService,
			MolgenisTransactionLogMetaData molgenisTransactionLogMetaData,
			MolgenisTransactionLogEntryMetaData molgenisTransactionLogEntryMetaData, LockMetaData lockMetaData,
			AsyncTransactionLog asyncTransactionLog)
	{
		this.dataService = dataService;
		this.molgenisTransactionLogMetaData = molgenisTransactionLogMetaData;
		this.molgenisTransactionLogEntryMetaData = molgenisTransactionLogEntryMetaData;
		this.lockMetaData = lockMetaData;
		this.asyncTransactionLog = asyncTransactionLog;
	}

	@Override
	public void transactionStarted(String transactionId)
	{
		// System.out.println("Start trans:" + transactionId);
		Entity trans = new DefaultEntity(molgenisTransactionLogMetaData, dataService);
		trans.set(MolgenisTransactionLogMetaData.TRANSACTION_ID, transactionId);
		trans.set(MolgenisTransactionLogMetaData.USER_NAME, SecurityUtils.getCurrentUsername());
		trans.set(MolgenisTransactionLogMetaData.STATUS, MolgenisTransactionLogMetaData.Status.STARTED.name());
		trans.set(MolgenisTransactionLogMetaData.START_TIME, new Date());

		RunAsSystemProxy.runAsSystem(() -> {
			dataService.add(MolgenisTransactionLogMetaData.ENTITY_NAME, trans);
			return null;
		});
	}

	@Override
	public void commitTransaction(String transactionId)
	{
		finishTransaction(transactionId, MolgenisTransactionLogMetaData.Status.COMMITED);
	}

	@Override
	public void rollbackTransaction(String transactionId)
	{
		finishTransaction(transactionId, MolgenisTransactionLogMetaData.Status.ROLLBACK);
	}

	/**
	 * Remove all pessimistic locks
	 */
	public void removeAllLocks()
	{
		runAsSystem(() -> {
			Query q = dataService.query(LockMetaData.ENTITY_NAME);
			if (q.count() > 0)
			{
				dataService.delete(LockMetaData.ENTITY_NAME, q.findAll());
			}
			return null;
		});
	}

	/**
	 * Throw PessimisticLockingException if the operation type is locked for the entity.
	 * 
	 * @param entityName
	 * @param type
	 */
	public synchronized void checkLocks(String entityName, MolgenisTransactionLogEntryMetaData.Type type)
	{
		runAsSystem(() -> {
			String attrName = null;
			switch (type)
			{
				case ADD:
					attrName = LockMetaData.ADD_LOCKED;
					break;
				case DELETE:
					attrName = LockMetaData.DELETE_LOCKED;
					break;
				case UPDATE:
					attrName = LockMetaData.UPDATE_LOCKED;
					break;
			}

			Query q = dataService.query(LockMetaData.ENTITY_NAME).eq(LockMetaData.ENTITY, entityName).and()
					.eq(attrName, true);
			String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
			if (transactionId != null)
			{
				q.and()
						.not()
						.eq(LockMetaData.MOLGENIS_TRANSACTION_LOG + "." + MolgenisTransactionLogMetaData.TRANSACTION_ID,
								transactionId);
			}

			if (q.count() > 0)
			{
				throw new PessimisticLockingException("Entity locked by another user. Please try again later.");
			}
			return null;
		});
	}

	/**
	 * Log and create locks for an add/update/delete operation on a Repository
	 * 
	 * @param entityMetaData
	 * @param type
	 */
	public synchronized void logAndLock(EntityMetaData entityMetaData, MolgenisTransactionLogEntryMetaData.Type type)
	{
		String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
		if (transactionId != null)
		{
			runAsSystem(() -> {
				Entity log = dataService.findOne(MolgenisTransactionLogMetaData.ENTITY_NAME, transactionId);
				if (log != null)
				{
					Entity logEntry = new DefaultEntity(molgenisTransactionLogEntryMetaData, dataService);
					logEntry.set(MolgenisTransactionLogEntryMetaData.MOLGENIS_TRANSACTION_LOG, log);
					logEntry.set(MolgenisTransactionLogEntryMetaData.ENTITY, entityMetaData.getName());
					logEntry.set(MolgenisTransactionLogEntryMetaData.TYPE, type);
					// dataService.add(MolgenisTransactionLogEntryMetaData.ENTITY_NAME, logEntry);

					asyncTransactionLog.addLogEntry(logEntry);

					switch (type)
					{
						case ADD:
							boolean hasNonReadOnlyUniqueAttribute = false;

							// lock deletes on each referred_to entity [to protect foreign key]
							List<Entity> locksAdd = new ArrayList<>();
							for (AttributeMetaData attr : entityMetaData.getAtomicAttributes())
							{
								if (attr.getRefEntity() != null)
								{
									Entity lock = new DefaultEntity(lockMetaData, dataService);
									lock.set(LockMetaData.MOLGENIS_TRANSACTION_LOG, log);
									lock.set(LockMetaData.ENTITY, attr.getRefEntity().getName());
									lock.set(LockMetaData.ADD_LOCKED, false);
									lock.set(LockMetaData.DELETE_LOCKED, true);
									lock.set(LockMetaData.UPDATE_LOCKED, false);
									locksAdd.add(lock);
								}

								if (attr.isUnique() && !attr.isReadonly()) hasNonReadOnlyUniqueAttribute = true;
							}

							if (!locksAdd.isEmpty()) dataService.add(LockMetaData.ENTITY_NAME, locksAdd);

							// lock inserts + updates when non-readonly unique fields [to ensure uniqueness of
							// updateable
							// field]
							// lock current entity when identifier type!=autoid fail y [to ensure uniqueness of
							// primary key]
							if (hasNonReadOnlyUniqueAttribute
									|| ((entityMetaData.getIdAttribute() != null) && !entityMetaData.getIdAttribute()
											.isAuto()))
							{
								Entity lock = new DefaultEntity(lockMetaData, dataService);
								lock.set(LockMetaData.MOLGENIS_TRANSACTION_LOG, log);
								lock.set(LockMetaData.ENTITY, entityMetaData.getName());
								lock.set(LockMetaData.ADD_LOCKED, true);
								lock.set(LockMetaData.DELETE_LOCKED, false);
								lock.set(LockMetaData.UPDATE_LOCKED, true);
								dataService.add(LockMetaData.ENTITY_NAME, lock);
							}

							break;

						case DELETE:
							// lock inserts + updates in referring_to_this entities
							List<Entity> locksDelete = new ArrayList<>();

							for (Pair<EntityMetaData, List<AttributeMetaData>> referencing : getReferencingEntityMetaData(
									entityMetaData, dataService))
							{
								Entity lock = new DefaultEntity(lockMetaData, dataService);
								lock.set(LockMetaData.MOLGENIS_TRANSACTION_LOG, log);
								lock.set(LockMetaData.ENTITY, referencing.getA().getName());
								lock.set(LockMetaData.ADD_LOCKED, true);
								lock.set(LockMetaData.DELETE_LOCKED, false);
								lock.set(LockMetaData.UPDATE_LOCKED, true);
								locksDelete.add(lock);
							}

							if (!locksDelete.isEmpty()) dataService.add(LockMetaData.ENTITY_NAME, locksDelete);

							break;
						case UPDATE:
							// lock deletes and updates on referred_to entities [to protect foreign key]
							List<Entity> locksUpdate = new ArrayList<>();
							for (AttributeMetaData attr : entityMetaData.getAtomicAttributes())
							{
								if (attr.getRefEntity() != null)
								{

									Entity lock = new DefaultEntity(lockMetaData, dataService);
									lock.set(LockMetaData.MOLGENIS_TRANSACTION_LOG, log);
									lock.set(LockMetaData.ENTITY, attr.getRefEntity().getName());
									lock.set(LockMetaData.ADD_LOCKED, false);
									lock.set(LockMetaData.DELETE_LOCKED, true);
									lock.set(LockMetaData.UPDATE_LOCKED, true);
									locksUpdate.add(lock);
								}
							}
							if (!locksUpdate.isEmpty()) dataService.add(LockMetaData.ENTITY_NAME, locksUpdate);

							break;
					}
				}

				return null;
			});
		}
	}

	private synchronized void finishTransaction(String transactionId, MolgenisTransactionLogMetaData.Status status)
	{
		RunAsSystemProxy.runAsSystem(() -> {
			Entity log = dataService.findOne(MolgenisTransactionLogMetaData.ENTITY_NAME, transactionId);
			try
			{
				log.set(MolgenisTransactionLogMetaData.END_TIME, new Date());
				log.set(MolgenisTransactionLogMetaData.STATUS, status.name());
				asyncTransactionLog.logTransactionFinished(log);
			}
			finally
			{
				// Release locks
				Iterable<Entity> locks = dataService.query(LockMetaData.ENTITY_NAME)
						.eq(LockMetaData.MOLGENIS_TRANSACTION_LOG, log).findAll();
				dataService.delete(LockMetaData.ENTITY_NAME, locks);
			}
			return null;
		});
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		// If the server was shutdown during a transaction the locks are not released, release them now.
		removeAllLocks();
	}

}
