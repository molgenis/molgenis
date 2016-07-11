package org.molgenis.data.listeners;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jonathanjetten on 7/8/16.Í
 */
@Service
public class EntityListenersService
{
	private static final ConcurrentHashMap<String, SetMultimap<Object, EntityListener>> entityListeners = new ConcurrentHashMap<String, SetMultimap<Object, EntityListener>>();

	/**
	 * returns a SetMultimap<Object, EntityListener> where the object is the id of the entity and the EntityListener is listener interface
	 *
	 * @param repoFullName
	 * @return SetMultimap<Object, EntityListener>
	 */
	public synchronized SetMultimap<Object, EntityListener> getEntityListeners(String repoFullName)
	{
		if (!entityListeners.containsKey(repoFullName))
			entityListeners.put(repoFullName, HashMultimap.<Object, EntityListener>create());
		return entityListeners.get(repoFullName);
	}

	/**
	 * Removes SetMultimap<Object, EntityListener> that is associated with the repo and cleans the listeners
	 *
	 * @param repoFullName
	 * @return SetMultimap<Object, EntityListener>
	 */
	public synchronized SetMultimap<Object, EntityListener> removeEntityListeners(String repoFullName)
	{
		if (!entityListeners.containsKey(repoFullName))
			entityListeners.get(repoFullName).clear();
		return entityListeners.remove(repoFullName);
	}
}

