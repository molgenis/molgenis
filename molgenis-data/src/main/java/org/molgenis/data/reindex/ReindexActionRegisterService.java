package org.molgenis.data.reindex;

import org.molgenis.data.reindex.meta.ReindexActionMetaData;
import org.molgenis.security.core.runas.RunAsSystem;

/**
 * Created by fkelpin on 15/07/16.
 */
public interface ReindexActionRegisterService
{
	/**
	 * Excludes an entity from being reindexed.
	 *
	 * @param entityFullName fully qualified name of the entity to exclude
	 */
	void addExcludedEntity(String entityFullName);

	/**
	 * Log and create locks for an add/update/delete operation on a Repository
	 *
	 * @param entityFullName the fully qualified name of the {@link org.molgenis.data.Repository}
	 * @param cudType        the {@link ReindexActionMetaData.CudType} of the action
	 * @param dataType       the {@link ReindexActionMetaData.DataType} of the action
	 * @param entityId       the ID of the entity, may be null to indicate change to entire repository
	 */
	void register(String entityFullName, ReindexActionMetaData.CudType cudType, ReindexActionMetaData.DataType dataType,
			String entityId);

	/**
	 * Stores the reindex actions in the repository.
	 * Creates a ReindesActionJob to group them by.
	 *
	 * @param transactionId ID for the transaction the reindex actions were registered under
	 */
	@RunAsSystem
	void storeReindexActions(String transactionId);

	/**
	 * Removes all reindex actions registered for a transaction.
	 *
	 * @param transactionId ID for the transaction the reindex actions were registered under
	 * @return boolean indicating if any work was present
	 */
	boolean forgetReindexActions(String transactionId);
}
