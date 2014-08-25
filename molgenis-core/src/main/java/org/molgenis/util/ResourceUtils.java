package org.molgenis.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import com.google.common.io.Resources;

public class ResourceUtils
{
	private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

	private ResourceUtils()
	{
	}

	public static File getFile(String resourceName)
	{
		URL resourceUrl = Resources.getResource(resourceName);
		return getFile(resourceUrl);
	}

	public static File getFile(Class<?> contextClass, String resourceName)
	{
		URL resourceUrl = Resources.getResource(contextClass, resourceName);
		return getFile(resourceUrl);
	}

	public static String getString(String resourceName) throws IOException
	{
		URL resourceUrl = Resources.getResource(resourceName);
		return getString(resourceUrl, CHARSET_UTF8);
	}

	public static String getString(Class<?> contextClass, String resourceName) throws IOException
	{
		URL resourceUrl = Resources.getResource(contextClass, resourceName);
		return getString(resourceUrl, CHARSET_UTF8);
	}

	public static String getString(Class<?> contextClass, String resourceName, Charset charset) throws IOException
	{
		URL resourceUrl = Resources.getResource(contextClass, resourceName);
		return getString(resourceUrl, charset);
	}

	public static byte[] getBytes(String resourceName) throws IOException
	{
		URL resourceUrl = Resources.getResource(resourceName);
		return getBytes(resourceUrl);
	}

	public static byte[] getBytes(Class<?> contextClass, String resourceName) throws IOException
	{
		URL resourceUrl = Resources.getResource(contextClass, resourceName);
		return getBytes(resourceUrl);
	}

	/**
	 * Workaround for http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4466485
	 * 
	 * @param resourceUrl
	 * @return
	 */
	private static File getFile(URL resourceUrl)
	{
		try
		{
			return new File(new URI(resourceUrl.toString()).getPath());
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static String getString(URL resourceUrl, Charset charset) throws IOException
	{
		return Resources.toString(resourceUrl, charset);
	}

	private static byte[] getBytes(URL resourceUrl) throws IOException
	{
		return Resources.toByteArray(resourceUrl);
	}
}
