package org.molgenis.diseasematcher.service;

import org.apache.commons.pool2.PooledObjectState;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Extended DefaultPooledObject that also keeps track of the times an object is used. OMIM keys can only be used four
 * times a second.
 * 
 * @author tommydeboer
 */

public class PooledOmimKey extends DefaultPooledObject<String>
{

	private int timesUsed = 0;
	private long firstUseTime = 0;

	public PooledOmimKey(String object)
	{
		super(object);
	}

	/**
	 * Allocates the object. Keeps track of usage per second so OMIM API keys do not get used more than 4 times a second
	 * (this gets the key banned).
	 * 
	 * @return {@code true} if the original state was {@link PooledObjectState#IDLE IDLE} and the object was used less
	 *         than 4 times within one second
	 */
	@Override
	public synchronized boolean allocate()
	{
		long currentTime = System.currentTimeMillis();

		if (currentTime > firstUseTime + 1000)
		{
			firstUseTime = currentTime;
			timesUsed = 0;
		}

		if (timesUsed < 4 && currentTime < firstUseTime + 1000)
		{
			timesUsed++;

			return super.allocate();
		}
		else
		{
			return false;
		}

	}

}
