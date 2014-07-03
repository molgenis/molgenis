package org.molgenis.diseasematcher.controller;

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
	/**
	 * 
	 */
	private ObjectPool<String> pool;

	/**
	 * 
	 * @param apiKeys
	 */
	@Autowired
	public OmimService(@Value("#{'${omim_key:@null}'.split(',')}") List<String> apiKeys)
	{
		PooledOmimKeyFactory keyFactory = new PooledOmimKeyFactory(apiKeys);

		PooledObjectFactory<String> syncFactory = PoolUtils.synchronizedPooledFactory(keyFactory);

		GenericObjectPool<String> genericPool = new GenericObjectPool<String>(syncFactory);
		genericPool.setBlockWhenExhausted(true);
		genericPool.setLifo(false);
		genericPool.setMaxTotal(apiKeys.size());

		pool = PoolUtils.synchronizedPool(genericPool);
	}

	/**
	 * 
	 * @param omimId
	 * @param out
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
	 * 
	 * @param omimId
	 * @param apiKey
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	protected String buildQueryURIString(String omimId, String apiKey) throws UnsupportedEncodingException
	{
		return String
				.format("http://api.europe.omim.org/api/entry?mimNumber=%s&include=text&include=clinicalSynopsis&format=json&apiKey=%s",
						omimId, URLEncoder.encode(apiKey, "UTF-8"));
	}

}