package org.molgenis.diseasematcher.controller;

import java.util.List;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;

/**
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
	 */
	public PooledOmimKeyFactory(List<String> omimKeys)
	{
		super();
		this.omimKeys = omimKeys;
	}

	/**
	 * 
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

	/**
	 * 
	 */
	@Override
	public PooledObject<String> wrap(String key)
	{
		return new PooledOmimKey(key);
	}

}
