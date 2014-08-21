package org.molgenis.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

public class ResourceFingerprintRegistry
{
	private final Map<String, String> resourceFingerprints;

	public ResourceFingerprintRegistry()
	{
		resourceFingerprints = new HashMap<String, String>();
	}

	public String getFingerprint(String resourceName) throws IOException
	{
		return getFingerprint(this.getClass(), resourceName);
	}

	public String getFingerprint(Class<?> contextClass, String resourceName) throws IOException
	{
		String resourceFingerprint = resourceFingerprints.get(resourceName);
		if (resourceFingerprint == null)
		{
			resourceFingerprint = createFingerprint(contextClass, resourceName);
			resourceFingerprints.put(resourceName, resourceFingerprint);
		}
		return resourceFingerprint;
	}

	private String createFingerprint(Class<?> contextClass, String resourceName) throws IOException
	{
		byte[] bytes = contextClass != null ? ResourceUtils.getBytes(contextClass, resourceName) : ResourceUtils
				.getBytes(resourceName);
		HashCode crc32 = Hashing.crc32().hashBytes(bytes);
		return BaseEncoding.base64Url().omitPadding().encode(crc32.asBytes());
	}
}
