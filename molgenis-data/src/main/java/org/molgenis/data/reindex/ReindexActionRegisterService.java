package org.molgenis.data.reindex;

import static org.molgenis.data.transaction.MolgenisTransactionManager.TRANSACTION_ID_RESOURCE_NAME;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.Arrays;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.reindex.job.ReindexJobExecutionMetaInterface;
import org.molgenis.data.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.CudType;
import org.molgenis.data.reindex.meta.ReindexActionMetaData.DataType;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Registers changes made to an indexed repository that need to be fixed by reindexing
 * the relevant data.
 */
public class ReindexActionRegisterService
{
	private static final Logger LOG = LoggerFactory.getLogger(ReindexActionRegisterService.class);

	public static final List<String> EXCLUDED_ENTITIES = Arrays.asList(ReindexActionMetaData.ENTITY_NAME,
			ReindexActionJobMetaData.ENTITY_NAME, ReindexJobExecutionMetaInterface.REINDEX_JOB_EXECUTION);

	private final DataService dataService;
	private final ReindexActionJobMetaData reindexActionJobMetaData;
	private final ReindexActionMetaData reindexActionMetaData;

	public ReindexActionRegisterService(DataService dataService, ReindexActionJobMetaData reindexActionJobMetaData,
			ReindexActionMetaData reindexActionMetaData)
	{
		this.dataService = dataService;
		this.reindexActionJobMetaData = reindexActionJobMetaData;
		this.reindexActionMetaData = reindexActionMetaData;
	}
	
	/**
	 * TODO JJ
	 * 
	 * @param entityFullName
	 * @param attributeName
	 */
	public void registerAddAttribute(String entityFullName, String attributeName)
	{
		LOG.info("registerAddAttribute(entityFullName: {}, attributeName: {})", entityFullName,
				attributeName);
		this.register(entityFullName, CudType.UPDATE, DataType.METADATA, null);

		/**
		 * FIXME
		 * Some entities are not registered in the entities table
		 * fullEntityName = Entities; Attributes; Packages;
		 */
		if (!EntityMetaDataMetaData.ENTITY_NAME.equals(entityFullName)
				&& !AttributeMetaDataMetaData.ENTITY_NAME.equals(entityFullName)
				&& !PackageMetaData.ENTITY_NAME.equals(entityFullName))
		{
			QueryImpl<Entity> q1 = new QueryImpl<Entity>();
			q1.eq(EntityMetaDataMetaData.FULL_NAME, entityFullName);
			Entity e1 = dataService.findOne(EntityMetaDataMetaData.ENTITY_NAME, q1);

			QueryImpl<Entity> q2 = new QueryImpl<Entity>();
			q2.eq(AttributeMetaDataMetaData.NAME, attributeName);
			Entity e2 = dataService.findOne(AttributeMetaDataMetaData.ENTITY_NAME, q2);
		}
		else
		{
			LOG.info("Entity [{}] is not allowed", entityFullName);
		}
	}
	
	/**
	 * TODO JJ
	 * 
	 * @param entityFullName
	 */
	public void registerDeleteEntityMetaData(String entityFullName)
	{
		LOG.info("registerDeleteEntityMetaData(entityFullName: {})", entityFullName);
		this.register(entityFullName, CudType.DELETE, DataType.METADATA, null);
	}
	
	/**
	 * TODO JJ
	 * 
	 * @param entityFullName
	 */
	public void registerAddEntityMetaData(String entityFullName)
	{
		LOG.info("addEntityMetaData(entityFullName: {})", entityFullName);
		this.register(entityFullName, CudType.CREATE, DataType.METADATA, null);
	}

	/**
	 * TODO JJ
	 * 
	 * @param entityFullName
	 * @param attributeName
	 */
	public void registerDeleteAttribute(String entityFullName, String attributeName)
	{
		LOG.info("registerDeleteAttribute(entityFullName: {}, attributeName: {})", entityFullName, attributeName);
		this.register(entityFullName, CudType.UPDATE, DataType.METADATA, null);
	}

	/**
	 * Log and create locks for an add/update/delete operation on a Repository
	 * 
	 * @param entityMetaData
	 * @param cudType
	 * @return
	 */
	public synchronized void register(String entityFullName, CudType cudType, DataType dataType, String entityId)
	{
		if (!ReindexActionRegisterService.EXCLUDED_ENTITIES.contains(entityFullName))
		{
			String transactionId = (String) TransactionSynchronizationManager.getResource(TRANSACTION_ID_RESOURCE_NAME);
			if (transactionId != null)
			{
				runAsSystem(() -> {
					Entity reindexActionJob = dataService.findOneById(ReindexActionJobMetaData.ENTITY_NAME, transactionId);

					if (reindexActionJob == null)
					{
						reindexActionJob = this.createReindexActionJob(transactionId);
						dataService.add(ReindexActionJobMetaData.ENTITY_NAME, reindexActionJob);
					}

					int actionOrder = increaseCountReindexActionJob(reindexActionJob);
					Entity reindexAction = this.createReindexAction(reindexActionJob, entityFullName,
							cudType, dataType, entityId, actionOrder);
					dataService.add(ReindexActionMetaData.ENTITY_NAME, reindexAction);
				});
			}
			else
			{
				LOG.warn("Transaction id is unknown");
			}
		}
	}

	public int increaseCountReindexActionJob(Entity reindexActionJob)
	{
		int count = reindexActionJob.getInt(ReindexActionJobMetaData.COUNT).intValue() + 1;
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, count);
		dataService.update(ReindexActionJobMetaData.ENTITY_NAME, reindexActionJob);
		return count;
	}

	public DefaultEntity createReindexActionJob(String id)
	{
		DefaultEntity reindexActionJob = new DefaultEntity(reindexActionJobMetaData, dataService);
		reindexActionJob.set(ReindexActionJobMetaData.ID, id);
		reindexActionJob.set(ReindexActionJobMetaData.COUNT, 0);
		return reindexActionJob;
	}

	public DefaultEntity createReindexAction(Entity reindexActionGroup, String entityFullName, CudType cudType,
			DataType dataType, String entityId, int actionOrder)
	{
		DefaultEntity reindexAction = new DefaultEntity(this.reindexActionMetaData, this.dataService);
		reindexAction.set(ReindexActionMetaData.REINDEX_ACTION_GROUP, reindexActionGroup);
		reindexAction.set(ReindexActionMetaData.ENTITY_FULL_NAME, entityFullName);
		reindexAction.set(ReindexActionMetaData.CUD_TYPE, cudType);
		reindexAction.set(ReindexActionMetaData.DATA_TYPE, dataType);
		reindexAction.set(ReindexActionMetaData.ENTITY_ID, entityId);
		reindexAction.set(ReindexActionMetaData.ACTION_ORDER, actionOrder);
		reindexAction.set(ReindexActionMetaData.REINDEX_STATUS, ReindexActionMetaData.ReindexStatus.PENDING);
		return reindexAction;
	}
}
