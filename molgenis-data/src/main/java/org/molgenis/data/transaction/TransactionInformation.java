package org.molgenis.data.transaction;

import org.molgenis.data.EntityKey;
import org.molgenis.data.meta.model.EntityType;

import java.util.Set;

/**
 * Gives information about the current transaction's changes to entities.
 * An entity or repository is considered dirty if it has been modified during this transaction.
 * If no transaction is currently running, everything is clean.
 */
public interface TransactionInformation
{
	/**
	 * Finds out if a specific entity instance has been dirtied in the current transaction.
	 *
	 * @param entityKey {@link EntityKey} representing the entity instance
	 * @return indication if the entity instance has been dirtied
	 */
	boolean isEntityDirty(EntityKey entityKey);

	/**
	 * Finds out if an entire repository has been dirtied in the current transaction.
	 * Even if this method returns false, {@link #isEntityDirty(EntityKey)}
	 * can still return true for one or more entities
	 *
	 * @param entityType of the repository
	 * @return indication if the entire repository has been dirtied
	 */
	boolean isEntireRepositoryDirty(EntityType entityType);

	/**
	 * Finds out if a repository is completely clean in the current transaction
	 *
	 * @param entityType of the repository
	 * @return indication if the entire repository is clean
	 */
	boolean isRepositoryCompletelyClean(EntityType entityType);

	/**
	 * Get all IDs of specific entity instances that have been dirtied by this transaction.
	 *
	 * @return Set of {@link EntityKey} s for all entity instances touched.
	 */
	Set<EntityKey> getDirtyEntities();

	/**
	 * Get all repositories that have been dirtied by this transaction.
	 * This happens during a streaming update or insert or delete of data or a metadata change.
	 * If a single row has been updated or multiple calls to single update methods have been made, the specific rows will
	 * not be returned here but in {@link #getDirtyEntities()} instead.
	 *
	 * @return Set of {@link String}s with fully qualified names of the dirty repositories
	 */
	Set<String> getEntirelyDirtyRepositories();

	/**
	 * Get all repositories that have been dirtied by this transaction,
	 * whether it is one entity, or a complete repository
	 *
	 * @return Set of {@link String}s with fully qualified names of the dirty repositories
	 */
	Set<String> getDirtyRepositories();
}
