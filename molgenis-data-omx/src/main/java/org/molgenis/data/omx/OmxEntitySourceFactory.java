package org.molgenis.data.omx;

import org.molgenis.data.EntitySource;
import org.molgenis.data.EntitySourceFactory;

/**
 * EntitySourceFactory implementation for omx (DataSet matrix)
 * 
 */
public class OmxEntitySourceFactory implements EntitySourceFactory
{
	private final EntitySource omxEntitySource;

	public OmxEntitySourceFactory(EntitySource omxEntitySource)
	{
		this.omxEntitySource = omxEntitySource;
	}

	@Override
	public String getUrlPrefix()
	{
		return "omx://";
	}

	@Override
	public EntitySource create(String url)
	{
		return omxEntitySource;
	}

}
