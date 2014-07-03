package org.molgenis.diseasematcher.controller;

import org.apache.commons.pool2.PooledObjectState;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class PooledOmimKey extends DefaultPooledObject<String>
{

	private int timesUsed = 0;
	private long firstUseTime = 0;

	public PooledOmimKey(String object)
	{
		super(object);
	}

	/**
	 * Allocates the object.
	 * 
	 * @return {@code true} if the original state was {@link PooledObjectState#IDLE IDLE}
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
