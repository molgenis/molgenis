package org.molgenis.data.index;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.runas.RunAsSystem;

/**
 * Registers changes made to an indexed repository that need to be fixed by indexing
 * the relevant data.
 */
public interface IndexActionRegisterService
{
	/**
	 * Excludes an entity from being indexed.
	 *
	 * @param entityFullName fully qualified name of the entity to exclude
	 */
	void addExcludedEntity(String entityFullName);

	/**
	 * Log and create locks for an add/update/delete operation on a Repository
	 *
	 * @param entityType
	 * @param entityId   the ID of the entity, may be null to indicate change to entire repository
	 */
	void register(EntityType entityType, String entityId);

	/**
	 * Stores the index actions in the repository.
	 * Creates a ReindexActionJob to group them by.
	 * storeIndexActions uses a set as optimization to remove the double index actions before the index actions are stored in database.
	 *
	 * @param transactionId ID for the transaction the index actions were registered under
	 */
	@RunAsSystem
	void storeIndexActions(String transactionId);

	/**
	 * Removes all index actions registered for a transaction.
	 *
	 * @param transactionId ID for the transaction the index actions were registered under
	 * @return boolean indicating if any work was present
	 */
	boolean forgetIndexActions(String transactionId);
}
