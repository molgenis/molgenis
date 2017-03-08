package org.molgenis.data.elasticsearch.index.job;

import com.google.common.util.concurrent.AtomicLongMap;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

/**
 * Keeps track of outstanding index actions.
 */
public class IndexStatus
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
				LOG.debug("All entities stable.");
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

	private boolean isIndexStableIncludingReferences(EntityType emd)
	{
		if (isAllIndicesStable())
		{
			return true;
		}
		Set<String> referencedEntityNames = stream(emd.getAtomicAttributes().spliterator(), false)
				.map(Attribute::getRefEntity).filter(e -> e != null).map(EntityType::getFullyQualifiedName).collect(toSet());
		referencedEntityNames.add(emd.getFullyQualifiedName());
		return referencedEntityNames.stream().noneMatch(actionCountsPerEntity::containsKey);
	}

	void waitForIndexToBeStableIncludingReferences(EntityType emd) throws InterruptedException
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
