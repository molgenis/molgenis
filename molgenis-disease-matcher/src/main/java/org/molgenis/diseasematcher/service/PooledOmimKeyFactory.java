package org.molgenis.diseasematcher.service;

import java.util.List;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;

/**
 * A factory for OMIM keys for a GenericObjectPool.
 * 
 * @author tommydeboer
 * 
 */
public class PooledOmimKeyFactory extends BasePooledObjectFactory<String>
{
	private List<String> omimKeys;
	private int index = 0;

	/**
	 * 
	 * @param omimKeys
	 *            list of OMIM keys
	 */
	public PooledOmimKeyFactory(List<String> omimKeys)
	{
		super();
		this.omimKeys = omimKeys;
	}

	/**
	 * Called when an object is borrowed from the pool but does not exist yet. Throws an exception when there are more
	 * objects created than OMIM keys available (this should not happen as long as the properties of the pool are set
	 * correctly).
	 */
	@Override
	public String create() throws IllegalArgumentException
	{
		if (index >= omimKeys.size())
		{
			throw new IllegalArgumentException("Exceeded the maximum of " + omimKeys.size() + " keys");
		}

		return omimKeys.get(index++);
	}

	@Override
	public PooledObject<String> wrap(String key)
	{
		return new PooledOmimKey(key);
	}

}
