package org.molgenis.diseasematcher.service;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PoolUtils;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

/**
 * Very simple single purpose OMIM Service for retrieving OMIM JSON objects.
 * 
 * @author tommydeboer
 * 
 */
@Service
public class OmimService
{

	private ObjectPool<String> pool;

	/**
	 * Constructor for OmimServices. Takes OMIM API keys from molgenis.properties and puts them in a pool.
	 * 
	 * @param apiKeys
	 *            list of OMIM API keys from molgenis.properties
	 */
	@Autowired
	public OmimService(@Value("#{'${omim_key:@null}'.split(',')}") List<String> apiKeys)
	{
		// initialize a pool for OMIM keys to circulate them and protect them from over-use
		PooledOmimKeyFactory keyFactory = new PooledOmimKeyFactory(apiKeys);

		PooledObjectFactory<String> syncFactory = PoolUtils.synchronizedPooledFactory(keyFactory);

		GenericObjectPool<String> genericPool = new GenericObjectPool<String>(syncFactory);
		genericPool.setBlockWhenExhausted(true);
		genericPool.setLifo(false);
		genericPool.setMaxTotal(apiKeys.size());

		pool = PoolUtils.synchronizedPool(genericPool);
	}

	/**
	 * Retrieves data form the OMIM service and returns it.
	 * 
	 * @param omimId
	 *            the OMIM identifier to request
	 * @param out
	 *            OutputStream with the response
	 */
	public void getOmimData(String omimId, OutputStream out)
	{
		String apiKey = null;
		try
		{
			apiKey = pool.borrowObject();

			URL omimRequest = new URL(buildQueryURIString(omimId, apiKey));
			FileCopyUtils.copy(omimRequest.openStream(), out);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if (apiKey != null)
			{
				try
				{
					pool.returnObject(apiKey);
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * Builds an OMIM API URI query based on an OMIM id and an API key.
	 * 
	 * @param omimId
	 *            the OMIM identifier to request
	 * @param apiKey
	 *            the OMIM API key to use
	 * @return a formatted OMIM API URI
	 * @throws UnsupportedEncodingException
	 */
	protected String buildQueryURIString(String omimId, String apiKey) throws UnsupportedEncodingException
	{
		return String
				.format("http://api.europe.omim.org/api/entry?mimNumber=%s&include=text&include=clinicalSynopsis&format=json&apiKey=%s",
						omimId, URLEncoder.encode(apiKey, "UTF-8"));
	}

}