package org.molgenis.data.elasticsearch.reindex.job;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AtomicLongMap;

/**
 * Keeps track of outstanding index actions.
 */
class IndexStatus
{
	private final Lock lock = new ReentrantLock();
	private final Condition allEntitiesStable = lock.newCondition();
	private final Condition singleEntityStable = lock.newCondition();

	/**
	 * Counts how many actions are pending for each entity.
	 */
	private AtomicLongMap<String> actionCountsPerEntity = AtomicLongMap.create();

	private static final Logger LOG = LoggerFactory.getLogger(IndexStatus.class);

	void addActionCounts(Map<String, Long> actionsRegistered)
	{
		LOG.debug("addActionCounts {}", actionsRegistered);
		lock.lock();
		try
		{
			for (Map.Entry<String, Long> addedForEntity : actionsRegistered.entrySet())
			{
				final String entityName = addedForEntity.getKey();
				final Long numberOfActions = addedForEntity.getValue();
				actionCountsPerEntity.addAndGet(entityName, numberOfActions);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	void removeActionCounts(Map<String, Long> actionsPerformed)
	{
		LOG.debug("removeActionCount {}", actionsPerformed);
		lock.lock();
		try
		{
			for (Map.Entry<String, Long> actionsPerEntity : actionsPerformed.entrySet())
			{
				final String entityName = actionsPerEntity.getKey();
				Long numberOfActions = actionsPerEntity.getValue();
				if (actionCountsPerEntity.addAndGet(entityName, -numberOfActions) == 0)
				{
					actionCountsPerEntity.removeAllZeros();
					LOG.debug("Entity {} is stable.", entityName);
					singleEntityStable.signalAll();
				}
			}
			if (isAllIndicesStable())
			{
				LOG.info("All entities stable.");
				allEntitiesStable.signalAll();
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	void waitForAllEntitiesToBeStable() throws InterruptedException
	{
		lock.lock();
		try
		{
			while (!isAllIndicesStable())
			{
				allEntitiesStable.await();
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	private boolean isIndexStableIncludingReferences(EntityMetaData emd)
	{
		if (isAllIndicesStable())
		{
			return true;
		}
		Set<String> referencedEntityNames = stream(emd.getAtomicAttributes().spliterator(), false)
				.map(AttributeMetaData::getRefEntity).filter(e -> e != null).map(EntityMetaData::getName)
				.collect(toSet());
		referencedEntityNames.add(emd.getName());
		return referencedEntityNames.stream().noneMatch(actionCountsPerEntity::containsKey);
	}

	void waitForIndexToBeStableIncludingReferences(EntityMetaData emd) throws InterruptedException
	{
		lock.lock();
		try
		{
			while (!isIndexStableIncludingReferences(emd))
			{
				singleEntityStable.await();
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	private boolean isAllIndicesStable()
	{
		return actionCountsPerEntity.isEmpty();
	}

}
